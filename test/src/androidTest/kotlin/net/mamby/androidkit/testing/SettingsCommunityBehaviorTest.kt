package net.mamby.androidkit.testing

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
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
    fun mainFooterIncludesAboutContent() {
        var supportClicks = 0
        var reportClicks = 0
        var sourceClicks = 0
        val about = aboutData(source = AndroidKitSettingsAction("Source code", { sourceClicks++ }))
        rule.setContent {
            SettingsTestViewport {
                AndroidKitSettingsPage(
                    configuration = AndroidKitSettingsPageConfiguration.Main(
                        support = AndroidKitSettingsSupport(
                            action = AndroidKitSettingsAction("Support", { supportClicks++ }),
                        ),
                        getInvolved = AndroidKitSettingsGetInvolved(
                            reportIssue = AndroidKitSettingsAction("Report issue", { reportClicks++ }),
                        ),
                        about = about,
                    ),
                ) {
                    repeat(20) { index -> section("setting-$index") { info("Setting $index") } }
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
        scrollTo("Example app")
        val positions = listOf("Setting 19", "Support", "Report issue", "Example app").map {
            rule.onNodeWithText(it).assertIsDisplayed()
            rule.onNodeWithText(it).fetchSemanticsNode().boundsInRoot.top
        }
        assertTrue(positions.zipWithNext().all { (first, second) -> first < second })
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Source code"))
        rule.onNodeWithText("Source code").performClick()
        rule.runOnIdle {
            assertEquals(1, supportClicks)
            assertEquals(1, reportClicks)
            assertEquals(1, sourceClicks)
        }
        rule.onNodeWithText("About").assertDoesNotExist()
    }

    @Test
    fun aboutUpdatesVersionAndRemovesUnavailableLinksAndEmptySections() {
        var showSource by mutableStateOf(true)
        var version by mutableStateOf("1.0")
        rule.setContent {
            SettingsTestViewport {
                AndroidKitSettingsPage(
                    configuration = AndroidKitSettingsPageConfiguration.Main(
                        support = AndroidKitSettingsSupport(
                            action = AndroidKitSettingsAction("Support", {}),
                        ),
                        getInvolved = AndroidKitSettingsGetInvolved(
                            reportIssue = AndroidKitSettingsAction("Report issue", {}),
                        ),
                        about = aboutData(
                            source = if (showSource) AndroidKitSettingsAction("Source code", {}) else null,
                        ).copy(version = version),
                    ),
                )
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
    appName = "Example app", version = "1.0", sourceCode = source,
)
