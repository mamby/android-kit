$ErrorActionPreference = 'Stop'
$root = Resolve-Path (Join-Path $PSScriptRoot '../..')
$gradle = if ($IsWindows) { '.\gradlew.bat' } else { './gradlew' }
$cases = @(
    @{ Name = 'supported'; Symbol = $null },
    @{ Name = 'page-catalog'; Symbol = 'catalog' },
    @{ Name = 'search-catalog'; Symbol = 'catalog' },
    @{ Name = 'host-history'; Symbol = 'history' }
)
Push-Location $root
try {
    foreach ($case in $cases) {
        $result = & $gradle --dependency-verification strict -I test/gradle/settings-api-fixtures.init.gradle "-PandroidKitSettingsApiFixture=$($case.Name)" :demo:compileDebugKotlin --console=plain --quiet 2>&1
        $code = $LASTEXITCODE
        $log = $result -join [Environment]::NewLine
        if ($null -eq $case.Symbol) {
            if ($code -ne 0) { throw "$($case.Name) should compile.$([Environment]::NewLine)$log" }
        } elseif ($code -eq 0 -or $log -notmatch 'e: .*SettingsContractCase.kt' -or !$log.Contains($case.Symbol)) {
            throw "$($case.Name) did not fail at the expected API boundary.$([Environment]::NewLine)$log"
        }
        Write-Output "PASS: $($case.Name)"
    }
    & $gradle --dependency-verification strict :demo:compileDebugKotlin --console=plain --quiet
    if ($LASTEXITCODE -ne 0) { throw 'Normal demo compilation failed after Settings fixtures.' }
} finally {
    Pop-Location
}
exit 0
