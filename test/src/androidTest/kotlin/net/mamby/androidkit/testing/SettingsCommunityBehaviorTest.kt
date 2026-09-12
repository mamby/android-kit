package net.mamby.androidkit.testing

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import net.mamby.androidkit.compose.form.AndroidKitSettingsAbout
import net.mamby.androidkit.compose.form.AndroidKitSettingsAction
import net.mamby.androidkit.compose.form.AndroidKitSettingsGetInvolved
import net.mamby.androidkit.compose.form.AndroidKitSettingsPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsPageConfiguration
import net.mamby.androidkit.compose.form.AndroidKitSettingsSupport
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsCommunityBehaviorTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun mainFooterFollowsSettingsAndAboutReturnsToRetainedScrollPosition() {
        var aboutOpen by mutableStateOf(false)
        var supportClicks = 0
        var reportClicks = 0
        var sourceClicks = 0
        lateinit var mainList: LazyListState
        val about = aboutData(source = AndroidKitSettingsAction("Source code", { sourceClicks++ }))
        rule.setContent {
            SettingsTestViewport {
                val holder = rememberSaveableStateHolder()
                holder.SaveableStateProvider(aboutOpen) {
                    if (aboutOpen) {
                        AndroidKitSettingsPage(
                            configuration = AndroidKitSettingsPageConfiguration.About(about),
                            onBack = { aboutOpen = false },
                        )
                    } else {
                        mainList = rememberLazyListState()
                        AndroidKitSettingsPage(
                            configuration = AndroidKitSettingsPageConfiguration.Main(
                                support = AndroidKitSettingsSupport("Support this work", "Fund maintenance", AndroidKitSettingsAction("Support", { supportClicks++ })),
                                getInvolved = AndroidKitSettingsGetInvolved("Get involved", reportIssue = AndroidKitSettingsAction("Report issue", { reportClicks++ })),
                                about = about, onAbout = { aboutOpen = true },
                            ),
                            listState = mainList,
                        ) {
                            repeat(20) { index -> section("setting-$index") { info("Setting $index") } }
                        }
                    }
                }
            }
        }
        fun scrollTo(label: String) {
            rule.onNode(hasScrollAction()).performScrollToNode(hasText(label))
            rule.onNodeWithText(label).assertIsDisplayed()
        }
        scrollTo("Setting 19")
        scrollTo("Support")
        rule.onNodeWithText("Support").performClick()
        scrollTo("Report issue")
        rule.onNodeWithText("Report issue").performClick()
        scrollTo("About")
        // All four fit in the controlled viewport at the end of the list.
        val positions = listOf("Setting 19", "Support this work", "Report issue", "About").map {
            rule.onNodeWithText(it).assertIsDisplayed()
            rule.onNodeWithText(it).fetchSemanticsNode().boundsInRoot.top
        }
        assertTrue(positions.zipWithNext().all { (first, second) -> first < second })
        var before = 0 to 0
        rule.runOnIdle { before = mainList.firstVisibleItemIndex to mainList.firstVisibleItemScrollOffset }
        rule.onNodeWithText("About").performClick()
        rule.onNodeWithText("Example app").assertIsDisplayed()
        rule.onNodeWithText("Support").assertDoesNotExist()
        rule.onNodeWithText("Report issue").assertDoesNotExist()
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Source code"))
        rule.onNodeWithText("Source code").performClick()
        rule.onNodeWithContentDescription("Back").performClick()
        rule.onNodeWithText("About").assertIsDisplayed()
        rule.runOnIdle {
            assertEquals(before, mainList.firstVisibleItemIndex to mainList.firstVisibleItemScrollOffset)
            assertEquals(1, supportClicks)
            assertEquals(1, reportClicks)
            assertEquals(1, sourceClicks)
        }
    }

    @Test
    fun aboutUpdatesVersionAndRemovesUnavailableLinksAndEmptySections() {
        var showSource by mutableStateOf(true)
        var version by mutableStateOf("1.0")
        rule.setContent {
            SettingsTestViewport {
                AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.About(
                    aboutData(source = if (showSource) AndroidKitSettingsAction("Source code", {}) else null).copy(version = version),
                ))
            }
        }
        rule.onNodeWithText("1.0").assertIsDisplayed()
        rule.onNodeWithText("Source code").assertIsDisplayed()
        rule.onNodeWithText("Information").assertDoesNotExist()
        rule.runOnIdle { version = "2.0"; showSource = false }
        rule.onNodeWithText("2.0").assertIsDisplayed()
        rule.onNodeWithText("1.0").assertDoesNotExist()
        rule.onNodeWithText("Source code").assertDoesNotExist()
        rule.onNodeWithText("Open source").assertDoesNotExist()
    }
}

@Composable
private fun SettingsTestViewport(content: @Composable () -> Unit) {
    AndroidKitTheme(content = content)
}

private fun aboutData(source: AndroidKitSettingsAction?) = AndroidKitSettingsAbout(
    title = "About", appName = "Example app", versionLabel = "Version", version = "1.0",
    openSourceTitle = "Open source", informationTitle = "Information", sourceCode = source,
)
