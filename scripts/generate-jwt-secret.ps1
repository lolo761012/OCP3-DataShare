$repoRoot = Split-Path -Parent $PSScriptRoot
$envPath = Join-Path $repoRoot ".env"

$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()

try {
    $rng.GetBytes($bytes)
}
finally {
    $rng.Dispose()
}

$secret = [Convert]::ToBase64String($bytes)

if (Test-Path $envPath) {
    $content = [System.IO.File]::ReadAllText($envPath)
}
else {
    $content = ""
}

if ($content -match '(?m)^JWT_SECRET=.*$') {
    $content = [regex]::Replace(
        $content,
        '(?m)^JWT_SECRET=.*$',
        "JWT_SECRET=$secret"
    )
}
else {
    if ($content.Length -gt 0) {
        $content = $content.TrimEnd() + "`r`n"
    }

    $content += "JWT_SECRET=$secret`r`n"
}

[System.IO.File]::WriteAllText(
    $envPath,
    $content,
    [System.Text.UTF8Encoding]::new($false)
)

Write-Host "JWT_SECRET generated successfully in $envPath"