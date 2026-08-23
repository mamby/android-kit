package net.mamby.androidkit.compose.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import net.mamby.androidkit.compose.layout.FloatingTitleBar
import net.mamby.androidkit.compose.layout.FloatingTitleBarAction
import net.mamby.androidkit.compose.layout.toggleTitleBarOnUnconsumedTap
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

@Composable
public fun SettingsItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = dimensions.spaceSmall),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent?.invoke()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall),
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        trailingContent?.invoke()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun AndroidKitModalSheet(
    title: String? = null,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: List<FloatingTitleBarAction> = emptyList(),
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
            FloatingTitleBar(
                title = title,
                onBack = onBack,
                actions = listOf(
                    FloatingTitleBarAction(
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
