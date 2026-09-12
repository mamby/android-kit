package net.mamby.androidkit.compose.form

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import java.text.Normalizer
import java.util.Locale
import net.mamby.androidkit.compose.action.AndroidKitActionItem
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.theme.AndroidKitFloatingSurfaceDefaults
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

/** Stable identity is independent of the translated label and filtered list position. */
public data class AndroidKitSettingsOption(public val id: String, public val label: String)

/** The host-localized system choice and the host-localized value it currently resolves to. */
public data class AndroidKitSettingsSystemOption(
    public val id: String,
    public val label: String,
    public val currentValueLabel: String,
) {
    init {
        require(id.isNotBlank()) { "System option ID must not be blank." }
        require(label.isNotBlank()) { "System option label must not be blank." }
        require(currentValueLabel.isNotBlank()) { "System option current value must not be blank." }
    }
}

public data class AndroidKitSettingsSelection(
    public val label: String,
    public val options: List<AndroidKitSettingsOption>,
    public val selectedId: String,
    public val onSelected: (String) -> Unit,
    public val closeContentDescription: String,
    public val enabled: Boolean = true,
    /** Optional row icon override; null uses the predefined language or theme icon. */
    public val icon: ImageVector? = null,
    public val systemOption: AndroidKitSettingsSystemOption,
) {
    init {
        val allIds = options.map { it.id } + systemOption.id
        require(allIds.isNotEmpty()) { "Selection options must not be empty." }
        require(allIds.distinct().size == allIds.size) { "Option IDs must be unique." }
        require(selectedId in allIds) { "The selected ID must identify an option." }
    }
}

public data class AndroidKitLanguageSetting(
    public val selection: AndroidKitSettingsSelection,
    public val searchLabel: String,
    public val emptyResultsLabel: String,
)

public data class AndroidKitFloatingOpacitySetting(
    public val label: String,
    public val value: Float,
    public val minimumLabel: String,
    public val maximumLabel: String,
    public val onValueChange: (Float) -> Unit,
    public val onValueChangeFinished: () -> Unit,
    public val supportingText: String? = null,
    public val enabled: Boolean = true,
) {
    init {
        require(value.isFinite() && value in AndroidKitFloatingSurfaceDefaults.MinimumOpacityLevel..
            AndroidKitFloatingSurfaceDefaults.MaximumOpacityLevel) { "Opacity level must be between 0 and 100." }
    }
}

/** Host-defined timeout choices. IDs have no duration semantics inside Android Kit. */
public data class AndroidKitAppLockTimeoutSetting(
    public val label: String,
    public val options: List<AndroidKitSettingsOption>,
    public val selectedId: String,
    public val onSelected: (String) -> Unit,
    public val enabled: Boolean = true,
) {
    init {
        require(options.isNotEmpty()) { "Timeout options must not be empty." }
        require(options.map { it.id }.distinct().size == options.size) { "Timeout option IDs must be unique." }
        require(options.any { it.id == selectedId }) { "The selected ID must identify a timeout option." }
    }
}

public data class AndroidKitAppLockSetting(
    public val label: String,
    public val checked: Boolean,
    public val onCheckedChange: (Boolean) -> Unit,
    public val supportingText: String? = null,
    public val enabled: Boolean = true,
    /** Optional row icon override; null uses the predefined app-lock icon. */
    public val icon: ImageVector? = null,
    /** Shown only while checked. The host persists and enforces the selected timeout. */
    public val timeout: AndroidKitAppLockTimeoutSetting? = null,
    public val lockNowLabel: String? = null,
    /** Optional host lock action, shown only while checked. Requires a localized [lockNowLabel]. */
    public val onLockNow: (() -> Unit)? = null,
) {
    init {
        require(onLockNow == null || !lockNowLabel.isNullOrBlank()) { "Lock now requires a localized label." }
    }
}

@DslMarker
public annotation class AndroidKitSettingsPageDsl

/** Typed settings declarations; resolve composable resources before entering this builder. */
@AndroidKitSettingsPageDsl
public class AndroidKitSettingsPageScope internal constructor() {
    private val sections = mutableListOf<@Composable (SettingsPageRenderScope) -> Unit>()
    private val keys = mutableSetOf<String>()

    internal val isEmpty: Boolean get() = sections.isEmpty()

    private fun add(key: String, render: @Composable (SettingsPageRenderScope) -> Unit) {
        require(keys.add(key)) { "Settings page keys must be unique: $key" }
        sections += render
    }

    public fun generalSection(key: String = "general", label: String? = null,
        language: AndroidKitLanguageSetting? = null, theme: AndroidKitSettingsSelection? = null,
        floatingOpacity: AndroidKitFloatingOpacitySetting? = null,
    ) { add(key) { it.generalSection(key, label, language, theme, floatingOpacity) } }

    public fun securitySection(key: String = "security", label: String? = null,
        appLock: AndroidKitAppLockSetting? = null,
        content: AndroidKitSettingSectionScope.() -> Unit = {},
    ) { add(key) { it.securitySection(key, label, appLock) { content() } } }

    public fun section(key: String, label: String? = null, description: String? = null,
        content: AndroidKitSettingSectionScope.() -> Unit,
    ) { add(key) { it.section(key, label, description) { content() } } }

    @Composable
    internal fun render(scope: SettingsPageRenderScope) { sections.forEach { it(scope) } }
}

@AndroidKitSettingsPageDsl
internal interface SettingsPageRenderScope {
    @Composable
    @NonSkippableComposable
    public fun generalSection(
        key: String = "general",
        label: String? = null,
        language: AndroidKitLanguageSetting? = null,
        theme: AndroidKitSettingsSelection? = null,
        floatingOpacity: AndroidKitFloatingOpacitySetting? = null,
    ): Unit

    @Composable
    @NonSkippableComposable
    public fun securitySection(
        key: String = "security",
        label: String? = null,
        appLock: AndroidKitAppLockSetting? = null,
        content: @Composable AndroidKitSettingSectionScope.() -> Unit = {},
    ): Unit

    @Composable
    @NonSkippableComposable
    public fun section(
        key: String,
        label: String? = null,
        description: String? = null,
        content: @Composable AndroidKitSettingSectionScope.() -> Unit,
    ): Unit

}

/**
 * A scrollable settings page. Main pages require Support, Get involved, and About data;
 * Kit appends those sections after host settings. Subpages do not repeat this footer.
 * Hosts own values, external links, navigation, and retained list state.
 */
@Composable
public fun AndroidKitSettingsPage(
    configuration: AndroidKitSettingsPageConfiguration,
    title: String? = null,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: List<AndroidKitActionItem> = emptyList(),
    listState: LazyListState = rememberLazyListState(),
    content: AndroidKitSettingsPageScope.() -> Unit = {},
): Unit {
    var activePicker by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = SettingsPageScopeImpl { activePicker = it }
    val declarations = AndroidKitSettingsPageScope().apply(content)
    if (configuration is AndroidKitSettingsPageConfiguration.About) {
        require(declarations.isEmpty) { "About content is supplied through AndroidKitSettingsAbout, not custom sections." }
        declarations.aboutContent(configuration.data)
    }
    declarations.render(scope)
    val dimensions = AndroidKitThemeTokens.dimensions
    val direction = LocalLayoutDirection.current
    val pageTitle = title ?: (configuration as? AndroidKitSettingsPageConfiguration.About)?.data?.title
    AndroidKitPage(title = pageTitle, modifier = modifier, onBack = onBack, actions = actions) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                start = padding.calculateStartPadding(direction) + dimensions.screenPadding,
                top = padding.calculateTopPadding(),
                end = padding.calculateEndPadding(direction) + dimensions.screenPadding,
                bottom = padding.calculateBottomPadding() + dimensions.spaceMedium,
            ),
            verticalArrangement = Arrangement.spacedBy(dimensions.settingsPageSectionSpacing),
        ) {
            items(scope.items.toList(), key = { "section:${it.key}" }) { it.content() }
            if (configuration is AndroidKitSettingsPageConfiguration.Main) {
                item(key = "kit:support") { SettingsSupportCard(configuration.support) }
                item(key = "kit:get-involved") { SettingsGetInvolvedSection(configuration.getInvolved) }
                item(key = "kit:about") { SettingsAboutEntry(configuration.about, configuration.onAbout) }
            }
        }
    }
    val picker = scope.pickers[activePicker]?.takeIf { it.selection.enabled }
    val timeoutPicker = scope.timeoutPickers[activePicker]
    if (picker != null) {
        key(activePicker) { SettingsPicker(picker) { activePicker = null } }
    } else if (timeoutPicker != null) {
        key(activePicker) { AppLockTimeoutDialog(timeoutPicker) { activePicker = null } }
    } else if (activePicker != null) {
        androidx.compose.runtime.LaunchedEffect(activePicker) { activePicker = null }
    }
}

private data class SettingsPageItem(val key: String, val content: @Composable () -> Unit)
private data class SettingsPickerDefinition(
    val selection: AndroidKitSettingsSelection,
    val searchLabel: String? = null,
    val emptyResultsLabel: String? = null,
)

private class SettingsPageScopeImpl(private val openPicker: (String) -> Unit) : SettingsPageRenderScope {
    val items = mutableStateListOf<SettingsPageItem>()
    val pickers = mutableMapOf<String, SettingsPickerDefinition>()
    val timeoutPickers = mutableStateMapOf<String, AndroidKitAppLockTimeoutSetting>()
    private val keys = mutableSetOf<String>()

    private fun registerItem(item: SettingsPageItem) {
        val existingIndex = items.indexOfFirst { it.key == item.key }
        if (existingIndex >= 0) {
            // Compose may re-enter only part of the DSL during state updates. Keep keyed
            // declarations idempotent so those updates replace their existing slot.
            items[existingIndex] = item
            return
        }
        require(keys.add(item.key)) { "Settings page keys must be unique: ${item.key}" }
        items += item
    }

    private fun removeItem(key: String) {
        val index = items.indexOfFirst { it.key == key }
        if (index >= 0) items.removeAt(index)
        keys.remove(key)
        pickers.keys.removeAll { it.endsWith(":$key") }
        timeoutPickers.keys.removeAll { it.endsWith(":$key") }
    }

    @Composable
    @NonSkippableComposable
    override fun section(
        key: String,
        label: String?,
        description: String?,
        content: @Composable AndroidKitSettingSectionScope.() -> Unit,
    ) {
        val entries = SettingSectionScopeImpl()
        androidx.compose.runtime.key(key) { entries.content() }
        if (entries.entries.isNotEmpty()) {
            registerItem(SettingsPageItem(key) {
                SettingsSection(entries = entries.entries, label = label, description = description)
            })
            DisposableEffect(this@SettingsPageScopeImpl, key) {
                onDispose { removeItem(key) }
            }
        } else {
            removeItem(key)
        }
    }


    @Composable
    @NonSkippableComposable
    override fun generalSection(
        key: String,
        label: String?,
        language: AndroidKitLanguageSetting?,
        theme: AndroidKitSettingsSelection?,
        floatingOpacity: AndroidKitFloatingOpacitySetting?,
    ) {
        section(key, label) {
            language?.let {
                addPicker(
                    key, "language", SettingsPickerDefinition(it.selection, it.searchLabel, it.emptyResultsLabel),
                    AndroidKitIcons.Language,
                )
            }
            theme?.let { addPicker(key, "theme", SettingsPickerDefinition(it), AndroidKitIcons.Theme) }
            floatingOpacity?.let {
                (this as SettingSectionScopeImpl).entries += SettingsEntryDefinition.Slider(
                    label = it.label, value = it.value, onValueChange = it.onValueChange,
                    modifier = Modifier,
                    valueRange = AndroidKitFloatingSurfaceDefaults.MinimumOpacityLevel..
                        AndroidKitFloatingSurfaceDefaults.MaximumOpacityLevel,
                    steps = 19, onValueChangeFinished = it.onValueChangeFinished,
                    supportingText = it.supportingText, icon = null, valueLabel = null,
                    enabled = it.enabled, colors = null,
                    minimumLabel = it.minimumLabel, maximumLabel = it.maximumLabel,
                    isOpacitySlider = true,
                )
            }
        }
    }

    private fun AndroidKitSettingSectionScope.addPicker(
        sectionKey: String,
        kind: String,
        picker: SettingsPickerDefinition,
        defaultIcon: ImageVector,
    ) {
        val pickerKey = "$kind:$sectionKey"
        pickers[pickerKey] = picker
        val selection = picker.selection
        val options = selection.displayOptions()
        button(
            label = selection.label,
            supportingText = options.first { it.id == selection.selectedId }.label,
            icon = selection.icon ?: defaultIcon, enabled = selection.enabled,
            onClick = { openPicker(pickerKey) },
        )
    }

    @Composable
    @NonSkippableComposable
    override fun securitySection(
        key: String,
        label: String?,
        appLock: AndroidKitAppLockSetting?,
        content: @Composable AndroidKitSettingSectionScope.() -> Unit,
    ) {
        val timeoutKey = "app-lock-timeout:$key"
        val timeout = appLock?.timeout?.takeIf { appLock.checked }
        if (timeout != null && appLock.enabled && timeout.enabled) {
            timeoutPickers[timeoutKey] = timeout
        } else {
            timeoutPickers.remove(timeoutKey)
        }
        section(key, label) {
            appLock?.let {
                toggle(it.label, it.checked, it.onCheckedChange, supportingText = it.supportingText,
                    icon = it.icon ?: AndroidKitIcons.AppLock, enabled = it.enabled)
                timeout?.let { selection ->
                    button(
                        label = selection.label,
                        supportingText = selection.options.first { option -> option.id == selection.selectedId }.label,
                        enabled = it.enabled && selection.enabled,
                        onClick = { openPicker(timeoutKey) },
                    )
                }
                if (it.checked && it.onLockNow != null) {
                    button(label = requireNotNull(it.lockNowLabel), onClick = it.onLockNow, enabled = it.enabled)
                }
            }
            content()
        }
    }
}

@Composable
private fun AppLockTimeoutDialog(selection: AndroidKitAppLockTimeoutSetting, onDismiss: () -> Unit) {
    val dimensions = AndroidKitThemeTokens.dimensions
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(selection.label) },
        confirmButton = {},
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().selectableGroup()) {
                items(selection.options, key = { it.id }) { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .selectable(
                                selected = option.id == selection.selectedId,
                                role = Role.RadioButton,
                                onClick = { onDismiss(); selection.onSelected(option.id) },
                            )
                            .heightIn(min = dimensions.minimumTouchTarget)
                            .padding(vertical = dimensions.settingSectionEntryVerticalPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
                    ) {
                        RadioButton(selected = option.id == selection.selectedId, onClick = null)
                        Text(option.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
    )
}

@Composable
private fun SettingsPicker(picker: SettingsPickerDefinition, onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val selection = picker.selection
    val displayOptions = selection.displayOptions()
    val options = remember(displayOptions, query) {
        val search = query.searchKey()
        displayOptions.filter { search.isEmpty() || it.label.searchKey().contains(search) }
    }
    val listState = rememberLazyListState()
    val dimensions = AndroidKitThemeTokens.dimensions
    val style = AndroidKitThemeTokens.bottomSheetStyle
    AndroidKitBottomSheet(
        visible = true, title = selection.label, onDismiss = onDismiss,
        closeContentDescription = selection.closeContentDescription,
        fitContent = picker.searchLabel == null,
        search = picker.searchLabel?.let { AndroidKitSheetSearch(query, { query = it }, it) },
        scrollMode = AndroidKitBottomSheetScrollMode.ContentManaged,
        dismissGesturesEnabled = !listState.canScrollBackward,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            state = listState,
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall),
        ) {
            if (options.isEmpty()) {
                item(key = "empty") { Text(picker.emptyResultsLabel.orEmpty()) }
            }
            items(options, key = { "option:${it.id}" }) { option ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .selectable(
                            selected = option.id == selection.selectedId, role = Role.RadioButton,
                            onClick = { selection.onSelected(option.id); onDismiss() },
                        )
                        .heightIn(min = dimensions.minimumTouchTarget)
                        .padding(horizontal = dimensions.spaceSmall, vertical = dimensions.settingSectionEntryVerticalPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
                ) {
                    Text(option.label, modifier = Modifier.weight(1f), style = AndroidKitThemeTokens.settingSectionStyle.entryLabelTextStyle)
                    if (option.id == selection.selectedId) {
                        Icon(AndroidKitIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

private val SearchMarks = Regex("\\p{M}+")
private fun String.searchKey(): String = Normalizer.normalize(trim(), Normalizer.Form.NFD)
    .replace(SearchMarks, "").lowercase(Locale.ROOT)

private fun AndroidKitSettingsSelection.displayOptions(): List<AndroidKitSettingsOption> =
    listOf(
        AndroidKitSettingsOption(
            id = systemOption.id,
            label = "${systemOption.label} (${systemOption.currentValueLabel})",
        )
    ) + options
