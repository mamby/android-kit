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
import net.mamby.androidkit.compose.action.AndroidKitFloatingAction
import net.mamby.androidkit.compose.action.AndroidKitActionItem
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.theme.AndroidKitFloatingSurfaceDefaults
import net.mamby.androidkit.compose.theme.AndroidKitStrings
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

/** Stable identity is independent of the translated label and filtered list position. */
public data class AndroidKitSettingsOption(public val id: String, public val label: String)

/** A system choice and the host-localized value it currently resolves to. */
public data class AndroidKitSettingsSystemOption(
    public val id: String,
    public val currentValueLabel: String,
) {
    init {
        require(id.isNotBlank()) { "System option ID must not be blank." }
        require(currentValueLabel.isNotBlank()) { "System option current value must not be blank." }
    }
}

public data class AndroidKitSettingsSelection(
    public val options: List<AndroidKitSettingsOption>,
    public val selectedId: String,
    public val onSelected: (String) -> Unit,
    public val enabled: Boolean = true,
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
)

public data class AndroidKitFloatingOpacitySetting(
    public val value: Float,
    public val onValueChange: (Float) -> Unit,
    public val onValueChangeFinished: () -> Unit,
    public val enabled: Boolean = true,
) {
    init {
        require(value.isFinite() && value in AndroidKitFloatingSurfaceDefaults.MinimumOpacityLevel..
            AndroidKitFloatingSurfaceDefaults.MaximumOpacityLevel) { "Opacity level must be between 0 and 100." }
    }
}

/** Timeout choices. Hosts own the policy and options; Kit supplies shared fallback labels. */
public data class AndroidKitAppLockTimeoutSetting(
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
    public val checked: Boolean,
    public val onCheckedChange: (Boolean) -> Unit,
    /** Host authentication failure; absent during normal operation. */
    public val errorMessage: String? = null,
    public val enabled: Boolean = true,
    /** Shown only while checked. The host persists and enforces the selected timeout. */
    public val timeout: AndroidKitAppLockTimeoutSetting? = null,
    /** Optional lock action, shown only while checked; Kit supplies the shared fallback label. */
    public val onLockNow: (() -> Unit)? = null,
)

@DslMarker
public annotation class AndroidKitSettingsPageDsl

/** Typed settings declarations; resolve composable resources before entering this builder. */
@AndroidKitSettingsPageDsl
public class AndroidKitSettingsPageScope internal constructor() {
    private val sections = mutableListOf<@Composable (SettingsPageRenderScope) -> Unit>()
    private val keys = mutableSetOf<String>()

    private fun add(key: String, render: @Composable (SettingsPageRenderScope) -> Unit) {
        require(keys.add(key)) { "Settings page keys must be unique: $key" }
        sections += render
    }

    public fun section(key: String, label: String? = null, description: String? = null,
        content: AndroidKitSettingsSectionScope.() -> Unit,
    ) { add(key) { it.section(key, label, description) { content() } } }

    @Composable
    internal fun render(scope: SettingsPageRenderScope) { sections.forEach { it(scope) } }
}

/** Host-ordered declarations; predefined entries retain Kit-owned presentation. */
@AndroidKitSettingSectionDsl
public class AndroidKitSettingsSectionScope internal constructor(
    private val entries: AndroidKitSettingSectionScope,
    private val onLanguage: (AndroidKitLanguageSetting) -> Unit,
    private val onTheme: (AndroidKitSettingsSelection) -> Unit,
    private val onTransparency: (AndroidKitFloatingOpacitySetting) -> Unit,
    private val onAppLock: (AndroidKitAppLockSetting) -> Unit,
) : AndroidKitSettingSectionScope by entries {
    public fun language(setting: AndroidKitLanguageSetting): Unit = onLanguage(setting)
    public fun theme(setting: AndroidKitSettingsSelection): Unit = onTheme(setting)
    public fun transparency(setting: AndroidKitFloatingOpacitySetting): Unit = onTransparency(setting)
    public fun appLock(setting: AndroidKitAppLockSetting): Unit = onAppLock(setting)
}

@AndroidKitSettingsPageDsl
internal interface SettingsPageRenderScope {
    @Composable
    @NonSkippableComposable
    public fun section(
        key: String,
        label: String? = null,
        description: String? = null,
        content: @Composable AndroidKitSettingsSectionScope.() -> Unit,
    ): Unit

}

/**
 * A scrollable settings page. An optional About action is always last on Main pages.
 * About renders predefined sections.
 * Hosts own values, external links, navigation, and retained list state.
 */
@Composable
public fun AndroidKitSettingsPage(
    configuration: AndroidKitSettingsPageConfiguration.Customizable,
    title: String? = null,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: List<AndroidKitActionItem> = emptyList(),
    listState: LazyListState = rememberLazyListState(),
    content: AndroidKitSettingsPageScope.() -> Unit = {},
): Unit {
    SettingsPage(configuration, title, modifier, onBack, actions, listState, content)
}

/** Fixed About surface. Kit owns its predefined entries, title, icons, and section order. */
@Composable
public fun AndroidKitSettingsPage(
    configuration: AndroidKitSettingsPageConfiguration.About,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
): Unit {
    SettingsPage(configuration, AndroidKitThemeTokens.strings.about, modifier, onBack,
        emptyList(), listState, {})
}

@Composable
private fun SettingsPage(
    configuration: AndroidKitSettingsPageConfiguration,
    title: String?,
    modifier: Modifier,
    onBack: (() -> Unit)?,
    actions: List<AndroidKitActionItem>,
    listState: LazyListState,
    content: AndroidKitSettingsPageScope.() -> Unit,
) {
    var activePicker by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = SettingsPageScopeImpl { activePicker = it }
    val declarations = AndroidKitSettingsPageScope().apply(content)
    declarations.render(scope)
    val dimensions = AndroidKitThemeTokens.dimensions
    val direction = LocalLayoutDirection.current
    AndroidKitPage(title = title, modifier = modifier, onBack = onBack, actions = actions) { padding ->
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
            when (configuration) {
                is AndroidKitSettingsPageConfiguration.Main -> {
                    configuration.about?.let { about ->
                        item(key = "kit:about") { SettingsAboutSection(about) }
                    }
                }
                is AndroidKitSettingsPageConfiguration.About -> {
                    val about = configuration.content
                    item(key = "kit:app") { SettingsAppInformationSection(about) }
                    if (about.contact != null) {
                        item(key = "kit:contact") { SettingsContactSection(about) }
                    }
                    if (about.privacyPolicy != null || about.termsOfUse != null || about.libraries != null || about.additionalLegalEntries.isNotEmpty()) {
                        item(key = "kit:legal") { SettingsLegalSection(about) }
                    }
                }
                AndroidKitSettingsPageConfiguration.Subpage -> Unit
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
        content: @Composable AndroidKitSettingsSectionScope.() -> Unit,
    ) {
        pickers.keys.removeAll { it.endsWith(":$key") }
        timeoutPickers.keys.removeAll { it.endsWith(":$key") }
        val entries = SettingSectionScopeImpl()
        val strings = AndroidKitThemeTokens.strings
        val declaredKinds = mutableSetOf<String>()
        val declarations = AndroidKitSettingsSectionScope(
            entries,
            onLanguage = { setting ->
                require(declaredKinds.add("language")) { "Duplicate language entry in $key" }
                entries.addPicker(key, "language", SettingsPickerDefinition(setting.selection,
                    strings.searchLanguages, strings.noMatchingLanguages), AndroidKitIcons.Language, strings)
            },
            onTheme = { setting ->
                require(declaredKinds.add("theme")) { "Duplicate theme entry in $key" }
                entries.addPicker(key, "theme", SettingsPickerDefinition(setting), AndroidKitIcons.Theme, strings)
            },
            onTransparency = { setting ->
                require(declaredKinds.add("transparency")) { "Duplicate transparency entry in $key" }
                entries.entries += SettingsEntryDefinition.Slider(
                    label = strings.transparency, value = setting.value, onValueChange = setting.onValueChange,
                    modifier = Modifier,
                    valueRange = AndroidKitFloatingSurfaceDefaults.MinimumOpacityLevel..
                        AndroidKitFloatingSurfaceDefaults.MaximumOpacityLevel,
                    steps = 19, onValueChangeFinished = setting.onValueChangeFinished,
                    supportingText = null, icon = null, valueLabel = null,
                    enabled = setting.enabled, colors = null,
                    minimumLabel = strings.min, maximumLabel = strings.max, isOpacitySlider = true,
                )
            },
            onAppLock = { setting ->
                require(declaredKinds.add("appLock")) { "Duplicate app-lock entry in $key" }
                val timeoutKey = "app-lock-timeout:$key"
                val timeout = setting.timeout?.takeIf { setting.checked }
                if (timeout != null && setting.enabled && timeout.enabled) timeoutPickers[timeoutKey] = timeout
                entries.toggle(strings.appLock, setting.checked, setting.onCheckedChange,
                    supportingText = setting.errorMessage, icon = AndroidKitIcons.AppLock, enabled = setting.enabled)
                timeout?.let { selection ->
                    entries.button(label = strings.lockAfterLeavingApp,
                        supportingText = selection.options.first { it.id == selection.selectedId }.label,
                        enabled = setting.enabled && selection.enabled, onClick = { openPicker(timeoutKey) })
                }
                if (setting.checked && setting.onLockNow != null) {
                    entries.button(strings.lockNow, setting.onLockNow, enabled = setting.enabled)
                }
            },
        )
        androidx.compose.runtime.key(key) { declarations.content() }
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

    private fun AndroidKitSettingSectionScope.addPicker(
        sectionKey: String,
        kind: String,
        picker: SettingsPickerDefinition,
        defaultIcon: ImageVector,
        strings: AndroidKitStrings,
    ) {
        val pickerKey = "$kind:$sectionKey"
        pickers[pickerKey] = picker
        val selection = picker.selection
        val selectionLabel = if (kind == "theme") strings.theme else strings.language
        val options = selection.displayOptions(strings.system)
        button(
            label = selectionLabel,
            supportingText = options.first { it.id == selection.selectedId }.label,
            icon = defaultIcon, enabled = selection.enabled,
            onClick = { openPicker(pickerKey) },
        )
    }

}

@Composable
private fun AppLockTimeoutDialog(selection: AndroidKitAppLockTimeoutSetting, onDismiss: () -> Unit) {
    val dimensions = AndroidKitThemeTokens.dimensions
    val strings = AndroidKitThemeTokens.strings
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.lockAfterLeavingApp) },
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
    val strings = AndroidKitThemeTokens.strings
    val displayOptions = selection.displayOptions(strings.system)
    val options = remember(displayOptions, query) {
        val search = query.searchKey()
        displayOptions.filter { search.isEmpty() || it.label.searchKey().contains(search) }
    }
    val listState = rememberLazyListState()
    val dimensions = AndroidKitThemeTokens.dimensions
    AndroidKitBottomSheet(
        visible = true, title = if (picker.searchLabel != null) strings.language else strings.theme, onDismiss = onDismiss,
        fitContent = picker.searchLabel == null,
        floatingAction = picker.searchLabel?.let { label ->
            AndroidKitFloatingAction.Search(
                query = query,
                onQueryChange = { query = it },
                onSearch = {},
                label = label,
            )
        },
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

private fun AndroidKitSettingsSelection.displayOptions(systemLabel: String): List<AndroidKitSettingsOption> =
    listOf(
        AndroidKitSettingsOption(
            id = systemOption.id,
            label = "$systemLabel (${systemOption.currentValueLabel})",
        )
    ) + options
