package net.mamby.androidkit.compose.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurfaceButton

@Composable
public fun PageScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: List<FloatingTitleBarAction> = emptyList(),
    titleBarAutoHide: Boolean = false,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
): Unit {
    var floatingActionHeightPx by remember { mutableIntStateOf(0) }
    var titleBarOverflowExpanded by remember { mutableStateOf(false) }
    val bottomOverlayKeys = remember { mutableStateMapOf<Any, Unit>() }
    val updateBottomOverlayProtection: (Any, Boolean) -> Unit = remember {
        { key, expanded ->
            if (expanded) {
                bottomOverlayKeys[key] = Unit
            } else {
                bottomOverlayKeys.remove(key)
            }
        }
    }
    val floatingActionHeight = with(LocalDensity.current) { floatingActionHeightPx.toDp() }
    val dimensions = AndroidKitThemeTokens.dimensions
    val measuredContentInsets = androidKitContentWindowInsets()
    val measuredContentPadding = measuredContentInsets.asPaddingValues()
    val navigationBottomClearance = measuredContentPadding.calculateBottomPadding()
    val navigationFlyoutClearance = LocalAndroidKitNavigationOverlayProtection.current
    val bottomOverlayClearance = maxOf(
        navigationFlyoutClearance,
        if (bottomOverlayKeys.isNotEmpty()) {
            dimensions.navigationFlyoutProtectionHeight
        } else {
            0.dp
        },
    )
    val floatingActionClearance = if (floatingActionHeightPx == 0) {
        0.dp
    } else {
        floatingActionHeight + dimensions.spaceMedium
    }
    val pageColorScheme = MaterialTheme.colorScheme.copy(
        background = PageNeutral50,
        onBackground = PageNeutral900,
        surface = Color.White,
        onSurface = PageNeutral900,
        surfaceVariant = PageNeutral100,
        onSurfaceVariant = PageNeutral600,
        outline = PageNeutral500,
        outlineVariant = PageNeutral200,
    )
    MaterialTheme(
        colorScheme = pageColorScheme,
    ) {
        CompositionLocalProvider(
            LocalAndroidKitBottomOverlayProtection provides updateBottomOverlayProtection,
        ) {
            Box(
                modifier = modifier.imePadding(),
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = measuredContentInsets.only(WindowInsetsSides.Horizontal),
                    topBar = {
                        FloatingTitleBar(
                            title = title,
                            onBack = onBack,
                            actions = actions,
                            autoHide = titleBarAutoHide,
                            onOverflowExpandedChange = { titleBarOverflowExpanded = it },
                        )
                    },
                    content = { contentPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .degradedEdgeProtection(
                                    topProtectedExtent = contentPadding.calculateTopPadding() +
                                        if (titleBarOverflowExpanded) {
                                            dimensions.navigationFlyoutProtectionHeight
                                        } else {
                                            0.dp
                                        },
                                    bottomProtectedExtent = navigationBottomClearance +
                                        bottomOverlayClearance +
                                        floatingActionClearance,
                                    fadeLength = dimensions.contentProtectionFadeLength,
                                    blurRadius = dimensions.contentProtectionBlurRadius,
                                    protectionColor = MaterialTheme.colorScheme.background,
                                ),
                        ) {
                            content(
                                contentPadding.withAdditionalBottomPadding(
                                    navigationBottomClearance + floatingActionClearance,
                                ),
                            )
                        }
                    },
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            measuredContentInsets.only(
                                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                            ),
                        )
                        .padding(dimensions.spaceMedium),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier.onSizeChanged { floatingActionHeightPx = it.height },
                        contentAlignment = Alignment.Center,
                    ) {
                        floatingActionButton()
                    }
                }
            }
        }
    }
}

@Composable
public fun AdaptiveGridPage(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    minimumCardWidth: Dp = AndroidKitThemeTokens.dimensions.cardMinWidth,
    content: LazyGridScope.() -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val layoutDirection = LocalLayoutDirection.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minimumCardWidth),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection) + dimensions.screenPadding,
            top = contentPadding.calculateTopPadding() + dimensions.spaceSmall,
            end = contentPadding.calculateEndPadding(layoutDirection) + dimensions.screenPadding,
            bottom = contentPadding.calculateBottomPadding() + dimensions.spaceExtraLarge,
        ),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
        verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
        content = content,
    )
}

@Composable
public fun DetailPage(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    maxWidth: Dp = AndroidKitThemeTokens.dimensions.detailMaxWidth,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val layoutDirection = LocalLayoutDirection.current
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = contentPadding.calculateStartPadding(layoutDirection) + dimensions.screenPadding,
                    top = contentPadding.calculateTopPadding() + dimensions.screenPadding,
                    end = contentPadding.calculateEndPadding(layoutDirection) + dimensions.screenPadding,
                    bottom = contentPadding.calculateBottomPadding() + dimensions.screenPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
            content = content,
        )
    }
}

@Composable
public fun PageFloatingAction(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    FloatingSurfaceButton(
        onClick = onClick,
        shape = CircleShape,
        visualSize = dimensions.floatingAddButtonSize,
        content = content,
    )
}

@Composable
private fun PaddingValues.withAdditionalBottomPadding(additionalBottom: Dp): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection),
        top = calculateTopPadding(),
        end = calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + additionalBottom,
    )
}

private val PageNeutral50 = Color(0xFFF7F7F7)
private val PageNeutral100 = Color(0xFFF5F5F5)
private val PageNeutral200 = Color(0xFFE2E2E2)
private val PageNeutral500 = Color(0xFF737373)
private val PageNeutral600 = Color(0xFF525252)
private val PageNeutral900 = Color(0xFF171717)
