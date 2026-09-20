package net.mamby.androidkit.compose.form

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import kotlinx.coroutines.launch
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

/** Predefined settings link. Kit owns its label, icon, and presentation; hosts own behavior. */
public data class AndroidKitSettingsLink(
    public val onClick: () -> Unit,
    public val enabled: Boolean = true,
)

/** Typed content for the predefined App info subpage. Hosts own all destinations. */
public data class AndroidKitSettingsAbout(
    public val version: String,
    public val privacyPolicy: AndroidKitSettingsLink,
    public val termsOfUse: AndroidKitSettingsLink,
    public val libraries: AndroidKitSettingsLink,
    public val openSource: AndroidKitSettingsLink,
    public val showOpenSource: Boolean = true,
) {
    init { require(version.isNotBlank()) { "App info requires a version." } }
}

public sealed interface AndroidKitSettingsPageConfiguration {
    /** Settings pages that accept host-defined sections and page chrome. */
    public sealed interface Customizable : AndroidKitSettingsPageConfiguration

    /** About is always present with Contact and App info. */
    public data class Main(
        public val contact: AndroidKitSettingsLink,
        public val appInfo: AndroidKitSettingsLink,
    ) : Customizable

    public data class AppInfo(public val about: AndroidKitSettingsAbout) : AndroidKitSettingsPageConfiguration

    /** Ordinary host-defined subpage; no main-settings footer. */
    public data object Subpage : Customizable
}

@Composable
internal fun SettingsAboutSection(main: AndroidKitSettingsPageConfiguration.Main) {
    val strings = AndroidKitThemeTokens.strings
    val scope = SettingSectionScopeImpl().apply {
        link(main.contact, strings.contact, AndroidKitIcons.Contact, strings.contactDescription)
        link(main.appInfo, strings.appInfo, AndroidKitIcons.Info)
    }
    SettingsSection(entries = scope.entries, label = strings.about)
}

@Composable
internal fun SettingsLegalSection(about: AndroidKitSettingsAbout) {
    val strings = AndroidKitThemeTokens.strings
    val scope = SettingSectionScopeImpl().apply {
        link(about.termsOfUse, strings.termsOfUse, AndroidKitIcons.Document)
        link(about.privacyPolicy, strings.privacyPolicy, AndroidKitIcons.AppLock)
        link(
            about.libraries,
            strings.thirdPartyLicenses,
            AndroidKitIcons.Document,
            strings.thirdPartyLicensesDescription,
        )
    }
    SettingsSection(entries = scope.entries, label = strings.legal)
}

@Composable
internal fun SettingsVersionSection(about: AndroidKitSettingsAbout) {
    val strings = AndroidKitThemeTokens.strings
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val scope = SettingSectionScopeImpl().apply {
        copyableInfo(
            label = about.version,
            onClickLabel = strings.copyVersion,
            onClick = {
                val clipEntry = ClipEntry(ClipData.newPlainText(strings.version, about.version))
                coroutineScope.launch { clipboard.setClipEntry(clipEntry) }
            },
            icon = AndroidKitIcons.Info,
        )
    }
    SettingsSection(entries = scope.entries, label = strings.version)
}

@Composable
internal fun SettingsOpenSourceSection(about: AndroidKitSettingsAbout) {
    val strings = AndroidKitThemeTokens.strings
    val scope = SettingSectionScopeImpl().apply {
        link(about.openSource, strings.openSourceDescription, AndroidKitIcons.Code)
    }
    SettingsSection(entries = scope.entries, label = strings.openSource)
}

private fun AndroidKitSettingSectionScope.link(
    link: AndroidKitSettingsLink,
    label: String,
    icon: ImageVector,
    supportingText: String? = null,
) {
    navigation(label = label, onClick = link.onClick,
        icon = icon, enabled = link.enabled, supportingText = supportingText)
}
