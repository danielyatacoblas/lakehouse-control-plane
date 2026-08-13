$ErrorActionPreference = "Stop"

$chromeCandidates = @(
    $env:PUPPETEER_EXECUTABLE_PATH,
    "$env:ProgramFiles\\Google\\Chrome\\Application\\chrome.exe",
    "${env:ProgramFiles(x86)}\\Google\\Chrome\\Application\\chrome.exe",
    "/usr/bin/google-chrome",
    "/usr/bin/chromium"
)
$chromePath = $chromeCandidates | Where-Object { $_ -and (Test-Path -LiteralPath $_) } | Select-Object -First 1
if ($chromePath) { $env:PUPPETEER_EXECUTABLE_PATH = $chromePath }

$repoRoot = Split-Path -Parent $PSScriptRoot
$sourceDir = Join-Path $repoRoot "diagrams"
$outputDir = Join-Path $sourceDir "rendered"
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

foreach ($source in Get-ChildItem -LiteralPath $sourceDir -Filter "*.mmd") {
    $name = [System.IO.Path]::GetFileNameWithoutExtension($source.Name)
    $svg = Join-Path $outputDir "$name.svg"
    $png = Join-Path $outputDir "$name.png"

    npm exec --yes --package=puppeteer@24.37.2 --package=@mermaid-js/mermaid-cli@11.16.0 -- mmdc -i $source.FullName -o $svg -b transparent -t dark
    if ($LASTEXITCODE -ne 0) { throw "Falló el SVG de $name." }
    npm exec --yes --package=puppeteer@24.37.2 --package=@mermaid-js/mermaid-cli@11.16.0 -- mmdc -i $source.FullName -o $png -b transparent -t dark -w 1600
    if ($LASTEXITCODE -ne 0) { throw "Falló el PNG de $name." }
}

Write-Host "Diagramas Mermaid regenerados en $outputDir"
