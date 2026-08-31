package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import net.mamby.androidkit.compose.action.AndroidKitFloatingActionBar
import net.mamby.androidkit.compose.action.AndroidKitFloatingActionButton
import net.mamby.androidkit.compose.action.AndroidKitFloatingDropdownMenu
import net.mamby.androidkit.compose.form.AndroidKitBottomSheet
import net.mamby.androidkit.compose.form.AndroidKitBottomSheetScrollMode
import net.mamby.androidkit.compose.form.AndroidKitSettingSection
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.layout.AndroidKitPageAction
import net.mamby.androidkit.compose.presentation.AndroidKitCard
import net.mamby.androidkit.compose.presentation.AndroidKitCardMenuItem
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
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
    floatingNavigationLayout: DemoFloatingNavigationLayout,
    onFloatingNavigationLayoutChange: (DemoFloatingNavigationLayout) -> Unit,
    showCompactNavigationLabels: Boolean,
    onShowCompactNavigationLabelsChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    when (demo.component) {
        ComponentId.AndroidKitPage -> AndroidKitPageDemo(demo = demo, onBack = onBack)
        ComponentId.AndroidKitFloatingActionButton -> AndroidKitFloatingActionButtonDemo(
            demo = demo,
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
    demo: ComponentDemo,
    onBack: () -> Unit,
) {
    var actionCount by rememberSaveable { mutableIntStateOf(0) }
    val hasTitleActions = demo == ComponentDemo.AndroidKitPageTitleActions ||
        demo == ComponentDemo.AndroidKitPageImmersiveMode
    val title = when (demo) {
        ComponentDemo.AndroidKitPageBasic -> null
        ComponentDemo.AndroidKitPageTitle,
        ComponentDemo.AndroidKitPageTitleActions,
        ComponentDemo.AndroidKitPageImmersiveMode,
        ComponentDemo.AndroidKitPageFloatingActionButton,
        -> stringResource(R.string.demo_page_title)
        else -> error("Unexpected AndroidKitPage demo: $demo")
    }
    val actions = if (hasTitleActions) {
        listOf(
            AndroidKitPageAction(
                icon = materialSymbol(R.drawable.ic_symbol_save),
                label = stringResource(R.string.action_save),
                onClick = { actionCount += 1 },
            ),
            AndroidKitPageAction(
                icon = materialSymbol(R.drawable.ic_symbol_share),
                label = stringResource(R.string.action_share),
                onClick = { actionCount += 1 },
            ),
        )
    } else {
        emptyList()
    }
    AndroidKitPage(
        title = title,
        onBack = listDetailBackAction(onBack),
        actions = actions,
        titleBarImmersiveMode = demo == ComponentDemo.AndroidKitPageImmersiveMode,
        floatingActionButton = {
            if (demo == ComponentDemo.AndroidKitPageFloatingActionButton) {
                AndroidKitFloatingActionButton(onClick = { actionCount += 1 }) {
                    Icon(
                        imageVector = materialSymbol(R.drawable.ic_symbol_check),
                        contentDescription = stringResource(R.string.action_confirm),
                    )
                }
            }
        },
    ) { contentPadding ->
        DemoList(contentPadding) {
            item {
                AndroidKitCard(
                    modifier = Modifier.fillMaxWidth(),
                    header = {
                        DemoCardHeader(
                            title = demo.component.apiName,
                            supportingText = stringResource(demo.titleResource),
                        )
                    },
                ) {
                    if (hasTitleActions) {
                        Text(stringResource(R.string.page_title_bar_demo_instruction))
                    }
                    Text(stringResource(R.string.action_count, actionCount))
                }
            }
            item { DemoScrollContent() }
        }
    }
}

@Composable
private fun AndroidKitFloatingActionButtonDemo(
    demo: ComponentDemo,
    onBack: () -> Unit,
) {
    var actionCount by rememberSaveable { mutableIntStateOf(0) }
    val alignment = when (demo) {
        ComponentDemo.AndroidKitFloatingActionButtonTopStart -> Alignment.TopStart
        ComponentDemo.AndroidKitFloatingActionButtonTopCenter -> Alignment.TopCenter
        ComponentDemo.AndroidKitFloatingActionButtonTopEnd -> Alignment.TopEnd
        ComponentDemo.AndroidKitFloatingActionButtonBottomStart -> Alignment.BottomStart
        ComponentDemo.AndroidKitFloatingActionButtonBottomCenter -> Alignment.BottomCenter
        ComponentDemo.AndroidKitFloatingActionButtonBottomEnd -> Alignment.BottomEnd
        else -> error("Unexpected AndroidKitFloatingActionButton demo: $demo")
    }
    val dimensions = AndroidKitThemeTokens.dimensions
    AndroidKitPage(
        title = componentDemoTitle(demo),
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
                item { DemoScrollContent() }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            ) {
                AndroidKitFloatingActionButton(
                    onClick = { actionCount += 1 },
                    modifier = Modifier
                        .align(alignment)
                        .padding(dimensions.screenPadding),
                ) {
                    Icon(
                        imageVector = materialSymbol(R.drawable.ic_symbol_edit),
                        contentDescription = stringResource(R.string.action_edit),
                    )
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
    val actionBar = demo.takeIf { it.component == ComponentId.AndroidKitFloatingActionBar }
    AndroidKitPage(
        title = componentDemoTitle(demo),
        onBack = listDetailBackAction(onBack),
        floatingActionButton = {
            actionBar?.let {
                AndroidKitFloatingActionBarDemo(
                    demo = it,
                    onAction = { actionCount += 1 },
                )
            }
        },
    ) { contentPadding ->
        DemoList(contentPadding) {
            item {
                when (demo.component) {
                    ComponentId.AndroidKitCard -> AndroidKitCardDemo(
                        demo = demo,
                        actionCount = actionCount,
                        onAction = { actionCount += 1 },
                    )
                    ComponentId.AndroidKitSettingSection -> AndroidKitSettingSectionDemo(
                        demo = demo,
                        onAction = { actionCount += 1 },
                    )

                    ComponentId.AndroidKitBottomSheet -> AndroidKitBottomSheetDemo(demo)
                    ComponentId.AndroidKitFloatingActionBar -> AndroidKitCard(
                        modifier = Modifier.fillMaxWidth(),
                        header = {
                            DemoCardHeader(
                                title = stringResource(R.string.active_variation),
                                supportingText = stringResource(demo.titleResource),
                            )
                        },
                    ) {
                        Text(stringResource(R.string.action_count, actionCount))
                    }

                    ComponentId.AndroidKitFloatingDropdownMenu ->
                        AndroidKitFloatingDropdownMenuDemo(demo)
                    ComponentId.AndroidKitFloatingNavigation ->
                        AndroidKitFloatingNavigationDemo(
                            demo = demo,
                            layout = floatingNavigationLayout,
                            onLayoutChange = onFloatingNavigationLayoutChange,
                            showLabels = showCompactNavigationLabels,
                            onShowLabelsChange = onShowCompactNavigationLabelsChange,
                        )

                    ComponentId.AndroidKitPage,
                    ComponentId.AndroidKitFloatingActionButton,
                    -> error("Handled by a dedicated demo screen")
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
        header = {
            DemoCardHeader(
                title = stringResource(R.string.demo_section_title),
                supportingText = stringResource(R.string.demo_supporting_text).takeIf {
                    demo != ComponentDemo.AndroidKitCardBasic
                },
            )
        },
    ) {
        Text(stringResource(R.string.demo_section_body))
        if (demo == ComponentDemo.AndroidKitCardRichContent) {
            Button(onClick = {}) { Text(stringResource(R.string.primary_action)) }
        }
        if (hasOverflow) {
            Text(stringResource(R.string.action_count, actionCount))
        }
    }
}

@Composable
private fun AndroidKitSettingSectionDemo(
    demo: ComponentDemo,
    onAction: () -> Unit,
) {
    var checked by rememberSaveable(demo) { mutableStateOf(false) }
    val activeVariation = stringResource(R.string.active_variation)
    val settingLabel = stringResource(R.string.demo_setting_title)
    val actionLabel = stringResource(R.string.primary_action)
    val description = stringResource(R.string.demo_supporting_text)
    val entrySupportingText = stringResource(R.string.demo_setting_supporting_text)
    val settingsIcon = materialSymbol(R.drawable.ic_symbol_settings)
    val checkIcon = materialSymbol(R.drawable.ic_symbol_check)
    AndroidKitSettingSection(
        label = activeVariation.takeUnless {
            demo == ComponentDemo.AndroidKitSettingSectionButton
        },
        description = description.takeIf {
            demo == ComponentDemo.AndroidKitSettingSectionGrouped
        },
    ) {
        when (demo) {
            ComponentDemo.AndroidKitSettingSectionButton -> button(
                label = settingLabel,
                onClick = onAction,
            )

            ComponentDemo.AndroidKitSettingSectionToggle -> toggle(
                label = settingLabel,
                checked = checked,
                onCheckedChange = {
                    checked = it
                    onAction()
                },
            )

            ComponentDemo.AndroidKitSettingSectionGrouped -> {
                toggle(
                    label = settingLabel,
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        onAction()
                    },
                    supportingText = entrySupportingText,
                    icon = settingsIcon,
                )
                button(
                    label = actionLabel,
                    onClick = onAction,
                    icon = checkIcon,
                )
            }

            else -> error("Unexpected AndroidKitSettingSection demo: $demo")
        }
    }
}

@Composable
private fun AndroidKitBottomSheetDemo(demo: ComponentDemo) {
    var visible by rememberSaveable { mutableStateOf(false) }
    var showingDetail by rememberSaveable { mutableStateOf(true) }
    Button(
        modifier = Modifier.testTag("open_bottom_sheet"),
        onClick = {
            showingDetail = true
            visible = true
        },
    ) {
        Text(stringResource(R.string.open_sheet))
    }

    val isBackNavigation = demo == ComponentDemo.AndroidKitBottomSheetBackNavigation
    val isChromeless = demo == ComponentDemo.AndroidKitBottomSheetChromelessFitContent
    val isContentManaged = demo == ComponentDemo.AndroidKitBottomSheetContentManaged
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
        fitContent = isChromeless,
        showChrome = !isChromeless,
        scrollMode = if (isContentManaged) {
            AndroidKitBottomSheetScrollMode.ContentManaged
        } else {
            AndroidKitBottomSheetScrollMode.VerticalScroll
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
            materialSymbol(R.drawable.ic_symbol_close) to cancel,
            materialSymbol(R.drawable.ic_symbol_check) to confirm,
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

            ComponentDemo.AndroidKitFloatingActionBarIconsAndLabels -> {
                iconAndLabel(
                    onClick = onAction,
                    icon = editIcon,
                    label = edit,
                )
                iconAndLabel(
                    onClick = onAction,
                    icon = saveIcon,
                    label = save,
                )
                iconAndLabel(
                    onClick = onAction,
                    icon = shareIcon,
                    label = share,
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
                text(onClick = onAction, label = save)
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
private fun AndroidKitFloatingDropdownMenuDemo(demo: ComponentDemo) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) {
            Icon(
                imageVector = materialSymbol(R.drawable.ic_symbol_more_vert),
                contentDescription = null,
            )
            Text(stringResource(R.string.action_more))
        }
        AndroidKitFloatingDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            listOf(
                materialSymbol(R.drawable.ic_symbol_edit) to R.string.action_edit,
                materialSymbol(R.drawable.ic_symbol_share) to R.string.action_share,
                materialSymbol(R.drawable.ic_symbol_delete) to R.string.action_delete,
            ).forEach { (icon, labelResource) ->
                DropdownMenuItem(
                    text = { Text(stringResource(labelResource)) },
                    onClick = { expanded = false },
                    leadingIcon = if (
                        demo == ComponentDemo.AndroidKitFloatingDropdownMenuIcons
                    ) {
                        { Icon(imageVector = icon, contentDescription = null) }
                    } else {
                        null
                    },
                )
            }
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
    AndroidKitSettingSection(
        label = stringResource(demo.titleResource),
        description = stringResource(R.string.floating_navigation_scenario_instruction),
    ) {
        toggle(
            label = labelsTitle,
            supportingText = labelsDescription,
            checked = showLabels,
            onCheckedChange = onShowLabelsChange,
        )
        toggle(
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
        toggle(
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
private fun componentDemoTitle(demo: ComponentDemo): String =
    "${demo.component.apiName} · ${stringResource(demo.titleResource)}"

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
