package net.mamby.androidkit.compose.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.action.AndroidKitAction
import net.mamby.androidkit.compose.action.AndroidKitActionItem
import net.mamby.androidkit.compose.action.AndroidKitActionSeparator
import net.mamby.androidkit.compose.action.AndroidKitFloatingToolbarIconAndLabelLayout
import net.mamby.androidkit.compose.action.AndroidKitIconAndLabelAction
import net.mamby.androidkit.compose.action.AndroidKitPageActionToolbar
import net.mamby.androidkit.compose.action.AndroidKitTextAction
import net.mamby.androidkit.compose.action.MaximumDirectHeaderActions
import net.mamby.androidkit.compose.action.isAndroidKitAction
import net.mamby.androidkit.compose.action.partitionAndroidKitActions
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitDimensions
import net.mamby.androidkit.compose.theme.AndroidKitFloatingToolbarStyle
import net.mamby.androidkit.compose.theme.AndroidKitPageTitleBarStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface
import net.mamby.androidkit.compose.theme.FloatingSurfaceButton

@Composable
internal fun AndroidKitPageTitleBar(
    title: String?,
    onBack: (() -> Unit)?,
    actions: List<AndroidKitActionItem>,
    visible: Boolean,
    style: AndroidKitPageTitleBarStyle,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val pageActions = actions.filter { it.isAndroidKitAction }
    if (title == null && onBack == null && pageActions.isEmpty()) return

    val hasButtons = onBack != null || pageActions.isNotEmpty()
    val hasTitle = title != null
    val visualHeight = pageTitleBarVisualHeight(
        title = title,
        actions = actions,
        style = style,
        dimensions = dimensions,
    )
    val controlSize = maxOf(dimensions.minimumTouchTarget, visualHeight)
    val actionWidths = pageTitleBarActionWidths(
        items = actions,
        controlSize = controlSize,
        dimensions = dimensions,
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Top,
                ),
            )
            .heightIn(min = dimensions.pageTitleBarHeight)
            .padding(
                horizontal = dimensions.spaceSmall,
                vertical = dimensions.pageTitleBarVerticalPadding,
            ),
    ) {
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = if (hasButtons) controlSize else visualHeight,
                    ),
            ) {
                val directActionCount = directPageTitleBarActionCount(
                    items = actions,
                    availableWidth = maxWidth,
                    hasTitle = hasTitle,
                    hasNavigation = onBack != null,
                    controlSize = controlSize,
                    actionWidths = actionWidths,
                    dimensions = dimensions,
                )
                val actionItems = partitionAndroidKitActions(
                    items = actions,
                    directActionCount = directActionCount,
                )
                val leadingWidth = controlRowWidth(
                    controlCount = onBack?.let { 1 } ?: 0,
                    controlSize = controlSize,
                    dimensions = dimensions,
                )
                val endWidth = pageActionRowWidth(
                    items = actionItems.direct,
                    hasOverflow = actionItems.overflow.isNotEmpty(),
                    actionWidths = actionWidths,
                    dimensions = dimensions,
                    controlSize = controlSize,
                )
                val titleStartPadding = leadingWidth + if (leadingWidth > 0.dp) {
                    dimensions.spaceExtraSmall
                } else {
                    0.dp
                }
                val titleEndPadding = endWidth + if (endWidth > 0.dp) {
                    dimensions.spaceExtraSmall
                } else {
                    0.dp
                }

                if (onBack != null) {
                    PageTitleBarBackButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart),
                        style = style,
                        visualSize = visualHeight,
                    )
                }

                title?.let { pageTitle ->
                    FloatingSurface(
                        shape = style.titleShape,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(
                                start = titleStartPadding,
                                end = titleEndPadding,
                            )
                            .height(visualHeight),
                        style = style.titleSurfaceStyle
                            ?: AndroidKitThemeTokens.floatingSurfaceStyle,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = dimensions.spaceMedium),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = pageTitle,
                                modifier = Modifier.clearAndSetSemantics {
                                    heading()
                                    contentDescription = pageTitle
                                },
                                textAlign = TextAlign.Start,
                                style = style.titleTextStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                if (endWidth > 0.dp) {
                    AndroidKitPageActionToolbar(
                        visualHeight = visualHeight,
                        modifier = Modifier.align(Alignment.CenterEnd),
                        style = pageActionToolbarStyle(style, dimensions),
                    ) {
                        actionItems.direct.forEach { pageActionItem ->
                            when (pageActionItem) {
                                is AndroidKitAction -> icon(
                                    onClick = pageActionItem.onClick,
                                    icon = pageActionItem.icon,
                                    contentDescription = pageActionItem.label,
                                    enabled = pageActionItem.enabled,
                                )

                                is AndroidKitTextAction -> text(
                                    onClick = pageActionItem.onClick,
                                    label = pageActionItem.label,
                                    enabled = pageActionItem.enabled,
                                )

                                is AndroidKitIconAndLabelAction -> iconAndLabel(
                                    onClick = pageActionItem.onClick,
                                    icon = pageActionItem.icon,
                                    label = pageActionItem.label,
                                    layout = AndroidKitFloatingToolbarIconAndLabelLayout.Horizontal,
                                    enabled = pageActionItem.enabled,
                                )

                                AndroidKitActionSeparator -> separator()
                            }
                        }
                        if (actionItems.overflow.isNotEmpty()) {
                            flyout(enabled = visible) {
                                actionItems.overflow.forEach { pageActionItem ->
                                    when (pageActionItem) {
                                        is AndroidKitAction -> item(
                                            icon = pageActionItem.icon,
                                            label = pageActionItem.label,
                                            onClick = pageActionItem.onClick,
                                            enabled = pageActionItem.enabled,
                                        )

                                        is AndroidKitTextAction -> item(
                                            label = pageActionItem.label,
                                            onClick = pageActionItem.onClick,
                                            enabled = pageActionItem.enabled,
                                        )

                                        is AndroidKitIconAndLabelAction -> item(
                                            icon = pageActionItem.icon,
                                            label = pageActionItem.label,
                                            onClick = pageActionItem.onClick,
                                            enabled = pageActionItem.enabled,
                                        )

                                        AndroidKitActionSeparator -> separator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageTitleBarBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AndroidKitPageTitleBarStyle,
    visualSize: Dp,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    FloatingSurfaceButton(
        onClick = onClick,
        shape = style.buttonShape,
        visualSize = visualSize,
        modifier = modifier,
        style = style.buttonSurfaceStyle ?: AndroidKitThemeTokens.floatingSurfaceStyle,
    ) {
        Icon(
            imageVector = AndroidKitIcons.ArrowBack,
            contentDescription = AndroidKitThemeTokens.strings.back,
            modifier = Modifier.size(dimensions.floatingActionIconSize),
        )
    }
}

@Composable
private fun pageActionToolbarStyle(
    pageTitleBarStyle: AndroidKitPageTitleBarStyle,
    dimensions: AndroidKitDimensions,
): AndroidKitFloatingToolbarStyle = AndroidKitFloatingToolbarStyle(
    surfaceStyle = pageTitleBarStyle.buttonSurfaceStyle,
    flyoutStyle = pageTitleBarStyle.flyoutStyle,
    separatorColor = AndroidKitThemeTokens.floatingToolbarStyle.separatorColor,
    shape = pageTitleBarStyle.buttonShape,
    itemShape = pageTitleBarStyle.buttonShape,
    labelTextStyle = AndroidKitThemeTokens.floatingToolbarStyle.labelTextStyle,
    iconSize = dimensions.floatingActionIconSize,
)

private fun directPageTitleBarActionCount(
    items: List<AndroidKitActionItem>,
    availableWidth: Dp,
    hasTitle: Boolean,
    hasNavigation: Boolean,
    controlSize: Dp,
    actionWidths: Map<AndroidKitActionItem, Dp>,
    dimensions: AndroidKitDimensions,
): Int {
    val actionCount = items.count { it.isAndroidKitAction }
    val navigationWidth = controlRowWidth(
        controlCount = if (hasNavigation) 1 else 0,
        controlSize = controlSize,
        dimensions = dimensions,
    )
    val minimumTitleWidth = if (hasTitle) dimensions.pageTitleBarMinimumTitleWidth else 0.dp
    val titleStartSpacing = if (hasTitle && hasNavigation) dimensions.spaceExtraSmall else 0.dp

    return (minOf(MaximumDirectHeaderActions, actionCount) downTo 0)
        .firstOrNull { directCount ->
            val directItems = partitionAndroidKitActions(
                items = items,
                directActionCount = directCount,
            ).direct
            val endWidth = pageActionRowWidth(
                items = directItems,
                hasOverflow = actionCount > directCount,
                actionWidths = actionWidths,
                dimensions = dimensions,
                controlSize = controlSize,
            )
            val titleEndSpacing = if (hasTitle && endWidth > 0.dp) {
                dimensions.spaceExtraSmall
            } else {
                0.dp
            }
            navigationWidth + titleStartSpacing + minimumTitleWidth + titleEndSpacing + endWidth <=
                availableWidth
        } ?: 0
}

private fun pageActionRowWidth(
    items: List<AndroidKitActionItem>,
    hasOverflow: Boolean,
    actionWidths: Map<AndroidKitActionItem, Dp>,
    dimensions: AndroidKitDimensions,
    controlSize: Dp,
): Dp = items.fold(0.dp) { width, item ->
    width + when (item) {
        is AndroidKitAction,
        is AndroidKitTextAction,
        is AndroidKitIconAndLabelAction,
        -> actionWidths.getValue(item)

        AndroidKitActionSeparator -> DividerDefaults.Thickness
    }
} + (if (hasOverflow) controlSize else 0.dp) +
    dimensions.spaceSmall * (items.size + (if (hasOverflow) 1 else 0) - 1).coerceAtLeast(0) +
    (if (items.isNotEmpty() || hasOverflow) dimensions.spaceSmall * 2 else 0.dp)

private fun controlRowWidth(
    controlCount: Int,
    dimensions: AndroidKitDimensions,
    controlSize: Dp = dimensions.minimumTouchTarget,
): Dp = if (controlCount == 0) {
    0.dp
} else {
    controlSize * controlCount
}

@Composable
private fun pageTitleBarVisualHeight(
    title: String?,
    actions: List<AndroidKitActionItem>,
    style: AndroidKitPageTitleBarStyle,
    dimensions: AndroidKitDimensions,
): Dp {
    val textMeasurer = rememberTextMeasurer(cacheSize = 2)
    val density = LocalDensity.current
    val titleTextHeight = title?.let { pageTitle ->
        with(density) {
            textMeasurer.measure(
                text = pageTitle,
                style = style.titleTextStyle,
                maxLines = 1,
            ).size.height.toDp()
        }
    } ?: 0.dp
    val hasLabeledAction = actions.any {
        it is AndroidKitTextAction || it is AndroidKitIconAndLabelAction
    }
    val actionTextHeight = if (hasLabeledAction) {
        with(density) {
            textMeasurer.measure(
                text = actions.firstNotNullOf { item ->
                    when (item) {
                        is AndroidKitTextAction -> item.label
                        is AndroidKitIconAndLabelAction -> item.label
                        else -> null
                    }
                },
                style = AndroidKitThemeTokens.floatingToolbarStyle.labelTextStyle,
                maxLines = 1,
            ).size.height.toDp()
        }
    } else {
        0.dp
    }
    return maxOf(
        dimensions.pageTitleBarButtonSize,
        titleTextHeight + dimensions.spaceSmall * 2,
        actionTextHeight + dimensions.spaceSmall * 2,
    )
}

@Composable
private fun pageTitleBarActionWidths(
    items: List<AndroidKitActionItem>,
    controlSize: Dp,
    dimensions: AndroidKitDimensions,
): Map<AndroidKitActionItem, Dp> {
    val textMeasurer = rememberTextMeasurer(cacheSize = items.size.coerceAtLeast(1))
    val density = LocalDensity.current
    val labelStyle = AndroidKitThemeTokens.floatingToolbarStyle.labelTextStyle
    return items.filter { it.isAndroidKitAction }.associateWith { item ->
        val contentWidth = when (item) {
            is AndroidKitAction -> 0.dp
            is AndroidKitTextAction -> with(density) {
                textMeasurer.measure(
                    text = item.label,
                    style = labelStyle,
                    maxLines = 1,
                ).size.width.toDp()
            }

            is AndroidKitIconAndLabelAction -> dimensions.floatingActionIconSize +
                dimensions.spaceExtraSmall + with(density) {
                textMeasurer.measure(
                    text = item.label,
                    style = labelStyle,
                    maxLines = 1,
                ).size.width.toDp()
            }

            AndroidKitActionSeparator -> 0.dp
        }
        maxOf(controlSize, contentWidth + dimensions.spaceSmall * 2)
    }
}
