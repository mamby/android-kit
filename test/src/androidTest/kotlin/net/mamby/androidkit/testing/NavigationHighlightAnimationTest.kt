package net.mamby.androidkit.testing

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigation
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigationItem
import net.mamby.androidkit.compose.navigation.rememberNavigationHighlightScale
import net.mamby.androidkit.compose.theme.AndroidKitFloatingSurfaceDefaults
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import net.mamby.androidkit.compose.theme.AndroidKitThemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NavigationHighlightAnimationTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private var firstSelected by mutableStateOf(true)
    private lateinit var first: Animatable<Float, AnimationVector1D>
    private lateinit var second: Animatable<Float, AnimationVector1D>

    private fun content() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            first = rememberNavigationHighlightScale(firstSelected)
            second = rememberNavigationHighlightScale(!firstSelected)
        }
        rule.waitForIdle()
    }

    private fun selectFirst(selected: Boolean) {
        rule.runOnIdle { firstSelected = selected }
        // Apply the selection and establish the animation's first frame.
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()
    }

    @Test fun highlightsEnterAndExitTogetherThenStaySettled() {
        content()
        rule.runOnIdle {
            assertEquals(1f, first.value, 0f)
            assertEquals(0f, second.value, 0f)
        }
        selectFirst(false)
        rule.mainClock.advanceTimeBy(450)
        rule.runOnIdle {
            assertTrue(first.value > 0f && first.value < 1f)
            assertTrue(second.value > 0f && second.value < 1f)
            assertEquals(1f, first.value + second.value, 0.001f)
        }
        rule.mainClock.advanceTimeBy(500)
        rule.runOnIdle {
            assertEquals(0f, first.value, 0f)
            assertEquals(1f, second.value, 0f)
        }
        rule.mainClock.advanceTimeBy(1_800)
        rule.runOnIdle {
            assertEquals(0f, first.value, 0f)
            assertEquals(1f, second.value, 0f)
        }
    }

    @Test fun interruptedSelectionContinuesFromCurrentScale() {
        content()
        selectFirst(false)
        rule.mainClock.advanceTimeBy(300)
        var before = 0f
        rule.runOnIdle { before = first.value }
        selectFirst(true)
        rule.runOnIdle {
            // Selection is applied on the next frame, so the outgoing animation
            // may advance once before cancellation. It must not jump to an endpoint.
            assertTrue(first.value > 0.4f && first.value < 0.65f)
            assertEquals(before, first.value, 0.06f)
        }
        rule.mainClock.advanceTimeBy(300)
        rule.runOnIdle {
            assertTrue(first.value > before)
            assertTrue(first.value < 1f)
        }
        rule.mainClock.advanceTimeBy(650)
        rule.runOnIdle {
            assertEquals(1f, first.value, 0f)
            assertEquals(0f, second.value, 0f)
        }
    }

    @Test fun repeatedSelectionDoesNotRestartAnimation() {
        content()
        selectFirst(false)
        rule.mainClock.advanceTimeBy(450)
        selectFirst(false)
        rule.mainClock.advanceTimeBy(500)
        rule.runOnIdle {
            assertEquals(0f, first.value, 0f)
            assertEquals(1f, second.value, 0f)
        }
    }

    @Test fun renderedHighlightsKeepGeometryAcrossLabelsRtlAndResize() {
        var selected by mutableStateOf(0)
        var labels by mutableStateOf(false)
        var rtl by mutableStateOf(false)
        var width by mutableStateOf(360.dp)
        val theme = AndroidKitThemes.Light.copy(
            floatingSurfaceOpacityLevel = AndroidKitFloatingSurfaceDefaults.MaximumOpacityLevel,
        )
        rule.mainClock.autoAdvance = false
        rule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowSize(DpSize(width, 800.dp))) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
                ) {
                    AndroidKitTheme(definition = theme) {
                        AndroidKitFloatingNavigation(
                            items = (0..4).map {
                                AndroidKitFloatingNavigationItem(
                                    it, "Destination $it", materialSymbol(R.drawable.ic_symbol_home),
                                )
                            },
                            selectedKey = selected,
                            onSelected = { selected = it },
                            showCompactLabels = labels,
                            compactVisibleDestinationCount = 2,
                        ) { Box(Modifier.fillMaxSize()) }
                    }
                }
            }
        }
        rule.mainClock.advanceTimeBy(1_000)
        val bar = rule.onNodeWithTag("androidKitFloatingNavigationBar")
        fun firstNode() = if (labels) {
            rule.onNodeWithText("Destination 0", useUnmergedTree = true)
        } else {
            rule.onNodeWithContentDescription("Destination 0", useUnmergedTree = true)
        }
        fun highlightedPixels(): Int {
            val pixels = bar.captureToImage().toPixelMap()
            val selectedColor = theme.colorScheme.secondaryContainer.toArgb()
            return (0 until pixels.height).sumOf { y ->
                (0 until pixels.width).count { x -> pixels[x, y].toArgb() == selectedColor }
            }
        }
        repeat(4) { configuration ->
            rule.runOnIdle {
                selected = 0
                labels = configuration % 2 != 0
                rtl = configuration >= 2
                width = if (configuration >= 2) 320.dp else 360.dp
            }
            rule.mainClock.advanceTimeBy(1_000)
            rule.waitForIdle()
            val bounds = firstNode().fetchSemanticsNode().boundsInRoot
            val fullPixels = highlightedPixels()
            assertTrue("Initial selection must be drawn", fullPixels > 0)
            rule.runOnIdle { selected = 1 }
            rule.mainClock.advanceTimeBy(330)
            rule.waitForIdle()
            assertEquals(bounds, firstNode().fetchSemanticsNode().boundsInRoot)
            val transitioningPixels = highlightedPixels()
            assertTrue("Highlights must shrink and grow within their original bounds", transitioningPixels in 1 until fullPixels)
            rule.mainClock.advanceTimeBy(1_000)
            assertTrue("Final highlight must reach full size", highlightedPixels() > transitioningPixels)
            // Both overflow destinations share the same More highlight.
            rule.runOnIdle { selected = 3 }
            rule.mainClock.advanceTimeBy(1_000)
            val morePixels = highlightedPixels()
            rule.runOnIdle { selected = 4 }
            rule.mainClock.advanceTimeBy(330)
            assertEquals(morePixels, highlightedPixels())
        }
    }
}
