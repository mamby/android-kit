package net.mamby.androidkit.testing

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import net.mamby.androidkit.compose.action.FloatingActionBar
import net.mamby.androidkit.compose.action.FloatingButton
import net.mamby.androidkit.compose.form.SettingsItem
import net.mamby.androidkit.compose.layout.FloatingTitleBarAction
import net.mamby.androidkit.compose.layout.PageScaffold
import net.mamby.androidkit.compose.navigation.AndroidKitNavigationItem
import net.mamby.androidkit.compose.navigation.FloatingNavigation
import net.mamby.androidkit.compose.presentation.AndroidKitCard
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import net.mamby.androidkit.compose.theme.AndroidKitThemeDefinition
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.AndroidKitThemes
import net.mamby.androidkit.compose.theme.FloatingSurfaceStyle

@PreviewTest
@AdaptiveDeviceMatrix
@Composable
fun androidKitLightDeviceMatrix() {
    ScreenshotGallery(AndroidKitThemes.Light)
}

@PreviewTest
@AdaptiveDeviceMatrix
@Composable
fun androidKitDarkDeviceMatrix() {
    ScreenshotGallery(AndroidKitThemes.Dark)
}

@PreviewTest
@Preview(
    name = "Large font",
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.5f,
    showBackground = true,
)
@Composable
fun androidKitLargeFont() {
    ScreenshotGallery(AndroidKitThemes.Light)
}

@PreviewTest
@Preview(
    name = "Compact labels stress",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f,
    showBackground = true,
)
@Composable
fun androidKitCompactLabelsStress() {
    ScreenshotGallery(
        theme = AndroidKitThemes.Light,
        showCompactLabels = true,
    )
}

@PreviewTest
@Preview(name = "Floating title actions", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun androidKitFloatingTitleActions() {
    AndroidKitTheme(definition = AndroidKitThemes.Light) {
        PageScaffold(
            title = "A centered page title that must ellipsize",
            onBack = {},
            actions = listOf(
                FloatingTitleBarAction(Icons.Default.Edit, "Edit", {}),
                FloatingTitleBarAction(Icons.Default.Share, "Share", {}),
                FloatingTitleBarAction(Icons.Default.Delete, "Delete", {}),
            ),
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Title bar content protection")
            }
        }
    }
}

@PreviewTest
@Preview(
    name = "RTL Arabic",
    widthDp = 360,
    heightDp = 800,
    locale = "ar",
    showBackground = true,
)
@Composable
fun androidKitRtl() {
    ScreenshotGallery(AndroidKitThemes.Light)
}

@PreviewTest
@Preview(name = "Floating transparent", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun androidKitFloatingTransparent() {
    ScreenshotGallery(
        theme = AndroidKitThemes.Light,
        floatingSurfaceStyle = FloatingSurfaceStyle(opacity = 0f),
    )
}

@PreviewTest
@Preview(name = "Floating opaque", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun androidKitFloatingOpaque() {
    ScreenshotGallery(
        theme = AndroidKitThemes.Light,
        floatingSurfaceStyle = FloatingSurfaceStyle(opacity = 1f),
    )
}

@PreviewTest
@Preview(name = "Dark surface transparent", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun androidKitDarkSurfaceTransparent() {
    ScreenshotGallery(
        theme = AndroidKitThemes.Dark,
        floatingSurfaceStyle = FloatingSurfaceStyle(
            opacity = 0f,
        ),
    )
}

@PreviewTest
@Preview(name = "Dark surface default", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun androidKitDarkSurfaceDefault() {
    ScreenshotGallery(
        theme = AndroidKitThemes.Dark,
    )
}

@PreviewTest
@Preview(name = "Dark surface opaque", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun androidKitDarkSurfaceOpaque() {
    ScreenshotGallery(
        theme = AndroidKitThemes.Dark,
        floatingSurfaceStyle = FloatingSurfaceStyle(
            opacity = 1f,
        ),
    )
}

@Composable
private fun ScreenshotGallery(
    theme: AndroidKitThemeDefinition,
    floatingSurfaceStyle: FloatingSurfaceStyle = FloatingSurfaceStyle(),
    showCompactLabels: Boolean = false,
) {
    AndroidKitTheme(
        definition = theme,
        floatingSurfaceStyle = floatingSurfaceStyle,
    ) {
        FloatingNavigation(
            items = screenshotNavigationItems,
            selectedKey = "home",
            onSelected = {},
            showCompactLabels = showCompactLabels,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                PageScaffold(
                    title = "Android Kit",
                    floatingActionButton = {
                        FloatingButton(onClick = {}) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    },
                ) { pagePadding ->
                    val dimensions = AndroidKitThemeTokens.dimensions
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = dimensions.screenPadding),
                        contentPadding = pagePadding,
                        verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
                    ) {
                        item {
                            AndroidKitCard(
                                modifier = Modifier.fillMaxWidth(),
                                header = {
                                    Text(
                                        text = "Actions",
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(
                                        text = "Opinionated Material 3 defaults",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                            ) {
                                Button(onClick = {}) { Text("Primary action") }
                                SettingsItem(
                                    title = "Encrypted backups",
                                    supportingText = "Stored on this device",
                                    onClick = {},
                                    trailingContent = {
                                        Icon(Icons.Default.Settings, contentDescription = null)
                                    },
                                )
                            }
                        }
                        item {
                            AndroidKitCard(
                                modifier = Modifier.fillMaxWidth(),
                                header = {
                                    Text(
                                        text = "Floating action bar",
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                },
                            ) {
                                FloatingActionBar {
                                    icon(
                                        onClick = {},
                                        icon = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                    )
                                    text(onClick = {}, label = "Save")
                                }
                            }
                        }
                        item {
                            AndroidKitCard(
                                modifier = Modifier.fillMaxWidth(),
                                header = {
                                    Text(
                                        text = "Scrollable content",
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                },
                            ) {
                                Text(
                                    "A muted block verifies scrolling across the adaptive device matrix.\n\n" +
                                        "Floating controls remain anchored while page content moves.",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val screenshotNavigationItems = listOf(
    AndroidKitNavigationItem("home", "Home", Icons.Default.Home),
    AndroidKitNavigationItem("list", "Lists", Icons.AutoMirrored.Filled.List),
    AndroidKitNavigationItem("edit", "Editor", Icons.Default.Edit),
    AndroidKitNavigationItem("language", "Language", Icons.Default.Language),
    AndroidKitNavigationItem("settings", "Settings", Icons.Default.Settings),
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@Preview(name = "Small phone portrait", widthDp = 320, heightDp = 640, showBackground = true)
@Preview(name = "Phone portrait", widthDp = 360, heightDp = 800, showBackground = true)
@Preview(name = "Phone landscape", widthDp = 800, heightDp = 360, showBackground = true)
@Preview(name = "Foldable folded", widthDp = 420, heightDp = 900, showBackground = true)
@Preview(name = "Foldable unfolded", widthDp = 841, heightDp = 900, showBackground = true)
@Preview(name = "Tablet portrait", widthDp = 800, heightDp = 1_280, showBackground = true)
@Preview(name = "Tablet landscape", widthDp = 1_280, heightDp = 800, showBackground = true)
@Preview(name = "Desktop", widthDp = 1_440, heightDp = 900, showBackground = true)
@Preview(
    name = "Desktop dark system",
    widthDp = 1_920,
    heightDp = 1_080,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
private annotation class AdaptiveDeviceMatrix
