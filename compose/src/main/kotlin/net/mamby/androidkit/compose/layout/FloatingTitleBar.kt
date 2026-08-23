package net.mamby.androidkit.compose.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.action.FloatingDropdownMenu
import net.mamby.androidkit.compose.theme.AndroidKitDimensions
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurfaceButton

@Immutable
public class FloatingTitleBarAction(
    public val icon: ImageVector,
    public val label: String,
    public val onClick: () -> Unit,
)

@Composable
public fun FloatingTitleBar(
    title: String? = null,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: List<FloatingTitleBarAction> = emptyList(),
    visible: Boolean = true,
    onOverflowExpandedChange: (Boolean) -> Unit = {},
    windowInsets: WindowInsets = WindowInsets.safeDrawing.only(
        WindowInsetsSides.Horizontal + WindowInsetsSides.Top,
    ),
): Unit {
    if (title == null && onBack == null && actions.isEmpty()) return

    val dimensions = AndroidKitThemeTokens.dimensions
    val strings = AndroidKitThemeTokens.strings
    val hasButtons = onBack != null || actions.isNotEmpty()
    val titleTextShadowRadius = with(LocalDensity.current) {
        dimensions.floatingTitleTextShadowRadius.toPx()
    }
    var overflowExpanded by remember { mutableStateOf(false) }

    fun setOverflowExpanded(expanded: Boolean) {
        overflowExpanded = expanded
        onOverflowExpandedChange(expanded)
    }

    LaunchedEffect(visible) {
        if (!visible && overflowExpanded) setOverflowExpanded(false)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
            .heightIn(min = dimensions.floatingTitleBarHeight)
            .padding(
                horizontal = dimensions.spaceSmall,
                vertical = dimensions.floatingTitleBarVerticalPadding,
            ),
    ) {
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = if (hasButtons) dimensions.minimumTouchTarget else 0.dp,
                    ),
            ) {
                val directActionCount = directTitleBarActionCount(
                    actionCount = actions.size,
                    availableWidth = maxWidth,
                    hasTitle = title != null,
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
                    FloatingTitleBarBackButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart),
                    )
                }

                title?.let {
                    Text(
                        text = it,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth()
                            .padding(
                                start = titleStartPadding,
                                end = titleEndPadding,
                            )
                            .clearAndSetSemantics {
                                heading()
                                contentDescription = it
                            },
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.titleMedium.copy(
                            shadow = Shadow(
                                color = MaterialTheme.colorScheme.background.copy(
                                    alpha = FloatingTitleTextShadowAlpha,
                                ),
                                offset = Offset.Zero,
                                blurRadius = titleTextShadowRadius,
                            ),
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (endControlCount > 0) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        directActions.forEach { action ->
                            FloatingTitleBarActionButton(action = action)
                        }
                        if (overflowActions.isNotEmpty()) {
                            Box {
                                FloatingTitleBarActionButton(
                                    action = FloatingTitleBarAction(
                                        icon = Icons.Default.MoreVert,
                                        label = strings.more,
                                        onClick = {
                                            setOverflowExpanded(true)
                                        },
                                    ),
                                )
                                FloatingDropdownMenu(
                                    expanded = overflowExpanded,
                                    onDismissRequest = {
                                        setOverflowExpanded(false)
                                    },
                                    offset = DpOffset(
                                        x = 0.dp,
                                        y = -dimensions.spaceExtraSmall,
                                    ),
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
private fun FloatingTitleBarBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    FloatingSurfaceButton(
        onClick = onClick,
        shape = CircleShape,
        visualSize = dimensions.floatingTitleBarButtonSize,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = AndroidKitThemeTokens.strings.back,
            modifier = Modifier.size(dimensions.floatingActionIconSize),
        )
    }
}

@Composable
private fun FloatingTitleBarActionButton(
    action: FloatingTitleBarAction,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    FloatingSurfaceButton(
        onClick = action.onClick,
        shape = CircleShape,
        visualSize = dimensions.floatingTitleBarButtonSize,
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = action.label,
            modifier = Modifier.size(dimensions.floatingActionIconSize),
        )
    }
}

private fun directTitleBarActionCount(
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
    val minimumTitleWidth = if (hasTitle) dimensions.floatingTitleMinimumWidth else 0.dp
    val titleStartSpacing = if (hasTitle && hasNavigation) dimensions.spaceExtraSmall else 0.dp

    return (minOf(MaximumDirectTitleBarActions, actionCount) downTo 0).firstOrNull { directCount ->
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

private const val MaximumDirectTitleBarActions = 2
private const val FloatingTitleTextShadowAlpha = 0.85f
