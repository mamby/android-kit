$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$gradle = Join-Path $repo $(if ($IsWindows) { 'gradlew.bat' } else { 'gradlew' })
Push-Location $repo
try {
    & ./test/gradle/verify-settings-search-lexicon.ps1
    $cases = @(
        @{ Name = 'valid'; Expected = $null },
        @{ Name = 'override'; Expected = 'is owned by AndroidKit' },
        @{ Name = 'alias'; Expected = 'is owned by AndroidKit' },
        @{ Name = 'generated'; Expected = 'is owned by AndroidKit' },
        @{ Name = 'dependency'; Expected = 'is owned by AndroidKit' },
        @{ Name = 'unsupported'; Expected = 'locale eo is not supported' },
        @{ Name = 'locale-config'; Expected = 'locale ga is not supported' },
        @{ Name = 'legacy-alias-valid'; Expected = $null },
        @{ Name = 'alias-id'; Expected = 'incompatible Android locale qualifier id' },
        @{ Name = 'alias-he'; Expected = 'incompatible Android locale qualifier he' },
        @{ Name = 'alias-yi'; Expected = 'incompatible Android locale qualifier yi' },
        @{ Name = 'alias-dependency'; Expected = 'incompatible Android locale qualifier id' },
        @{ Name = 'filter-id'; Expected = 'incompatible Android locale qualifier id' },
        @{ Name = 'filter-he'; Expected = 'incompatible Android locale qualifier he' },
        @{ Name = 'filter-yi'; Expected = 'incompatible Android locale qualifier yi' }
    )
    foreach ($case in $cases) {
        # mergeDebugAssets exercises the packaging dependency, not just the validator task.
        $result = & $gradle --dependency-verification strict -I test/gradle/localization-fixtures.init.gradle "-PandroidKitLocalizationFixture=$($case.Name)" :demo:mergeDebugAssets --console=plain --quiet 2>&1
        $code = $LASTEXITCODE
        $log = $result -join [Environment]::NewLine
        if ($null -eq $case.Expected) {
            if ($code -ne 0) { throw "$($case.Name) should pass.$([Environment]::NewLine)$log" }
        } elseif ($code -eq 0 -or !$log.Contains($case.Expected)) {
            throw "$($case.Name) did not fail for the expected reason.$([Environment]::NewLine)$log"
        }
        Write-Output "PASS: $($case.Name)"
    }
    $result = & $gradle --dependency-verification strict '-PandroidKitSupportedLocales=en,eo' :demo:validateDebugAndroidKitLocalization --console=plain --quiet 2>&1
    if ($LASTEXITCODE -eq 0 -or !($result -join [Environment]::NewLine).Contains('locale eo is not supported')) {
        throw "Unsupported declared locale was not rejected: $result"
    }
    Write-Output 'PASS: unsupported declared locale'
} finally {
    Pop-Location
}

# Expected Gradle rejections leave LASTEXITCODE nonzero; the verified suite succeeded.
exit 0
