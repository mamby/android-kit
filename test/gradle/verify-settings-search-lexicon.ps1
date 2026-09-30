$ErrorActionPreference = 'Stop'

$repo = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$contract = Get-Content -Raw (Join-Path $repo 'compose/src/main/assets/androidkit-localization-contract.json') |
    ConvertFrom-Json
$lexicon = Get-Content -Raw (
    Join-Path $repo 'compose/src/main/res/raw/androidkit_compose_settings_search_lexicon.json'
) | ConvertFrom-Json -AsHashtable

$localeDirectories = @{}
foreach ($locale in $contract.supportedLocales) {
    $subtags = $locale.Split('-')
    $subtags[0] = switch ($subtags[0]) {
        'id' { 'in' }
        'he' { 'iw' }
        'yi' { 'ji' }
        default { $subtags[0] }
    }
    $localeDirectories[$locale] = if ($locale -eq 'en') {
        'values'
    } elseif ($subtags.Count -eq 1) {
        "values-$($subtags[0])"
    } else {
        "values-b+$($subtags -join '+')"
    }
}
$resourceKeys = [ordered]@{
    language = 'androidkit_compose_language'; theme = 'androidkit_compose_theme'
    transparency = 'androidkit_compose_transparency'; 'app-lock' = 'androidkit_compose_app_lock'
    'app-lock-timeout' = 'androidkit_compose_lock_after_leaving_app'
    'lock-now' = 'androidkit_compose_lock_now'; about = 'androidkit_compose_about'
    website = 'androidkit_compose_website'; 'source-code' = 'androidkit_compose_source_code'
    version = 'androidkit_compose_version'; contact = 'androidkit_compose_contact'
    privacy = 'androidkit_compose_privacy_policy'; terms = 'androidkit_compose_terms_of_use'
    libraries = 'androidkit_compose_third_party_licenses'
}

function Normalize([string]$value) {
    $withoutMarks = [regex]::Replace($value.Trim().Normalize([Text.NormalizationForm]::FormD), '\p{M}+', '')
    return ([regex]::Replace($withoutMarks.ToLowerInvariant(), '[^\p{L}\p{N}]+', ' ')).Trim()
}

$expectedLocales = @($contract.supportedLocales | Sort-Object)
$actualLocales = @($lexicon.locales | Sort-Object)
if (Compare-Object $expectedLocales $actualLocales) {
    throw "Settings search lexicon locales do not match the localization contract."
}
if (Compare-Object @($resourceKeys.Keys | Sort-Object) @($lexicon.entries.Keys | Sort-Object)) {
    throw "Settings search lexicon entry IDs do not match the required built-in settings."
}

foreach ($entryId in $resourceKeys.Keys) {
    foreach ($locale in $contract.supportedLocales) {
        $terms = @($lexicon.entries[$entryId][$locale])
        if ($terms.Count -eq 0 -or $terms.Where({ [string]::IsNullOrWhiteSpace($_) }).Count -gt 0) {
            throw "Settings search lexicon entry $entryId has missing terms for $locale."
        }
        $normalized = @($terms | ForEach-Object { Normalize $_ })
        if ($normalized.Count -ne @($normalized | Select-Object -Unique).Count) {
            throw "Settings search lexicon entry $entryId has duplicate normalized terms for $locale."
        }
        [xml]$resources = Get-Content -Raw (
            Join-Path $repo "compose/src/main/res/$($localeDirectories[$locale])/strings.xml"
        )
        $resourceName = $resourceKeys[$entryId]
        $label = [string]($resources.resources.string | Where-Object name -eq $resourceName).'#text'
        # Android removes string-resource escapes after XML parsing.
        $label = [regex]::Unescape($label)
        if ($label -notin $terms) {
            throw "Settings search lexicon entry $entryId does not contain its $locale label."
        }
    }
}

Write-Output 'PASS: settings search lexicon'
