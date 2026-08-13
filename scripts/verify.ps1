$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$gradle = if ($IsWindows) { Join-Path $repoRoot "gradlew.bat" } else { Join-Path $repoRoot "gradlew" }

Write-Host "Verificando backend..."
& $gradle clean test --no-daemon
if ($LASTEXITCODE -ne 0) { throw "Falló la verificación Gradle." }

Write-Host "Verificando consola web..."
Push-Location (Join-Path $repoRoot "frontend")
try {
    npm ci
    if ($LASTEXITCODE -ne 0) { throw "Falló npm ci." }
    npm test
    if ($LASTEXITCODE -ne 0) { throw "Fallaron las pruebas frontend." }
    npm run build
    if ($LASTEXITCODE -ne 0) { throw "Falló el build frontend." }
}
finally {
    Pop-Location
}

Write-Host "Backend, pruebas frontend y build verificados."
