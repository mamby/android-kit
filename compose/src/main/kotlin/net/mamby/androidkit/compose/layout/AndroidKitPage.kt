package net.mamby.androidkit.compose.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

@Composable
public fun AndroidKitPage(
    title: String? = null,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: List<AndroidKitFloatingTitleBarAction> = emptyList(),
    titleBarImmersiveMode: Boolean = false,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
): Unit {
    var floatingActionHeightPx by remember { mutableIntStateOf(0) }
    var titleBarVisible by rememberSaveable(titleBarImmersiveMode) { mutableStateOf(true) }
    val floatingActionHeight = with(LocalDensity.current) { floatingActionHeightPx.toDp() }
    val colorScheme = AndroidKitThemeTokens.colorScheme
    val dimensions = AndroidKitThemeTokens.dimensions
    val measuredContentInsets = androidKitContentWindowInsets()
    val measuredContentPadding = measuredContentInsets.asPaddingValues()
    val statusBarClearance = measuredContentPadding.calculateTopPadding()
    val navigationBottomClearance = measuredContentPadding.calculateBottomPadding()
    val hasTitleBar = title != null || onBack != null || actions.isNotEmpty()
    val floatingActionClearance = if (floatingActionHeightPx == 0) {
        0.dp
    } else {
        floatingActionHeight + dimensions.spaceMedium
    }
    Box(
        modifier = modifier
            .toggleTitleBarOnUnconsumedTap(
                enabled = titleBarImmersiveMode && hasTitleBar,
                titleBarVisible = titleBarVisible,
                onToggleTitleBar = { titleBarVisible = !titleBarVisible },
            )
            .imePadding(),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = colorScheme.background,
            contentWindowInsets = measuredContentInsets.only(WindowInsetsSides.Horizontal),
            topBar = {
                if (hasTitleBar) {
                    AndroidKitFloatingTitleBar(
                        title = title,
                        onBack = onBack,
                        actions = actions,
                        visible = titleBarVisible,
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
                            protectionColor = colorScheme.background,
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
