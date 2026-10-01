package net.mamby.androidkit.testing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import net.mamby.androidkit.navigation3.AndroidKitNavDisplay
import kotlinx.serialization.Serializable
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigation
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigationItem
import net.mamby.androidkit.navigation3.MultiBackStackNavigationState
import net.mamby.androidkit.navigation3.rememberMultiBackStackNavigationState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NavigationEntryStateBehaviorTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun shellChangesAndLockingPreserveStateButPoppingDiscardsIt() {
        var locked by mutableStateOf(false)
        rule.setContent { NavigationFixture(locked = locked) }
        rule.onNodeWithText("Increment Root").performClick()
        rule.onNodeWithText("Open Root").performClick()
        rule.onNodeWithText("Increment Detail").performClick()
        pressBack()
        rule.onNodeWithText("Root count: 1").assertIsDisplayed()
        rule.onNodeWithText("Open Root").performClick()
        rule.onNodeWithText("Detail count: 0").assertIsDisplayed()
        rule.onNodeWithText("Increment Detail").performClick()
        rule.runOnIdle { locked = true }
        rule.onNodeWithText("Detail count: 1").assertDoesNotExist()
        rule.onNodeWithText("Locked").assertIsDisplayed()
        rule.runOnIdle { locked = false }
        rule.onNodeWithText("Detail count: 1").assertIsDisplayed()
        pressBack()
        rule.onNodeWithText("Root count: 1").assertIsDisplayed()
        rule.onNodeWithText("Open Root").performClick()
        rule.onNodeWithText("Detail count: 0").assertIsDisplayed()
    }

    @Test
    fun switchingRootsAndRestorationRetainEntryStateUntilAnActualPop() {
        lateinit var navigation: MultiBackStackNavigationState<StateNavigationRoot>
        val restoration = StateRestorationTester(rule)
        restoration.setContent { NavigationFixture(onNavigation = { navigation = it }) }
        rule.onNodeWithText("Increment Root").performClick()
        rule.onNodeWithText("Open Root").performClick()
        rule.onNodeWithText("Increment Detail").performClick()
        rule.runOnIdle { navigation.selectRoot(OtherStateRoot) }
        rule.onNodeWithText("Increment Other").performClick()
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("Other count: 1").assertIsDisplayed()
        rule.runOnIdle { navigation.selectRoot(StateRoot, popToRootOnReselect = false) }
        rule.onNodeWithText("Detail count: 1").assertIsDisplayed()
        // Pop an inactive stack, which must discard only its removed entry's state.
        rule.runOnIdle {
            navigation.selectRoot(OtherStateRoot)
            navigation.backStackFor(StateRoot).removeLastOrNull()
        }
        rule.runOnIdle { navigation.selectRoot(StateRoot) }
        rule.onNodeWithText("Root count: 1").assertIsDisplayed()
        rule.onNodeWithText("Open Root").performClick()
        rule.onNodeWithText("Detail count: 0").assertIsDisplayed()
        rule.runOnIdle { navigation.selectRoot(OtherStateRoot) }
        rule.onNodeWithText("Other count: 1").assertIsDisplayed()
    }

    @Test
    fun systemBackFromASecondaryRootDetailReturnsToThatRoot() {
        lateinit var navigation: MultiBackStackNavigationState<StateNavigationRoot>
        rule.setContent { NavigationFixture(onNavigation = { navigation = it }) }
        rule.runOnIdle { navigation.selectRoot(OtherStateRoot) }
        rule.onNodeWithText("Open Other").performClick()
        rule.onNodeWithText("Other detail count: 0").assertIsDisplayed()
        pressBack()
        rule.onNodeWithText("Other count: 0").assertIsDisplayed()
        pressBack()
        rule.onNodeWithText("Root count: 0").assertIsDisplayed()
    }

    @Test
    fun cachedEntriesUseLatestValuesAndCallbacksWithoutRecreatingProviders() {
        var version by mutableIntStateOf(0)
        var actionVersion = -1
        var entryCreations = 0
        rule.setContent {
            val renderedVersion = version
            NavigationFixture(
                version = renderedVersion,
                onAction = { actionVersion = renderedVersion },
                onEntryCreated = { entryCreations++ },
            )
        }
        rule.onNodeWithText("Open Root").performClick()
        val creationsAfterNavigation = rule.runOnIdle { entryCreations }
        rule.runOnIdle { version = 1 }
        rule.onNodeWithText("Version: 1").assertIsDisplayed()
        rule.onNodeWithText("Current action").performClick()
        rule.runOnIdle {
            assertEquals(1, actionVersion)
            assertEquals(creationsAfterNavigation, entryCreations)
        }
    }

    @Test
    fun tabsAndPagesSwitchImmediatelyInBothDirections() {
        lateinit var navigation: MultiBackStackNavigationState<StateNavigationRoot>
        rule.mainClock.autoAdvance = false
        rule.setContent { NavigationFixture(onNavigation = { navigation = it }) }
        rule.runOnIdle { navigation.selectRoot(OtherStateRoot) }
        rule.mainClock.advanceTimeBy(64)
        rule.onNodeWithText("Other count: 0").assertIsDisplayed()
        rule.onNodeWithText("Root count: 0").assertDoesNotExist()
        rule.runOnIdle { navigation.openRoot(StateRoot) }
        rule.mainClock.advanceTimeBy(64)
        rule.onNodeWithText("Root count: 0").assertIsDisplayed()
        rule.onNodeWithText("Other count: 0").assertDoesNotExist()
        rule.runOnIdle { navigation.navigate(StateDetail) }
        rule.mainClock.advanceTimeBy(64)
        rule.onNodeWithText("Detail count: 0").assertIsDisplayed()
        rule.onNodeWithText("Root count: 0").assertDoesNotExist()
        rule.runOnIdle { navigation.goBack() }
        rule.mainClock.advanceTimeBy(64)
        rule.onNodeWithText("Root count: 0").assertIsDisplayed()
        rule.onNodeWithText("Detail count: 0").assertDoesNotExist()
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun NavigationFixture(
    locked: Boolean = false,
    version: Int = 0,
    onAction: () -> Unit = {},
    onEntryCreated: () -> Unit = {},
    onNavigation: (MultiBackStackNavigationState<StateNavigationRoot>) -> Unit = {},
) {
    val navigation = rememberMultiBackStackNavigationState(listOf(StateRoot, OtherStateRoot))
    onNavigation(navigation)
    val currentVersion by rememberUpdatedState(version)
    val currentAction by rememberUpdatedState(onAction)
    TestKitTheme {
        val strategy = rememberListDetailSceneStrategy<NavKey>()
        val provider = remember(navigation) {
            entryProvider<NavKey> {
                entry<StateRoot>(metadata = ListDetailSceneStrategy.listPane()) {
                    StatefulEntry("Root", currentVersion, currentAction) { navigation.navigate(StateDetail) }
                }
                entry<StateDetail>(metadata = ListDetailSceneStrategy.detailPane()) {
                    StatefulEntry("Detail", currentVersion, currentAction) { navigation.goBack() }
                }
                entry<OtherStateRoot> {
                    StatefulEntry("Other", currentVersion, currentAction) { navigation.navigate(OtherStateDetail) }
                }
                entry<OtherStateDetail> {
                    StatefulEntry("Other detail", currentVersion, currentAction) { navigation.goBack() }
                }
            }
        }
        val entriesByRoot = navigation.roots.associateWith { root ->
            key(root) {
                rememberDecoratedNavEntries(
                    backStack = navigation.backStackFor(root),
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<NavKey>()),
                    entryProvider = { route ->
                        onEntryCreated()
                        provider(route)
                    },
                )
            }
        }
        if (locked) {
            Text("Locked")
            return@TestKitTheme
        }
        BackHandler(enabled = !navigation.isAtRoot || navigation.selectedRoot != StateRoot) {
            navigation.goBack()
        }
        val currentContent by rememberUpdatedState<@Composable () -> Unit> {
            AndroidKitNavDisplay(
                entries = entriesByRoot.getValue(navigation.selectedRoot),
                sceneStrategies = listOf(strategy),
                onBack = { navigation.goBack() },
            )
        }
        val content = remember { movableContentOf { currentContent() } }
        if (navigation.isAtRoot) {
            AndroidKitFloatingNavigation(
                items = listOf(
                    AndroidKitFloatingNavigationItem<StateNavigationRoot>(StateRoot, "Home", materialSymbol(R.drawable.ic_symbol_home)),
                    AndroidKitFloatingNavigationItem<StateNavigationRoot>(OtherStateRoot, "Other home", materialSymbol(R.drawable.ic_symbol_home)),
                ),
                selectedKey = navigation.selectedRoot,
                onSelected = navigation::openRoot,
                content = content,
            )
        } else {
            content()
        }
    }
}

@Composable
private fun StatefulEntry(name: String, version: Int, onAction: () -> Unit, onOpen: () -> Unit) {
    var count by rememberSaveable { mutableIntStateOf(0) }
    Column {
        Text("$name count: $count")
        Text("Version: $version")
        Button(onClick = onAction) { Text("Current action") }
        Button(onClick = { count++ }) { Text("Increment $name") }
        Button(onClick = onOpen) { Text("Open $name") }
    }
}

private sealed interface StateNavigationRoot : NavKey
@Serializable private data object StateRoot : StateNavigationRoot
@Serializable private data object OtherStateRoot : StateNavigationRoot
@Serializable private data object StateDetail : NavKey
@Serializable private data object OtherStateDetail : NavKey
