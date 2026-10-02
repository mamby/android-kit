package net.mamby.androidkit.testing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.mamby.androidkit.compose.presentation.AndroidKitCard
import net.mamby.androidkit.compose.theme.AndroidKitCardColors
import net.mamby.androidkit.compose.theme.AndroidKitThemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ThemeAppearanceBehaviorTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun nestedHostThemeChangesBodyTypographyWithoutChangingKitCardChrome() {
        var customTheme by mutableStateOf(false)
        var bodyFontSize = 0.sp
        val hostTypography = Typography(bodyLarge = TextStyle(fontSize = 40.sp), titleMedium = TextStyle(fontSize = 48.sp))
        val hostShapes = Shapes(extraLarge = RoundedCornerShape(0.dp))
        rule.setContent {
            TestKitTheme(AndroidKitThemes.Light) {
                @Composable
                fun CardContent() {
                    AndroidKitCard(title = "Kit title", modifier = Modifier.width(280.dp).testTag("card")) {
                        val actualFontSize = LocalTextStyle.current.fontSize
                        SideEffect { bodyFontSize = actualFontSize }
                    }
                }
                if (customTheme) {
                    MaterialTheme(typography = hostTypography, shapes = hostShapes) { CardContent() }
                } else {
                    CardContent()
                }
            }
        }
        val before = rule.onNodeWithTag("card").captureToImage().toPixelMap()
        rule.runOnIdle { customTheme = true }
        val after = rule.onNodeWithTag("card").captureToImage().toPixelMap()
        rule.runOnIdle { assertEquals(40.sp, bodyFontSize) }
        assertEquals(before.width, after.width)
        assertEquals(before.height, after.height)
        assertTrue((0 until before.height).all { y -> (0 until before.width).all { x -> before[x, y] == after[x, y] } })
    }

    @Test
    fun cardUpdatesPaletteAndHonorsExplicitInstanceColors() {
        var paletteSurface by mutableStateOf(Color.Red)
        var instanceColor by mutableStateOf(Color.Unspecified)
        rule.setContent {
            val definition = AndroidKitThemes.Light.copy(
                colorScheme = AndroidKitThemes.Light.colorScheme.copy(surface = paletteSurface),
            )
            TestKitTheme(definition) {
                AndroidKitCard(colors = AndroidKitCardColors(containerColor = instanceColor), modifier = Modifier.width(200.dp).testTag("card")) {
                    Box(Modifier.height(60.dp))
                }
            }
        }
        fun renderedCenter(): Color {
            val pixels = rule.onNodeWithTag("card").captureToImage().toPixelMap()
            return pixels[pixels.width / 2, pixels.height / 2]
        }
        assertEquals(Color.Red, renderedCenter())
        rule.runOnIdle { paletteSurface = Color.Blue }
        assertEquals(Color.Blue, renderedCenter())
        rule.runOnIdle { instanceColor = Color.Green }
        assertEquals(Color.Green, renderedCenter())
        rule.runOnIdle { paletteSurface = Color.Yellow }
        assertEquals(Color.Green, renderedCenter())
    }
}
