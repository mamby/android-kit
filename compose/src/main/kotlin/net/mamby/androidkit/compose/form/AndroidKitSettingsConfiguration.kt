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

/** Available actions are always rendered in report, suggest, translate order. */
public data class AndroidKitSettingsGetInvolved(
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
    public val appName: String,
    public val version: String,
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

/** Main-page content is optional and rendered in the declared footer order. */
public sealed interface AndroidKitSettingsPageConfiguration {
    public data class Main(
        public val support: AndroidKitSettingsSupport? = null,
        public val getInvolved: AndroidKitSettingsGetInvolved? = null,
        public val about: AndroidKitSettingsAbout? = null,
    ) : AndroidKitSettingsPageConfiguration

    /** Ordinary host-defined subpage; no main-settings footer. */
    public data object Subpage : AndroidKitSettingsPageConfiguration
}

@Composable
internal fun SettingsAboutContent(about: AndroidKitSettingsAbout) {
    val strings = AndroidKitThemeTokens.strings
    val dimensions = AndroidKitThemeTokens.dimensions
    val identity = SettingSectionScopeImpl().apply {
        info(label = about.appName, supportingText = about.description, icon = about.appIcon)
        about.maintainer?.let { info(label = it) }
        info(label = strings.version, value = about.version)
        about.whatsNew?.let { action(it) }
    }
    val openSource = SettingSectionScopeImpl().apply {
        listOfNotNull(about.sourceCode, about.contributors, about.license, about.libraries).forEach { action(it) }
    }
    val information = SettingSectionScopeImpl().apply {
        listOfNotNull(about.privacyPolicy, about.contact).forEach { action(it) }
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.settingsPageSectionSpacing),
    ) {
        SettingsSection(entries = identity.entries)
        SettingsSection(entries = openSource.entries, label = strings.openSource)
        SettingsSection(entries = information.entries, label = strings.information)
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

@Composable
internal fun SettingsGetInvolvedSection(data: AndroidKitSettingsGetInvolved) {
    val scope = SettingSectionScopeImpl()
    listOfNotNull(data.reportIssue, data.suggestImprovement, data.helpTranslate).forEach { scope.action(it) }
    SettingsSection(entries = scope.entries, label = AndroidKitThemeTokens.strings.getInvolved)
}
