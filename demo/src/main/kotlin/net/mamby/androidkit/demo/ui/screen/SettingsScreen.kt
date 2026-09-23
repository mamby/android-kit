package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.form.AndroidKitAppLockSetting
import net.mamby.androidkit.compose.form.AndroidKitAppLockTimeoutSetting
import net.mamby.androidkit.compose.form.AndroidKitFloatingOpacitySetting
import net.mamby.androidkit.compose.form.AndroidKitLanguageSetting
import net.mamby.androidkit.compose.form.AndroidKitSettingsAbout
import net.mamby.androidkit.compose.form.AndroidKitSettingsCatalog
import net.mamby.androidkit.compose.form.AndroidKitSettingsLegalEntry
import net.mamby.androidkit.compose.form.AndroidKitSettingsLink
import net.mamby.androidkit.compose.form.AndroidKitSettingsOption
import net.mamby.androidkit.compose.form.AndroidKitSettingsPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsSearchConfiguration
import net.mamby.androidkit.compose.form.AndroidKitSettingsSearchPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsSelection
import net.mamby.androidkit.compose.form.AndroidKitSettingsSystemOption
import net.mamby.androidkit.compose.form.androidKitSettingsCatalog
import net.mamby.androidkit.demo.BuildConfig
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.DemoAppLockTimeout
import net.mamby.androidkit.demo.ui.DemoThemeChoice
import net.mamby.androidkit.localization.AppLocaleManager

internal const val DemoMainSettingsPageKey = "main"
internal const val DemoAboutSettingsPageKey = "about"

@Composable
internal fun demoSettingsCatalog(
    onAbout: () -> Unit,
    onSearch: () -> Unit,
    recentSearches: List<String>,
    onRecentSearchesChange: (List<String>) -> Unit,
    appLockEnabled: Boolean,
    appLockTimeout: DemoAppLockTimeout,
    onAppLockTimeoutChange: (DemoAppLockTimeout) -> Unit,
    onAppLockChange: (Boolean) -> Unit,
    appLockBusy: Boolean,
    appLockError: String?,
    onLockNow: () -> Unit,
    themeChoice: DemoThemeChoice,
    onThemeChoice: (DemoThemeChoice) -> Unit,
    floatingSurfaceOpacityLevel: Float,
    onFloatingSurfaceOpacityLevelChange: (Float) -> Unit,
    onFloatingSurfaceOpacityLevelChangeFinished: () -> Unit,
): AndroidKitSettingsCatalog {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val displayLocale = LocalLocale.current.platformLocale
    val localeManager = remember(context) { AppLocaleManager(context, SupportedLanguageTags.toSet()) }
    val themeLightText = stringResource(R.string.theme_light)
    val themeDarkText = stringResource(R.string.theme_dark)
    val themePrismText = stringResource(R.string.theme_prism)
    val systemThemeText = stringResource(
        if (isSystemInDarkTheme()) R.string.theme_dark else R.string.theme_light,
    )
    val timeoutOptions = DemoAppLockTimeout.entries.map { timeout ->
        AndroidKitSettingsOption(
            id = timeout.name,
            label = stringResource(
                when (timeout) {
                    DemoAppLockTimeout.Immediately -> R.string.settings_lock_immediately
                    DemoAppLockTimeout.OneMinute -> R.string.settings_lock_after_one_minute
                    DemoAppLockTimeout.FiveMinutes -> R.string.settings_lock_after_five_minutes
                    DemoAppLockTimeout.FifteenMinutes -> R.string.settings_lock_after_fifteen_minutes
                },
            ),
        )
    }
    val languageTitle = stringResource(R.string.settings_language)
    val appearanceTitle = stringResource(R.string.settings_appearance)
    val securityTitle = stringResource(R.string.settings_security)
    val settingsTitle = stringResource(R.string.settings_title)
    val about = AndroidKitSettingsAbout(
        appName = stringResource(R.string.app_name),
        version = BuildConfig.VERSION_NAME,
        sourceCode = AndroidKitSettingsLink(onClick = { uriHandler.openUri(CatalogRepository) }),
        contact = AndroidKitSettingsLink(onClick = { uriHandler.openUri("https://github.com/mamby") }),
        privacyPolicy = AndroidKitSettingsLink(onClick = { uriHandler.openUri(CatalogRepository) }),
        termsOfUse = AndroidKitSettingsLink(onClick = { uriHandler.openUri(CatalogRepository) }),
        libraries = AndroidKitSettingsLink(onClick = {
            uriHandler.openUri("$CatalogRepository/THIRD_PARTY_NOTICES.md")
        }),
        description = stringResource(R.string.settings_about_description),
        additionalLegalEntries = listOf(
            AndroidKitSettingsLegalEntry(
                id = "contributors",
                title = stringResource(R.string.settings_contributors),
                onClick = { uriHandler.openUri("$CatalogRepository/graphs/contributors") },
            ),
        ),
    )
    return androidKitSettingsCatalog(
        search = AndroidKitSettingsSearchConfiguration(
            onOpenSearch = onSearch,
            recentQueries = recentSearches,
            onRecentQueriesChange = onRecentSearchesChange,
        ),
    ) {
        main(key = DemoMainSettingsPageKey, title = settingsTitle) {
            section("language", label = languageTitle) {
                language(AndroidKitLanguageSetting(
                    selection = AndroidKitSettingsSelection(
                        options = SupportedLanguageTags.map { tag ->
                            AndroidKitSettingsOption(tag, nativeLanguageName(tag))
                        },
                        selectedId = localeManager.selectedLanguageTag() ?: "system",
                        onSelected = { id ->
                            localeManager.setApplicationLanguage(id.takeUnless { it == "system" })
                        },
                        systemOption = AndroidKitSettingsSystemOption(
                            id = "system",
                            currentValueLabel = localeManager.systemLocale().getDisplayLanguage(displayLocale),
                        ),
                    ),
                ))
            }
            section("appearance", label = appearanceTitle) {
                theme(AndroidKitSettingsSelection(
                    options = listOf(
                        AndroidKitSettingsOption(DemoThemeChoice.Light.name, themeLightText),
                        AndroidKitSettingsOption(DemoThemeChoice.Dark.name, themeDarkText),
                        AndroidKitSettingsOption(DemoThemeChoice.Prism.name, themePrismText),
                    ),
                    selectedId = themeChoice.name,
                    onSelected = { onThemeChoice(DemoThemeChoice.valueOf(it)) },
                    systemOption = AndroidKitSettingsSystemOption(
                        id = DemoThemeChoice.System.name,
                        currentValueLabel = systemThemeText,
                    ),
                ))
                transparency(AndroidKitFloatingOpacitySetting(
                    value = floatingSurfaceOpacityLevel,
                    onValueChange = onFloatingSurfaceOpacityLevelChange,
                    onValueChangeFinished = onFloatingSurfaceOpacityLevelChangeFinished,
                ))
            }
            section("security", label = securityTitle) {
                appLock(AndroidKitAppLockSetting(
                    checked = appLockEnabled,
                    onCheckedChange = onAppLockChange,
                    enabled = !appLockBusy,
                    errorMessage = appLockError,
                    onLockNow = onLockNow,
                    timeout = AndroidKitAppLockTimeoutSetting(
                        options = timeoutOptions,
                        selectedId = appLockTimeout.name,
                        onSelected = { onAppLockTimeoutChange(DemoAppLockTimeout.valueOf(it)) },
                    ),
                ))
            }
        }
        about(key = DemoAboutSettingsPageKey, content = about, onOpen = onAbout)
    }
}

@Composable
internal fun SettingsScreen(catalog: AndroidKitSettingsCatalog) {
    AndroidKitSettingsPage(catalog = catalog, pageKey = DemoMainSettingsPageKey)
}

@Composable
internal fun AboutScreen(catalog: AndroidKitSettingsCatalog, onBack: () -> Unit) {
    AndroidKitSettingsPage(catalog = catalog, pageKey = DemoAboutSettingsPageKey, onBack = onBack)
}

@Composable
internal fun SettingsSearchScreen(catalog: AndroidKitSettingsCatalog, onBack: () -> Unit) {
    AndroidKitSettingsSearchPage(catalog = catalog, onBack = onBack)
}

private const val CatalogRepository = "https://github.com/mamby/android-kit"
