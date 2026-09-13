package net.mamby.androidkit.compose.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

/** Host-localized action. The host opens its URL, mail intent, or navigation destination. */
public data class AndroidKitSettingsAction(
    public val label: String,
    public val onClick: () -> Unit,
    public val supportingText: String? = null,
    public val icon: ImageVector? = null,
    public val enabled: Boolean = true,
) {
    init { require(label.isNotBlank()) { "Settings action label must not be blank." } }
}

public data class AndroidKitSettingsSupport(
    public val action: AndroidKitSettingsAction,
)

/** Predefined settings link. Its label comes from the current Kit strings. */
public data class AndroidKitSettingsLink(
    public val onClick: () -> Unit,
    public val supportingText: String? = null,
    public val icon: ImageVector? = null,
    public val enabled: Boolean = true,
)

/** Typed content for the predefined App info subpage. Hosts own all destinations. */
public data class AndroidKitSettingsAbout(
    public val version: String,
    public val privacyPolicy: AndroidKitSettingsLink,
    public val termsOfUse: AndroidKitSettingsLink,
    public val libraries: AndroidKitSettingsLink,
    public val sourceCode: AndroidKitSettingsLink,
    public val license: AndroidKitSettingsLink,
    public val contributors: AndroidKitSettingsLink,
) {
    init { require(version.isNotBlank()) { "App info requires a version." } }
}

public sealed interface AndroidKitSettingsPageConfiguration {
    /** About is always present; the donation banner is optional and appears last. */
    public data class Main(
        public val contact: AndroidKitSettingsLink,
        public val appInfo: AndroidKitSettingsLink,
        public val support: AndroidKitSettingsSupport? = null,
    ) : AndroidKitSettingsPageConfiguration

    public data class AppInfo(public val about: AndroidKitSettingsAbout) : AndroidKitSettingsPageConfiguration

    /** Ordinary host-defined subpage; no main-settings footer. */
    public data object Subpage : AndroidKitSettingsPageConfiguration
}

@Composable
internal fun SettingsAboutSection(main: AndroidKitSettingsPageConfiguration.Main) {
    val strings = AndroidKitThemeTokens.strings
    val scope = SettingSectionScopeImpl().apply {
        link(main.contact, strings.contact, AndroidKitIcons.Contact)
        link(main.appInfo, strings.appInfo, AndroidKitIcons.Info)
    }
    SettingsSection(entries = scope.entries, label = strings.about)
}

@Composable
internal fun SettingsAppSection(about: AndroidKitSettingsAbout) {
    val strings = AndroidKitThemeTokens.strings
    val scope = SettingSectionScopeImpl().apply {
        link(about.privacyPolicy, strings.privacyPolicy, AndroidKitIcons.AppLock)
        link(about.termsOfUse, strings.termsOfUse, AndroidKitIcons.Document)
        link(about.libraries, strings.thirdPartyLicenses, AndroidKitIcons.Document)
        info(label = strings.version, value = about.version, icon = AndroidKitIcons.Info)
    }
    SettingsSection(entries = scope.entries, label = strings.app)
}

@Composable
internal fun SettingsOpenSourceSection(about: AndroidKitSettingsAbout) {
    val strings = AndroidKitThemeTokens.strings
    val scope = SettingSectionScopeImpl().apply {
        link(about.sourceCode, strings.sourceCode, AndroidKitIcons.Code)
        link(about.license, strings.license, AndroidKitIcons.Document)
        link(about.contributors, strings.contributors, AndroidKitIcons.Contributors)
    }
    SettingsSection(entries = scope.entries, label = strings.openSource)
}

private fun AndroidKitSettingSectionScope.link(link: AndroidKitSettingsLink, label: String, icon: ImageVector) {
    navigation(label = label, onClick = link.onClick,
        supportingText = link.supportingText, icon = link.icon ?: icon, enabled = link.enabled)
}

@Composable
internal fun SettingsSupportCard(support: AndroidKitSettingsSupport) {
    val dimensions = AndroidKitThemeTokens.dimensions
    val style = AndroidKitThemeTokens.settingSectionStyle
    val strings = AndroidKitThemeTokens.strings
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = style.shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(dimensions.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spaceMedium)) {
                Icon(AndroidKitIcons.Support, contentDescription = null)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall)) {
                    Text(strings.supportTitle, style = style.entryLabelTextStyle)
                    Text(strings.supportDescription, style = style.descriptionTextStyle)
                }
            }
            Button(onClick = support.action.onClick, enabled = support.action.enabled, modifier = Modifier.fillMaxWidth()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    support.action.icon?.let { Icon(it, contentDescription = null) }
                    Text(support.action.label)
                }
            }
            support.action.supportingText?.let { Text(it, style = style.descriptionTextStyle) }
        }
    }
}
