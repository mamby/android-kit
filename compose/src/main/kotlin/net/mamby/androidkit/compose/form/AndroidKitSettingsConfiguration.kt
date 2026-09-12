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
    public val title: String? = null,
    public val description: String? = null,
    public val action: AndroidKitSettingsAction,
) {
    init { require(title == null || title.isNotBlank()) { "Support title must not be blank." } }
}

/** Available actions are always rendered in report, suggest, translate order. */
public data class AndroidKitSettingsGetInvolved(
    public val title: String? = null,
    public val reportIssue: AndroidKitSettingsAction? = null,
    public val suggestImprovement: AndroidKitSettingsAction? = null,
    public val helpTranslate: AndroidKitSettingsAction? = null,
) {
    init {
        require(reportIssue != null || suggestImprovement != null || helpTranslate != null) {
            "Get involved requires at least one action."
        }
    }
}

/** About is settings content, using the same section and row renderers as other settings. */
public data class AndroidKitSettingsAbout(
    public val title: String? = null,
    public val appName: String,
    public val versionLabel: String? = null,
    public val version: String,
    public val openSourceTitle: String? = null,
    public val informationTitle: String? = null,
    public val description: String? = null,
    public val maintainer: String? = null,
    public val appIcon: ImageVector? = null,
    public val whatsNew: AndroidKitSettingsAction? = null,
    public val sourceCode: AndroidKitSettingsAction? = null,
    public val contributors: AndroidKitSettingsAction? = null,
    public val license: AndroidKitSettingsAction? = null,
    public val libraries: AndroidKitSettingsAction? = null,
    public val privacyPolicy: AndroidKitSettingsAction? = null,
    public val contact: AndroidKitSettingsAction? = null,
) {
    init {
        require(appName.isNotBlank() && version.isNotBlank()) {
            "About requires a localized app name and version."
        }
    }
}

/** Required page role: main pages cannot accidentally omit the shared footer. */
public sealed interface AndroidKitSettingsPageConfiguration {
    public data class Main(
        public val support: AndroidKitSettingsSupport,
        public val getInvolved: AndroidKitSettingsGetInvolved,
        public val about: AndroidKitSettingsAbout,
        public val onAbout: () -> Unit,
    ) : AndroidKitSettingsPageConfiguration

    /** Ordinary host-defined subpage; no main-settings footer. */
    public data object Subpage : AndroidKitSettingsPageConfiguration

    /** Kit-defined About subpage. Pass the same data used by [Main.about]. */
    public data class About(public val data: AndroidKitSettingsAbout) : AndroidKitSettingsPageConfiguration
}

internal fun AndroidKitSettingsPageScope.aboutContent(about: AndroidKitSettingsAbout) {
    val strings = AndroidKitThemeTokens.strings
    section("identity") {
        info(label = about.appName, supportingText = about.description, icon = about.appIcon)
        about.maintainer?.let { info(label = it) }
        info(label = about.versionLabel ?: strings.version, value = about.version)
        about.whatsNew?.let { action(it) }
    }
    section("open-source", label = about.openSourceTitle ?: strings.openSource) {
        listOfNotNull(about.sourceCode, about.contributors, about.license, about.libraries).forEach { action(it) }
    }
    section("information", label = about.informationTitle ?: strings.information) {
        listOfNotNull(about.privacyPolicy, about.contact).forEach { action(it) }
    }
}

internal fun AndroidKitSettingSectionScope.action(action: AndroidKitSettingsAction) {
    navigation(action.label, action.onClick, supportingText = action.supportingText,
        icon = action.icon, enabled = action.enabled)
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
                    Text(support.title ?: strings.supportTitle, style = style.entryLabelTextStyle)
                    Text(support.description ?: strings.supportDescription, style = style.descriptionTextStyle)
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

@Composable
internal fun SettingsGetInvolvedSection(data: AndroidKitSettingsGetInvolved) {
    val scope = SettingSectionScopeImpl()
    listOfNotNull(data.reportIssue, data.suggestImprovement, data.helpTranslate).forEach { scope.action(it) }
    SettingsSection(entries = scope.entries, label = data.title ?: AndroidKitThemeTokens.strings.getInvolved)
}

@Composable
internal fun SettingsAboutEntry(data: AndroidKitSettingsAbout, onClick: () -> Unit) {
    val scope = SettingSectionScopeImpl()
    scope.navigation(label = data.title ?: AndroidKitThemeTokens.strings.about, supportingText = data.appName, onClick = onClick)
    SettingsSection(entries = scope.entries)
}
