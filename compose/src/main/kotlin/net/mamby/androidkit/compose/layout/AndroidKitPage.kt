package net.mamby.androidkit.compose.layout

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.action.AndroidKitAction
import net.mamby.androidkit.compose.action.AndroidKitFloatingAction
import net.mamby.androidkit.compose.action.AndroidKitIconAndLabelAction
import net.mamby.androidkit.compose.action.RenderFloatingAction
import net.mamby.androidkit.compose.action.AndroidKitActionItem
import net.mamby.androidkit.compose.action.AndroidKitActionSeparator
import net.mamby.androidkit.compose.action.AndroidKitTextAction
import net.mamby.androidkit.compose.action.isAndroidKitAction
import net.mamby.androidkit.compose.theme.AndroidKitPageStyle
import net.mamby.androidkit.compose.theme.AndroidKitPageTitleBarStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

public typealias AndroidKitPageActionItem = AndroidKitActionItem

public typealias AndroidKitPageAction = AndroidKitAction

public typealias AndroidKitPageTextAction = AndroidKitTextAction

public typealias AndroidKitPageIconAndLabelAction = AndroidKitIconAndLabelAction

public typealias AndroidKitPageActionSeparator = AndroidKitActionSeparator

/**
 * Displays a page whose content viewport is always edge-to-edge behind its title bar and system
 * bars. Apply the provided [PaddingValues] to a scrollable component's `contentPadding`, not its
 * [Modifier], so items remain unobscured without shrinking the viewport.
 */
@Composable
public fun AndroidKitPage(
    title: String? = null,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: List<AndroidKitActionItem> = emptyList(),
    titleBarImmersiveMode: Boolean = false,
    floatingActionButton: AndroidKitFloatingAction? = null,
    style: AndroidKitPageStyle = AndroidKitThemeTokens.pageStyle,
    titleBarStyle: AndroidKitPageTitleBarStyle = AndroidKitThemeTokens.pageTitleBarStyle,
    contentWindowInsets: WindowInsets = androidKitContentWindowInsets(),
    floatingActionAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    floatingActionMargin: Dp = AndroidKitThemeTokens.componentTokens.page.floatingActionMargin,
    applyImePadding: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
): Unit {
    val tokens = AndroidKitThemeTokens.componentTokens.page
    var titleBarVisible by rememberSaveable(titleBarImmersiveMode) { mutableStateOf(true) }
    val dimensions = AndroidKitThemeTokens.dimensions
    val measuredContentInsets = contentWindowInsets
    val measuredContentPadding = measuredContentInsets.asPaddingValues()
    val hasTitleBar = title != null ||
        onBack != null ||
        actions.any { it.isAndroidKitAction }
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
        floatingActionButton = { RenderFloatingAction(floatingActionButton) },
    ) { floatingActionPadding ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = style.containerColor,
            contentWindowInsets = measuredContentInsets.only(WindowInsetsSides.Horizontal),
            topBar = {
                if (hasTitleBar) {
                        AndroidKitPageTitleBar(
                            title = title,
                            onBack = onBack,
                            actions = actions,
                            visible = titleBarVisible,
                            style = titleBarStyle,
                        )
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
                        contentPadding.withContentClearance(
                            windowInsetsPadding = measuredContentPadding,
                            additionalTop = if (hasTitleBar) {
                                tokens.titleContentSpacing
                            } else {
                                dimensions.pageTitlelessTopPadding
                            },
                            includeTopInset = !hasTitleBar,
                            floatingActionPadding = floatingActionPadding,
                        ),
                    )
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AndroidKitPageLayout(
    contentWindowInsets: WindowInsets,
    floatingActionMargin: Dp,
    floatingActionAlignment: Alignment.Horizontal,
    floatingActionButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (floatingActionPadding: PaddingValues) -> Unit,
): Unit {
    val floatingActionInsets = remember { MutableWindowInsets() }
    // Stable slots and deferred padding reads keep animated viewport constraints out of
    // composition. Clearance updates during measurement, before the body is measured.
    val floatingActionSlot: @Composable () -> Unit = {
        Box(
            modifier = Modifier.consumeWindowInsets(
                contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            ),
            contentAlignment = Alignment.Center,
        ) {
            floatingActionButton()
        }
    }
    val contentSlot: @Composable () -> Unit = { content(floatingActionInsets.asPaddingValues()) }
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
        val floatingActionPlaceables = subcompose(AndroidKitPageSlot.FloatingAction, floatingActionSlot)
            .map { measurable -> measurable.measure(floatingActionConstraints) }
        val measuredFloatingActionHeight = floatingActionPlaceables.maxOfOrNull { it.height } ?: 0
        floatingActionInsets.insets = WindowInsets(
            bottom = if (measuredFloatingActionHeight == 0) 0 else measuredFloatingActionHeight + margin,
        )
        val contentPlaceables = subcompose(AndroidKitPageSlot.Content, contentSlot)
            .map { measurable -> measurable.measure(constraints) }
        val width = maxOf(
            contentPlaceables.maxOfOrNull { it.width } ?: 0,
            floatingActionPlaceables.maxOfOrNull { it.width } ?: 0,
        ).coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = maxOf(
            contentPlaceables.maxOfOrNull { it.height } ?: 0,
            measuredFloatingActionHeight,
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
private fun PaddingValues.withContentClearance(
    windowInsetsPadding: PaddingValues,
    additionalTop: Dp,
    includeTopInset: Boolean,
    floatingActionPadding: PaddingValues,
): PaddingValues = remember(this, windowInsetsPadding, additionalTop, includeTopInset, floatingActionPadding) {
    val scaffoldPadding = this
    object : PaddingValues {
        override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
            scaffoldPadding.calculateLeftPadding(layoutDirection)

        override fun calculateTopPadding(): Dp = scaffoldPadding.calculateTopPadding() +
            additionalTop + if (includeTopInset) windowInsetsPadding.calculateTopPadding() else 0.dp

        override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
            scaffoldPadding.calculateRightPadding(layoutDirection)

        override fun calculateBottomPadding(): Dp = scaffoldPadding.calculateBottomPadding() +
            windowInsetsPadding.calculateBottomPadding() + floatingActionPadding.calculateBottomPadding()
    }
}
