package net.mamby.androidkit.compose.form

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import net.mamby.androidkit.compose.action.AndroidKitAction
import net.mamby.androidkit.compose.action.AndroidKitFloatingAction
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.theme.AndroidKitFloatingSurfaceDefaults
import net.mamby.androidkit.compose.theme.AndroidKitStrings
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

/** Stable identity is independent of the translated label and filtered list position. */
public data class AndroidKitSettingsOption(
    public val id: String,
    public val label: String,
    public val searchTerms: AndroidKitSettingsSearchTerms? = null,
) {
    init {
        require(id.isNotBlank()) { "Settings option IDs must not be blank." }
        require(label.isNotBlank()) { "Settings option labels must not be blank." }
    }
}

/** A system choice and the host-localized value it currently resolves to. */
public data class AndroidKitSettingsSystemOption(
    public val id: String,
    public val currentValueLabel: String,
    public val searchTerms: AndroidKitSettingsSearchTerms? = null,
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
        require(allIds.distinct().size == allIds.size) { "Option IDs must be unique." }
        require(selectedId in allIds) { "The selected ID must identify an option." }
    }
}

public data class AndroidKitLanguageSetting(public val selection: AndroidKitSettingsSelection)

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
    public val errorMessage: String? = null,
    public val enabled: Boolean = true,
    public val timeout: AndroidKitAppLockTimeoutSetting? = null,
    public val onLockNow: (() -> Unit)? = null,
)

@DslMarker
public annotation class AndroidKitSettingsPageDsl

internal data class SettingsPageSectionDefinition(
    val key: String,
    val label: String?,
    val description: String?,
    val searchTerms: AndroidKitSettingsSearchTerms?,
    val content: AndroidKitSettingsSectionScope.() -> Unit,
)

/** Typed settings declarations; resolve composable resources before entering this builder. */
@AndroidKitSettingsPageDsl
public class AndroidKitSettingsPageScope internal constructor() {
    private val sections = mutableListOf<SettingsPageSectionDefinition>()
    private val keys = mutableSetOf<String>()

    public fun section(
        key: String,
        label: String? = null,
        description: String? = null,
        searchTerms: AndroidKitSettingsSearchTerms? = null,
        content: AndroidKitSettingsSectionScope.() -> Unit,
    ) {
        require(key.isNotBlank()) { "Settings section keys must not be blank." }
        require(keys.add(key)) { "Settings page keys must be unique: $key" }
        sections += SettingsPageSectionDefinition(key, label, description, searchTerms, content)
    }

    internal fun render(scope: SettingsPageRenderScope) {
        sections.forEach { section ->
            scope.section(
                section.key,
                section.label,
                section.description,
                section.searchTerms,
                section.content,
            )
        }
    }
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

internal data class SettingsRenderedSection(
    val key: String,
    val label: String?,
    val description: String?,
    val entries: List<SettingsEntryDefinition>,
    val searchTerms: AndroidKitSettingsSearchTerms? = null,
)

@AndroidKitSettingsPageDsl
internal interface SettingsPageRenderScope {
    fun section(
        key: String,
        label: String? = null,
        description: String? = null,
        searchTerms: AndroidKitSettingsSearchTerms? = null,
        content: AndroidKitSettingsSectionScope.() -> Unit,
    )
}

/** Renders a page from the shared Settings catalog. */
@Composable
public fun AndroidKitSettingsPage(
    catalog: AndroidKitSettingsCatalog,
    pageKey: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
): Unit {
    val page = requireNotNull(catalog.pagesByKey[pageKey]) { "Unknown Settings page key: $pageKey" }
    var activePicker by rememberSaveable(pageKey) { mutableStateOf<String?>(null) }
    val strings = AndroidKitThemeTokens.strings
    val scope = SettingsPageScopeImpl(pageKey, strings) { activePicker = it }
    val sections = when (page) {
        is AndroidKitSettingsCatalogPage.Main -> {
            AndroidKitSettingsPageScope().apply(page.content).render(scope)
            scope.items.toList()
        }
        is AndroidKitSettingsCatalogPage.Subpage -> {
            AndroidKitSettingsPageScope().apply(page.content).render(scope)
            scope.items.toList()
        }
        is AndroidKitSettingsCatalogPage.About -> settingsAboutSections(page.content)
    }
    val title = page.title ?: strings.about
    val searchAction = AndroidKitAction(AndroidKitIcons.Search, strings.searchSettings,
        catalog.search.onOpenSearch)
    val dimensions = AndroidKitThemeTokens.dimensions
    val direction = LocalLayoutDirection.current
    AndroidKitPage(
        title = title,
        modifier = modifier,
        onBack = onBack,
        actions = listOf(searchAction) + page.actions,
    ) { padding ->
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
            items(sections, key = { "section:${it.key}" }) { section ->
                SettingsSection(section.entries, label = section.label, description = section.description)
            }
            if (page is AndroidKitSettingsCatalogPage.Main) {
                catalog.about?.let { about ->
                    item(key = "kit:about") {
                        val entry = SettingSectionScopeImpl().apply {
                            navigation(
                                key = "about",
                                label = strings.about,
                                onClick = about.onOpen,
                                icon = AndroidKitIcons.Info,
                                searchable = false,
                            )
                        }
                        SettingsSection(entry.entries)
                    }
                }
            }
        }
    }
    val availablePickerKeys = scope.pickers.keys + scope.timeoutPickers.keys
    LaunchedEffect(activePicker, availablePickerKeys) {
        if (activePicker != null && activePicker !in availablePickerKeys) activePicker = null
    }
    RenderSettingsPicker(scope, activePicker) { activePicker = null }
}

internal data class SettingsPickerDefinition(
    val selection: AndroidKitSettingsSelection,
    val searchLabel: String? = null,
    val emptyResultsLabel: String? = null,
)

internal class SettingsPageScopeImpl(
    private val pageKey: String,
    private val strings: AndroidKitStrings,
    private val openPicker: (String) -> Unit,
) : SettingsPageRenderScope {
    val items = mutableListOf<SettingsRenderedSection>()
    val pickers = mutableMapOf<String, SettingsPickerDefinition>()
    val timeoutPickers = mutableMapOf<String, AndroidKitAppLockTimeoutSetting>()
    private val keys = mutableSetOf<String>()

    private fun fullKey(key: String): String = "$pageKey:$key"

    private fun registerItem(item: SettingsRenderedSection) {
        require(keys.add(item.key)) { "Settings page keys must be unique: ${item.key}" }
        items += item
    }

    override fun section(
        key: String,
        label: String?,
        description: String?,
        searchTerms: AndroidKitSettingsSearchTerms?,
        content: AndroidKitSettingsSectionScope.() -> Unit,
    ) {
        val sectionKey = fullKey(key)
        val entries = SettingSectionScopeImpl()
        val declaredKinds = mutableSetOf<String>()
        val declarations = AndroidKitSettingsSectionScope(
            entries,
            onLanguage = { setting ->
                require(declaredKinds.add("language")) { "Duplicate language entry in $key" }
                entries.addPicker(sectionKey, "language", SettingsPickerDefinition(setting.selection,
                    strings.searchLanguages, strings.noMatchingLanguages), AndroidKitIcons.Language, strings)
            },
            onTheme = { setting ->
                require(declaredKinds.add("theme")) { "Duplicate theme entry in $key" }
                entries.addPicker(sectionKey, "theme", SettingsPickerDefinition(setting), AndroidKitIcons.Theme, strings)
            },
            onTransparency = { setting ->
                require(declaredKinds.add("transparency")) { "Duplicate transparency entry in $key" }
                entries.entries += SettingsEntryDefinition.Slider(
                    key = "transparency", label = strings.transparency, value = setting.value,
                    onValueChange = setting.onValueChange, modifier = Modifier,
                    valueRange = AndroidKitFloatingSurfaceDefaults.MinimumOpacityLevel..
                        AndroidKitFloatingSurfaceDefaults.MaximumOpacityLevel,
                    steps = 19, onValueChangeFinished = setting.onValueChangeFinished,
                    supportingText = null, icon = null, valueLabel = null,
                    enabled = setting.enabled, colors = null,
                    minimumLabel = strings.min, maximumLabel = strings.max, isOpacitySlider = true,
                    searchTerms = builtInSearchTerms("transparency"),
                )
            },
            onAppLock = { setting ->
                require(declaredKinds.add("appLock")) { "Duplicate app-lock entry in $key" }
                val timeoutKey = "app-lock-timeout:$sectionKey"
                val timeout = setting.timeout?.takeIf { setting.checked }
                if (timeout != null && setting.enabled && timeout.enabled) timeoutPickers[timeoutKey] = timeout
                entries.toggle(
                    key = "app-lock", label = strings.appLock, checked = setting.checked,
                    onCheckedChange = setting.onCheckedChange, supportingText = setting.errorMessage,
                    icon = AndroidKitIcons.AppLock, enabled = setting.enabled,
                    searchTerms = builtInSearchTerms("app-lock"),
                )
                timeout?.let { selection ->
                    entries.button(
                        key = "app-lock-timeout", label = strings.lockAfterLeavingApp,
                        supportingText = selection.options.first { it.id == selection.selectedId }.label,
                        enabled = setting.enabled && selection.enabled, onClick = { openPicker(timeoutKey) },
                        searchTerms = builtInSearchTerms("app-lock-timeout"),
                    )
                }
                if (setting.checked && setting.onLockNow != null) {
                    entries.button(
                        key = "lock-now", label = strings.lockNow, onClick = setting.onLockNow,
                        enabled = setting.enabled, searchTerms = builtInSearchTerms("lock-now"),
                    )
                }
            },
        )
        declarations.content()
        if (entries.entries.isNotEmpty()) {
            registerItem(SettingsRenderedSection(sectionKey, label, description, entries.entries.toList(), searchTerms))
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
            key = kind,
            label = selectionLabel,
            supportingText = options.first { it.id == selection.selectedId }.label,
            icon = defaultIcon,
            enabled = selection.enabled,
            onClick = { openPicker(pickerKey) },
            searchTerms = mergeSearchTerms(
                builtInSearchTerms(kind),
                selection.options.mapNotNull { it.searchTerms } + listOfNotNull(selection.systemOption.searchTerms),
            ),
        )
    }
}

@Composable
internal fun RenderSettingsPicker(
    scope: SettingsPageScopeImpl,
    activePicker: String?,
    onDismiss: () -> Unit,
) {
    val picker = scope.pickers[activePicker]?.takeIf { it.selection.enabled }
    val timeoutPicker = scope.timeoutPickers[activePicker]
    if (picker != null) {
        key(activePicker) { SettingsPicker(picker, onDismiss) }
    } else if (timeoutPicker != null) {
        key(activePicker) { AppLockTimeoutDialog(timeoutPicker, onDismiss) }
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
        val search = normalizeSettingsSearchText(query)
        displayOptions.filter { search.isEmpty() || normalizeSettingsSearchText(it.label).contains(search) }
    }
    val listState = rememberLazyListState()
    val dimensions = AndroidKitThemeTokens.dimensions
    AndroidKitBottomSheet(
        visible = true,
        title = if (picker.searchLabel != null) strings.language else strings.theme,
        onDismiss = onDismiss,
        fitContent = picker.searchLabel == null,
        floatingAction = picker.searchLabel?.let { label ->
            AndroidKitFloatingAction.Search(query, { query = it }, {}, label)
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
            if (options.isEmpty()) item(key = "empty") { Text(picker.emptyResultsLabel.orEmpty()) }
            items(options, key = { "option:${it.id}" }) { option ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .selectable(
                            selected = option.id == selection.selectedId,
                            role = Role.RadioButton,
                            onClick = { selection.onSelected(option.id); onDismiss() },
                        )
                        .heightIn(min = dimensions.minimumTouchTarget)
                        .padding(horizontal = dimensions.spaceSmall,
                            vertical = dimensions.settingSectionEntryVerticalPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
                ) {
                    Text(option.label, modifier = Modifier.weight(1f),
                        style = AndroidKitThemeTokens.settingSectionStyle.entryLabelTextStyle)
                    if (option.id == selection.selectedId) {
                        Icon(AndroidKitIcons.Check, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

private fun AndroidKitSettingsSelection.displayOptions(systemLabel: String): List<AndroidKitSettingsOption> =
    listOf(AndroidKitSettingsOption(systemOption.id, "$systemLabel (${systemOption.currentValueLabel})",
        systemOption.searchTerms)) + options
