$ErrorActionPreference = 'Stop'
$root = Resolve-Path (Join-Path $PSScriptRoot '../..')
$gradle = if ($IsWindows) { '.\gradlew.bat' } else { './gradlew' }
$cases = @(
    @{ Name = 'supported'; Symbol = $null },
    @{ Name = 'theme-shapes'; Symbol = 'shapes' },
    @{ Name = 'theme-typography'; Symbol = 'typography' },
    @{ Name = 'theme-dimensions'; Symbol = 'dimensions' },
    @{ Name = 'component-style'; Symbol = 'style' },
    @{ Name = 'card-padding'; Symbol = 'contentPadding' },
    @{ Name = 'toolbar-spacing'; Symbol = 'itemSpacing' },
    @{ Name = 'sheet-spacing'; Symbol = 'chromeContentSpacing' },
    @{ Name = 'dimensions-construction'; Symbol = 'AndroidKitDimensions' },
    @{ Name = 'dimensions-copy'; Symbol = 'copy' },
    @{ Name = 'color-geometry'; Symbol = 'shape' }
)
Push-Location $root
try {
    foreach ($case in $cases) {
        $result = & $gradle --dependency-verification strict -I test/gradle/theme-api-fixtures.init.gradle "-PandroidKitThemeApiFixture=$($case.Name)" :demo:compileDebugKotlin --console=plain --quiet 2>&1
        $code = $LASTEXITCODE
        $log = $result -join [Environment]::NewLine
        if ($null -eq $case.Symbol) {
            if ($code -ne 0) { throw "$($case.Name) should compile.$([Environment]::NewLine)$log" }
        } elseif ($code -eq 0 -or $log -notmatch 'e: .*ThemeContractCase.kt' -or !$log.Contains($case.Symbol)) {
            throw "$($case.Name) did not fail at the expected API boundary.$([Environment]::NewLine)$log"
        }
        Write-Output "PASS: $($case.Name)"
    }
    & $gradle --dependency-verification strict :demo:compileDebugKotlin --console=plain --quiet
    if ($LASTEXITCODE -ne 0) { throw 'Normal demo compilation failed after theme fixtures.' }
} finally {
    Pop-Location
}
exit 0
