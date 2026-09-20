package net.mamby.androidkit.testing

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
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
import net.mamby.androidkit.compose.form.AndroidKitSettingsLink
import net.mamby.androidkit.compose.form.AndroidKitSettingsPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsPageConfiguration
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsCommunityBehaviorTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun mainAlwaysShowsAboutLinksInOrder() {
        var contactClicks = 0
        var appInfoClicks = 0
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(
                    configuration = AndroidKitSettingsPageConfiguration.Main(
                        contact = AndroidKitSettingsLink(onClick = { contactClicks++ }),
                        appInfo = AndroidKitSettingsLink(onClick = { appInfoClicks++ }),
                    ),
                ) {
                    section("host") { info("Host setting") }
                }
            }
        }
        assertOrder("Host setting", "About", "Contact", "App info")
        for (label in listOf("Contact", "App info")) {
            scrollTo(label)
            rule.onNodeWithText(label).performClick()
        }
        rule.runOnIdle {
            assertEquals(1, contactClicks)
            assertEquals(1, appInfoClicks)
        }
        scrollTo("About")
        scrollTo("Contact")
        scrollTo("App info")
        rule.onNodeWithText("Version").assertDoesNotExist()
    }

    @Test
    fun appInfoHasPredefinedOrderAndInvokesLinks() {
        var clicks = 0
        val link = AndroidKitSettingsLink(onClick = { clicks++ })
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(
                    configuration = AndroidKitSettingsPageConfiguration.AppInfo(
                        AndroidKitSettingsAbout(
                            version = "1.0", privacyPolicy = link, termsOfUse = link,
                            libraries = link, openSource = link,
                        ),
                    ),
                )
            }
        }
        assertOrder("Legal", "Terms of use", "Privacy policy", "Third-party licenses",
            "Open source", "Version", "1.0")
        for (label in listOf("Privacy policy", "Terms of use", "Third-party licenses", "Explore, use or contribute")) {
            scrollTo(label)
            rule.onNodeWithText(label).performClick()
        }
        scrollTo("Version")
        rule.onNodeWithText("MIT").assertDoesNotExist()
        rule.runOnIdle { assertEquals(4, clicks) }
        rule.onNodeWithText("Contact").assertDoesNotExist()
        rule.onNodeWithText("About").assertDoesNotExist()
    }

    @Test
    fun appInfoKeepsAllRequiredEntriesVisible() {
        var clicks = 0
        var version by mutableStateOf("1.0")
        val link = AndroidKitSettingsLink(onClick = { clicks++ })
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(
                    configuration = AndroidKitSettingsPageConfiguration.AppInfo(
                        AndroidKitSettingsAbout(version = version,
                            privacyPolicy = link,
                            termsOfUse = link,
                            libraries = link,
                            openSource = link,
                        ),
                    ),
                )
            }
        }
        rule.onNodeWithText("1.0").assertIsDisplayed()
        rule.onNodeWithText("Privacy policy").assertIsDisplayed()
        rule.onNodeWithText("Terms of use").assertIsDisplayed()
        rule.onNodeWithText("Third-party licenses").assertIsDisplayed()
        rule.runOnIdle { version = "2.0" }
        rule.onNodeWithText("2.0").assertIsDisplayed()
        rule.onNodeWithText("1.0").assertDoesNotExist()
        rule.onNodeWithText("Legal").assertIsDisplayed()
        rule.runOnIdle { assertEquals(0, clicks) }
    }

    private fun scrollTo(label: String) {
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(label))
        rule.onNodeWithText(label).assertIsDisplayed()
    }

    private fun assertOrder(vararg labels: String) {
        labels.toList().zipWithNext().forEach { (first, second) ->
            scrollTo(second)
            val firstTop = rule.onNodeWithText(first).fetchSemanticsNode().boundsInRoot.top
            val secondTop = rule.onNodeWithText(second).fetchSemanticsNode().boundsInRoot.top
            assertTrue("$first must precede $second", firstTop < secondTop)
        }
    }
}
