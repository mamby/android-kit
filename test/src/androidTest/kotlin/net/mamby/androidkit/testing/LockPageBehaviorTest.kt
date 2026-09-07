package net.mamby.androidkit.testing

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import net.mamby.androidkit.compose.layout.AndroidKitLockPage
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LockPageBehaviorTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun authenticationProgressPreventsDuplicateActionsAndErrorAllowsRetry() {
        var unlocking by mutableStateOf(false)
        var error by mutableStateOf<String?>(null)
        var attempts = 0
        rule.setContent {
            AndroidKitTheme {
                AndroidKitLockPage(
                    message = "Unlock to continue",
                    unlockLabel = "Unlock",
                    onUnlock = { attempts++; unlocking = true },
                    isUnlocking = unlocking,
                    errorMessage = error,
                )
            }
        }

        rule.onNodeWithText("Unlock").performClick()
        rule.onNodeWithText("Unlock").assertDoesNotExist()
        rule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo.Indeterminate,
            ),
        ).assertIsDisplayed()
        rule.runOnIdle {
            assertEquals(1, attempts)
            error = "Try again"
            unlocking = false
        }
        rule.onNodeWithText("Try again").assertIsDisplayed()
        rule.onNodeWithText("Unlock").performClick()
        rule.runOnIdle { assertEquals(2, attempts) }
    }
}
