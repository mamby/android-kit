package net.mamby.androidkit.compose.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitPageStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

/**
 * A title-free access-gate page. The host owns authentication, lock policy and navigation.
 * [isUnlocking] replaces the unlock action with progress; [errorMessage] supports retry feedback.
 * A null [icon] uses the Kit lock icon. All text is supplied and localized by the host.
 */
@Composable
public fun AndroidKitLockPage(
    message: String,
    unlockLabel: String,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
    isUnlocking: Boolean = false,
    errorMessage: String? = null,
    icon: ImageVector? = null,
    style: AndroidKitPageStyle = AndroidKitThemeTokens.pageStyle,
    contentWindowInsets: WindowInsets = androidKitContentWindowInsets(),
) {
    val dimensions = AndroidKitThemeTokens.dimensions
    val direction = LocalLayoutDirection.current
    AndroidKitPage(
        modifier = modifier,
        style = style,
        contentWindowInsets = contentWindowInsets,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = padding.calculateStartPadding(direction) + dimensions.screenPadding,
                top = padding.calculateTopPadding() + dimensions.spaceMedium,
                end = padding.calculateEndPadding(direction) + dimensions.screenPadding,
                bottom = padding.calculateBottomPadding() + dimensions.spaceMedium,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium, Alignment.CenterVertically),
        ) {
            item { Icon(imageVector = icon ?: AndroidKitIcons.AppLock, contentDescription = null) }
            item {
                Text(
                    text = message,
                    modifier = Modifier.widthIn(max = dimensions.contentMaxWidth),
                    textAlign = TextAlign.Center,
                )
            }
            errorMessage?.let { error ->
                item {
                    Text(
                        text = error,
                        modifier = Modifier
                            .widthIn(max = dimensions.contentMaxWidth)
                            .semantics { liveRegion = LiveRegionMode.Polite },
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            item {
                if (isUnlocking) {
                    CircularProgressIndicator()
                } else {
                    Button(onClick = onUnlock) { Text(unlockLabel) }
                }
            }
        }
    }
}
