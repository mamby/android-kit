package net.mamby.androidkit.compose.form

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SwitchColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCard
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCardControlColors
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCardEntry
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
        entries = entries.map { it.toSectionCardEntry(onEntryAction) },
        modifier = modifier,
        title = label,
        description = description,
        style = style,
        sectionSpacing = sectionSpacing,
        sectionTextPadding = sectionTextPadding,
        dividerPadding = dividerPadding,
        entryModifiers = entries.associate { it.key to it.modifier },
        controlColors = entries.mapNotNull { entry ->
            when (entry) {
                is SettingsEntryDefinition.Toggle -> entry.key to AndroidKitSectionCardControlColors(switch = entry.colors)
                is SettingsEntryDefinition.Slider -> entry.key to AndroidKitSectionCardControlColors(slider = entry.colors)
                else -> null
            }
        }.toMap(),
        opacitySliderKeys = entries.filterIsInstance<SettingsEntryDefinition.Slider>()
            .filter(SettingsEntryDefinition.Slider::isOpacitySlider).mapTo(mutableSetOf()) { it.key },
        entryContentPadding = entryContentPadding,
    )
}

private fun SettingsEntryDefinition.toSectionCardEntry(
    onEntryAction: ((SettingsEntryDefinition) -> Unit)?,
): AndroidKitSectionCardEntry = when (this) {
    is SettingsEntryDefinition.Button -> AndroidKitSectionCardEntry.Navigation(
        key = key, label = label, supportingText = supportingText, icon = icon, enabled = enabled,
        onClick = { onEntryAction?.invoke(this); onClick() },
    )
    is SettingsEntryDefinition.CopyableInfo -> AndroidKitSectionCardEntry.CopyableInfo(
        key = key, label = label, supportingText = supportingText, icon = icon,
        actionLabel = onClickLabel, onClick = { onEntryAction?.invoke(this); onClick() },
    )
    is SettingsEntryDefinition.Toggle -> AndroidKitSectionCardEntry.Toggle(
        key = key, label = label, supportingText = supportingText, icon = icon, enabled = enabled,
        checked = checked,
        onCheckedChange = { value -> onEntryAction?.invoke(this); onCheckedChange(value) },
    )
    is SettingsEntryDefinition.Info -> AndroidKitSectionCardEntry.InlineInfo(
        key = key, label = label, supportingText = supportingText, icon = icon, value = value,
    )
    is SettingsEntryDefinition.Slider -> AndroidKitSectionCardEntry.Slider(
        key = key, label = label, supportingText = supportingText, icon = icon, enabled = enabled,
        value = value, onValueChange = onValueChange, valueRange = valueRange, steps = steps,
        valueLabel = valueLabel, minimumLabel = minimumLabel, maximumLabel = maximumLabel,
        onValueChangeFinished = { onEntryAction?.invoke(this); onValueChangeFinished?.invoke() },
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
