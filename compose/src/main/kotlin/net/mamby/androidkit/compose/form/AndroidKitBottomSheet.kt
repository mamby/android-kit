package net.mamby.androidkit.compose.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.mamby.androidkit.compose.layout.AndroidKitFloatingTitleBar
import net.mamby.androidkit.compose.layout.AndroidKitFloatingTitleBarAction
import net.mamby.androidkit.compose.layout.toggleTitleBarOnUnconsumedTap
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun AndroidKitBottomSheet(
    title: String? = null,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: List<AndroidKitFloatingTitleBarAction> = emptyList(),
    titleBarImmersiveMode: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val strings = AndroidKitThemeTokens.strings
    var titleBarVisible by rememberSaveable(titleBarImmersiveMode) { mutableStateOf(true) }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        contentWindowInsets = {
            WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .toggleTitleBarOnUnconsumedTap(
                    enabled = titleBarImmersiveMode,
                    titleBarVisible = titleBarVisible,
                    onTitleBarVisibilityChange = { titleBarVisible = it },
                ),
        ) {
            AndroidKitFloatingTitleBar(
                title = title,
                onBack = onBack,
                actions = listOf(
                    AndroidKitFloatingTitleBarAction(
                        icon = Icons.Default.Close,
                        label = strings.close,
                        onClick = onDismissRequest,
                    ),
                ) + actions,
                visible = titleBarVisible,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = dimensions.screenPadding)
                    .padding(bottom = dimensions.spaceExtraLarge),
                verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
            ) {
                content()
            }
        }
    }
}
