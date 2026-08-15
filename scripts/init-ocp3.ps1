$Root = Split-Path -Parent $PSScriptRoot

$Directories = @(
    "docs",
    "docs\architecture",
    "docs\data-model",
    "docs\api",
    "storage"
)

$Files = @(
    "TESTING.md",
    "SECURITY.md",
    "PERF.md",
    "MAINTENANCE.md"
)

foreach ($Directory in $Directories) {
    New-Item -ItemType Directory `
        -Force `
        -Path (Join-Path $Root $Directory) | Out-Null
}

foreach ($File in $Files) {
    $Path = Join-Path $Root $File

    if (-not (Test-Path $Path)) {
        New-Item -ItemType File -Path $Path | Out-Null
    }
}

Write-Host "OCP3 repository structure initialized."