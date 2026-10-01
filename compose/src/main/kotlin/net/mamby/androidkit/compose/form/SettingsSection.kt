package net.mamby.androidkit.compose.form

import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.presentation.SectionCardEntryContent
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCard
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCardInteraction
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCardEntry
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitSettingSectionStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

@DslMarker
public annotation class AndroidKitSettingSectionDsl

@AndroidKitSettingSectionDsl
public sealed interface AndroidKitSettingSectionScope {
    public fun button(
        key: String,
        label: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        supportingText: String? = null,
        icon: ImageVector? = null,
        enabled: Boolean = true,
        searchable: Boolean = true,
        searchTerms: AndroidKitSettingsSearchTerms? = null,
    ): Unit

    /** Opens a host-owned destination using the shared navigation-row presentation. */
    public fun navigation(
        key: String,
        label: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        supportingText: String? = null,
        icon: ImageVector? = null,
        enabled: Boolean = true,
        searchable: Boolean = true,
        searchTerms: AndroidKitSettingsSearchTerms? = null,
    ): Unit = button(key, label, onClick, modifier, supportingText, icon, enabled, searchable, searchTerms)

    public fun toggle(
        key: String,
        label: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        supportingText: String? = null,
        icon: ImageVector? = null,
        enabled: Boolean = true,
        colors: SwitchColors? = null,
        searchable: Boolean = true,
        searchTerms: AndroidKitSettingsSearchTerms? = null,
    ): Unit

    public fun slider(
        key: String,
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
        searchable: Boolean = true,
        searchTerms: AndroidKitSettingsSearchTerms? = null,
    ): Unit

    public fun info(key: String, label: String, value: String? = null, supportingText: String? = null,
        icon: ImageVector? = null, modifier: Modifier = Modifier, searchable: Boolean = true,
        searchTerms: AndroidKitSettingsSearchTerms? = null): Unit

}

@Composable
internal fun SettingsSection(
    entries: List<SettingsEntryDefinition>,
    modifier: Modifier = Modifier,
    label: String? = null,
    description: String? = null,
    style: AndroidKitSettingSectionStyle = AndroidKitThemeTokens.settingSectionStyle,
    sectionSpacing: Dp = AndroidKitThemeTokens.dimensions.settingSectionSpacing,
    sectionTextPadding: PaddingValues = PaddingValues(
        horizontal = AndroidKitThemeTokens.dimensions.sectionCardHorizontalPadding,
    ),
    entryContentPadding: PaddingValues = PaddingValues(
        horizontal = AndroidKitThemeTokens.dimensions.sectionCardHorizontalPadding,
        vertical = AndroidKitThemeTokens.dimensions.settingSectionEntryVerticalPadding,
    ),
    dividerPadding: PaddingValues = PaddingValues(
        horizontal = AndroidKitThemeTokens.dimensions.sectionCardHorizontalPadding,
    ),
    onEntryAction: ((SettingsEntryDefinition) -> Unit)? = null,
): Unit {
    AndroidKitSectionCard(
        entries = entries.map { entry ->
            AndroidKitSectionCardEntry.Custom(
                key = entry.key,
                interaction = settingsEntryInteraction(entry, onEntryAction),
            ) {
                SettingsEntry(entry, style, onEntryAction)
            }
        },
        modifier = modifier,
        title = label,
        description = description,
        style = style,
        sectionSpacing = sectionSpacing,
        sectionTextPadding = sectionTextPadding,
        dividerPadding = dividerPadding,
        customEntryLayoutModifier = { custom ->
            val entry = entries.first { it.key == custom.key }
            if (entry is SettingsEntryDefinition.Info) {
                entry.modifier.heightIn(min = AndroidKitThemeTokens.dimensions.minimumTouchTarget)
            } else {
                entry.modifier
            }
        },
        entryContentPadding = entryContentPadding,
    )
}

internal class SettingSectionScopeImpl : AndroidKitSettingSectionScope {
    val entries: MutableList<SettingsEntryDefinition> = mutableListOf()

    override fun button(
        key: String,
        label: String,
        onClick: () -> Unit,
        modifier: Modifier,
        supportingText: String?,
        icon: ImageVector?,
        enabled: Boolean,
        searchable: Boolean,
        searchTerms: AndroidKitSettingsSearchTerms?,
    ) {
        requireEntryKey(key)
        entries += SettingsEntryDefinition.Button(
            key = key,
            label = label,
            onClick = onClick,
            modifier = modifier,
            supportingText = supportingText,
            icon = icon,
            enabled = enabled,
            searchable = searchable,
            searchTerms = searchTerms,
        )
    }

    override fun toggle(
        key: String,
        label: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier,
        supportingText: String?,
        icon: ImageVector?,
        enabled: Boolean,
        colors: SwitchColors?,
        searchable: Boolean,
        searchTerms: AndroidKitSettingsSearchTerms?,
    ) {
        requireEntryKey(key)
        entries += SettingsEntryDefinition.Toggle(
            key = key,
            label = label,
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            supportingText = supportingText,
            icon = icon,
            enabled = enabled,
            colors = colors,
            searchable = searchable,
            searchTerms = searchTerms,
        )
    }

    override fun slider(
        key: String,
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
        searchable: Boolean,
        searchTerms: AndroidKitSettingsSearchTerms?,
    ) {
        requireEntryKey(key)
        entries += SettingsEntryDefinition.Slider(
            key = key,
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
            searchable = searchable,
            searchTerms = searchTerms,
        )
    }

    override fun info(key: String, label: String, value: String?, supportingText: String?,
        icon: ImageVector?, modifier: Modifier, searchable: Boolean,
        searchTerms: AndroidKitSettingsSearchTerms?) {
        requireEntryKey(key)
        entries += SettingsEntryDefinition.Info(key, label, value, supportingText, icon, modifier,
            searchable, searchTerms)
    }

    fun copyableInfo(
        key: String,
        label: String,
        supportingText: String? = null,
        onClickLabel: String,
        onClick: () -> Unit,
        icon: ImageVector? = null,
        searchTerms: AndroidKitSettingsSearchTerms? = null,
    ) {
        requireEntryKey(key)
        entries += SettingsEntryDefinition.CopyableInfo(key, label, supportingText, onClickLabel,
            onClick, icon, searchTerms = searchTerms)
    }

    private fun requireEntryKey(key: String) {
        require(key.isNotBlank()) { "Settings entry keys must not be blank." }
        require(entries.none { it.key == key }) { "Settings entry keys must be unique within a section: $key" }
    }

}

internal sealed interface SettingsEntryDefinition {
    val key: String
    val label: String
    val modifier: Modifier
    val supportingText: String?
    val icon: ImageVector?
    val searchable: Boolean
    val searchTerms: AndroidKitSettingsSearchTerms?

    class Button(
        override val key: String,
        override val label: String,
        val onClick: () -> Unit,
        override val modifier: Modifier,
        override val supportingText: String?,
        override val icon: ImageVector?,
        val enabled: Boolean,
        override val searchable: Boolean = true,
        override val searchTerms: AndroidKitSettingsSearchTerms? = null,
    ) : SettingsEntryDefinition

    class Toggle(
        override val key: String,
        override val label: String,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        override val modifier: Modifier,
        override val supportingText: String?,
        override val icon: ImageVector?,
        val enabled: Boolean,
        val colors: SwitchColors?,
        override val searchable: Boolean = true,
        override val searchTerms: AndroidKitSettingsSearchTerms? = null,
    ) : SettingsEntryDefinition

    class Slider(
        override val key: String,
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
        val minimumLabel: String? = null,
        val maximumLabel: String? = null,
        val isOpacitySlider: Boolean = false,
        override val searchable: Boolean = true,
        override val searchTerms: AndroidKitSettingsSearchTerms? = null,
    ) : SettingsEntryDefinition

    class Info(
        override val key: String,
        override val label: String,
        val value: String?,
        override val supportingText: String?,
        override val icon: ImageVector?,
        override val modifier: Modifier,
        override val searchable: Boolean = true,
        override val searchTerms: AndroidKitSettingsSearchTerms? = null,
    ) : SettingsEntryDefinition

    class CopyableInfo(
        override val key: String,
        override val label: String,
        override val supportingText: String?,
        val onClickLabel: String,
        val onClick: () -> Unit,
        override val icon: ImageVector?,
        override val modifier: Modifier = Modifier,
        override val searchable: Boolean = true,
        override val searchTerms: AndroidKitSettingsSearchTerms? = null,
    ) : SettingsEntryDefinition

}

private fun settingsEntryInteraction(
    entry: SettingsEntryDefinition,
    onEntryAction: ((SettingsEntryDefinition) -> Unit)?,
): AndroidKitSectionCardInteraction? = when (entry) {
    is SettingsEntryDefinition.Button -> AndroidKitSectionCardInteraction.Click(
        enabled = entry.enabled,
        onClick = { onEntryAction?.invoke(entry); entry.onClick() },
    )
    is SettingsEntryDefinition.CopyableInfo -> AndroidKitSectionCardInteraction.Click(
        actionLabel = entry.onClickLabel,
        onClick = { onEntryAction?.invoke(entry); entry.onClick() },
    )
    is SettingsEntryDefinition.Toggle -> AndroidKitSectionCardInteraction.Toggle(
        checked = entry.checked,
        enabled = entry.enabled,
        onCheckedChange = { value -> onEntryAction?.invoke(entry); entry.onCheckedChange(value) },
    )
    is SettingsEntryDefinition.Info, is SettingsEntryDefinition.Slider -> null
}

@Composable
private fun SettingsEntry(
    entry: SettingsEntryDefinition,
    style: AndroidKitSettingSectionStyle,
    onEntryAction: ((SettingsEntryDefinition) -> Unit)?,
) {
    if (entry is SettingsEntryDefinition.Slider) {
        SettingsSliderEntry(entry, style, onEntryAction)
        return
    }
    SectionCardEntryContent(
        label = entry.label,
        supportingText = entry.supportingText,
        icon = entry.icon,
        modifier = Modifier.fillMaxWidth(),
        style = style,
    ) {
        when (entry) {
            is SettingsEntryDefinition.Button -> Box(
                modifier = Modifier.size(
                    AndroidKitIcons.ChevronRight.defaultWidth,
                    AndroidKitIcons.ChevronRight.defaultHeight,
                ),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = AndroidKitIcons.SettingsChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(
                        AndroidKitIcons.SettingsChevronRight.defaultWidth,
                        AndroidKitIcons.SettingsChevronRight.defaultHeight,
                    ),
                    tint = style.secondaryContentColor,
                )
            }
            is SettingsEntryDefinition.CopyableInfo -> Icon(
                imageVector = AndroidKitIcons.Copy,
                contentDescription = null,
                tint = style.secondaryContentColor,
            )
            is SettingsEntryDefinition.Info -> entry.value?.let {
                Text(it, style = style.valueLabelTextStyle, color = style.secondaryContentColor)
            }
            is SettingsEntryDefinition.Toggle -> Switch(
                checked = entry.checked,
                onCheckedChange = null,
                enabled = entry.enabled,
                colors = entry.colors ?: SwitchDefaults.colors(),
            )
            is SettingsEntryDefinition.Slider -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSliderEntry(
    entry: SettingsEntryDefinition.Slider,
    style: AndroidKitSettingSectionStyle,
    onEntryAction: ((SettingsEntryDefinition) -> Unit)?,
): Unit {
    val tokens = AndroidKitThemeTokens.componentTokens.settingsSlider
    val dimensions = AndroidKitThemeTokens.dimensions
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.trackSpacing),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(tokens.labelContentSpacing),
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
                verticalArrangement = Arrangement.spacedBy(tokens.supportingTextSpacing),
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
        val colors = entry.colors ?: if (entry.isOpacitySlider) {
            SliderDefaults.colors(thumbColor = AndroidKitThemeTokens.colorScheme.onPrimary)
        } else {
            SliderDefaults.colors()
        }
        val interactionSource = remember { MutableInteractionSource() }
        val sliderState = rememberSliderState(
            value = entry.value,
            steps = entry.steps,
            trackRange = entry.valueRange,
        )
        SideEffect {
            sliderState.value = entry.value
        }
        Slider(
            state = sliderState,
            onValueChange = entry.onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = entry.label },
            onValueChangeFinished = {
                onEntryAction?.invoke(entry)
                entry.onValueChangeFinished?.invoke()
            },
            enabled = entry.enabled,
            colors = colors,
            interactionSource = interactionSource,
            thumb = {
                if (entry.isOpacitySlider) {
                    Box(
                        Modifier
                            .size(tokens.opacityThumbSize)
                            .dropShadow(
                                shape = CircleShape,
                                shadow = Shadow(
                                    radius = dimensions.spaceExtraSmall,
                                    spread = 0.dp,
                                    offset = DpOffset.Zero,
                                    color = if (entry.enabled) {
                                        AndroidKitThemeTokens.colorScheme.outlineVariant
                                    } else {
                                        Color.Transparent
                                    },
                                ),
                            )
                            .clip(CircleShape)
                            .background(
                                color = if (entry.enabled) colors.thumbColor else colors.disabledThumbColor,
                                shape = CircleShape,
                            )
                            .indication(interactionSource, ripple()),
                    )
                } else {
                    SliderDefaults.Thumb(
                        interactionSource = interactionSource,
                        colors = colors,
                        enabled = entry.enabled,
                    )
                }
            },
            track = { sliderState ->
                if (entry.isOpacitySlider) {
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        colors = colors,
                        enabled = entry.enabled,
                        drawStopIndicator = null,
                        drawTick = { _, _ -> },
                        thumbTrackGapSize = 0.dp,
                    )
                } else {
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        colors = colors,
                        enabled = entry.enabled,
                    )
                }
            },
        )
        if (entry.minimumLabel != null && entry.maximumLabel != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(entry.minimumLabel, style = style.supportingTextStyle, color = style.secondaryContentColor)
                Text(entry.maximumLabel, style = style.supportingTextStyle, color = style.secondaryContentColor)
            }
        }
    }
}
