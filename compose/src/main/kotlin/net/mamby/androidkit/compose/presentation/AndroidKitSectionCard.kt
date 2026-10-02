package net.mamby.androidkit.compose.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import net.mamby.androidkit.compose.action.AndroidKitActionFlyoutScope
import net.mamby.androidkit.compose.action.AndroidKitContextMenuContent
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitSettingSectionStyle
import net.mamby.androidkit.compose.theme.AndroidKitComponentTheme
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

/** Host-owned, localized content. Keys must be nonblank, stable and unique within a card. */
public sealed interface AndroidKitSectionCardEntry {
    public val key: String

    /** Read-only content, with no action semantics or navigation affordance. */
    public data class Info(
        override val key: String,
        public val label: String,
        public val value: String? = null,
        public val supportingText: String? = null,
        public val contextMenu: (AndroidKitActionFlyoutScope.() -> Unit)? = null,
    ) : AndroidKitSectionCardEntry

    /** [actionLabel] describes the action to accessibility services, e.g. "Call work number". */
    public data class Action(
        override val key: String,
        public val label: String,
        public val actionLabel: String,
        public val onClick: () -> Unit,
        public val supportingText: String? = null,
        public val icon: ImageVector? = null,
        public val enabled: Boolean = true,
        public val trailingIcon: ImageVector? = null,
        public val trailingIconTint: Color = Color.Unspecified,
        public val contextMenu: (AndroidKitActionFlyoutScope.() -> Unit)? = null,
    ) : AndroidKitSectionCardEntry {
        init {
            require(actionLabel.isNotBlank()) { "Section card action labels must not be blank." }
        }
    }

    /** Read-only text that preserves line breaks and wraps without a line limit. */
    public data class Multiline(
        override val key: String,
        public val text: String,
        public val label: String? = null,
        public val contextMenu: (AndroidKitActionFlyoutScope.() -> Unit)? = null,
    ) : AndroidKitSectionCardEntry

    /** A single Kit-owned switch row; the switch delegates input to the full-entry wrapper. */
    public data class Toggle(
        override val key: String,
        public val label: String,
        public val checked: Boolean,
        public val onCheckedChange: (Boolean) -> Unit,
        public val supportingText: String? = null,
        public val icon: ImageVector? = null,
        public val enabled: Boolean = true,
    ) : AndroidKitSectionCardEntry

    /** Opens a destination; Kit renders the navigation affordance. */
    public data class Navigation(
        override val key: String,
        public val label: String,
        public val onClick: () -> Unit,
        public val supportingText: String? = null,
        public val icon: ImageVector? = null,
        public val enabled: Boolean = true,
    ) : AndroidKitSectionCardEntry

    /** Informational content with a trailing value, as used in Settings. */
    public data class InlineInfo(
        override val key: String,
        public val label: String,
        public val value: String? = null,
        public val supportingText: String? = null,
        public val icon: ImageVector? = null,
    ) : AndroidKitSectionCardEntry

    /** Copying remains a host action; Kit owns its full-row interaction and copy affordance. */
    public data class CopyableInfo(
        override val key: String,
        public val label: String,
        public val actionLabel: String,
        public val onClick: () -> Unit,
        public val supportingText: String? = null,
        public val icon: ImageVector? = null,
    ) : AndroidKitSectionCardEntry {
        init {
            require(actionLabel.isNotBlank()) { "Section card action labels must not be blank." }
        }
    }

    /** Kit-rendered slider. Hosts supply state, its range, and already-localized value labels. */
    public data class Slider(
        override val key: String,
        public val label: String,
        public val value: Float,
        public val onValueChange: (Float) -> Unit,
        public val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        public val steps: Int = 0,
        public val onValueChangeFinished: (() -> Unit)? = null,
        public val supportingText: String? = null,
        public val icon: ImageVector? = null,
        public val valueLabel: String? = null,
        public val enabled: Boolean = true,
        public val minimumLabel: String? = null,
        public val maximumLabel: String? = null,
    ) : AndroidKitSectionCardEntry
}

/** Preserve the existing Settings color APIs without exposing entry rendering or style slots. */
internal data class AndroidKitSectionCardControlColors(
    val switch: SwitchColors? = null,
    val slider: SliderColors? = null,
)

/**
 * A sealed section using the Settings card's theme tokens and visual foundation.
 * Empty cards render nothing. Entries retain composition identity by [AndroidKitSectionCardEntry.key].
 * Whole-row content and interactions are Kit-rendered. Hosts declare content, callbacks and menu
 * items. The public API has no composable entry-body slots.
 */
@Composable
public fun AndroidKitSectionCard(
    entries: List<AndroidKitSectionCardEntry>,
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
): Unit {
    AndroidKitSectionCard(
        entries = entries,
        modifier = modifier,
        title = title,
        description = description,
        style = AndroidKitThemeTokens.settingSectionStyle,
    )
}

@Composable
internal fun AndroidKitSectionCard(
    entries: List<AndroidKitSectionCardEntry>,
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    style: AndroidKitSettingSectionStyle,
    sectionSpacing: Dp = AndroidKitThemeTokens.dimensions.settingSectionSpacing,
    sectionTextPadding: PaddingValues = PaddingValues(
        horizontal = AndroidKitThemeTokens.dimensions.sectionCardHorizontalPadding,
    ),
    dividerPadding: PaddingValues = PaddingValues(
        horizontal = AndroidKitThemeTokens.dimensions.sectionCardHorizontalPadding,
    ),
    entryModifiers: Map<String, Modifier> = emptyMap(),
    controlColors: Map<String, AndroidKitSectionCardControlColors> = emptyMap(),
    opacitySliderKeys: Set<String> = emptySet(),
    entryContentPadding: PaddingValues = PaddingValues(
        horizontal = AndroidKitThemeTokens.dimensions.sectionCardHorizontalPadding,
        vertical = AndroidKitThemeTokens.dimensions.settingSectionEntryVerticalPadding,
    ),
): Unit {
    AndroidKitComponentTheme {
        require(entries.all { it.key.isNotBlank() }) { "Section card entry keys must not be blank." }
        require(entries.map { it.key }.toSet().size == entries.size) {
            "Section card entry keys must be unique within a card."
        }
        if (entries.isEmpty()) return@AndroidKitComponentTheme

        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(sectionSpacing),
        ) {
            title?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(sectionTextPadding).semantics { heading() },
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
                    entries.forEachIndexed { index, entry ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(dividerPadding),
                                color = style.dividerColor,
                            )
                        }
                        key(entry.key) {
                            SectionCardEntry(
                                entry, style, entryContentPadding,
                                entryModifiers[entry.key] ?: Modifier,
                                controlColors[entry.key], entry.key in opacitySliderKeys,
                            )
                        }
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
}

@Composable
private fun SectionCardEntry(
    entry: AndroidKitSectionCardEntry,
    style: AndroidKitSettingSectionStyle,
    padding: PaddingValues,
    layoutModifier: Modifier,
    controlColors: AndroidKitSectionCardControlColors?,
    isOpacitySlider: Boolean,
) {
    val dimensions = AndroidKitThemeTokens.dimensions
    val menu = when (entry) {
        is AndroidKitSectionCardEntry.Action -> entry.contextMenu
        is AndroidKitSectionCardEntry.Info -> entry.contextMenu
        is AndroidKitSectionCardEntry.Multiline -> entry.contextMenu
        else -> null
    }
    val entryModifier = when (entry) {
        is AndroidKitSectionCardEntry.Action -> if (menu != null) {
            Modifier.heightIn(min = dimensions.minimumTouchTarget)
        } else Modifier.sectionCardClick(entry.onClick, entry.actionLabel, entry.enabled)
        is AndroidKitSectionCardEntry.Navigation -> Modifier.sectionCardClick(entry.onClick, null, entry.enabled)
        is AndroidKitSectionCardEntry.CopyableInfo -> Modifier.sectionCardClick(entry.onClick, entry.actionLabel, true)
        is AndroidKitSectionCardEntry.Toggle -> Modifier
            .toggleable(entry.checked, enabled = entry.enabled, role = Role.Switch, onValueChange = entry.onCheckedChange)
            .heightIn(min = dimensions.minimumTouchTarget)
        is AndroidKitSectionCardEntry.Info -> Modifier
            .semantics(mergeDescendants = true) {}
            .heightIn(min = dimensions.minimumTouchTarget)
        is AndroidKitSectionCardEntry.Multiline -> Modifier.semantics(mergeDescendants = true) {}
        is AndroidKitSectionCardEntry.InlineInfo -> Modifier
            .semantics(mergeDescendants = true) {}
            .heightIn(min = dimensions.minimumTouchTarget)
        is AndroidKitSectionCardEntry.Slider -> Modifier
    }
    val body: @Composable () -> Unit = {
        SectionCardEntryBody(entry, style, controlColors, isOpacitySlider)
    }
    if (menu != null) {
        val action = entry as? AndroidKitSectionCardEntry.Action
        AndroidKitContextMenuContent(
            menu = menu,
            modifier = layoutModifier.then(entryModifier).fillMaxWidth(),
            enabled = action?.enabled ?: true,
            onClick = action?.onClick,
            onClickLabel = action?.actionLabel,
            role = if (action != null) Role.Button else null,
        ) {
            Box(Modifier.fillMaxWidth().padding(padding), propagateMinConstraints = true) { body() }
        }
    } else {
        Box(layoutModifier.then(entryModifier).fillMaxWidth().padding(padding), propagateMinConstraints = true) { body() }
    }
}

@Composable
private fun SectionCardEntryBody(
    entry: AndroidKitSectionCardEntry,
    style: AndroidKitSettingSectionStyle,
    controlColors: AndroidKitSectionCardControlColors?,
    isOpacitySlider: Boolean,
) {
    val tokens = AndroidKitThemeTokens.componentTokens.sectionCard
    when (entry) {
        is AndroidKitSectionCardEntry.Action -> SectionCardEntryContent(
            label = entry.label,
            supportingText = entry.supportingText,
            icon = entry.icon,
            modifier = Modifier.fillMaxWidth(),
            style = style,
            fillTextWidth = true,
        ) {
            entry.trailingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(tokens.trailingIconSize),
                    tint = entry.trailingIconTint.takeUnless { color -> color == Color.Unspecified }
                        ?: style.contentColor,
                )
            }
        }
        is AndroidKitSectionCardEntry.Toggle -> SectionCardEntryContent(
            label = entry.label,
            supportingText = entry.supportingText,
            icon = entry.icon,
            modifier = Modifier.fillMaxWidth(),
            style = style,
        ) {
            Switch(
                checked = entry.checked, onCheckedChange = null, enabled = entry.enabled,
                colors = controlColors?.switch ?: SwitchDefaults.colors(),
            )
        }
        is AndroidKitSectionCardEntry.Navigation -> SectionCardEntryContent(
            entry.label, entry.supportingText, entry.icon, Modifier.fillMaxWidth(), style,
        ) {
            Box(
                modifier = Modifier.size(AndroidKitIcons.ChevronRight.defaultWidth, AndroidKitIcons.ChevronRight.defaultHeight),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = AndroidKitIcons.SettingsChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(
                        AndroidKitIcons.SettingsChevronRight.defaultWidth, AndroidKitIcons.SettingsChevronRight.defaultHeight,
                    ),
                    tint = style.secondaryContentColor,
                )
            }
        }
        is AndroidKitSectionCardEntry.CopyableInfo -> SectionCardEntryContent(
            entry.label, entry.supportingText, entry.icon, Modifier.fillMaxWidth(), style,
        ) {
            Icon(AndroidKitIcons.Copy, contentDescription = null, tint = style.secondaryContentColor)
        }
        is AndroidKitSectionCardEntry.InlineInfo -> SectionCardEntryContent(
            entry.label, entry.supportingText, entry.icon, Modifier.fillMaxWidth(), style,
        ) {
            entry.value?.let { Text(it, style = style.valueLabelTextStyle, color = style.secondaryContentColor) }
        }
        is AndroidKitSectionCardEntry.Slider -> SectionCardSliderEntry(entry, style, controlColors?.slider, isOpacitySlider)
        is AndroidKitSectionCardEntry.Multiline -> Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(tokens.multilineSpacing),
        ) {
            entry.label?.let { Text(it, style = style.entryLabelTextStyle) }
            Text(entry.text, style = style.entryLabelTextStyle)
        }
        is AndroidKitSectionCardEntry.Info -> Column(
            // Stack values to leave the full width available for addresses and large text.
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(tokens.multilineSpacing),
        ) {
            Text(entry.label, style = style.entryLabelTextStyle)
            entry.value?.let { Text(it, style = style.valueLabelTextStyle, color = style.secondaryContentColor) }
            entry.supportingText?.let { Text(it, style = style.supportingTextStyle, color = style.secondaryContentColor) }
        }
    }
}

@Composable
private fun Modifier.sectionCardClick(onClick: () -> Unit, label: String?, enabled: Boolean): Modifier =
    clickable(enabled = enabled, onClickLabel = label, role = Role.Button, onClick = onClick)
        .heightIn(min = AndroidKitThemeTokens.dimensions.minimumTouchTarget)
