package net.mamby.androidkit.compose.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import net.mamby.androidkit.compose.theme.AndroidKitSettingSectionStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

/** Host-owned, localized content. Keys must be nonblank, stable and unique within a card. */
public sealed interface AndroidKitSectionCardEntry {
    public val key: String

    /**
     * Host-rendered entry body. The card owns its frame, entry padding, dividers and keyed identity.
     * Content inherits Kit entry typography and card content color. The host owns
     * layout within the padded body and its controls. Optional [interaction] is rendered
     * by the card on the full entry wrapper, including padding and minimum touch size.
     */
    public class Custom(
        override val key: String,
        public val interaction: AndroidKitSectionCardInteraction? = null,
        public val content: @Composable () -> Unit,
    ) : AndroidKitSectionCardEntry {
        public constructor(key: String, content: @Composable () -> Unit) : this(key, null, content)
    }

    /** Read-only content, with no action semantics or navigation affordance. */
    public data class Info(
        override val key: String,
        public val label: String,
        public val value: String? = null,
        public val supportingText: String? = null,
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
    ) : AndroidKitSectionCardEntry
}

/** Full-entry interaction. The Kit owns touch sizing, ripple, focus and accessibility roles. */
public sealed interface AndroidKitSectionCardInteraction {
    public class Click(
        public val onClick: () -> Unit,
        public val actionLabel: String? = null,
        public val enabled: Boolean = true,
        public val onLongClick: (() -> Unit)? = null,
        public val longClickLabel: String? = null,
    ) : AndroidKitSectionCardInteraction {
        init {
            require(actionLabel == null || actionLabel.isNotBlank()) {
                "Section card action labels must not be blank when supplied."
            }
            require(longClickLabel == null || longClickLabel.isNotBlank()) {
                "Section card long-click labels must not be blank when supplied."
            }
        }
    }

    public class Toggle(
        public val checked: Boolean,
        public val onCheckedChange: (Boolean) -> Unit,
        public val enabled: Boolean = true,
    ) : AndroidKitSectionCardInteraction
}

/**
 * A sealed section using the Settings card's theme tokens and visual foundation.
 * Empty cards render nothing. Entries retain composition identity by [AndroidKitSectionCardEntry.key].
 * The host owns scrolling, localized text and actions. Built-in entries are Kit-rendered;
 * [AndroidKitSectionCardEntry.Custom] opens only the entry body to host composition.
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
    customEntryLayoutModifier: @Composable (AndroidKitSectionCardEntry.Custom) -> Modifier = { Modifier },
    entryContentPadding: PaddingValues = PaddingValues(
        horizontal = AndroidKitThemeTokens.dimensions.sectionCardHorizontalPadding,
        vertical = AndroidKitThemeTokens.dimensions.settingSectionEntryVerticalPadding,
    ),
): Unit {
    require(entries.all { it.key.isNotBlank() }) { "Section card entry keys must not be blank." }
    require(entries.map { it.key }.toSet().size == entries.size) {
        "Section card entry keys must be unique within a card."
    }
    if (entries.isEmpty()) return

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
                        SectionCardEntry(entry, style, entryContentPadding, customEntryLayoutModifier)
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

@Composable
private fun SectionCardEntry(
    entry: AndroidKitSectionCardEntry,
    style: AndroidKitSettingSectionStyle,
    padding: PaddingValues,
    customEntryLayoutModifier: @Composable (AndroidKitSectionCardEntry.Custom) -> Modifier,
) {
    val tokens = AndroidKitThemeTokens.componentTokens.sectionCard
    val dimensions = AndroidKitThemeTokens.dimensions
    val entryModifier = when (entry) {
        is AndroidKitSectionCardEntry.Custom -> customEntryLayoutModifier(entry)
            .sectionCardInteraction(entry.interaction)
        is AndroidKitSectionCardEntry.Action -> Modifier.sectionCardInteraction(
            AndroidKitSectionCardInteraction.Click(entry.onClick, entry.actionLabel, entry.enabled),
        )
        is AndroidKitSectionCardEntry.Info -> Modifier
            .semantics(mergeDescendants = true) {}
            .heightIn(min = dimensions.minimumTouchTarget)
        is AndroidKitSectionCardEntry.Multiline -> Modifier.semantics(mergeDescendants = true) {}
    }
    Box(
        modifier = entryModifier.fillMaxWidth().padding(padding),
        propagateMinConstraints = true,
    ) {
        when (entry) {
            is AndroidKitSectionCardEntry.Custom -> ProvideTextStyle(style.entryLabelTextStyle) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    entry.content()
                }
            }
            is AndroidKitSectionCardEntry.Action -> SectionCardEntryContent(
                label = entry.label,
                supportingText = entry.supportingText,
                icon = entry.icon,
                modifier = Modifier.fillMaxWidth(),
                style = style,
                fillTextWidth = true,
            ) {}
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
}

@Composable
private fun Modifier.sectionCardInteraction(interaction: AndroidKitSectionCardInteraction?): Modifier {
    val modifier = when (interaction) {
        null -> return this
        is AndroidKitSectionCardInteraction.Click -> if (interaction.onLongClick != null) {
            combinedClickable(
                enabled = interaction.enabled,
                onClickLabel = interaction.actionLabel,
                role = Role.Button,
                onLongClickLabel = interaction.longClickLabel,
                onLongClick = interaction.onLongClick,
                onClick = interaction.onClick,
            )
        } else {
            clickable(
                enabled = interaction.enabled,
                onClickLabel = interaction.actionLabel,
                role = Role.Button,
                onClick = interaction.onClick,
            )
        }
        is AndroidKitSectionCardInteraction.Toggle -> toggleable(
            value = interaction.checked,
            enabled = interaction.enabled,
            role = Role.Switch,
            onValueChange = interaction.onCheckedChange,
        )
    }
    return modifier.heightIn(min = AndroidKitThemeTokens.dimensions.minimumTouchTarget)
}
