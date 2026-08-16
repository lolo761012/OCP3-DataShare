param(
    [Parameter(Position = 0)]
    [ValidateSet('start', 'stop', 'status')]
    [string]$Action = 'status'
)

$ErrorActionPreference = 'Stop'

$ScriptDir = $PSScriptRoot
$Root = Split-Path -Parent $ScriptDir
$ConfigPath = Join-Path $ScriptDir 'dev-config.psd1'

if (-not (Test-Path $ConfigPath)) {
    throw "Missing configuration file: $ConfigPath"
}

$Config = Import-PowerShellDataFile -Path $ConfigPath

$BackendDir = Join-Path $Root $Config.BackendDirectory
$FrontendDir = Join-Path $Root $Config.FrontendDirectory
$BackendHealthUrl = "http://localhost:$($Config.BackendPort)$($Config.BackendHealthPath)"
$FrontendUrl = "http://localhost:$($Config.FrontendPort)"

function Get-ListenerInfo {
    param([int]$Port)

    $connection = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue |
        Select-Object -First 1

    if (-not $connection) {
        return $null
    }

    $process = Get-CimInstance Win32_Process -Filter "ProcessId=$($connection.OwningProcess)" -ErrorAction SilentlyContinue

    [PSCustomObject]@{
        Port        = $Port
        ProcessId   = $connection.OwningProcess
        Name        = $process.Name
        CommandLine = $process.CommandLine
    }
}

function Test-IsDataShareProcess {
    param($ProcessInfo)

    if (-not $ProcessInfo -or -not $ProcessInfo.CommandLine) {
        return $false
    }

    if ($ProcessInfo.CommandLine.IndexOf(
        $Root,
        [System.StringComparison]::OrdinalIgnoreCase
    ) -ge 0) {
        return $true
    }

    return $ProcessInfo.CommandLine -match 'com\.openclassrooms\.datashare\.DatashareBackendApplication'
}

function Wait-Postgres {
    $deadline = (Get-Date).AddSeconds($Config.StartupTimeoutSec)

    while ((Get-Date) -lt $deadline) {
        docker exec $Config.PostgresContainer pg_isready `
            -U $Config.PostgresUser `
            -d $Config.PostgresDatabase *> $null

        if ($LASTEXITCODE -eq 0) {
            return $true
        }

        Start-Sleep -Seconds 1
    }

    return $false
}

function Wait-Backend {
    $deadline = (Get-Date).AddSeconds($Config.StartupTimeoutSec)

    while ((Get-Date) -lt $deadline) {
        try {
            $health = Invoke-RestMethod -Uri $BackendHealthUrl -TimeoutSec 2
            if ($health.status -eq 'UP') {
                return $true
            }
        }
        catch {}

        Start-Sleep -Seconds 1
    }

    return $false
}

function Wait-Frontend {
    $deadline = (Get-Date).AddSeconds($Config.StartupTimeoutSec)

    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -Uri $FrontendUrl -UseBasicParsing -TimeoutSec 2
            if ($response.StatusCode -eq 200) {
                return $true
            }
        }
        catch {}

        Start-Sleep -Seconds 1
    }

    return $false
}

function Assert-PortAvailableOrDataShare {
    param(
        [int]$Port,
        [string]$Label
    )

    $info = Get-ListenerInfo -Port $Port

    if (-not $info) {
        return $false
    }

    if (Test-IsDataShareProcess $info) {
        Write-Host "$Label already running on port $Port (PID $($info.ProcessId))."
        return $true
    }

    throw "Port $Port is used by another process: $($info.Name) (PID $($info.ProcessId))."
}

function Start-DataShare {
    Write-Host "Starting DataShare..."
    Write-Host "Root: $Root"

    docker info *> $null
    if ($LASTEXITCODE -ne 0) {
        throw 'Docker is unavailable. Start Docker Desktop first.'
    }

    Push-Location $Root
    try {
        docker compose up -d
        if ($LASTEXITCODE -ne 0) {
            throw 'docker compose up -d failed.'
        }
    }
    finally {
        Pop-Location
    }

    if (-not (Wait-Postgres)) {
        throw 'PostgreSQL did not become ready.'
    }
    Write-Host "PostgreSQL: UP"

    $backendAlreadyRunning = Assert-PortAvailableOrDataShare `
        -Port $Config.BackendPort `
        -Label 'Backend'

    if (-not $backendAlreadyRunning) {
        $backendCommand = '' +
                          'Set-Location "' + $BackendDir + '"; ' +
                          '.\mvnw.cmd spring-boot:run'

        Start-Process powershell.exe `
            -ArgumentList @('-NoLogo', '-NoProfile', '-Command', $backendCommand) |
            Out-Null
    }

    if (-not (Wait-Backend)) {
        throw "Backend did not become healthy: $BackendHealthUrl"
    }
    Write-Host "Backend: UP"

    $frontendAlreadyRunning = Assert-PortAvailableOrDataShare `
        -Port $Config.FrontendPort `
        -Label 'Frontend'

    if (-not $frontendAlreadyRunning) {
        $frontendCommand = '' +
                           'Set-Location "' + $FrontendDir + '"; ' +
                           'npm start'

        Start-Process powershell.exe `
            -ArgumentList @('-NoLogo', '-NoProfile', '-Command', $frontendCommand) |
            Out-Null
    }

    if (-not (Wait-Frontend)) {
        throw "Frontend did not become available: $FrontendUrl"
    }

    Write-Host "Frontend: UP"
    Write-Host ""
    Write-Host "DataShare is ready."
    Write-Host "Frontend: $FrontendUrl"
    Write-Host "Backend health: $BackendHealthUrl"
}

function Stop-ProcessOnPortIfDataShare {
    param(
        [int]$Port,
        [string]$Label
    )

    $info = Get-ListenerInfo -Port $Port

    if (-not $info) {
        Write-Host "${Label}: already stopped."
        return
    }

    if (-not (Test-IsDataShareProcess $info)) {
        Write-Warning "$Label not stopped: port $Port belongs to $($info.Name) (PID $($info.ProcessId)), not DataShare."
        return
    }

    Write-Host "Stopping $Label (PID $($info.ProcessId))..."
    Stop-Process -Id $info.ProcessId -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 1
}

function Stop-DataShare {
    Write-Host "Stopping DataShare..."

    Stop-ProcessOnPortIfDataShare -Port $Config.FrontendPort -Label 'Frontend'
    Stop-ProcessOnPortIfDataShare -Port $Config.BackendPort -Label 'Backend'

    Push-Location $Root
    try {
        docker compose down
    }
    finally {
        Pop-Location
    }

    Write-Host 'PostgreSQL container: stopped.'
    Write-Host 'PostgreSQL volume: preserved.'
    Write-Host 'DataShare is stopped.'
}

function Show-PortStatus {
    param(
        [int]$Port,
        [string]$Label
    )

    $info = Get-ListenerInfo -Port $Port

    if (-not $info) {
        Write-Host "$Label ($Port): DOWN"
        return
    }

    $scope = if (Test-IsDataShareProcess $info) { 'DataShare' } else { 'other process' }

    Write-Host "$Label ($Port): LISTENING - PID $($info.ProcessId) - $($info.Name) - $scope"
}

function Show-DataShareStatus {
    Write-Host 'DataShare status'
    Write-Host "Root: $Root"
    Write-Host ''

    docker info *> $null
    if ($LASTEXITCODE -eq 0) {
        Write-Host 'Docker: UP'

        Push-Location $Root
        try {
            docker compose ps
        }
        finally {
            Pop-Location
        }

        $postgresRunning = docker ps `
            --filter "name=^/$($Config.PostgresContainer)$" `
            --filter "status=running" `
            --format "{{.Names}}" 2>$null

        if ($postgresRunning -contains $Config.PostgresContainer) {
            docker exec $Config.PostgresContainer pg_isready `
                -U $Config.PostgresUser `
                -d $Config.PostgresDatabase *> $null

            if ($LASTEXITCODE -eq 0) {
                Write-Host 'PostgreSQL health: UP'
            }
            else {
                Write-Host 'PostgreSQL health: DOWN'
            }
        }
        else {
            Write-Host 'PostgreSQL health: DOWN (container not running)'
        }
    }
    else {
        Write-Host 'Docker: DOWN / unavailable'
    }

    Write-Host ''
    Show-PortStatus -Port $Config.PostgresPort -Label 'PostgreSQL'
    Show-PortStatus -Port $Config.BackendPort -Label 'Backend'
    Show-PortStatus -Port $Config.FrontendPort -Label 'Frontend'

    Write-Host ''

    try {
        $health = Invoke-RestMethod -Uri $BackendHealthUrl -TimeoutSec 2
        Write-Host "Backend health endpoint: $($health.status)"
    }
    catch {
        Write-Host 'Backend health endpoint: unavailable'
    }

    try {
        $response = Invoke-WebRequest -Uri $FrontendUrl -UseBasicParsing -TimeoutSec 2
        Write-Host "Frontend HTTP: $($response.StatusCode)"
    }
    catch {
        Write-Host 'Frontend HTTP: unavailable'
    }
}

switch ($Action) {
    'start'  { Start-DataShare }
    'stop'   { Stop-DataShare }
    'status' { Show-DataShareStatus }
}