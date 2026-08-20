package net.mamby.androidkit.testing

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import net.mamby.androidkit.navigation3.MultiBackStackNavigationState
import net.mamby.androidkit.navigation3.rememberMultiBackStackNavigationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MultiBackStackNavigationStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rootsRetainIndependentHistoryAndBackReturnsToTheFirstRoot() {
        lateinit var navigation: MultiBackStackNavigationState<TestRoot>
        composeRule.setContent {
            navigation = rememberMultiBackStackNavigationState(
                roots = listOf(FirstRoot, SecondRoot),
            )
        }

        composeRule.runOnIdle {
            navigation.navigate(Detail("first-detail"))
            navigation.selectRoot(SecondRoot)
            navigation.navigate(Detail("second-detail"))
            navigation.selectRoot(FirstRoot, popToRootOnReselect = false)

            assertEquals(Detail("first-detail"), navigation.currentBackStack.last())
            assertTrue(navigation.goBack())
            assertEquals(listOf(FirstRoot), navigation.currentBackStack.toList())

            navigation.selectRoot(SecondRoot, popToRootOnReselect = false)
            assertEquals(Detail("second-detail"), navigation.currentBackStack.last())
            navigation.selectRoot(SecondRoot)
            assertEquals(listOf(SecondRoot), navigation.currentBackStack.toList())

            assertTrue(navigation.goBack())
            assertEquals(FirstRoot, navigation.selectedRoot)
            assertFalse(navigation.goBack())
        }
    }
}

private sealed interface TestRoot : NavKey

@Serializable
private data object FirstRoot : TestRoot

@Serializable
private data object SecondRoot : TestRoot

@Serializable
private data class Detail(val id: String) : NavKey
