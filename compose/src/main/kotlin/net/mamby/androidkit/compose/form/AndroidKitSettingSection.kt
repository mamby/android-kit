package net.mamby.androidkit.compose.form

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitSettingSectionStyle
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
        enabled: Boolean = true,
    ): Unit

    public fun toggle(
        label: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        supportingText: String? = null,
        icon: ImageVector? = null,
        enabled: Boolean = true,
        colors: SwitchColors? = null,
    ): Unit

    public fun slider(
        label: String,
        value: Float,
        onValueChange: (Float) -> Unit,
        modifier: Modifier = Modifier,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        steps: Int = 0,
        onValueChangeFinished: (() -> Unit)? = null,
        supportingText: String? = null,
        icon: ImageVector? = null,
        valueLabel: String? = null,
        enabled: Boolean = true,
        colors: SliderColors? = null,
    ): Unit

    public fun item(
        modifier: Modifier = Modifier,
        content: @Composable RowScope.() -> Unit,
    ): Unit
}

@Composable
public fun AndroidKitSettingSection(
    modifier: Modifier = Modifier,
    label: String? = null,
    description: String? = null,
    style: AndroidKitSettingSectionStyle = AndroidKitThemeTokens.settingSectionStyle,
    sectionSpacing: Dp = AndroidKitThemeTokens.dimensions.settingSectionSpacing,
    sectionTextPadding: PaddingValues = PaddingValues(
        horizontal = AndroidKitThemeTokens.dimensions.spaceMedium,
    ),
    entryContentPadding: PaddingValues = PaddingValues(
        horizontal = AndroidKitThemeTokens.dimensions.spaceMedium,
        vertical = AndroidKitThemeTokens.dimensions.settingSectionEntryVerticalPadding,
    ),
    dividerPadding: PaddingValues = PaddingValues(
        horizontal = AndroidKitThemeTokens.dimensions.spaceMedium,
    ),
    content: AndroidKitSettingSectionScope.() -> Unit,
): Unit {
    val scope = SettingSectionScopeImpl().apply(content)
    require(scope.entries.isNotEmpty()) { "At least one settings entry is required." }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
    ) {
        label?.let {
            Text(
                text = it,
                modifier = Modifier.padding(sectionTextPadding),
                style = style.sectionLabelTextStyle,
                color = style.secondaryContentColor,
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = style.shape,
            colors = CardDefaults.cardColors(
                containerColor = style.containerColor,
                contentColor = style.contentColor,
            ),
            border = BorderStroke(style.borderWidth, style.borderColor),
        ) {
            Column {
                scope.entries.forEachIndexed { index, entry ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(dividerPadding),
                            color = style.dividerColor,
                        )
                    }
                    SettingsEntry(
                        entry = entry,
                        style = style,
                        contentPadding = entryContentPadding,
                    )
                }
            }
        }
        description?.let {
            Text(
                text = it,
                modifier = Modifier.padding(sectionTextPadding),
                style = style.descriptionTextStyle,
                color = style.secondaryContentColor,
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
        enabled: Boolean,
    ) {
        entries += SettingsEntryDefinition.Button(
            label = label,
            onClick = onClick,
            modifier = modifier,
            supportingText = supportingText,
            icon = icon,
            enabled = enabled,
        )
    }

    override fun toggle(
        label: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier,
        supportingText: String?,
        icon: ImageVector?,
        enabled: Boolean,
        colors: SwitchColors?,
    ) {
        entries += SettingsEntryDefinition.Toggle(
            label = label,
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            supportingText = supportingText,
            icon = icon,
            enabled = enabled,
            colors = colors,
        )
    }

    override fun slider(
        label: String,
        value: Float,
        onValueChange: (Float) -> Unit,
        modifier: Modifier,
        valueRange: ClosedFloatingPointRange<Float>,
        steps: Int,
        onValueChangeFinished: (() -> Unit)?,
        supportingText: String?,
        icon: ImageVector?,
        valueLabel: String?,
        enabled: Boolean,
        colors: SliderColors?,
    ) {
        entries += SettingsEntryDefinition.Slider(
            label = label,
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            supportingText = supportingText,
            icon = icon,
            valueLabel = valueLabel,
            enabled = enabled,
            colors = colors,
        )
    }

    override fun item(
        modifier: Modifier,
        content: @Composable RowScope.() -> Unit,
    ) {
        entries += SettingsEntryDefinition.Custom(modifier, content)
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
        val enabled: Boolean,
    ) : SettingsEntryDefinition

    class Toggle(
        override val label: String,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        override val modifier: Modifier,
        override val supportingText: String?,
        override val icon: ImageVector?,
        val enabled: Boolean,
        val colors: SwitchColors?,
    ) : SettingsEntryDefinition

    class Slider(
        override val label: String,
        val value: Float,
        val onValueChange: (Float) -> Unit,
        override val modifier: Modifier,
        val valueRange: ClosedFloatingPointRange<Float>,
        val steps: Int,
        val onValueChangeFinished: (() -> Unit)?,
        override val supportingText: String?,
        override val icon: ImageVector?,
        val valueLabel: String?,
        val enabled: Boolean,
        val colors: SliderColors?,
    ) : SettingsEntryDefinition

    class Custom(
        override val modifier: Modifier,
        val content: @Composable RowScope.() -> Unit,
    ) : SettingsEntryDefinition {
        override val label: String = ""
        override val supportingText: String? = null
        override val icon: ImageVector? = null
    }
}

@Composable
private fun SettingsEntry(
    entry: SettingsEntryDefinition,
    style: AndroidKitSettingSectionStyle,
    contentPadding: PaddingValues,
): Unit = when (entry) {
    is SettingsEntryDefinition.Button -> SettingsButtonEntry(entry, style, contentPadding)
    is SettingsEntryDefinition.Slider -> SettingsSliderEntry(entry, style, contentPadding)
    is SettingsEntryDefinition.Toggle -> SettingsToggleEntry(entry, style, contentPadding)
    is SettingsEntryDefinition.Custom -> Row(
        modifier = entry.modifier
            .fillMaxWidth()
            .heightIn(min = AndroidKitThemeTokens.dimensions.minimumTouchTarget)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(AndroidKitThemeTokens.dimensions.spaceMedium),
        verticalAlignment = Alignment.CenterVertically,
        content = entry.content,
    )
}

@Composable
private fun SettingsButtonEntry(
    entry: SettingsEntryDefinition.Button,
    style: AndroidKitSettingSectionStyle,
    contentPadding: PaddingValues,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    SettingsEntryContent(
        label = entry.label,
        supportingText = entry.supportingText,
        icon = entry.icon,
        modifier = entry.modifier
            .fillMaxWidth()
            .clickable(enabled = entry.enabled, role = Role.Button, onClick = entry.onClick)
            .heightIn(min = dimensions.minimumTouchTarget),
        style = style,
        contentPadding = contentPadding,
    ) {
        Icon(
            imageVector = AndroidKitIcons.ChevronRight,
            contentDescription = null,
            tint = style.secondaryContentColor,
        )
    }
}

@Composable
private fun SettingsToggleEntry(
    entry: SettingsEntryDefinition.Toggle,
    style: AndroidKitSettingSectionStyle,
    contentPadding: PaddingValues,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    SettingsEntryContent(
        label = entry.label,
        supportingText = entry.supportingText,
        icon = entry.icon,
        modifier = entry.modifier
            .fillMaxWidth()
            .toggleable(
                value = entry.checked,
                enabled = entry.enabled,
                role = Role.Switch,
                onValueChange = entry.onCheckedChange,
            )
            .heightIn(min = dimensions.minimumTouchTarget),
        style = style,
        contentPadding = contentPadding,
    ) {
        Switch(
            checked = entry.checked,
            onCheckedChange = null,
            enabled = entry.enabled,
            colors = entry.colors ?: SwitchDefaults.colors(),
        )
    }
}

@Composable
private fun SettingsSliderEntry(
    entry: SettingsEntryDefinition.Slider,
    style: AndroidKitSettingSectionStyle,
    contentPadding: PaddingValues,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    Column(
        modifier = entry.modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            entry.icon?.let {
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
                    text = entry.label,
                    style = style.entryLabelTextStyle,
                )
                entry.supportingText?.let {
                    Text(
                        text = it,
                        style = style.supportingTextStyle,
                        color = style.secondaryContentColor,
                    )
                }
            }
            entry.valueLabel?.let {
                Text(
                    text = it,
                    style = style.valueLabelTextStyle,
                    color = style.secondaryContentColor,
                )
            }
        }
        Slider(
            value = entry.value,
            onValueChange = entry.onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = entry.label },
            valueRange = entry.valueRange,
            steps = entry.steps,
            onValueChangeFinished = entry.onValueChangeFinished,
            enabled = entry.enabled,
            colors = entry.colors ?: SliderDefaults.colors(),
        )
    }
}

@Composable
private fun SettingsEntryContent(
    label: String,
    supportingText: String?,
    icon: ImageVector?,
    modifier: Modifier,
    style: AndroidKitSettingSectionStyle,
    contentPadding: PaddingValues,
    trailingContent: @Composable () -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    Row(
        modifier = modifier.padding(contentPadding),
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
                style = style.entryLabelTextStyle,
            )
            supportingText?.let {
                Text(
                    text = it,
                    style = style.supportingTextStyle,
                    color = style.secondaryContentColor,
                )
            }
        }
        trailingContent()
    }
}
