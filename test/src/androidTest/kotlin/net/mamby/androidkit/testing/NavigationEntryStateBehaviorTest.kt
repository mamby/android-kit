package net.mamby.androidkit.testing

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigation
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigationItem
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class NavigationEntryStateBehaviorTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun shellChangesAndLockingPreserveStateButPoppingDiscardsIt() {
        var locked by mutableStateOf(false)
        rule.setContent {
            val stack = rememberNavBackStack(StateRoot)
            val holder = rememberSaveableStateHolder()
            TestKitTheme {
                val strategy = rememberListDetailSceneStrategy<NavKey>()
                val provider = entryProvider<NavKey> {
                    entry<StateRoot>(metadata = ListDetailSceneStrategy.listPane()) {
                        StatefulEntry("Root") { stack.add(StateDetail) }
                    }
                    entry<StateDetail>(metadata = ListDetailSceneStrategy.detailPane()) {
                        StatefulEntry("Detail") { stack.removeLastOrNull() }
                    }
                }
                val entries = rememberDecoratedNavEntries(
                    entries = stack.map(provider),
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<NavKey>(holder)),
                )
                if (locked) {
                    Text("Locked")
                    return@TestKitTheme
                }
                val currentContent by rememberUpdatedState<@Composable () -> Unit> {
                    NavDisplay(
                        entries = entries,
                        sceneStrategies = listOf(strategy),
                        onBack = { stack.removeLastOrNull() },
                    )
                }
                val content = remember { movableContentOf { currentContent() } }
                if (stack.size == 1) {
                    AndroidKitFloatingNavigation(
                        items = listOf(AndroidKitFloatingNavigationItem(
                            StateRoot, "Home", materialSymbol(R.drawable.ic_symbol_home),
                        )),
                        selectedKey = StateRoot,
                        onSelected = {},
                        content = content,
                    )
                } else {
                    content()
                }
            }
        }
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
}

@Composable
private fun StatefulEntry(name: String, onOpen: () -> Unit) {
    var count by rememberSaveable { mutableIntStateOf(0) }
    Column {
        Text("$name count: $count")
        Button(onClick = { count++ }) { Text("Increment $name") }
        Button(onClick = onOpen) { Text("Open $name") }
    }
}

@Serializable private data object StateRoot : NavKey
@Serializable private data object StateDetail : NavKey
