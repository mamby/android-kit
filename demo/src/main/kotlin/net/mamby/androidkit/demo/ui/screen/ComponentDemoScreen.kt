package net.mamby.androidkit.demo.ui.screen

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import net.mamby.androidkit.compose.action.AndroidKitFloatingAction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import net.mamby.androidkit.compose.action.AndroidKitAction
import net.mamby.androidkit.compose.action.AndroidKitActionSeparator
import net.mamby.androidkit.compose.action.AndroidKitFloatingActionBar
import net.mamby.androidkit.compose.action.AndroidKitFloatingActionBarIconAndLabelLayout
import net.mamby.androidkit.compose.action.AndroidKitFloatingActionButton
import net.mamby.androidkit.compose.action.AndroidKitActionFlyout
import net.mamby.androidkit.compose.form.AndroidKitBottomSheet
import net.mamby.androidkit.compose.form.AndroidKitBottomSheetScrollMode
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.presentation.AndroidKitCard
import net.mamby.androidkit.compose.presentation.AndroidKitCardMenuItem
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.DemoToggle
import net.mamby.androidkit.demo.ui.DemoPageAction
import net.mamby.androidkit.demo.ui.fabPositions
import net.mamby.androidkit.demo.ui.ComponentDemo
import net.mamby.androidkit.demo.ui.ComponentId
import net.mamby.androidkit.demo.ui.DemoFloatingNavigationLayout
import net.mamby.androidkit.demo.ui.materialSymbol
import net.mamby.androidkit.navigation3.listDetailBackAction

@Composable
fun ComponentPlaceholder() {
    AndroidKitPage { contentPadding ->
        DemoList(contentPadding) {
            item {
                Text(
                    text = stringResource(R.string.component_placeholder_body),
                    modifier = Modifier.padding(
                        vertical = AndroidKitThemeTokens.dimensions.spaceExtraLarge,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item { DemoScrollContent() }
        }
    }
}

@Composable
internal fun ComponentDemoScreen(
    demo: ComponentDemo,
    demoToggles: Set<DemoToggle>,
    onDemoToggleChange: (DemoToggle, Boolean) -> Unit,
    selectedPageAction: DemoPageAction?,
    onPageActionSelected: (DemoPageAction) -> Unit,
    floatingNavigationLayout: DemoFloatingNavigationLayout,
    onFloatingNavigationLayoutChange: (DemoFloatingNavigationLayout) -> Unit,
    showCompactNavigationLabels: Boolean,
    onShowCompactNavigationLabelsChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    when (demo.component) {
        ComponentId.AndroidKitPage -> AndroidKitPageDemo(
            toggles = demoToggles,
            onToggleChange = onDemoToggleChange,
            selectedAction = selectedPageAction,
            onActionSelected = onPageActionSelected,
            onBack = onBack,
        )
        ComponentId.AndroidKitFloatingActionButton -> AndroidKitFloatingActionButtonDemo(
            toggles = demoToggles,
            onToggleChange = onDemoToggleChange,
            onBack = onBack,
        )
        else -> StandardComponentDemo(
            demo = demo,
            floatingNavigationLayout = floatingNavigationLayout,
            onFloatingNavigationLayoutChange = onFloatingNavigationLayoutChange,
            showCompactNavigationLabels = showCompactNavigationLabels,
            onShowCompactNavigationLabelsChange = onShowCompactNavigationLabelsChange,
            onBack = onBack,
        )
    }
}

@Composable
private fun AndroidKitPageDemo(
    toggles: Set<DemoToggle>,
    onToggleChange: (DemoToggle, Boolean) -> Unit,
    selectedAction: DemoPageAction?,
    onActionSelected: (DemoPageAction) -> Unit,
    onBack: () -> Unit,
) {
    val actions = if (DemoToggle.PageActions in toggles) {
        buildList {
            DemoPageAction.entries.filter { it != DemoPageAction.Confirm }.forEach { action ->
                if (action == DemoPageAction.Share || action == DemoPageAction.Delete) {
                    add(AndroidKitActionSeparator)
                }
                add(
                    AndroidKitAction(
                        icon = materialSymbol(action.icon),
                        label = stringResource(action.label),
                        onClick = { onActionSelected(action) },
                    ),
                )
            }
        }
    } else emptyList()
    AndroidKitPage(
        title = if (DemoToggle.PageTitle in toggles) stringResource(R.string.demo_page_title) else null,
        onBack = listDetailBackAction(onBack),
        actions = actions,
        titleBarImmersiveMode = DemoToggle.PageImmersive in toggles,
        floatingActionButton = if (DemoToggle.PageFab in toggles) {
                AndroidKitFloatingAction.Button(onClick = { onActionSelected(DemoPageAction.Confirm) }, icon = materialSymbol(R.drawable.ic_symbol_check),
                        label = stringResource(R.string.action_confirm))
            } else null,
    ) { contentPadding ->
        DemoList(contentPadding) {
            item {
                Text(stringResource(
                    R.string.demo_selected_action,
                    selectedAction?.let { stringResource(it.label) }
                        ?: stringResource(R.string.demo_no_action),
                ))
            }
            listOf(
                DemoToggle.PageTitle,
                DemoToggle.PageActions,
                DemoToggle.PageImmersive,
                DemoToggle.PageFab,
            ).forEach { toggle ->
                item(key = toggle.name) { DemoToggleRow(toggle, toggles, onToggleChange) }
            }
            item { DemoScrollContent() }
        }
    }
}

@Composable
private fun AndroidKitFloatingActionButtonDemo(
    toggles: Set<DemoToggle>,
    onToggleChange: (DemoToggle, Boolean) -> Unit,
    onBack: () -> Unit,
) {
    var actionCount by rememberSaveable { mutableIntStateOf(0) }
    val dimensions = AndroidKitThemeTokens.dimensions
    AndroidKitPage(
        title = ComponentId.AndroidKitFloatingActionButton.apiName,
        onBack = listDetailBackAction(onBack),
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensions.screenPadding),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
            ) {
                item {
                    Text(text = stringResource(R.string.action_count, actionCount))
                }
                (listOf(DemoToggle.FabEnabled) + fabPositions.keys).forEach { toggle ->
                    item(key = toggle.name) { DemoToggleRow(toggle, toggles, onToggleChange) }
                }
                item { DemoScrollContent() }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            ) {
                fabPositions.filterKeys { it in toggles }.forEach { (_, alignment) ->
                    AndroidKitFloatingActionButton(AndroidKitFloatingAction.Button(onClick = { actionCount += 1 },
                        modifier = Modifier
                            .align(alignment)
                            .padding(dimensions.screenPadding),
                        enabled = DemoToggle.FabEnabled in toggles, icon = materialSymbol(R.drawable.ic_symbol_edit),
                            label = stringResource(R.string.action_edit)))
                }
            }
        }
    }
}

@Composable
private fun StandardComponentDemo(
    demo: ComponentDemo,
    floatingNavigationLayout: DemoFloatingNavigationLayout,
    onFloatingNavigationLayoutChange: (DemoFloatingNavigationLayout) -> Unit,
    showCompactNavigationLabels: Boolean,
    onShowCompactNavigationLabelsChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    var actionCount by rememberSaveable { mutableIntStateOf(0) }
    AndroidKitPage(
        title = demo.component.apiName,
        onBack = listDetailBackAction(onBack),
    ) { contentPadding ->
        DemoList(contentPadding) {
            ComponentDemo.entries.filter { it.component == demo.component }.forEach { variation ->
                item(key = variation.name) {
                    Column(verticalArrangement = Arrangement.spacedBy(
                        AndroidKitThemeTokens.dimensions.spaceSmall,
                    )) {
                        Text(stringResource(variation.titleResource), style = MaterialTheme.typography.titleMedium)
                        when (demo.component) {
                            ComponentId.AndroidKitCard -> AndroidKitCardDemo(
                                demo = variation,
                                actionCount = actionCount,
                                onAction = { actionCount += 1 },
                            )
                            ComponentId.AndroidKitBottomSheet -> AndroidKitBottomSheetDemo(variation)
                            ComponentId.AndroidKitFloatingActionBar -> {
                                AndroidKitFloatingActionBarDemo(variation, onAction = { actionCount += 1 })
                                Text(stringResource(R.string.action_count, actionCount))
                            }
                            ComponentId.AndroidKitActionFlyout -> AndroidKitActionFlyoutDemo(variation)
                            ComponentId.AndroidKitFloatingNavigation -> AndroidKitFloatingNavigationDemo(
                                demo = variation,
                                layout = floatingNavigationLayout,
                                onLayoutChange = onFloatingNavigationLayoutChange,
                                showLabels = showCompactNavigationLabels,
                                onShowLabelsChange = onShowCompactNavigationLabelsChange,
                            )
                            else -> error("Handled by a dedicated demo screen")
                        }
                    }
                }
            }
            item { DemoScrollContent() }
        }
    }
}

@Composable
private fun AndroidKitCardDemo(
    demo: ComponentDemo,
    actionCount: Int,
    onAction: () -> Unit,
) {
    val hasOverflow = demo == ComponentDemo.AndroidKitCardOverflow
    AndroidKitCard(
        modifier = Modifier.fillMaxWidth(),
        menuItems = if (hasOverflow) {
            listOf(
                AndroidKitCardMenuItem(
                    label = stringResource(R.string.action_edit),
                    onClick = onAction,
                    icon = materialSymbol(R.drawable.ic_symbol_edit),
                ),
                AndroidKitCardMenuItem(
                    label = stringResource(R.string.action_share),
                    onClick = onAction,
                    icon = materialSymbol(R.drawable.ic_symbol_share),
                ),
                AndroidKitCardMenuItem(
                    label = stringResource(R.string.action_delete),
                    onClick = onAction,
                    icon = materialSymbol(R.drawable.ic_symbol_delete),
                ),
            )
        } else {
            emptyList()
        },
        title = stringResource(R.string.demo_section_title),
        supportingText = if (demo != ComponentDemo.AndroidKitCardBasic) stringResource(R.string.demo_supporting_text) else null,
    ) {
        Text(stringResource(R.string.demo_section_body))
        if (demo == ComponentDemo.AndroidKitCardRichContent) {
            Button(onClick = onAction) { Text(stringResource(R.string.primary_action)) }
        }
        if (hasOverflow) {
            Text(stringResource(R.string.action_count, actionCount))
        }
    }
}

@Composable
private fun AndroidKitBottomSheetDemo(demo: ComponentDemo) {
    var visible by rememberSaveable { mutableStateOf(false) }
    var showingDetail by rememberSaveable { mutableStateOf(true) }
    var headerActionCount by rememberSaveable { mutableIntStateOf(0) }
    Button(
        modifier = Modifier.testTag(if (demo == ComponentDemo.AndroidKitBottomSheetStandard) {
            "open_bottom_sheet"
        } else {
            "open_bottom_sheet_${demo.name}"
        }),
        onClick = {
            showingDetail = true
            visible = true
        },
    ) {
        Text(stringResource(demo.titleResource))
    }

    val isBackNavigation = demo == ComponentDemo.AndroidKitBottomSheetBackNavigation
    val hasHeaderActions = demo == ComponentDemo.AndroidKitBottomSheetHeaderActions
    val isChromeless = demo == ComponentDemo.AndroidKitBottomSheetChromelessFitContent
    val isContentManaged = demo == ComponentDemo.AndroidKitBottomSheetContentManaged
    val cancel = stringResource(R.string.action_cancel)
    val confirm = stringResource(R.string.action_confirm)
    val headerActions = if (hasHeaderActions) {
        listOf(
            AndroidKitAction(
                icon = materialSymbol(R.drawable.ic_symbol_save),
                label = stringResource(R.string.action_save),
                onClick = { headerActionCount += 1 },
            ),
            AndroidKitAction(
                icon = materialSymbol(R.drawable.ic_symbol_share),
                label = stringResource(R.string.action_share),
                onClick = { headerActionCount += 1 },
            ),
            AndroidKitActionSeparator,
            AndroidKitAction(
                icon = materialSymbol(R.drawable.ic_symbol_delete),
                label = stringResource(R.string.action_delete),
                onClick = { headerActionCount += 1 },
            ),
        )
    } else {
        emptyList()
    }
    AndroidKitBottomSheet(
        visible = visible,
        title = if (isBackNavigation && showingDetail) {
            stringResource(R.string.sheet_detail_title)
        } else {
            stringResource(R.string.sheet_title)
        },
        onDismiss = { visible = false },
        modifier = Modifier
            .testTag("bottom_sheet")
            .semantics { testTagsAsResourceId = true },
        onBack = ({ showingDetail = false }).takeIf {
            isBackNavigation && showingDetail
        },
        actions = headerActions,
        fitContent = isChromeless,
        showChrome = !isChromeless,
        scrollMode = if (isContentManaged) {
            AndroidKitBottomSheetScrollMode.ContentManaged
        } else {
            AndroidKitBottomSheetScrollMode.VerticalScroll
        },
        floatingAction = AndroidKitFloatingAction.Bar {
                text(onClick = { visible = false }, label = cancel)
                text(onClick = { visible = false }, label = confirm)
            },
    ) { managedContentPadding ->
        if (isContentManaged) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = managedContentPadding,
                verticalArrangement = Arrangement.spacedBy(
                    AndroidKitThemeTokens.dimensions.spaceSmall,
                ),
            ) {
                items(24) { index ->
                    Text(stringResource(R.string.sheet_list_item, index + 1))
                }
            }
        } else {
            Text(stringResource(R.string.sheet_body))
            if (hasHeaderActions) {
                Text(stringResource(R.string.action_count, headerActionCount))
            }
            if (!isChromeless) DemoScrollContent()
        }
    }
}

@Composable
private fun AndroidKitFloatingActionBarDemo(
    demo: ComponentDemo,
    onAction: () -> Unit,
) {
    val edit = stringResource(R.string.action_edit)
    val add = stringResource(R.string.action_add)
    val close = stringResource(R.string.action_close)
    val retry = stringResource(R.string.action_retry)
    val cancel = stringResource(R.string.action_cancel)
    val confirm = stringResource(R.string.action_confirm)
    val save = stringResource(R.string.action_save)
    val share = stringResource(R.string.action_share)
    val delete = stringResource(R.string.action_delete)
    val editIcon = materialSymbol(R.drawable.ic_symbol_edit)
    val saveIcon = materialSymbol(R.drawable.ic_symbol_save)
    val shareIcon = materialSymbol(R.drawable.ic_symbol_share)
    val flyoutItemGroups = listOf(
        listOf(
            materialSymbol(R.drawable.ic_symbol_add) to add,
            materialSymbol(R.drawable.ic_symbol_close) to close,
        ),
        listOf(
            materialSymbol(R.drawable.ic_symbol_refresh) to retry,
            null to cancel,
            null to confirm,
        ),
        listOf(materialSymbol(R.drawable.ic_symbol_delete) to delete),
    )
    AndroidKitFloatingActionBar {
        when (demo) {
            ComponentDemo.AndroidKitFloatingActionBarIcons -> {
                icon(
                    onClick = onAction,
                    icon = editIcon,
                    contentDescription = edit,
                )
                icon(
                    onClick = onAction,
                    icon = saveIcon,
                    contentDescription = save,
                    enabled = false,
                )
                icon(
                    onClick = onAction,
                    icon = shareIcon,
                    contentDescription = share,
                )
                separator()
                flyout {
                    flyoutItemGroups.forEachIndexed { index, group ->
                        if (index > 0) separator()
                        group.forEach { (icon, label) ->
                            item(icon = icon, label = label, onClick = onAction)
                        }
                    }
                }
            }

            ComponentDemo.AndroidKitFloatingActionBarIconsAndLabels,
            ComponentDemo.AndroidKitFloatingActionBarHorizontalIconsAndLabels,
            -> {
                val layout = if (
                    demo == ComponentDemo.AndroidKitFloatingActionBarHorizontalIconsAndLabels
                ) {
                    AndroidKitFloatingActionBarIconAndLabelLayout.Horizontal
                } else {
                    AndroidKitFloatingActionBarIconAndLabelLayout.Vertical
                }
                iconAndLabel(
                    onClick = onAction,
                    icon = editIcon,
                    label = edit,
                    layout = layout,
                )
                iconAndLabel(
                    onClick = onAction,
                    icon = saveIcon,
                    label = save,
                    layout = layout,
                    enabled = false,
                )
                iconAndLabel(
                    onClick = onAction,
                    icon = shareIcon,
                    label = share,
                    layout = layout,
                )
                separator()
                flyout {
                    flyoutItemGroups.forEachIndexed { index, group ->
                        if (index > 0) separator()
                        group.forEach { (icon, label) ->
                            item(icon = icon, label = label, onClick = onAction)
                        }
                    }
                }
            }

            ComponentDemo.AndroidKitFloatingActionBarText -> {
                text(onClick = onAction, label = edit)
                text(onClick = onAction, label = save, enabled = false)
                text(onClick = onAction, label = share)
                separator()
                flyout {
                    flyoutItemGroups.forEachIndexed { index, group ->
                        if (index > 0) separator()
                        group.forEach { (icon, label) ->
                            item(icon = icon, label = label, onClick = onAction)
                        }
                    }
                }
            }

            else -> error("Unexpected AndroidKitFloatingActionBar demo: $demo")
        }
    }
}

@Composable
private fun AndroidKitActionFlyoutDemo(demo: ComponentDemo) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val actions = listOf(
        materialSymbol(R.drawable.ic_symbol_edit) to stringResource(R.string.action_edit),
        materialSymbol(R.drawable.ic_symbol_share) to stringResource(R.string.action_share),
        materialSymbol(R.drawable.ic_symbol_delete) to stringResource(R.string.action_delete),
    )
    val shareLabel = stringResource(R.string.action_share)
    val copyLinkLabel = stringResource(R.string.flyout_demo_copy_link)
    val exportLabel = stringResource(R.string.flyout_demo_export)
    val documentLabel = stringResource(R.string.flyout_demo_document)
    val pdfLabel = stringResource(R.string.flyout_demo_pdf)
    val plainTextLabel = stringResource(R.string.flyout_demo_plain_text)
    val imageLabel = stringResource(R.string.flyout_demo_image)
    val cancelLabel = stringResource(R.string.action_cancel)
    Box {
        Button(onClick = { expanded = true }) {
            Icon(
                imageVector = materialSymbol(R.drawable.ic_symbol_more_vert),
                contentDescription = null,
            )
            Text(stringResource(demo.titleResource))
        }
        AndroidKitActionFlyout(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            actions.forEach { (icon, label) ->
                item(
                    label = label,
                    onClick = {},
                    icon = if (
                        demo == ComponentDemo.AndroidKitActionFlyoutIcons
                    ) {
                        icon
                    } else {
                        null
                    },
                )
            }
            if (demo == ComponentDemo.AndroidKitActionFlyoutSubmenus) {
                submenu(label = shareLabel) {
                    item(label = copyLinkLabel, onClick = {})
                    submenu(label = exportLabel) {
                        submenu(label = documentLabel) {
                            item(label = pdfLabel, onClick = {})
                            item(label = plainTextLabel, onClick = {})
                        }
                        item(
                            label = imageLabel,
                            enabled = false,
                            onClick = {},
                        )
                    }
                }
            }
            separator()
            item(
                label = cancelLabel,
                onClick = {},
            )
        }
    }
}

@Composable
private fun AndroidKitFloatingNavigationDemo(
    demo: ComponentDemo,
    layout: DemoFloatingNavigationLayout,
    onLayoutChange: (DemoFloatingNavigationLayout) -> Unit,
    showLabels: Boolean,
    onShowLabelsChange: (Boolean) -> Unit,
) {
    val labelsTitle = stringResource(R.string.floating_navigation_labels_title)
    val labelsDescription = stringResource(R.string.floating_navigation_labels_description)
    val fiveItemsTitle = stringResource(R.string.floating_navigation_five_items_title)
    val fiveItemsDescription = stringResource(
        R.string.floating_navigation_five_items_description,
    )
    val sevenItemsTitle = stringResource(R.string.floating_navigation_seven_items_title)
    val sevenItemsDescription = stringResource(
        R.string.floating_navigation_seven_items_description,
    )
    Column {
        Text(stringResource(demo.titleResource))
        Text(stringResource(R.string.floating_navigation_scenario_instruction))
        NavigationDemoToggle(
            label = labelsTitle,
            supportingText = labelsDescription,
            checked = showLabels,
            onCheckedChange = onShowLabelsChange,
        )
        NavigationDemoToggle(
            label = fiveItemsTitle,
            supportingText = fiveItemsDescription,
            checked = layout == DemoFloatingNavigationLayout.FiveItemsWithMore,
            onCheckedChange = { useFiveItems ->
                onLayoutChange(
                    if (useFiveItems) {
                        DemoFloatingNavigationLayout.FiveItemsWithMore
                    } else {
                        DemoFloatingNavigationLayout.ThreeItemsWithoutMore
                    },
                )
            },
        )
        NavigationDemoToggle(
            label = sevenItemsTitle,
            supportingText = sevenItemsDescription,
            checked = layout == DemoFloatingNavigationLayout.SevenItemsWithMore,
            onCheckedChange = { useSevenItems ->
                onLayoutChange(
                    if (useSevenItems) {
                        DemoFloatingNavigationLayout.SevenItemsWithMore
                    } else {
                        DemoFloatingNavigationLayout.ThreeItemsWithoutMore
                    },
                )
            },
        )
    }
}

@Composable
private fun DemoToggleRow(
    toggle: DemoToggle,
    toggles: Set<DemoToggle>,
    onToggleChange: (DemoToggle, Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(stringResource(toggle.label)) },
        trailingContent = {
            Switch(checked = toggle in toggles, onCheckedChange = { onToggleChange(toggle, it) })
        },
    )
}

@Composable
private fun DemoList(
    contentPadding: PaddingValues,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AndroidKitThemeTokens.dimensions.screenPadding),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(AndroidKitThemeTokens.dimensions.spaceMedium),
        content = content,
    )
}

@Composable
private fun NavigationDemoToggle(
    label: String,
    supportingText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = { Text(supportingText) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
    )
}
