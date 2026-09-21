package net.mamby.androidkit.testing

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import net.mamby.androidkit.compose.form.AndroidKitSettingsAbout
import net.mamby.androidkit.compose.form.AndroidKitSettingsLink
import net.mamby.androidkit.compose.form.AndroidKitSettingsLegalEntry
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
    fun mainShowsOneAboutDestination() {
        var aboutClicks = 0
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(
                    configuration = AndroidKitSettingsPageConfiguration.Main(
                        about = AndroidKitSettingsLink(onClick = { aboutClicks++ }),
                    ),
                ) {
                    section("host") { info("Host setting") }
                }
            }
        }
        assertOrder("Host setting", "About")
        rule.onNodeWithText("Contact, legal and more").assertDoesNotExist()
        scrollTo("About")
        rule.onNodeWithText("About").performClick()
        rule.runOnIdle { assertEquals(1, aboutClicks) }
        scrollTo("About")
        rule.onNodeWithText("Contact").assertDoesNotExist()
        rule.onNodeWithText("App info").assertDoesNotExist()
        rule.onNodeWithText("Version").assertDoesNotExist()
    }

    @Test
    fun aboutHasPredefinedOrderAndInvokesLinks() {
        var builtInClicks = 0
        var additionalClicks = 0
        var disabledClicks = 0
        val link = AndroidKitSettingsLink(onClick = { builtInClicks++ })
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(
                    configuration = AndroidKitSettingsPageConfiguration.About(
                        AndroidKitSettingsAbout(appName = "Android Kit",
                            description = "Reusable Android components.",
                            version = "1.0", privacyPolicy = link, termsOfUse = link,
                            libraries = link, contact = link, website = link, sourceCode = link,
                            additionalLegalEntries = listOf(
                                AndroidKitSettingsLegalEntry("contributors", "Contributors", { additionalClicks++ }),
                                AndroidKitSettingsLegalEntry("notices", "Notices", { disabledClicks++ }, enabled = false),
                            ),
                        ),
                    ),
                )
            }
        }
        assertOrder("Android Kit", "Website", "Source code", "Version",
            "Contact", "Privacy policy", "Terms of use",
            "Third-party licenses", "Contributors", "Notices")
        for (label in listOf("Website", "Source code", "Contact", "Privacy policy", "Terms of use", "Third-party licenses")) {
            scrollTo(label)
            rule.onNodeWithText(label).performClick()
        }
        scrollTo("Contributors")
        rule.onNodeWithText("Contributors").performClick()
        scrollTo("Notices")
        rule.onNodeWithText("Notices").assertIsNotEnabled()
        rule.runOnIdle {
            assertEquals(6, builtInClicks)
            assertEquals(1, additionalClicks)
            assertEquals(0, disabledClicks)
        }
        rule.onNodeWithText("App info").assertDoesNotExist()
    }

    @Test
    fun aboutKeepsRequiredEntriesVisibleAndOmitsBlankDescription() {
        var clicks = 0
        var version by mutableStateOf("1.0")
        val link = AndroidKitSettingsLink(onClick = { clicks++ })
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(
                    configuration = AndroidKitSettingsPageConfiguration.About(
                        AndroidKitSettingsAbout(appName = "Android Kit", version = version,
                            contact = link,
                            privacyPolicy = link,
                            termsOfUse = link,
                            libraries = link,
                            description = "   ",
                        ),
                    ),
                )
            }
        }
        rule.onNodeWithText("1.0").assertIsDisplayed()
        scrollTo("Privacy policy")
        scrollTo("Terms of use")
        scrollTo("Third-party licenses")
        rule.runOnIdle { version = "2.0" }
        scrollTo("2.0")
        rule.onNodeWithText("1.0").assertDoesNotExist()
        rule.onNodeWithText("Legal information").assertDoesNotExist()
        rule.onNodeWithText("App information").assertDoesNotExist()
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
