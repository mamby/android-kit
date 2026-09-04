package net.mamby.androidkit.testing

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import net.mamby.androidkit.compose.action.AndroidKitAction
import net.mamby.androidkit.compose.action.AndroidKitFloatingActionBar
import net.mamby.androidkit.compose.action.AndroidKitFloatingActionButton
import net.mamby.androidkit.compose.form.AndroidKitBottomSheet
import net.mamby.androidkit.compose.form.AndroidKitSettingSection
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigation
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigationItem
import net.mamby.androidkit.compose.presentation.AndroidKitCard
import net.mamby.androidkit.compose.theme.AndroidKitFloatingSurfaceDefaults
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import net.mamby.androidkit.compose.theme.AndroidKitThemeDefinition
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.AndroidKitThemes

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
        AndroidKitPage(
            title = "A centered page title that must ellipsize",
            onBack = {},
            actions = listOf(
                AndroidKitAction(
                    materialSymbol(R.drawable.ic_symbol_edit),
                    "Edit",
                    {},
                ),
                AndroidKitAction(
                    materialSymbol(R.drawable.ic_symbol_share),
                    "Share",
                    {},
                ),
                AndroidKitAction(
                    materialSymbol(R.drawable.ic_symbol_delete),
                    "Delete",
                    {},
                ),
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
@Preview(name = "Bottom sheet actions", widthDp = 360, heightDp = 800, showBackground = true)
@Preview(
    name = "Bottom sheet actions RTL",
    widthDp = 360,
    heightDp = 800,
    locale = "ar",
    showBackground = true,
)
@Composable
fun androidKitBottomSheetHeaderActions() {
    AndroidKitTheme(definition = AndroidKitThemes.Light) {
        AndroidKitBottomSheet(
            visible = true,
            title = "Sheet actions",
            onDismiss = {},
            actions = listOf(
                AndroidKitAction(
                    materialSymbol(R.drawable.ic_symbol_edit),
                    "Edit",
                    {},
                ),
                AndroidKitAction(
                    materialSymbol(R.drawable.ic_symbol_share),
                    "Share",
                    {},
                ),
                AndroidKitAction(
                    materialSymbol(R.drawable.ic_symbol_delete),
                    "Delete",
                    {},
                ),
            ),
            fitContent = true,
        ) {
            Text("Actions remain flat beside the close button.")
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
@Preview(name = "Floating minimum opacity", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun androidKitFloatingMinimumOpacity() {
    ScreenshotGallery(
        theme = AndroidKitThemes.Light,
        floatingSurfaceOpacityLevel = AndroidKitFloatingSurfaceDefaults.MinimumOpacityLevel,
    )
}

@PreviewTest
@Preview(name = "Floating opaque", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun androidKitFloatingOpaque() {
    ScreenshotGallery(
        theme = AndroidKitThemes.Light,
        floatingSurfaceOpacityLevel = AndroidKitFloatingSurfaceDefaults.MaximumOpacityLevel,
    )
}

@PreviewTest
@Preview(name = "Dark surface minimum opacity", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun androidKitDarkSurfaceMinimumOpacity() {
    ScreenshotGallery(
        theme = AndroidKitThemes.Dark,
        floatingSurfaceOpacityLevel = AndroidKitFloatingSurfaceDefaults.MinimumOpacityLevel,
    )
}

@PreviewTest
@Preview(name = "Dark surface opaque", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun androidKitDarkSurfaceOpaque() {
    ScreenshotGallery(
        theme = AndroidKitThemes.Dark,
        floatingSurfaceOpacityLevel = AndroidKitFloatingSurfaceDefaults.MaximumOpacityLevel,
    )
}

@Composable
private fun ScreenshotGallery(
    theme: AndroidKitThemeDefinition,
    floatingSurfaceOpacityLevel: Float = theme.floatingSurfaceOpacityLevel,
    showCompactLabels: Boolean = false,
) {
    val addIcon = materialSymbol(R.drawable.ic_symbol_add)
    val settingsIcon = materialSymbol(R.drawable.ic_symbol_settings)
    val editIcon = materialSymbol(R.drawable.ic_symbol_edit)
    AndroidKitTheme(
        definition = theme.copy(floatingSurfaceOpacityLevel = floatingSurfaceOpacityLevel),
    ) {
        AndroidKitFloatingNavigation(
            items = screenshotNavigationItems(),
            selectedKey = "home",
            onSelected = {},
            showCompactLabels = showCompactLabels,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidKitPage(
                    title = "Android Kit",
                    floatingActionButton = {
                        AndroidKitFloatingActionButton(onClick = {}) {
                            Icon(
                                addIcon,
                                contentDescription = "Add",
                            )
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
                            AndroidKitSettingSection(
                                label = "Actions",
                                description = "Opinionated Material 3 defaults",
                            ) {
                                button(label = "Primary action", onClick = {})
                                toggle(
                                    label = "Encrypted backups",
                                    checked = true,
                                    onCheckedChange = {},
                                    supportingText = "Stored on this device",
                                    icon = settingsIcon,
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
                                AndroidKitFloatingActionBar {
                                    icon(
                                        onClick = {},
                                        icon = editIcon,
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

@Composable
private fun screenshotNavigationItems() = listOf(
    AndroidKitFloatingNavigationItem(
        "home",
        "Home",
        materialSymbol(R.drawable.ic_symbol_home),
    ),
    AndroidKitFloatingNavigationItem(
        "list",
        "Lists",
        materialSymbol(R.drawable.ic_symbol_list),
    ),
    AndroidKitFloatingNavigationItem(
        "edit",
        "Editor",
        materialSymbol(R.drawable.ic_symbol_edit),
    ),
    AndroidKitFloatingNavigationItem(
        "language",
        "Language",
        materialSymbol(R.drawable.ic_symbol_language),
    ),
    AndroidKitFloatingNavigationItem(
        "settings",
        "Settings",
        materialSymbol(R.drawable.ic_symbol_settings),
    ),
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
