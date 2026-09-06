package net.mamby.androidkit.demo.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalLocale
import net.mamby.androidkit.compose.form.AndroidKitAppLockSetting
import net.mamby.androidkit.compose.form.AndroidKitFloatingOpacitySetting
import net.mamby.androidkit.compose.form.AndroidKitLanguageSetting
import net.mamby.androidkit.compose.form.AndroidKitSettingsOption
import net.mamby.androidkit.compose.form.AndroidKitSettingsPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsSelection
import net.mamby.androidkit.compose.form.AndroidKitSettingsSystemOption
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.ComponentDemo

@Composable
internal fun SettingsPageDemo(demo: ComponentDemo, onBack: () -> Unit) {
    var destination by rememberSaveable(demo) { mutableStateOf("root") }
    var language by rememberSaveable { mutableStateOf("system") }
    var theme by rememberSaveable { mutableStateOf("system") }
    var opacity by rememberSaveable { mutableFloatStateOf(0f) }
    var locked by rememberSaveable { mutableStateOf(false) }
    var showLanguage by rememberSaveable { mutableStateOf(true) }
    val systemLanguageId = when (LocalLocale.current.platformLocale.language) {
        "fr" -> "fr"
        "ar" -> "ar"
        else -> "en"
    }
    val stateHolder = rememberSaveableStateHolder()
    val grouped = demo == ComponentDemo.AndroidKitSettingsPageSubpages
    val optional = demo == ComponentDemo.AndroidKitSettingsPageOptional
    val back = { if (destination == "root") onBack() else destination = "root" }
    BackHandler(destination != "root", back)
    stateHolder.SaveableStateProvider(destination) {
        AndroidKitSettingsPage(
            title = stringResource(when (destination) {
                "appearance" -> R.string.appearance_section
                "security" -> R.string.settings_security
                else -> R.string.settings_title
            }),
            onBack = back,
            listState = rememberLazyListState(),
        ) {
            if (grouped && destination == "root") {
                section(key = "groups") {
                    navigation(label = stringResource(R.string.appearance_section), onClick = { destination = "appearance" })
                    navigation(label = stringResource(R.string.settings_security), onClick = { destination = "security" })
                }
            } else {
                if (optional) {
                    section(key = "visibility") {
                        toggle(stringResource(R.string.settings_show_language), showLanguage, { showLanguage = it })
                    }
                }
                if (destination != "security") {
                    generalSection(
                        label = stringResource(R.string.settings_general),
                        language = if (showLanguage) AndroidKitLanguageSetting(
                            selection = AndroidKitSettingsSelection(
                                label = stringResource(R.string.language_section),
                                options = listOf(
                                    AndroidKitSettingsOption("en", "English"),
                                    AndroidKitSettingsOption("fr", "Français"),
                                    AndroidKitSettingsOption("ar", "العربية"),
                                ),
                                selectedId = language, onSelected = { language = it },
                                closeContentDescription = stringResource(R.string.action_close),
                                systemOption = AndroidKitSettingsSystemOption(
                                    id = "system",
                                    label = stringResource(R.string.language_system),
                                    currentValueLabel = when (systemLanguageId) {
                                        "fr" -> "Français"
                                        "ar" -> "العربية"
                                        else -> "English"
                                    },
                                ),
                            ),
                            searchLabel = stringResource(R.string.settings_search_languages),
                            emptyResultsLabel = stringResource(R.string.settings_no_languages),
                        ) else null,
                        theme = AndroidKitSettingsSelection(
                            label = stringResource(R.string.settings_theme),
                            options = listOf(
                                AndroidKitSettingsOption("light", stringResource(R.string.theme_light)),
                                AndroidKitSettingsOption("dark", stringResource(R.string.theme_dark)),
                            ),
                            selectedId = theme, onSelected = { theme = it },
                            closeContentDescription = stringResource(R.string.action_close),
                            systemOption = AndroidKitSettingsSystemOption(
                                id = "system",
                                label = stringResource(R.string.language_system),
                                currentValueLabel = stringResource(
                                    if (isSystemInDarkTheme()) R.string.theme_dark else R.string.theme_light
                                ),
                            ),
                        ),
                        floatingOpacity = if (optional) null else AndroidKitFloatingOpacitySetting(
                            label = stringResource(R.string.floating_surface_opacity), value = opacity,
                            minimumLabel = stringResource(R.string.settings_min), maximumLabel = stringResource(R.string.settings_max),
                            onValueChange = { opacity = it }, onValueChangeFinished = {},
                        ),
                    )
                }
                section(key = "custom") {
                    item { Text(stringResource(R.string.settings_demo_state)) }
                }
                if (destination != "appearance") {
                    securitySection(
                        label = stringResource(R.string.settings_security),
                        appLock = if (optional) null else AndroidKitAppLockSetting(
                            label = stringResource(R.string.settings_app_lock), checked = locked,
                            onCheckedChange = { locked = it },
                            supportingText = stringResource(R.string.settings_demo_state),
                        ),
                    )
                }
            }
            item(key = "scroll") { DemoScrollContent() }
        }
    }
}
