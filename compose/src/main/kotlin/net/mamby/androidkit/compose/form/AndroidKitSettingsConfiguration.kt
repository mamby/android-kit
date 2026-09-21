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

/** Typed content for the predefined About subpage. Hosts own all destinations and custom copy. */
public data class AndroidKitSettingsAbout(
    public val appName: String,
    public val version: String,
    public val contact: AndroidKitSettingsLink? = null,
    public val privacyPolicy: AndroidKitSettingsLink? = null,
    public val termsOfUse: AndroidKitSettingsLink? = null,
    public val libraries: AndroidKitSettingsLink? = null,
    /** Public app website, separate from its source repository. */
    public val website: AndroidKitSettingsLink? = null,
    /** Source repository destination. */
    public val sourceCode: AndroidKitSettingsLink? = null,
    public val description: String? = null,
    public val additionalLegalEntries: List<AndroidKitSettingsLegalEntry> = emptyList(),
) {
    init {
        require(appName.isNotBlank()) { "About requires an app name." }
        require(version.isNotBlank()) { "About requires a version." }
        val ids = additionalLegalEntries.map(AndroidKitSettingsLegalEntry::id)
        require(ids.size == ids.toSet().size) { "Additional legal entry IDs must be unique." }
    }
}

/** Host-owned legal destination rendered after the predefined legal entries. */
public data class AndroidKitSettingsLegalEntry(
    public val id: String,
    public val title: String,
    public val onClick: () -> Unit,
    public val supportingText: String? = null,
    public val enabled: Boolean = true,
) {
    init {
        require(id.isNotBlank()) { "A legal entry ID cannot be blank." }
        require(title.isNotBlank()) { "A legal entry title cannot be blank." }
    }
}

public sealed interface AndroidKitSettingsPageConfiguration {
    /** Settings pages that accept host-defined sections and page chrome. */
    public sealed interface Customizable : AndroidKitSettingsPageConfiguration

    /** Main settings page with an optional About destination, always placed last. */
    public data class Main(
        public val about: AndroidKitSettingsLink? = null,
    ) : Customizable

    public data class About(public val content: AndroidKitSettingsAbout) : AndroidKitSettingsPageConfiguration

    /** Ordinary host-defined subpage; no main-settings footer. */
    public data object Subpage : Customizable
}

@Composable
internal fun SettingsAboutSection(about: AndroidKitSettingsLink) {
    val strings = AndroidKitThemeTokens.strings
    val scope = SettingSectionScopeImpl().apply {
        link(about, strings.about, AndroidKitIcons.Info)
    }
    SettingsSection(entries = scope.entries)
}

@Composable
internal fun SettingsContactSection(about: AndroidKitSettingsAbout) {
    val strings = AndroidKitThemeTokens.strings
    val scope = SettingSectionScopeImpl().apply {
        about.contact?.let { link(it, strings.contact, AndroidKitIcons.Contact, strings.contactDescription) }
    }
    SettingsSection(entries = scope.entries)
}

@Composable
internal fun SettingsLegalSection(about: AndroidKitSettingsAbout) {
    val strings = AndroidKitThemeTokens.strings
    val scope = SettingSectionScopeImpl().apply {
        about.privacyPolicy?.let { link(it, strings.privacyPolicy, AndroidKitIcons.AppLock) }
        about.termsOfUse?.let { link(it, strings.termsOfUse, AndroidKitIcons.Document) }
        about.libraries?.let { link(it, strings.thirdPartyLicenses, AndroidKitIcons.Document) }
        about.additionalLegalEntries.forEach { entry ->
            navigation(
                label = entry.title,
                onClick = entry.onClick,
                supportingText = entry.supportingText,
                icon = AndroidKitIcons.Document,
                enabled = entry.enabled,
            )
        }
    }
    SettingsSection(entries = scope.entries)
}

@Composable
internal fun SettingsAppInformationSection(about: AndroidKitSettingsAbout) {
    val strings = AndroidKitThemeTokens.strings
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val scope = SettingSectionScopeImpl().apply {
        info(label = about.appName, supportingText = about.description?.takeIf(String::isNotBlank))
        about.website?.let { link(it, strings.website, AndroidKitIcons.Website) }
        about.sourceCode?.let { link(it, strings.sourceCode, AndroidKitIcons.Code) }
        copyableInfo(
            label = strings.version,
            supportingText = about.version,
            onClickLabel = strings.copyVersion,
            onClick = {
                val clipEntry = ClipEntry(ClipData.newPlainText(strings.version, about.version))
                coroutineScope.launch { clipboard.setClipEntry(clipEntry) }
            },
        )
    }
    SettingsSection(entries = scope.entries)
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
