package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.action.FloatingActionBar
import net.mamby.androidkit.compose.action.FloatingActionBarFlyout
import net.mamby.androidkit.compose.action.FloatingActionBarFlyoutItem
import net.mamby.androidkit.compose.action.FloatingActionBarIconItem
import net.mamby.androidkit.compose.action.FloatingActionBarIconLabelItem
import net.mamby.androidkit.compose.action.FloatingActionBarTextItem
import net.mamby.androidkit.compose.action.FloatingAddButton
import net.mamby.androidkit.compose.action.FloatingBackButton
import net.mamby.androidkit.compose.action.FloatingDropdownMenu
import net.mamby.androidkit.compose.form.AndroidKitModalSheet
import net.mamby.androidkit.compose.form.EditorFieldPair
import net.mamby.androidkit.compose.form.EditorSection
import net.mamby.androidkit.compose.form.FormDialog
import net.mamby.androidkit.compose.form.ReadOnlyPickerField
import net.mamby.androidkit.compose.form.SettingsItem
import net.mamby.androidkit.compose.form.StringListEditor
import net.mamby.androidkit.compose.form.SwitchField
import net.mamby.androidkit.compose.layout.AdaptiveGridPage
import net.mamby.androidkit.compose.layout.DetailPage
import net.mamby.androidkit.compose.layout.PageFloatingAction
import net.mamby.androidkit.compose.layout.PageScaffold
import net.mamby.androidkit.compose.navigation.AdaptiveNavigationScaffold
import net.mamby.androidkit.compose.navigation.AndroidKitNavigationItem
import net.mamby.androidkit.compose.presentation.LabeledValue
import net.mamby.androidkit.compose.presentation.MetricCard
import net.mamby.androidkit.compose.presentation.PresentationKind
import net.mamby.androidkit.compose.presentation.SectionCard
import net.mamby.androidkit.compose.presentation.StatePresentation
import net.mamby.androidkit.compose.theme.AndroidKitCardDefaults
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.ComponentId
import net.mamby.androidkit.navigation3.listDetailBackAction

@Composable
fun ComponentPlaceholder() {
    val dimensions = AndroidKitThemeTokens.dimensions
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensions.spaceExtraLarge),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
        ) {
            Text(
                text = stringResource(R.string.component_placeholder_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.component_placeholder_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ComponentDetailScreen(
    componentId: ComponentId,
    onBack: () -> Unit,
) {
    val backAction = listDetailBackAction(onBack)
    PageScaffold(
        title = componentId.apiName,
        subtitle = stringResource(componentId.category.labelResource),
        navigationIcon = backAction?.let { action ->
            {
                IconButton(onClick = action) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            }
        },
    ) { contentPadding ->
        DetailPage(contentPadding = contentPadding) {
            SectionCard(title = stringResource(R.string.component_detail_about_title)) {
                Text(
                    text = stringResource(componentId.descriptionResource),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            SectionCard(
                title = stringResource(R.string.component_detail_preview_title),
                supportingText = stringResource(R.string.component_detail_preview_hint),
            ) {
                ComponentPreview(componentId)
            }
        }
    }
}

@Composable
private fun ComponentPreview(componentId: ComponentId) {
    when (componentId) {
        ComponentId.PageScaffold -> PageScaffoldPreview()
        ComponentId.AdaptiveGridPage -> AdaptiveGridPagePreview()
        ComponentId.DetailPage -> DetailPagePreview()
        ComponentId.PageFloatingAction -> PageFloatingActionPreview()
        ComponentId.StatePresentation -> StatePresentationPreview()
        ComponentId.SectionCard -> SectionCardPreview()
        ComponentId.LabeledValue -> LabeledValuePreview()
        ComponentId.MetricCard -> MetricCardPreview()
        ComponentId.EditorSection -> EditorSectionPreview()
        ComponentId.EditorFieldPair -> EditorFieldPairPreview()
        ComponentId.SwitchField -> SwitchFieldPreview()
        ComponentId.ReadOnlyPickerField -> ReadOnlyPickerFieldPreview()
        ComponentId.StringListEditor -> StringListEditorPreview()
        ComponentId.FormDialog -> FormDialogPreview()
        ComponentId.SettingsItem -> SettingsItemPreview()
        ComponentId.AndroidKitModalSheet -> AndroidKitModalSheetPreview()
        ComponentId.FloatingBackButton -> FloatingBackButtonPreview()
        ComponentId.FloatingAddButton -> FloatingAddButtonPreview()
        ComponentId.FloatingActionBar -> FloatingActionBarPreview()
        ComponentId.FloatingActionBarIconItem -> FloatingActionBarIconItemPreview()
        ComponentId.FloatingActionBarIconLabelItem -> FloatingActionBarIconLabelItemPreview()
        ComponentId.FloatingActionBarTextItem -> FloatingActionBarTextItemPreview()
        ComponentId.FloatingActionBarFlyout -> FloatingActionBarFlyoutPreview()
        ComponentId.FloatingDropdownMenu -> FloatingDropdownMenuPreview()
        ComponentId.AdaptiveNavigationScaffold -> AdaptiveNavigationScaffoldPreview()
    }
}

@Composable
private fun PageScaffoldPreview() {
    PreviewViewport {
        PageScaffold(
            title = stringResource(R.string.components_title),
            modifier = Modifier.fillMaxSize(),
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.catalog_tagline),
                    modifier = Modifier.padding(AndroidKitThemeTokens.dimensions.spaceMedium),
                )
            }
        }
    }
}

@Composable
private fun AdaptiveGridPagePreview() {
    PreviewViewport {
        AdaptiveGridPage(contentPadding = PaddingValues()) {
            items(count = PreviewGridItemCount) { index ->
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AndroidKitThemeTokens.dimensions.spaceMedium),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = (index + 1).toString())
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailPagePreview() {
    PreviewViewport {
        DetailPage(contentPadding = PaddingValues()) {
            LabeledValue(
                label = stringResource(R.string.detail_behavior_title),
                value = stringResource(R.string.detail_behavior_body),
            )
            LabeledValue(
                label = stringResource(R.string.detail_tokens_title),
                value = stringResource(R.string.detail_tokens_body),
            )
        }
    }
}

@Composable
private fun PageFloatingActionPreview() {
    ActionPreview { onAction ->
        PageFloatingAction(onClick = onAction) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.action_add),
            )
        }
    }
}

@Composable
private fun StatePresentationPreview() {
    StatePresentation(
        kind = PresentationKind.Empty,
        title = stringResource(R.string.empty_title),
        message = stringResource(R.string.empty_message),
        actionLabel = stringResource(R.string.action_retry),
        onAction = {},
    )
}

@Composable
private fun SectionCardPreview() {
    SectionCard(
        title = stringResource(R.string.components_buttons_title),
        supportingText = stringResource(R.string.components_buttons_description),
    ) {
        Text(text = stringResource(R.string.catalog_tagline))
    }
}

@Composable
private fun LabeledValuePreview() {
    LabeledValue(
        label = stringResource(R.string.theme_count_label),
        value = stringResource(R.string.theme_count),
    )
}

@Composable
private fun MetricCardPreview() {
    MetricCard(
        value = ComponentId.entries.size.toString(),
        label = stringResource(R.string.component_count_label),
    )
}

@Composable
private fun EditorSectionPreview() {
    EditorSection(
        title = stringResource(R.string.profile_section),
        description = stringResource(R.string.profile_section_description),
    ) {
        Text(text = stringResource(R.string.catalog_tagline))
    }
}

@Composable
private fun EditorFieldPairPreview() {
    var firstValue by rememberSaveable { mutableStateOf("") }
    var secondValue by rememberSaveable { mutableStateOf("") }
    EditorFieldPair(
        first = {
            OutlinedTextField(
                value = firstValue,
                onValueChange = { firstValue = it },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.field_name)) },
                singleLine = true,
            )
        },
        second = {
            OutlinedTextField(
                value = secondValue,
                onValueChange = { secondValue = it },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.field_email)) },
                singleLine = true,
            )
        },
    )
}

@Composable
private fun SwitchFieldPreview() {
    var checked by rememberSaveable { mutableStateOf(true) }
    SwitchField(
        title = stringResource(R.string.reminders),
        supportingText = stringResource(R.string.reminders_description),
        checked = checked,
        onCheckedChange = { checked = it },
    )
}

@Composable
private fun ReadOnlyPickerFieldPreview() {
    var darkSelected by rememberSaveable { mutableStateOf(false) }
    ReadOnlyPickerField(
        label = stringResource(R.string.appearance_section),
        value = stringResource(if (darkSelected) R.string.theme_dark else R.string.theme_light),
        onClick = { darkSelected = !darkSelected },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
            )
        },
    )
}

@Composable
private fun StringListEditorPreview() {
    val design = stringResource(R.string.tag_design)
    val compose = stringResource(R.string.tag_compose)
    var values by remember(design, compose) { mutableStateOf(listOf(design, compose)) }
    StringListEditor(
        values = values,
        label = stringResource(R.string.tag_hint),
        onValuesChange = { values = it },
    )
}

@Composable
private fun FormDialogPreview() {
    var visible by rememberSaveable { mutableStateOf(false) }
    Button(onClick = { visible = true }) {
        Text(stringResource(R.string.show_dialog))
    }
    if (visible) {
        FormDialog(
            title = stringResource(R.string.dialog_title),
            onConfirm = { visible = false },
            onDismiss = { visible = false },
        ) {
            Text(stringResource(R.string.dialog_body))
        }
    }
}

@Composable
private fun SettingsItemPreview() {
    var actionCount by rememberSaveable { mutableIntStateOf(0) }
    SettingsItem(
        title = stringResource(R.string.about_section),
        supportingText = stringResource(R.string.about_body),
        onClick = { actionCount += 1 },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
            )
        },
        trailingContent = {
            Text(
                text = stringResource(R.string.settings_scroll_value),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
    Text(
        text = stringResource(R.string.action_count, actionCount),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AndroidKitModalSheetPreview() {
    var visible by rememberSaveable { mutableStateOf(false) }
    Button(onClick = { visible = true }) {
        Text(stringResource(R.string.open_sheet))
    }
    if (visible) {
        AndroidKitModalSheet(
            title = stringResource(R.string.sheet_title),
            onDismissRequest = { visible = false },
        ) {
            Text(stringResource(R.string.sheet_body))
        }
    }
}

@Composable
private fun FloatingBackButtonPreview() {
    ActionPreview { onAction ->
        FloatingBackButton(onClick = onAction)
    }
}

@Composable
private fun FloatingAddButtonPreview() {
    ActionPreview { onAction ->
        FloatingAddButton(onClick = onAction)
    }
}

@Composable
private fun FloatingActionBarPreview() {
    ActionPreview { onAction ->
        FloatingActionBar {
            FloatingActionBarIconLabelItem(
                onClick = onAction,
                icon = Icons.Default.Add,
                label = stringResource(R.string.action_add),
            )
            FloatingActionBarTextItem(
                onClick = onAction,
                label = stringResource(R.string.action_save),
            )
        }
    }
}

@Composable
private fun FloatingActionBarIconItemPreview() {
    ActionPreview { onAction ->
        FloatingActionBar {
            FloatingActionBarIconItem(
                onClick = onAction,
                icon = Icons.Default.Save,
                contentDescription = stringResource(R.string.action_save),
            )
        }
    }
}

@Composable
private fun FloatingActionBarIconLabelItemPreview() {
    ActionPreview { onAction ->
        FloatingActionBar {
            FloatingActionBarIconLabelItem(
                onClick = onAction,
                icon = Icons.Default.Share,
                label = stringResource(R.string.action_share),
            )
        }
    }
}

@Composable
private fun FloatingActionBarTextItemPreview() {
    ActionPreview { onAction ->
        FloatingActionBar {
            FloatingActionBarTextItem(
                onClick = onAction,
                label = stringResource(R.string.action_confirm),
            )
        }
    }
}

@Composable
private fun FloatingActionBarFlyoutPreview() {
    ActionPreview { onAction ->
        FloatingActionBar {
            FloatingActionBarFlyout(
                items = listOf(
                    FloatingActionBarFlyoutItem(
                        icon = Icons.Default.Share,
                        label = stringResource(R.string.action_share),
                        onClick = onAction,
                    ),
                    FloatingActionBarFlyoutItem(
                        icon = Icons.Default.Delete,
                        label = stringResource(R.string.action_delete),
                        onClick = onAction,
                    ),
                ),
                showLabel = true,
            )
        }
    }
}

@Composable
private fun FloatingDropdownMenuPreview() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) {
            Text(stringResource(R.string.action_more))
        }
        FloatingDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_share)) },
                onClick = { expanded = false },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_delete)) },
                onClick = { expanded = false },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun AdaptiveNavigationScaffoldPreview() {
    var selectedKey by rememberSaveable { mutableStateOf(PreviewNavigationKey.Home) }
    val items = listOf(
        AndroidKitNavigationItem(
            key = PreviewNavigationKey.Home,
            label = stringResource(R.string.nav_components),
            icon = Icons.Default.Home,
        ),
        AndroidKitNavigationItem(
            key = PreviewNavigationKey.Edit,
            label = stringResource(R.string.nav_forms),
            icon = Icons.Default.EditNote,
        ),
        AndroidKitNavigationItem(
            key = PreviewNavigationKey.Settings,
            label = stringResource(R.string.nav_settings),
            icon = Icons.Default.Settings,
        ),
    )
    val selectedLabel = items.first { it.key == selectedKey }.label
    PreviewViewport {
        AdaptiveNavigationScaffold(
            items = items,
            selectedKey = selectedKey,
            onSelected = { selectedKey = it },
            modifier = Modifier.fillMaxSize(),
            compactVisibleDestinationCount = 2,
            showCompactLabels = true,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AndroidKitThemeTokens.dimensions.spaceMedium),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        AndroidKitThemeTokens.dimensions.spaceSmall,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.DashboardCustomize,
                        contentDescription = null,
                    )
                    Text(stringResource(R.string.component_preview_selected_destination, selectedLabel))
                }
            }
        }
    }
}

@Composable
private fun ActionPreview(content: @Composable (onAction: () -> Unit) -> Unit) {
    var actionCount by rememberSaveable { mutableIntStateOf(0) }
    val dimensions = AndroidKitThemeTokens.dimensions
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
    ) {
        content { actionCount += 1 }
        Text(
            text = stringResource(R.string.action_count, actionCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PreviewViewport(content: @Composable BoxScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(PreviewViewportAspectRatio),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.background,
        border = AndroidKitCardDefaults.border(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

private enum class PreviewNavigationKey {
    Home,
    Edit,
    Settings,
}

private const val PreviewGridItemCount = 4
private const val PreviewViewportAspectRatio = 1.2f
