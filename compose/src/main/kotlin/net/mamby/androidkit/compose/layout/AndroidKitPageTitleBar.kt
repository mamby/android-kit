package net.mamby.androidkit.compose.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.action.AndroidKitFloatingDropdownMenu
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitDimensions
import net.mamby.androidkit.compose.theme.AndroidKitPageTitleBarStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface
import net.mamby.androidkit.compose.theme.FloatingSurfaceButton

@Composable
internal fun AndroidKitPageTitleBar(
    title: String?,
    onBack: (() -> Unit)?,
    actions: List<AndroidKitPageAction>,
    visible: Boolean,
    style: AndroidKitPageTitleBarStyle,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val strings = AndroidKitThemeTokens.strings
    if (title == null && onBack == null && actions.isEmpty()) return

    val hasButtons = onBack != null || actions.isNotEmpty()
    val hasTitle = title != null
    var overflowExpanded by remember { mutableStateOf(false) }

    fun setOverflowExpanded(expanded: Boolean) {
        overflowExpanded = expanded
    }

    LaunchedEffect(visible) {
        if (!visible && overflowExpanded) setOverflowExpanded(false)
    }

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
                        min = if (hasButtons) dimensions.minimumTouchTarget else 0.dp,
                    ),
            ) {
                val directActionCount = directPageTitleBarActionCount(
                    actionCount = actions.size,
                    availableWidth = maxWidth,
                    hasTitle = hasTitle,
                    hasNavigation = onBack != null,
                    dimensions = dimensions,
                )
                val directActions = actions.take(directActionCount)
                val overflowActions = actions.drop(directActionCount)
                LaunchedEffect(overflowActions.isNotEmpty()) {
                    if (overflowActions.isEmpty()) setOverflowExpanded(false)
                }
                val endControlCount = directActions.size + if (overflowActions.isNotEmpty()) 1 else 0
                val leadingWidth = controlRowWidth(
                    controlCount = onBack?.let { 1 } ?: 0,
                    dimensions = dimensions,
                )
                val endWidth = controlRowWidth(endControlCount, dimensions)
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
                            ),
                        style = style.titleSurfaceStyle
                            ?: AndroidKitThemeTokens.floatingSurfaceStyle,
                    ) {
                        Box(
                            modifier = Modifier.padding(
                                horizontal = dimensions.spaceMedium,
                                vertical = dimensions.spaceSmall,
                            ),
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
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        directActions.forEach { action ->
                            PageTitleBarActionButton(action = action, style = style)
                        }
                        if (overflowActions.isNotEmpty()) {
                            Box {
                                PageTitleBarActionButton(
                                    action = AndroidKitPageAction(
                                        icon = AndroidKitIcons.More,
                                        label = strings.more,
                                        onClick = {
                                            setOverflowExpanded(true)
                                        },
                                    ),
                                    style = style,
                                )
                                AndroidKitFloatingDropdownMenu(
                                    expanded = overflowExpanded,
                                    onDismissRequest = {
                                        setOverflowExpanded(false)
                                    },
                                    offset = DpOffset(
                                        x = 0.dp,
                                        y = -dimensions.spaceExtraSmall,
                                    ),
                                    style = style.dropdownMenuStyle
                                        ?: AndroidKitThemeTokens.floatingDropdownMenuStyle,
                                ) {
                                    overflowActions.forEach { action ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = action.label,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            },
                                            onClick = {
                                                setOverflowExpanded(false)
                                                action.onClick()
                                            },
                                            enabled = action.enabled,
                                            contentPadding = PaddingValues(
                                                start = dimensions.spaceMedium,
                                                end = dimensions.spaceLarge,
                                            ),
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = action.icon,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(
                                                        dimensions.floatingActionIconSize,
                                                    ),
                                                )
                                            },
                                        )
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
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    FloatingSurfaceButton(
        onClick = onClick,
        shape = style.buttonShape,
        visualSize = dimensions.pageTitleBarButtonSize,
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
private fun PageTitleBarActionButton(
    action: AndroidKitPageAction,
    style: AndroidKitPageTitleBarStyle,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    FloatingSurfaceButton(
        onClick = action.onClick,
        shape = style.buttonShape,
        visualSize = dimensions.pageTitleBarButtonSize,
        enabled = action.enabled,
        style = style.buttonSurfaceStyle ?: AndroidKitThemeTokens.floatingSurfaceStyle,
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = action.label,
            modifier = Modifier.size(dimensions.floatingActionIconSize),
        )
    }
}

private fun directPageTitleBarActionCount(
    actionCount: Int,
    availableWidth: Dp,
    hasTitle: Boolean,
    hasNavigation: Boolean,
    dimensions: AndroidKitDimensions,
): Int {
    val navigationWidth = controlRowWidth(
        controlCount = if (hasNavigation) 1 else 0,
        dimensions = dimensions,
    )
    val minimumTitleWidth = if (hasTitle) dimensions.pageTitleBarMinimumTitleWidth else 0.dp
    val titleStartSpacing = if (hasTitle && hasNavigation) dimensions.spaceExtraSmall else 0.dp

    return (minOf(MaximumDirectPageTitleBarActions, actionCount) downTo 0)
        .firstOrNull { directCount ->
            val endControlCount = directCount + if (actionCount > directCount) 1 else 0
            val endWidth = controlRowWidth(endControlCount, dimensions)
            val titleEndSpacing = if (hasTitle && endControlCount > 0) {
                dimensions.spaceExtraSmall
            } else {
                0.dp
            }
            navigationWidth + titleStartSpacing + minimumTitleWidth + titleEndSpacing + endWidth <=
                availableWidth
        } ?: 0
}

private fun controlRowWidth(
    controlCount: Int,
    dimensions: AndroidKitDimensions,
): Dp = if (controlCount == 0) {
    0.dp
} else {
    dimensions.minimumTouchTarget * controlCount
}

private const val MaximumDirectPageTitleBarActions = 2
