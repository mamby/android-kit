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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.action.AndroidKitFloatingActionBar
import net.mamby.androidkit.compose.action.AndroidKitFloatingActionBarFlyoutStyle
import net.mamby.androidkit.compose.action.AndroidKitFloatingActionButton
import net.mamby.androidkit.compose.action.AndroidKitFloatingDropdownMenu
import net.mamby.androidkit.compose.form.AndroidKitBottomSheet
import net.mamby.androidkit.compose.form.AndroidKitSettingSection
import net.mamby.androidkit.compose.layout.AndroidKitFloatingTitleBarAction
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.presentation.AndroidKitCard
import net.mamby.androidkit.compose.presentation.AndroidKitCardMenuItem
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.ComponentDemo
import net.mamby.androidkit.demo.ui.ComponentId
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
fun ComponentDemoScreen(
    demo: ComponentDemo,
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
    val title = when (demo) {
        ComponentDemo.AndroidKitPageBasic -> null
        ComponentDemo.AndroidKitPageTitle -> stringResource(R.string.demo_page_title)
        ComponentDemo.AndroidKitPageFloatingActionButton -> stringResource(R.string.demo_page_title)
        else -> error("Unexpected AndroidKitPage demo: $demo")
    }
    AndroidKitPage(
        title = title,
        onBack = listDetailBackAction(onBack),
        floatingActionButton = {
            if (demo == ComponentDemo.AndroidKitPageFloatingActionButton) {
                AndroidKitFloatingActionButton(onClick = { actionCount += 1 }) {
                    Icon(
                        imageVector = Icons.Default.Check,
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
                        imageVector = Icons.Default.Edit,
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
    showCompactNavigationLabels: Boolean,
    onShowCompactNavigationLabelsChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    var actionCount by rememberSaveable { mutableIntStateOf(0) }
    val actionBar = demo.takeIf { it.component == ComponentId.AndroidKitFloatingActionBar }
    val titleBarDemo = demo.takeIf { it.component == ComponentId.AndroidKitFloatingTitleBar }
    val titleBarActions = if (
        titleBarDemo == ComponentDemo.AndroidKitFloatingTitleBarBackTitleActions ||
        titleBarDemo == ComponentDemo.AndroidKitFloatingTitleBarImmersiveMode
    ) {
        listOf(
            AndroidKitFloatingTitleBarAction(
                icon = Icons.Default.Save,
                label = stringResource(R.string.action_save),
                onClick = { actionCount += 1 },
            ),
            AndroidKitFloatingTitleBarAction(
                icon = Icons.Default.Share,
                label = stringResource(R.string.action_share),
                onClick = { actionCount += 1 },
            ),
        )
    } else {
        emptyList()
    }
    AndroidKitPage(
        title = if (titleBarDemo == ComponentDemo.AndroidKitFloatingTitleBarBackOnly) {
            null
        } else {
            componentDemoTitle(demo)
        },
        onBack = listDetailBackAction(onBack),
        actions = titleBarActions,
        titleBarImmersiveMode =
            titleBarDemo == ComponentDemo.AndroidKitFloatingTitleBarImmersiveMode,
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
                    ComponentId.AndroidKitFloatingTitleBar ->
                        AndroidKitFloatingTitleBarDemoContent(
                        demo = demo,
                        actionCount = actionCount,
                    )
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
                    ComponentId.AndroidKitFloatingNavigation -> AndroidKitFloatingNavigationDemo(
                        demo = demo,
                        showCompactNavigationLabels = showCompactNavigationLabels,
                        onShowCompactNavigationLabelsChange =
                            onShowCompactNavigationLabelsChange,
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
private fun AndroidKitFloatingTitleBarDemoContent(
    demo: ComponentDemo,
    actionCount: Int,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AndroidKitThemeTokens.dimensions.spaceSmall),
    ) {
        Text(stringResource(R.string.floating_title_bar_demo_instruction))
        if (
            demo == ComponentDemo.AndroidKitFloatingTitleBarBackTitleActions ||
            demo == ComponentDemo.AndroidKitFloatingTitleBarImmersiveMode
        ) {
            Text(stringResource(R.string.action_count, actionCount))
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
                    icon = Icons.Default.Edit,
                ),
                AndroidKitCardMenuItem(
                    label = stringResource(R.string.action_share),
                    onClick = onAction,
                    icon = Icons.Default.Share,
                ),
                AndroidKitCardMenuItem(
                    label = stringResource(R.string.action_delete),
                    onClick = onAction,
                    icon = Icons.Default.Delete,
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
                    icon = Icons.Default.Settings,
                )
                button(
                    label = actionLabel,
                    onClick = onAction,
                    icon = Icons.Default.Check,
                )
            }

            else -> error("Unexpected AndroidKitSettingSection demo: $demo")
        }
    }
}

@Composable
private fun AndroidKitBottomSheetDemo(demo: ComponentDemo) {
    var visible by rememberSaveable { mutableStateOf(false) }
    Button(onClick = { visible = true }) {
        Text(stringResource(R.string.open_sheet))
    }
    if (visible) {
        AndroidKitBottomSheet(
            title = stringResource(R.string.sheet_title).takeUnless {
                demo == ComponentDemo.AndroidKitBottomSheetTitleless
            },
            onDismissRequest = { visible = false },
            onBack = ({ visible = false }).takeIf {
                demo == ComponentDemo.AndroidKitBottomSheetBackAndActions
            },
            actions = if (demo == ComponentDemo.AndroidKitBottomSheetBackAndActions) {
                listOf(
                    AndroidKitFloatingTitleBarAction(
                        icon = Icons.Default.Save,
                        label = stringResource(R.string.action_save),
                        onClick = { visible = false },
                    ),
                )
            } else {
                emptyList()
            },
        ) {
            Text(stringResource(R.string.sheet_body))
            DemoScrollContent()
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
    val flyoutItems = listOf(
        Icons.Default.Add to add,
        Icons.Default.Close to close,
        Icons.Default.Refresh to retry,
        Icons.Default.Close to cancel,
        Icons.Default.Check to confirm,
        Icons.Default.Delete to delete,
    )
    AndroidKitFloatingActionBar {
        when (demo) {
            ComponentDemo.AndroidKitFloatingActionBarIcons -> {
                icon(onClick = onAction, icon = Icons.Default.Edit, contentDescription = edit)
                icon(onClick = onAction, icon = Icons.Default.Save, contentDescription = save)
                icon(onClick = onAction, icon = Icons.Default.Share, contentDescription = share)
                flyout(style = AndroidKitFloatingActionBarFlyoutStyle.Icon) {
                    flyoutItems.forEach { (icon, label) ->
                        item(icon = icon, label = label, onClick = onAction)
                    }
                }
            }

            ComponentDemo.AndroidKitFloatingActionBarIconsAndLabels -> {
                iconAndLabel(onClick = onAction, icon = Icons.Default.Edit, label = edit)
                iconAndLabel(onClick = onAction, icon = Icons.Default.Save, label = save)
                iconAndLabel(onClick = onAction, icon = Icons.Default.Share, label = share)
                flyout(style = AndroidKitFloatingActionBarFlyoutStyle.IconAndLabel) {
                    flyoutItems.forEach { (icon, label) ->
                        item(icon = icon, label = label, onClick = onAction)
                    }
                }
            }

            ComponentDemo.AndroidKitFloatingActionBarText -> {
                text(onClick = onAction, label = edit)
                text(onClick = onAction, label = save)
                text(onClick = onAction, label = share)
                flyout(style = AndroidKitFloatingActionBarFlyoutStyle.Text) {
                    flyoutItems.forEach { (icon, label) ->
                        item(icon = icon, label = label, onClick = onAction)
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
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
            Text(stringResource(R.string.action_more))
        }
        AndroidKitFloatingDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            listOf(
                Icons.Default.Edit to R.string.action_edit,
                Icons.Default.Share to R.string.action_share,
                Icons.Default.Delete to R.string.action_delete,
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
    showCompactNavigationLabels: Boolean,
    onShowCompactNavigationLabelsChange: (Boolean) -> Unit,
) {
    if (demo == ComponentDemo.AndroidKitFloatingNavigationLabels) {
        val entryLabel = stringResource(R.string.floating_navigation_labels_title)
        AndroidKitSettingSection(
            label = stringResource(R.string.active_variation),
            description = stringResource(R.string.floating_navigation_labels_description),
        ) {
            toggle(
                label = entryLabel,
                checked = showCompactNavigationLabels,
                onCheckedChange = onShowCompactNavigationLabelsChange,
            )
        }
    } else {
        AndroidKitCard(
            modifier = Modifier.fillMaxWidth(),
            header = {
                DemoCardHeader(
                    title = stringResource(R.string.active_variation),
                    supportingText = stringResource(demo.titleResource),
                )
            },
        ) {
            Text(stringResource(R.string.floating_navigation_overflow_instruction))
        }
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
