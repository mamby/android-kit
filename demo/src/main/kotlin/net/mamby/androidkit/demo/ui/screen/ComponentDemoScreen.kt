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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import net.mamby.androidkit.compose.action.FloatingActionBar
import net.mamby.androidkit.compose.action.FloatingButton
import net.mamby.androidkit.compose.action.FloatingDropdownMenu
import net.mamby.androidkit.compose.form.AndroidKitModalSheet
import net.mamby.androidkit.compose.form.SettingsItem
import net.mamby.androidkit.compose.layout.FloatingTitleBarAction
import net.mamby.androidkit.compose.layout.PageScaffold
import net.mamby.androidkit.compose.presentation.AndroidKitCard
import net.mamby.androidkit.compose.presentation.AndroidKitCardMenuItem
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.ComponentDemo
import net.mamby.androidkit.demo.ui.ComponentId
import net.mamby.androidkit.navigation3.listDetailBackAction

@Composable
fun ComponentPlaceholder() {
    PageScaffold { contentPadding ->
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
        ComponentId.PageScaffold -> PageScaffoldDemo(demo = demo, onBack = onBack)
        ComponentId.FloatingButton -> FloatingButtonDemo(demo = demo, onBack = onBack)
        else -> StandardComponentDemo(
            demo = demo,
            showCompactNavigationLabels = showCompactNavigationLabels,
            onShowCompactNavigationLabelsChange = onShowCompactNavigationLabelsChange,
            onBack = onBack,
        )
    }
}

@Composable
private fun PageScaffoldDemo(
    demo: ComponentDemo,
    onBack: () -> Unit,
) {
    var actionCount by rememberSaveable { mutableIntStateOf(0) }
    val title = when (demo) {
        ComponentDemo.PageScaffoldBasic -> null
        ComponentDemo.PageScaffoldTitle -> stringResource(R.string.demo_page_title)
        ComponentDemo.PageScaffoldFloatingButton -> stringResource(R.string.demo_page_title)
        else -> error("Unexpected PageScaffold demo: $demo")
    }
    PageScaffold(
        title = title,
        onBack = listDetailBackAction(onBack),
        floatingActionButton = {
            if (demo == ComponentDemo.PageScaffoldFloatingButton) {
                FloatingButton(onClick = { actionCount += 1 }) {
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
private fun FloatingButtonDemo(
    demo: ComponentDemo,
    onBack: () -> Unit,
) {
    var actionCount by rememberSaveable { mutableIntStateOf(0) }
    val alignment = when (demo) {
        ComponentDemo.FloatingButtonTopStart -> Alignment.TopStart
        ComponentDemo.FloatingButtonTopCenter -> Alignment.TopCenter
        ComponentDemo.FloatingButtonTopEnd -> Alignment.TopEnd
        ComponentDemo.FloatingButtonBottomStart -> Alignment.BottomStart
        ComponentDemo.FloatingButtonBottomCenter -> Alignment.BottomCenter
        ComponentDemo.FloatingButtonBottomEnd -> Alignment.BottomEnd
        else -> error("Unexpected FloatingButton demo: $demo")
    }
    val dimensions = AndroidKitThemeTokens.dimensions
    PageScaffold(
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
                FloatingButton(
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
    val actionBar = demo.takeIf { it.component == ComponentId.FloatingActionBar }
    val titleBarDemo = demo.takeIf { it.component == ComponentId.FloatingTitleBar }
    val titleBarActions = if (
        titleBarDemo == ComponentDemo.FloatingTitleBarBackTitleActions ||
        titleBarDemo == ComponentDemo.FloatingTitleBarImmersiveMode
    ) {
        listOf(
            FloatingTitleBarAction(
                icon = Icons.Default.Save,
                label = stringResource(R.string.action_save),
                onClick = { actionCount += 1 },
            ),
            FloatingTitleBarAction(
                icon = Icons.Default.Share,
                label = stringResource(R.string.action_share),
                onClick = { actionCount += 1 },
            ),
        )
    } else {
        emptyList()
    }
    PageScaffold(
        title = if (titleBarDemo == ComponentDemo.FloatingTitleBarBackOnly) {
            null
        } else {
            componentDemoTitle(demo)
        },
        onBack = listDetailBackAction(onBack),
        actions = titleBarActions,
        titleBarImmersiveMode = titleBarDemo == ComponentDemo.FloatingTitleBarImmersiveMode,
        floatingActionButton = {
            actionBar?.let {
                FloatingActionBarDemo(
                    demo = it,
                    onAction = { actionCount += 1 },
                )
            }
        },
    ) { contentPadding ->
        DemoList(contentPadding) {
            item {
                when (demo.component) {
                    ComponentId.FloatingTitleBar -> FloatingTitleBarDemoContent(
                        demo = demo,
                        actionCount = actionCount,
                    )
                    ComponentId.AndroidKitCard -> AndroidKitCardDemo(
                        demo = demo,
                        actionCount = actionCount,
                        onAction = { actionCount += 1 },
                    )
                    ComponentId.SettingsItem -> SettingsItemDemo(
                        demo = demo,
                        onAction = { actionCount += 1 },
                    )

                    ComponentId.AndroidKitModalSheet -> ModalSheetDemo(demo)
                    ComponentId.FloatingActionBar -> AndroidKitCard(
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

                    ComponentId.FloatingDropdownMenu -> FloatingDropdownMenuDemo(demo)
                    ComponentId.FloatingNavigation -> FloatingNavigationDemo(
                        demo = demo,
                        showCompactNavigationLabels = showCompactNavigationLabels,
                        onShowCompactNavigationLabelsChange =
                            onShowCompactNavigationLabelsChange,
                    )

                    ComponentId.PageScaffold,
                    ComponentId.FloatingButton,
                    -> error("Handled by a dedicated demo screen")
                }
            }
            item { DemoScrollContent() }
        }
    }
}

@Composable
private fun FloatingTitleBarDemoContent(
    demo: ComponentDemo,
    actionCount: Int,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AndroidKitThemeTokens.dimensions.spaceSmall),
    ) {
        Text(stringResource(R.string.floating_title_bar_demo_instruction))
        if (
            demo == ComponentDemo.FloatingTitleBarBackTitleActions ||
            demo == ComponentDemo.FloatingTitleBarImmersiveMode
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
private fun SettingsItemDemo(
    demo: ComponentDemo,
    onAction: () -> Unit,
) {
    AndroidKitCard(
        modifier = Modifier.fillMaxWidth(),
        header = {
            DemoCardHeader(title = stringResource(R.string.active_variation))
        },
    ) {
        SettingsItem(
            title = stringResource(R.string.demo_setting_title),
            supportingText = stringResource(R.string.demo_supporting_text).takeIf {
                demo != ComponentDemo.SettingsItemBasic
            },
            onClick = onAction,
            leadingContent = if (demo == ComponentDemo.SettingsItemAccessories) {
                {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                    )
                }
            } else {
                null
            },
            trailingContent = if (demo == ComponentDemo.SettingsItemAccessories) {
                { Icon(imageVector = Icons.Default.Check, contentDescription = null) }
            } else {
                null
            },
        )
    }
}

@Composable
private fun ModalSheetDemo(demo: ComponentDemo) {
    var visible by rememberSaveable { mutableStateOf(false) }
    Button(onClick = { visible = true }) {
        Text(stringResource(R.string.open_sheet))
    }
    if (visible) {
        AndroidKitModalSheet(
            title = stringResource(R.string.sheet_title).takeUnless {
                demo == ComponentDemo.ModalSheetTitleless
            },
            onDismissRequest = { visible = false },
            onBack = ({ visible = false }).takeIf {
                demo == ComponentDemo.ModalSheetBackAndActions
            },
            actions = if (demo == ComponentDemo.ModalSheetBackAndActions) {
                listOf(
                    FloatingTitleBarAction(
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
private fun FloatingActionBarDemo(
    demo: ComponentDemo,
    onAction: () -> Unit,
) {
    val add = stringResource(R.string.action_add)
    val save = stringResource(R.string.action_save)
    val share = stringResource(R.string.action_share)
    val delete = stringResource(R.string.action_delete)
    FloatingActionBar {
        when (demo) {
            ComponentDemo.FloatingActionBarIcons -> {
                icon(onClick = onAction, icon = Icons.Default.Edit, contentDescription = add)
                icon(onClick = onAction, icon = Icons.Default.Save, contentDescription = save)
            }

            ComponentDemo.FloatingActionBarIconsAndLabels -> {
                iconAndLabel(onClick = onAction, icon = Icons.Default.Edit, label = add)
                iconAndLabel(onClick = onAction, icon = Icons.Default.Save, label = save)
            }

            ComponentDemo.FloatingActionBarText -> {
                text(onClick = onAction, label = add)
                text(onClick = onAction, label = save)
            }

            ComponentDemo.FloatingActionBarWithFlyout -> flyout {
                item(icon = Icons.Default.Share, label = share, onClick = onAction)
                item(icon = Icons.Default.Delete, label = delete, onClick = onAction)
            }

            else -> error("Unexpected FloatingActionBar demo: $demo")
        }
    }
}

@Composable
private fun FloatingDropdownMenuDemo(demo: ComponentDemo) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
            Text(stringResource(R.string.action_more))
        }
        FloatingDropdownMenu(
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
                    leadingIcon = if (demo == ComponentDemo.FloatingDropdownMenuIcons) {
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
private fun FloatingNavigationDemo(
    demo: ComponentDemo,
    showCompactNavigationLabels: Boolean,
    onShowCompactNavigationLabelsChange: (Boolean) -> Unit,
) {
    AndroidKitCard(
        modifier = Modifier.fillMaxWidth(),
        header = {
            DemoCardHeader(
                title = stringResource(R.string.active_variation),
                supportingText = stringResource(demo.titleResource),
            )
        },
    ) {
        if (demo == ComponentDemo.FloatingNavigationLabels) {
            SettingsItem(
                title = stringResource(R.string.floating_navigation_labels_title),
                supportingText = stringResource(R.string.floating_navigation_labels_description),
                onClick = {
                    onShowCompactNavigationLabelsChange(!showCompactNavigationLabels)
                },
                trailingContent = {
                    Switch(
                        checked = showCompactNavigationLabels,
                        onCheckedChange = null,
                    )
                },
            )
        } else {
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
