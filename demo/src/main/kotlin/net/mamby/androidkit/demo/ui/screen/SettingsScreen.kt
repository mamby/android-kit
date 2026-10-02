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
import net.mamby.androidkit.compose.form.AndroidKitSettingsStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
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
    settingsStore: AndroidKitSettingsStore,
    onSettingsStorageFailure: (Throwable) -> Unit,
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
    val languageOptions = remember {
        SupportedLanguageTags.map { tag ->
            AndroidKitSettingsOption(tag, nativeLanguageName(tag))
        }
    }
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
            history = remember(settingsStore) { settingsStore.searchHistory("settings") },
            onStorageFailure = onSettingsStorageFailure,
        ),
    ) {
        main(key = DemoMainSettingsPageKey, title = settingsTitle) {
            section("language", label = languageTitle) {
                language(AndroidKitLanguageSetting(
                    selection = AndroidKitSettingsSelection(
                        persistence = settingsStore.setting(stringPreferencesKey("selected_language_tag"), localeManager.selectedLanguageTag() ?: "system"),
                        options = languageOptions,
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
                    persistence = settingsStore.setting(stringPreferencesKey("theme_choice"), DemoThemeChoice.System.storedValue),
                    options = listOf(
                        AndroidKitSettingsOption(DemoThemeChoice.Light.storedValue, themeLightText),
                        AndroidKitSettingsOption(DemoThemeChoice.Dark.storedValue, themeDarkText),
                        AndroidKitSettingsOption(DemoThemeChoice.Prism.storedValue, themePrismText),
                    ),
                    onSelected = { onThemeChoice(DemoThemeChoice.fromStoredValue(it)) },
                    systemOption = AndroidKitSettingsSystemOption(
                        id = DemoThemeChoice.System.storedValue,
                        currentValueLabel = systemThemeText,
                    ),
                ))
                transparency(AndroidKitFloatingOpacitySetting(
                    persistence = settingsStore.setting(floatPreferencesKey("floating_surface_opacity_level"), net.mamby.androidkit.compose.theme.AndroidKitFloatingSurfaceDefaults.DefaultOpacityLevel),
                    onValueChange = onFloatingSurfaceOpacityLevelChange,
                    onValueChangeFinished = onFloatingSurfaceOpacityLevelChangeFinished,
                ))
            }
            section("security", label = securityTitle) {
                appLock(AndroidKitAppLockSetting(
                    persistence = settingsStore.setting(booleanPreferencesKey("app_lock_enabled"), false),
                    onCheckedChange = onAppLockChange,
                    enabled = !appLockBusy,
                    errorMessage = appLockError,
                    onLockNow = onLockNow,
                    timeout = AndroidKitAppLockTimeoutSetting(
                        persistence = settingsStore.setting(stringPreferencesKey("app_lock_timeout"), DemoAppLockTimeout.Immediately.name),
                        options = timeoutOptions,
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
