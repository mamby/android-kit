package net.mamby.androidkit.compose.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import net.mamby.androidkit.compose.theme.AndroidKitCardDefaults
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

@DslMarker
public annotation class AndroidKitSettingSectionDsl

@AndroidKitSettingSectionDsl
public interface AndroidKitSettingSectionScope {
    public fun button(
        label: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        supportingText: String? = null,
        icon: ImageVector? = null,
    ): Unit

    public fun toggle(
        label: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        supportingText: String? = null,
        icon: ImageVector? = null,
    ): Unit
}

@Composable
public fun AndroidKitSettingSection(
    modifier: Modifier = Modifier,
    label: String? = null,
    description: String? = null,
    content: AndroidKitSettingSectionScope.() -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val scope = SettingSectionScopeImpl().apply(content)
    require(scope.entries.isNotEmpty()) { "At least one settings entry is required." }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
    ) {
        label?.let {
            Text(
                text = it,
                modifier = Modifier.padding(horizontal = dimensions.spaceMedium),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = AndroidKitCardDefaults.colors(),
            border = AndroidKitCardDefaults.border(),
        ) {
            Column {
                scope.entries.forEachIndexed { index, entry ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = dimensions.spaceMedium),
                        )
                    }
                    SettingsEntry(entry)
                }
            }
        }
        description?.let {
            Text(
                text = it,
                modifier = Modifier.padding(horizontal = dimensions.spaceMedium),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private class SettingSectionScopeImpl : AndroidKitSettingSectionScope {
    val entries: MutableList<SettingsEntryDefinition> = mutableListOf()

    override fun button(
        label: String,
        onClick: () -> Unit,
        modifier: Modifier,
        supportingText: String?,
        icon: ImageVector?,
    ) {
        entries += SettingsEntryDefinition.Button(
            label = label,
            onClick = onClick,
            modifier = modifier,
            supportingText = supportingText,
            icon = icon,
        )
    }

    override fun toggle(
        label: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier,
        supportingText: String?,
        icon: ImageVector?,
    ) {
        entries += SettingsEntryDefinition.Toggle(
            label = label,
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            supportingText = supportingText,
            icon = icon,
        )
    }
}

private sealed interface SettingsEntryDefinition {
    val label: String
    val modifier: Modifier
    val supportingText: String?
    val icon: ImageVector?

    class Button(
        override val label: String,
        val onClick: () -> Unit,
        override val modifier: Modifier,
        override val supportingText: String?,
        override val icon: ImageVector?,
    ) : SettingsEntryDefinition

    class Toggle(
        override val label: String,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        override val modifier: Modifier,
        override val supportingText: String?,
        override val icon: ImageVector?,
    ) : SettingsEntryDefinition
}

@Composable
private fun SettingsEntry(entry: SettingsEntryDefinition): Unit = when (entry) {
    is SettingsEntryDefinition.Button -> SettingsButtonEntry(entry)
    is SettingsEntryDefinition.Toggle -> SettingsToggleEntry(entry)
}

@Composable
private fun SettingsButtonEntry(entry: SettingsEntryDefinition.Button): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    SettingsEntryContent(
        label = entry.label,
        supportingText = entry.supportingText,
        icon = entry.icon,
        modifier = entry.modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = entry.onClick)
            .heightIn(min = dimensions.minimumTouchTarget),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsToggleEntry(entry: SettingsEntryDefinition.Toggle): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    SettingsEntryContent(
        label = entry.label,
        supportingText = entry.supportingText,
        icon = entry.icon,
        modifier = entry.modifier
            .fillMaxWidth()
            .toggleable(
                value = entry.checked,
                role = Role.Switch,
                onValueChange = entry.onCheckedChange,
            )
            .heightIn(min = dimensions.minimumTouchTarget),
    ) {
        Switch(
            checked = entry.checked,
            onCheckedChange = null,
        )
    }
}

@Composable
private fun SettingsEntryContent(
    label: String,
    supportingText: String?,
    icon: ImageVector?,
    modifier: Modifier,
    trailingContent: @Composable () -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    Row(
        modifier = modifier.padding(
            horizontal = dimensions.spaceMedium,
            vertical = dimensions.settingSectionEntryVerticalPadding,
        ),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        trailingContent()
    }
}
