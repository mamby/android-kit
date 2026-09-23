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
import net.mamby.androidkit.compose.action.AndroidKitPageActionToolbar
import net.mamby.androidkit.compose.action.MaximumDirectHeaderActions
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
    val pageActions = actions.filterIsInstance<AndroidKitAction>()
    if (title == null && onBack == null && pageActions.isEmpty()) return

    val hasButtons = onBack != null || pageActions.isNotEmpty()
    val hasTitle = title != null
    val visualHeight = pageTitleBarVisualHeight(
        title = title,
        style = style,
        dimensions = dimensions,
    )
    val controlSize = maxOf(dimensions.minimumTouchTarget, visualHeight)

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
                    dimensions = dimensions,
                )
                val actionItems = partitionAndroidKitActions(
                    items = actions,
                    directActionCount = directActionCount,
                )
                val endControlCount = directActionCount +
                    if (actionItems.overflow.isNotEmpty()) 1 else 0
                val leadingWidth = controlRowWidth(
                    controlCount = onBack?.let { 1 } ?: 0,
                    controlSize = controlSize,
                    dimensions = dimensions,
                )
                val endWidth = pageActionRowWidth(
                    controlCount = endControlCount,
                    separatorCount = actionItems.direct.count {
                        it === AndroidKitActionSeparator
                    },
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

                if (endControlCount > 0) {
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
    dimensions: AndroidKitDimensions,
): Int {
    val actionCount = items.count { it is AndroidKitAction }
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
            val endControlCount = directCount + if (actionCount > directCount) 1 else 0
            val endWidth = pageActionRowWidth(
                controlCount = endControlCount,
                separatorCount = directItems.count {
                    it === AndroidKitActionSeparator
                },
                dimensions = dimensions,
                controlSize = controlSize,
            )
            val titleEndSpacing = if (hasTitle && endControlCount > 0) {
                dimensions.spaceExtraSmall
            } else {
                0.dp
            }
            navigationWidth + titleStartSpacing + minimumTitleWidth + titleEndSpacing + endWidth <=
                availableWidth
        } ?: 0
}

private fun pageActionRowWidth(
    controlCount: Int,
    separatorCount: Int,
    dimensions: AndroidKitDimensions,
    controlSize: Dp,
): Dp = controlRowWidth(
    controlCount = controlCount,
    dimensions = dimensions,
    controlSize = controlSize,
) +
    (dimensions.spaceSmall * 2 + DividerDefaults.Thickness) * separatorCount

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
    style: AndroidKitPageTitleBarStyle,
    dimensions: AndroidKitDimensions,
): Dp {
    if (title == null) return dimensions.pageTitleBarButtonSize

    val textMeasurer = rememberTextMeasurer(cacheSize = 1)
    val density = LocalDensity.current
    val titleTextHeight = with(density) {
        textMeasurer.measure(
            text = title,
            style = style.titleTextStyle,
            maxLines = 1,
        ).size.height.toDp()
    }
    return maxOf(
        dimensions.pageTitleBarButtonSize,
        titleTextHeight + dimensions.spaceSmall * 2,
    )
}
