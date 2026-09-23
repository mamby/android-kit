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

/** Typed content for the predefined About subpage. Hosts own destinations and app copy. */
public data class AndroidKitSettingsAbout(
    public val appName: String,
    public val version: String,
    public val contact: AndroidKitSettingsLink? = null,
    public val privacyPolicy: AndroidKitSettingsLink? = null,
    public val termsOfUse: AndroidKitSettingsLink? = null,
    public val libraries: AndroidKitSettingsLink? = null,
    public val website: AndroidKitSettingsLink? = null,
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
    public val searchTerms: AndroidKitSettingsSearchTerms? = null,
) {
    init {
        require(id.isNotBlank()) { "A legal entry ID cannot be blank." }
        require(title.isNotBlank()) { "A legal entry title cannot be blank." }
    }
}

@Composable
internal fun settingsAboutSections(about: AndroidKitSettingsAbout): List<SettingsRenderedSection> {
    val strings = AndroidKitThemeTokens.strings
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val app = SettingSectionScopeImpl().apply {
        info(
            key = "app",
            label = about.appName,
            supportingText = about.description?.takeIf(String::isNotBlank),
            searchTerms = builtInSearchTerms("about"),
        )
        about.website?.let { link("website", it, strings.website, AndroidKitIcons.Website) }
        about.sourceCode?.let { link("source-code", it, strings.sourceCode, AndroidKitIcons.Code) }
        copyableInfo(
            key = "version",
            label = strings.version,
            supportingText = about.version,
            onClickLabel = strings.copyVersion,
            searchTerms = builtInSearchTerms("version"),
            onClick = {
                val clipEntry = ClipEntry(ClipData.newPlainText(strings.version, about.version))
                coroutineScope.launch { clipboard.setClipEntry(clipEntry) }
            },
        )
    }
    val contact = SettingSectionScopeImpl().apply {
        about.contact?.let {
            link("contact", it, strings.contact, AndroidKitIcons.Contact, strings.contactDescription)
        }
    }
    val legal = SettingSectionScopeImpl().apply {
        about.privacyPolicy?.let { link("privacy", it, strings.privacyPolicy, AndroidKitIcons.AppLock) }
        about.termsOfUse?.let { link("terms", it, strings.termsOfUse, AndroidKitIcons.Document) }
        about.libraries?.let { link("libraries", it, strings.thirdPartyLicenses, AndroidKitIcons.Document) }
        about.additionalLegalEntries.forEach { entry ->
            navigation(
                key = "legal:${entry.id}",
                label = entry.title,
                onClick = entry.onClick,
                supportingText = entry.supportingText,
                icon = AndroidKitIcons.Document,
                enabled = entry.enabled,
                searchTerms = entry.searchTerms,
            )
        }
    }
    return buildList {
        add(SettingsRenderedSection("kit:app", null, null, app.entries))
        if (contact.entries.isNotEmpty()) add(SettingsRenderedSection("kit:contact", null, null, contact.entries))
        if (legal.entries.isNotEmpty()) add(SettingsRenderedSection("kit:legal", null, null, legal.entries))
    }
}

private fun SettingSectionScopeImpl.link(
    key: String,
    link: AndroidKitSettingsLink,
    label: String,
    icon: ImageVector,
    supportingText: String? = null,
) {
    navigation(
        key = key,
        label = label,
        onClick = link.onClick,
        icon = icon,
        enabled = link.enabled,
        supportingText = supportingText,
        searchTerms = builtInSearchTerms(key),
    )
}
