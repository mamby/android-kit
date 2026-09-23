$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
Push-Location $repo
try {
    & ./test/gradle/verify-settings-search-lexicon.ps1
    $cases = @(
        @{ Name = 'valid'; Expected = $null },
        @{ Name = 'override'; Expected = 'is owned by AndroidKit' },
        @{ Name = 'alias'; Expected = 'is owned by AndroidKit' },
        @{ Name = 'generated'; Expected = 'is owned by AndroidKit' },
        @{ Name = 'dependency'; Expected = 'is owned by AndroidKit' },
        @{ Name = 'unsupported'; Expected = 'locale sw is not supported' },
        @{ Name = 'locale-config'; Expected = 'locale uk is not supported' }
    )
    foreach ($case in $cases) {
        # mergeDebugAssets exercises the packaging dependency, not just the validator task.
        $result = & ./gradlew.bat -I test/gradle/localization-fixtures.init.gradle "-PandroidKitLocalizationFixture=$($case.Name)" :demo:mergeDebugAssets --console=plain --quiet 2>&1
        $code = $LASTEXITCODE
        $log = $result -join [Environment]::NewLine
        if ($null -eq $case.Expected) {
            if ($code -ne 0) { throw "$($case.Name) should pass.$([Environment]::NewLine)$log" }
        } elseif ($code -eq 0 -or !$log.Contains($case.Expected)) {
            throw "$($case.Name) did not fail for the expected reason.$([Environment]::NewLine)$log"
        }
        Write-Output "PASS: $($case.Name)"
    }
    $result = & ./gradlew.bat '-PandroidKitSupportedLocales=en,sw' :demo:validateDebugAndroidKitLocalization --console=plain --quiet 2>&1
    if ($LASTEXITCODE -eq 0 -or !($result -join [Environment]::NewLine).Contains('locale sw is not supported')) {
        throw "Unsupported declared locale was not rejected: $result"
    }
    Write-Output 'PASS: unsupported declared locale'
} finally {
    Pop-Location
}
