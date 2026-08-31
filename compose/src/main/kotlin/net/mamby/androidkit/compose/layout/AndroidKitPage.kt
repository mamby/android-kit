package net.mamby.androidkit.compose.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.theme.AndroidKitPageStyle
import net.mamby.androidkit.compose.theme.AndroidKitPageTitleBarStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

@Immutable
public sealed interface AndroidKitPageActionItem

@Immutable
public class AndroidKitPageAction(
    public val icon: ImageVector,
    public val label: String,
    public val onClick: () -> Unit,
    public val enabled: Boolean = true,
) : AndroidKitPageActionItem

@Immutable
public data object AndroidKitPageActionSeparator : AndroidKitPageActionItem

@Composable
public fun AndroidKitPage(
    title: String? = null,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: List<AndroidKitPageActionItem> = emptyList(),
    titleBarImmersiveMode: Boolean = false,
    floatingActionButton: @Composable () -> Unit = {},
    style: AndroidKitPageStyle = AndroidKitThemeTokens.pageStyle,
    titleBarStyle: AndroidKitPageTitleBarStyle = AndroidKitThemeTokens.pageTitleBarStyle,
    contentWindowInsets: WindowInsets = androidKitContentWindowInsets(),
    floatingActionAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    floatingActionMargin: Dp = AndroidKitThemeTokens.dimensions.spaceMedium,
    applyImePadding: Boolean = true,
    topBar: (@Composable (visible: Boolean) -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
): Unit {
    var titleBarVisible by rememberSaveable(titleBarImmersiveMode) { mutableStateOf(true) }
    val dimensions = AndroidKitThemeTokens.dimensions
    val measuredContentInsets = contentWindowInsets
    val measuredContentPadding = measuredContentInsets.asPaddingValues()
    val statusBarClearance = measuredContentPadding.calculateTopPadding()
    val navigationBottomClearance = measuredContentPadding.calculateBottomPadding()
    val hasTitleBar = topBar != null ||
        title != null ||
        onBack != null ||
        actions.any { it is AndroidKitPageAction }
    AndroidKitPageLayout(
        modifier = modifier
            .toggleTitleBarOnUnconsumedTap(
                enabled = titleBarImmersiveMode && hasTitleBar,
                titleBarVisible = titleBarVisible,
                onToggleTitleBar = { titleBarVisible = !titleBarVisible },
            )
            .then(if (applyImePadding) Modifier.imePadding() else Modifier),
        contentWindowInsets = measuredContentInsets,
        floatingActionMargin = floatingActionMargin,
        floatingActionAlignment = floatingActionAlignment,
        floatingActionButton = floatingActionButton,
    ) { floatingActionHeight ->
        val floatingActionClearance = if (floatingActionHeight == 0.dp) {
            0.dp
        } else {
            floatingActionHeight + floatingActionMargin
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = style.containerColor,
            contentWindowInsets = measuredContentInsets.only(WindowInsetsSides.Horizontal),
            topBar = {
                if (hasTitleBar) {
                    if (topBar != null) {
                        topBar(titleBarVisible)
                    } else {
                        AndroidKitPageTitleBar(
                            title = title,
                            onBack = onBack,
                            actions = actions,
                            visible = titleBarVisible,
                            style = titleBarStyle,
                        )
                    }
                }
            },
            content = { contentPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarEdgeProtection(
                            statusBarInsets = WindowInsets.statusBars,
                            fadeLength = dimensions.contentProtectionFadeLength,
                            protectionColor = style.contentProtectionColor.takeIf {
                                it != Color.Unspecified
                            } ?: style.containerColor,
                        ),
                ) {
                    content(
                        contentPadding.withAdditionalPadding(
                            additionalTop = if (hasTitleBar) {
                                dimensions.spaceMedium
                            } else {
                                statusBarClearance
                            },
                            additionalBottom = navigationBottomClearance +
                                floatingActionClearance,
                        ),
                    )
                }
            },
        )
    }
}

@Composable
private fun AndroidKitPageLayout(
    contentWindowInsets: WindowInsets,
    floatingActionMargin: Dp,
    floatingActionAlignment: Alignment.Horizontal,
    floatingActionButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (floatingActionHeight: Dp) -> Unit,
): Unit {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val margin = floatingActionMargin.roundToPx()
        val leftInset = contentWindowInsets.getLeft(this, layoutDirection)
        val rightInset = contentWindowInsets.getRight(this, layoutDirection)
        val bottomInset = contentWindowInsets.getBottom(this)
        val floatingActionConstraints = constraints.copy(
            minWidth = 0,
            minHeight = 0,
            maxWidth = (constraints.maxWidth - leftInset - rightInset - margin * 2)
                .coerceAtLeast(0),
            maxHeight = (constraints.maxHeight - bottomInset - margin * 2)
                .coerceAtLeast(0),
        )
        val floatingActionPlaceables = subcompose(AndroidKitPageSlot.FloatingAction) {
            Box(
                modifier = Modifier.consumeWindowInsets(
                    contentWindowInsets.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                    ),
                ),
                contentAlignment = Alignment.Center,
            ) {
                floatingActionButton()
            }
        }.map { measurable -> measurable.measure(floatingActionConstraints) }
        val floatingActionHeight = floatingActionPlaceables.maxOfOrNull { it.height } ?: 0
        val contentPlaceables = subcompose(AndroidKitPageSlot.Content) {
            content(floatingActionHeight.toDp())
        }.map { measurable -> measurable.measure(constraints) }
        val width = maxOf(
            contentPlaceables.maxOfOrNull { it.width } ?: 0,
            floatingActionPlaceables.maxOfOrNull { it.width } ?: 0,
        ).coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = maxOf(
            contentPlaceables.maxOfOrNull { it.height } ?: 0,
            floatingActionHeight,
        ).coerceIn(constraints.minHeight, constraints.maxHeight)
        val availableFloatingActionWidth =
            (width - leftInset - rightInset - margin * 2).coerceAtLeast(0)
        layout(width, height) {
            contentPlaceables.forEach { it.placeRelative(0, 0) }
            floatingActionPlaceables.forEach { placeable ->
                placeable.place(
                    x = leftInset + margin + floatingActionAlignment.align(
                        size = placeable.width,
                        space = availableFloatingActionWidth,
                        layoutDirection = layoutDirection,
                    ),
                    y = (height - bottomInset - margin - placeable.height).coerceAtLeast(0),
                )
            }
        }
    }
}

private enum class AndroidKitPageSlot {
    Content,
    FloatingAction,
}

@Composable
private fun PaddingValues.withAdditionalPadding(
    additionalTop: Dp,
    additionalBottom: Dp,
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection),
        top = calculateTopPadding() + additionalTop,
        end = calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + additionalBottom,
    )
}
