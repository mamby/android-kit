package net.mamby.androidkit.testing

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.NavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import kotlinx.serialization.Serializable
import net.mamby.androidkit.navigation3.AndroidKitNavDisplay
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ImmediatePageNavigationTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun pageMotionMetadataCannotOverrideImmediateForwardBackOrPredictiveBack() {
        val dispatcher = NavigationEventDispatcher()
        val owner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher = dispatcher
        }
        val input = PageBackInput()
        lateinit var stack: NavBackStack<NavKey>
        rule.mainClock.autoAdvance = false
        rule.runOnUiThread { dispatcher.addInput(input) }
        rule.setContent {
            CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner) {
                TestKitTheme {
                    stack = rememberNavBackStack(ImmediateRoot)
                    val motionMetadata = remember {
                        metadata {
                            put(NavDisplay.TransitionKey) {
                                fadeIn(tween(700)) togetherWith fadeOut(tween(700))
                            }
                            put(NavDisplay.PopTransitionKey) {
                                fadeIn(tween(700)) togetherWith fadeOut(tween(700))
                            }
                            put(NavDisplay.PredictivePopTransitionKey) {
                                fadeIn(tween(700)) togetherWith slideOutHorizontally { it }
                            }
                        }
                    }
                    AndroidKitNavDisplay(
                        backStack = stack,
                        onBack = { stack.removeLastOrNull() },
                        modifier = Modifier.fillMaxSize().testTag("pageContainer"),
                        entryProvider = entryProvider {
                            entry<ImmediateRoot>(metadata = motionMetadata) {
                                Box(Modifier.fillMaxSize().background(Color.Blue)) { Text("Root page") }
                            }
                            entry<ImmediateDetail>(metadata = motionMetadata) {
                                Box(Modifier.fillMaxSize().background(Color.Red).testTag("detailPage")) { Text("Detail page") }
                            }
                        },
                    )
                }
            }
        }
        rule.runOnIdle { stack.add(ImmediateDetail) }
        rule.mainClock.advanceTimeBy(64)
        rule.onNodeWithText("Detail page").assertIsDisplayed()
        rule.onNodeWithText("Root page").assertDoesNotExist()
        val detailBounds = rule.onNodeWithTag("detailPage").fetchSemanticsNode().boundsInRoot
        fun assertCurrentPageIsUnchanged() {
            assertEquals(detailBounds, rule.onNodeWithTag("detailPage").fetchSemanticsNode().boundsInRoot)
            val pixels = rule.onNodeWithTag("pageContainer").captureToImage().toPixelMap()
            assertEquals(Color.Red, pixels[pixels.width / 2, pixels.height / 2])
        }
        rule.runOnIdle {
            input.start()
            input.progress(0.5f)
        }
        rule.mainClock.advanceTimeBy(64)
        assertCurrentPageIsUnchanged()
        rule.runOnIdle { input.cancel() }
        rule.mainClock.advanceTimeBy(64)
        assertCurrentPageIsUnchanged()
        rule.runOnIdle {
            assertEquals(ImmediateDetail, stack.last())
            input.start()
            input.progress(0.9f)
        }
        rule.mainClock.advanceTimeBy(64)
        assertCurrentPageIsUnchanged()
        rule.runOnIdle { input.complete() }
        rule.mainClock.advanceTimeBy(64)
        rule.onNodeWithText("Root page").assertIsDisplayed()
        rule.onNodeWithText("Detail page").assertDoesNotExist()
        rule.runOnIdle { stack.add(ImmediateDetail) }
        rule.mainClock.advanceTimeBy(64)
        rule.runOnIdle { stack.removeLastOrNull() }
        rule.mainClock.advanceTimeBy(64)
        rule.onNodeWithText("Root page").assertIsDisplayed()
        rule.onNodeWithText("Detail page").assertDoesNotExist()
        rule.runOnUiThread { dispatcher.removeInput(input) }
    }
}

private class PageBackInput : NavigationEventInput() {
    fun start() = dispatchOnBackStarted(NavigationEvent(swipeEdge = NavigationEvent.EDGE_LEFT))
    fun progress(fraction: Float) = dispatchOnBackProgressed(NavigationEvent(swipeEdge = NavigationEvent.EDGE_LEFT, progress = fraction))
    fun cancel() = dispatchOnBackCancelled()
    fun complete() = dispatchOnBackCompleted()
}

@Serializable private data object ImmediateRoot : NavKey
@Serializable private data object ImmediateDetail : NavKey
