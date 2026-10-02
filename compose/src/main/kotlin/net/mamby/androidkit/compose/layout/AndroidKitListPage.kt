package net.mamby.androidkit.compose.layout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import net.mamby.androidkit.compose.action.AndroidKitActionItem
import net.mamby.androidkit.compose.action.AndroidKitFloatingAction
import net.mamby.androidkit.compose.presentation.*
import net.mamby.androidkit.compose.theme.AndroidKitCardColors
import net.mamby.androidkit.compose.theme.AndroidKitCardStyle
import net.mamby.androidkit.compose.theme.AndroidKitPageColors
import net.mamby.androidkit.compose.theme.AndroidKitPageStyle
import net.mamby.androidkit.compose.theme.AndroidKitPageTitleBarColors
import net.mamby.androidkit.compose.theme.AndroidKitPageTitleBarStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.toColors
import net.mamby.androidkit.compose.theme.withColors

/**
 * A page with one Kit-owned lazy list. [supportPrompt] precedes [listContent] and scrolls with it.
 * Use stable keys for body items to retain their position when the prompt changes.
 * [contentPadding] adds body margins to the page's measured chrome/inset clearance exactly once.
 */
@Composable
public fun AndroidKitPage(
    listContent: LazyListScope.() -> Unit,
    title: String? = null,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: List<AndroidKitActionItem> = emptyList(),
    titleBarImmersiveMode: Boolean = false,
    floatingActionButton: AndroidKitFloatingAction? = null,
    colors: AndroidKitPageColors = AndroidKitPageColors(),
    titleBarColors: AndroidKitPageTitleBarColors = AndroidKitPageTitleBarColors(),
    contentWindowInsets: WindowInsets = androidKitContentWindowInsets(),
    floatingActionAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    applyImePadding: Boolean = true,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(horizontal = AndroidKitThemeTokens.componentTokens.listPage.horizontalPadding),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(AndroidKitThemeTokens.componentTokens.listPage.itemSpacing),
    supportPrompt: AndroidKitSupportPrompt? = null,
    supportCardColors: AndroidKitCardColors = AndroidKitCardColors(),
): Unit {
    AndroidKitPage(
        listContent = listContent,
        title = title,
        modifier = modifier,
        onBack = onBack,
        actions = actions,
        titleBarImmersiveMode = titleBarImmersiveMode,
        floatingActionButton = floatingActionButton,
        style = AndroidKitThemeTokens.pageStyle.withColors(colors),
        titleBarStyle = AndroidKitThemeTokens.pageTitleBarStyle.withColors(titleBarColors, AndroidKitThemeTokens.floatingSurfaceStyle),
        contentWindowInsets = contentWindowInsets,
        floatingActionAlignment = floatingActionAlignment,
        applyImePadding = applyImePadding,
        listState = listState,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        supportPrompt = supportPrompt,
        supportCardStyle = AndroidKitThemeTokens.componentTokens.supportPrompt.cardStyle.withColors(supportCardColors),
    )
}

@Composable
internal fun AndroidKitPage(
    listContent: LazyListScope.() -> Unit,
    title: String? = null,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: List<AndroidKitActionItem> = emptyList(),
    titleBarImmersiveMode: Boolean = false,
    floatingActionButton: AndroidKitFloatingAction? = null,
    style: AndroidKitPageStyle,
    titleBarStyle: AndroidKitPageTitleBarStyle = AndroidKitThemeTokens.pageTitleBarStyle,
    contentWindowInsets: WindowInsets = androidKitContentWindowInsets(),
    floatingActionAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    floatingActionMargin: Dp = AndroidKitThemeTokens.componentTokens.listPage.floatingActionMargin,
    applyImePadding: Boolean = true,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(horizontal = AndroidKitThemeTokens.componentTokens.listPage.horizontalPadding),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(AndroidKitThemeTokens.componentTokens.listPage.itemSpacing),
    supportPrompt: AndroidKitSupportPrompt? = null,
    supportCardStyle: AndroidKitCardStyle = AndroidKitThemeTokens.componentTokens.supportPrompt.cardStyle,
): Unit {
    val direction = LocalLayoutDirection.current
    var ended by rememberSaveable(supportPrompt?.id) { mutableStateOf(false) }
    var sheetOpen by rememberSaveable(supportPrompt?.id) { mutableStateOf(false) }
    val finish: (Boolean) -> Unit = { donate ->
        if (!ended && supportPrompt != null && (!donate || supportPrompt.enabled)) {
            ended = true
            sheetOpen = false
            if (donate) supportPrompt.onDonate() else supportPrompt.onDismiss()
        }
    }
    AndroidKitPage(
        title = title, modifier = modifier, onBack = onBack, actions = actions,
        titleBarImmersiveMode = titleBarImmersiveMode, floatingActionButton = floatingActionButton,
        colors = style.toColors(), titleBarColors = titleBarStyle.toColors(), contentWindowInsets = contentWindowInsets,
        floatingActionAlignment = floatingActionAlignment,
        applyImePadding = applyImePadding,
    ) { clearance ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(), state = listState,
            verticalArrangement = verticalArrangement,
            contentPadding = PaddingValues(
                start = clearance.calculateStartPadding(direction) + contentPadding.calculateStartPadding(direction),
                top = clearance.calculateTopPadding() + contentPadding.calculateTopPadding(),
                end = clearance.calculateEndPadding(direction) + contentPadding.calculateEndPadding(direction),
                bottom = clearance.calculateBottomPadding() + contentPadding.calculateBottomPadding(),
            ),
        ) {
            if (supportPrompt != null && !ended) {
                item(key = "androidkit:page:support:${supportPrompt.id}") {
                    SupportPromptCard(
                        enabled = supportPrompt.enabled, style = supportCardStyle,
                        onLearnMore = { sheetOpen = true }, onDismiss = { finish(false) },
                    )
                }
            }
            listContent()
        }
    }
    if (supportPrompt != null && !ended && sheetOpen) {
        SupportPromptSheet(
            enabled = supportPrompt.enabled, onDonate = { finish(true) }, onDismiss = { finish(false) },
        )
    }
}
