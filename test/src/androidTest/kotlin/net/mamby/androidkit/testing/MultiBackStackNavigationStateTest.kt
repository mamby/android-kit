package net.mamby.androidkit.testing

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import net.mamby.androidkit.navigation3.MultiBackStackNavigationState
import net.mamby.androidkit.navigation3.rememberMultiBackStackNavigationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
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

            assertEquals(listOf(SecondRoot, Detail("second-detail")), navigation.backStackFor(SecondRoot).toList())
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

    @Test
    fun selectedRootAndIndependentHistoriesSurviveStateRestoration() {
        lateinit var navigation: MultiBackStackNavigationState<TestRoot>
        val restoration = StateRestorationTester(composeRule)
        restoration.setContent {
            navigation = rememberMultiBackStackNavigationState(listOf(FirstRoot, SecondRoot))
        }
        composeRule.runOnIdle {
            navigation.navigate(Detail("first-detail"))
            navigation.selectRoot(SecondRoot)
            navigation.navigate(Detail("second-detail"))
        }
        restoration.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle {
            assertEquals(SecondRoot, navigation.selectedRoot)
            assertEquals(listOf(SecondRoot, Detail("second-detail")), navigation.currentBackStack.toList())
            assertFalse(navigation.isAtRoot)
            navigation.selectRoot(FirstRoot, popToRootOnReselect = false)
            assertEquals(listOf(FirstRoot, Detail("first-detail")), navigation.currentBackStack.toList())
        }
    }

    @Test
    fun openRootClearsOnlyItsHistoryAndResetClearsAllHistories() {
        lateinit var navigation: MultiBackStackNavigationState<TestRoot>
        composeRule.setContent {
            navigation = rememberMultiBackStackNavigationState(listOf(FirstRoot, SecondRoot))
        }
        composeRule.runOnIdle {
            navigation.navigate(Detail("first-detail"))
            navigation.selectRoot(SecondRoot)
            navigation.navigate(Detail("second-detail"))
            navigation.openRoot(FirstRoot)
            assertEquals(listOf(FirstRoot), navigation.currentBackStack.toList())
            assertTrue(navigation.isAtRoot)
            navigation.selectRoot(SecondRoot, popToRootOnReselect = false)
            assertEquals(listOf(SecondRoot, Detail("second-detail")), navigation.currentBackStack.toList())
            navigation.navigate(Detail("second-extra"))
            navigation.selectRoot(FirstRoot)
            navigation.navigate(Detail("first-extra"))
            navigation.reset(SecondRoot)
            assertEquals(SecondRoot, navigation.selectedRoot)
            assertEquals(listOf(SecondRoot), navigation.currentBackStack.toList())
            navigation.selectRoot(FirstRoot)
            assertEquals(listOf(FirstRoot), navigation.currentBackStack.toList())
        }
    }

    @Test
    fun replacingDetailsPreservesHistoryAndRootsCannotBeReplacedOrSelectedWhenUnregistered() {
        lateinit var navigation: MultiBackStackNavigationState<TestRoot>
        composeRule.setContent {
            navigation = rememberMultiBackStackNavigationState(listOf(FirstRoot, SecondRoot))
        }
        composeRule.runOnIdle {
            assertThrows(IllegalStateException::class.java) { navigation.replaceTop(Detail("invalid")) }
            assertEquals(listOf(FirstRoot), navigation.currentBackStack.toList())
            navigation.navigate(Detail("parent"))
            navigation.navigate(Detail("old"))
            navigation.replaceTop(Detail("replacement"))
            assertEquals(listOf(FirstRoot, Detail("parent"), Detail("replacement")), navigation.currentBackStack.toList())
            assertThrows(IllegalArgumentException::class.java) { navigation.selectRoot(UnregisteredRoot) }
            assertThrows(IllegalArgumentException::class.java) { navigation.openRoot(UnregisteredRoot) }
            assertThrows(IllegalArgumentException::class.java) { navigation.backStackFor(UnregisteredRoot) }
            assertEquals(FirstRoot, navigation.selectedRoot)
            assertTrue(navigation.goBack())
            assertEquals(Detail("parent"), navigation.currentBackStack.last())
        }
    }
}

private sealed interface TestRoot : NavKey

@Serializable
private data object FirstRoot : TestRoot

@Serializable
private data object SecondRoot : TestRoot

@Serializable
private data object UnregisteredRoot : TestRoot

@Serializable
private data class Detail(val id: String) : NavKey
