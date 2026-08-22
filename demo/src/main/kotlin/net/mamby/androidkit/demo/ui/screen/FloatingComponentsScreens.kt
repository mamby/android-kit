package net.mamby.androidkit.demo.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.action.FloatingActionBar
import net.mamby.androidkit.compose.action.FloatingActionBarFlyout
import net.mamby.androidkit.compose.action.FloatingActionBarFlyoutItem
import net.mamby.androidkit.compose.action.FloatingActionBarFlyoutStyle
import net.mamby.androidkit.compose.action.FloatingActionBarIconItem
import net.mamby.androidkit.compose.action.FloatingActionBarIconLabelItem
import net.mamby.androidkit.compose.action.FloatingActionBarTextItem
import net.mamby.androidkit.compose.layout.DetailPage
import net.mamby.androidkit.compose.layout.PageScaffold
import net.mamby.androidkit.compose.presentation.SectionCard
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.FloatingActionDemoVariant

@Composable
fun FloatingCatalogScreen(
    onOpenFloatingNavigation: (showLabels: Boolean) -> Unit,
    onOpenFloatingActions: (FloatingActionDemoVariant) -> Unit,
) {
    PageScaffold(
        title = stringResource(R.string.nav_floating),
    ) { contentPadding ->
        DetailPage(contentPadding = contentPadding) {
            SectionCard(
                title = stringResource(R.string.floating_navigation_variations_title),
                supportingText = stringResource(R.string.floating_navigation_variations_description),
            ) {
                OutlinedButton(onClick = { onOpenFloatingNavigation(true) }) {
                    Text(stringResource(R.string.floating_navigation_labels_title))
                }
                OutlinedButton(onClick = { onOpenFloatingNavigation(false) }) {
                    Text(stringResource(R.string.floating_navigation_icons_title))
                }
            }
            SectionCard(
                title = stringResource(R.string.floating_actions_variations_title),
                supportingText = stringResource(R.string.floating_actions_variations_description),
            ) {
                FloatingActionDemoVariant.entries.forEach { variant ->
                    OutlinedButton(onClick = { onOpenFloatingActions(variant) }) {
                        Text(stringResource(variant.titleResource()))
                    }
                }
            }
            ScrollTestContent()
        }
    }
}

@Composable
fun FloatingNavigationDemoScreen(
    showLabels: Boolean,
    onBack: () -> Unit,
) {
    PageScaffold(
        title = stringResource(
            if (showLabels) {
                R.string.floating_navigation_labels_title
            } else {
                R.string.floating_navigation_icons_title
            },
        ),
        onBack = onBack,
    ) { contentPadding ->
        DetailPage(contentPadding = contentPadding) {
            SectionCard(
                title = stringResource(R.string.active_variation),
                supportingText = stringResource(
                    if (showLabels) {
                        R.string.floating_navigation_labels_description
                    } else {
                        R.string.floating_navigation_icons_description
                    },
                ),
            ) {
                Text(stringResource(R.string.floating_navigation_demo_instruction))
            }
            ScrollTestContent()
        }
    }
}

@Composable
fun FloatingActionsDemoScreen(
    variant: FloatingActionDemoVariant,
    onBack: () -> Unit,
) {
    var actionCount by rememberSaveable { mutableIntStateOf(0) }
    val flyoutItems = listOf(
        FloatingActionBarFlyoutItem(
            icon = Icons.Default.Share,
            label = stringResource(R.string.action_share),
            onClick = { actionCount += 1 },
        ),
        FloatingActionBarFlyoutItem(
            icon = Icons.Default.Delete,
            label = stringResource(R.string.action_delete),
            onClick = { actionCount += 1 },
        ),
        FloatingActionBarFlyoutItem(
            icon = Icons.Default.Edit,
            label = stringResource(R.string.action_edit),
            onClick = { actionCount += 1 },
        ),
    )
    PageScaffold(
        title = stringResource(variant.titleResource()),
        onBack = onBack,
        floatingActionButton = {
            FloatingActionBar {
                when (variant) {
                    FloatingActionDemoVariant.IconAndText -> {
                        FloatingActionBarIconLabelItem(
                            onClick = { actionCount += 1 },
                            icon = Icons.Default.Add,
                            label = stringResource(R.string.action_add),
                        )
                        FloatingActionBarIconLabelItem(
                            onClick = { actionCount += 1 },
                            icon = Icons.Default.Save,
                            label = stringResource(R.string.action_save),
                        )
                        FloatingActionBarFlyout(
                            items = flyoutItems,
                            style = FloatingActionBarFlyoutStyle.IconAndLabel,
                        )
                    }

                    FloatingActionDemoVariant.IconsOnly -> {
                        FloatingActionBarIconItem(
                            onClick = { actionCount += 1 },
                            icon = Icons.Default.Add,
                            contentDescription = stringResource(R.string.action_add),
                        )
                        FloatingActionBarIconItem(
                            onClick = { actionCount += 1 },
                            icon = Icons.Default.Save,
                            contentDescription = stringResource(R.string.action_save),
                        )
                        FloatingActionBarFlyout(
                            items = flyoutItems,
                        )
                    }

                    FloatingActionDemoVariant.TextOnly -> {
                        FloatingActionBarTextItem(
                            onClick = { actionCount = 0 },
                            label = stringResource(R.string.action_cancel),
                        )
                        FloatingActionBarTextItem(
                            onClick = { actionCount += 1 },
                            label = stringResource(R.string.action_save),
                        )
                        FloatingActionBarFlyout(
                            items = flyoutItems,
                            style = FloatingActionBarFlyoutStyle.Text,
                        )
                    }
                }
            }
        },
    ) { contentPadding ->
        DetailPage(contentPadding = contentPadding) {
            SectionCard(
                title = stringResource(R.string.active_variation),
                supportingText = stringResource(variant.descriptionResource()),
            ) {
                Text(stringResource(R.string.action_count, actionCount))
            }
            ScrollTestContent()
        }
    }
}

private fun FloatingActionDemoVariant.titleResource(): Int = when (this) {
    FloatingActionDemoVariant.IconAndText -> R.string.floating_actions_icon_text_title
    FloatingActionDemoVariant.IconsOnly -> R.string.floating_actions_icons_title
    FloatingActionDemoVariant.TextOnly -> R.string.floating_actions_text_title
}

private fun FloatingActionDemoVariant.descriptionResource(): Int = when (this) {
    FloatingActionDemoVariant.IconAndText -> R.string.floating_actions_icon_text_description
    FloatingActionDemoVariant.IconsOnly -> R.string.floating_actions_icons_description
    FloatingActionDemoVariant.TextOnly -> R.string.floating_actions_text_description
}
