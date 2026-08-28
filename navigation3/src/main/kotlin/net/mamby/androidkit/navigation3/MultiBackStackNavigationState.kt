package net.mamby.androidkit.navigation3

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

public class MultiBackStackNavigationState<Root : NavKey> internal constructor(
    public val roots: List<Root>,
    selectedRootIndex: MutableIntState,
    private val backStacks: Map<Root, NavBackStack<NavKey>>,
) {
    private var selectedIndex: Int by selectedRootIndex

    public val selectedRoot: Root
        get() = roots[selectedIndex]

    public val currentBackStack: NavBackStack<NavKey>
        get() = backStacks.getValue(selectedRoot)

    public val isAtRoot: Boolean
        get() = currentBackStack.size == 1

    public fun selectRoot(root: Root, popToRootOnReselect: Boolean = true) {
        val newIndex = registeredRootIndex(root)
        if (newIndex == selectedIndex && popToRootOnReselect) {
            popCurrentStackToRoot()
        }
        selectedIndex = newIndex
    }

    public fun openRoot(root: Root) {
        selectedIndex = registeredRootIndex(root)
        popCurrentStackToRoot()
    }

    public fun navigate(route: NavKey) {
        currentBackStack.add(route)
    }

    public fun replaceTop(route: NavKey) {
        check(currentBackStack.size > 1) { "A top-level root cannot be replaced." }
        currentBackStack.removeLastOrNull()
        currentBackStack.add(route)
    }

    public fun goBack(): Boolean = when {
        currentBackStack.size > 1 -> {
            currentBackStack.removeLastOrNull()
            true
        }

        selectedIndex != 0 -> {
            selectedIndex = 0
            true
        }

        else -> false
    }

    public fun reset(root: Root = roots.first()) {
        backStacks.values.forEach(::popToRoot)
        selectedIndex = roots.indexOf(root).also {
            require(it >= 0) { "The reset root is not registered." }
        }
    }

    private fun popCurrentStackToRoot() {
        popToRoot(currentBackStack)
    }

    private fun registeredRootIndex(root: Root): Int = roots.indexOf(root).also { index ->
        require(index >= 0) { "The selected root is not registered." }
    }

    private companion object {
        private fun popToRoot(backStack: NavBackStack<NavKey>) {
            while (backStack.size > 1) backStack.removeLastOrNull()
        }
    }
}

@Composable
public fun <Root : NavKey> rememberMultiBackStackNavigationState(
    roots: List<Root>,
    startRoot: Root = roots.first(),
): MultiBackStackNavigationState<Root> {
    require(roots.isNotEmpty()) { "At least one navigation root is required." }
    require(roots.distinct().size == roots.size) { "Navigation roots must be unique." }
    val initialIndex = roots.indexOf(startRoot)
    require(initialIndex >= 0) { "The start root is not registered." }

    val selectedIndex = rememberSaveable(roots, startRoot) { mutableIntStateOf(initialIndex) }
    val stacks = roots.associateWith { root ->
        key(root) { rememberNavBackStack(root) }
    }
    return remember(roots, selectedIndex) {
        MultiBackStackNavigationState(
            roots = roots.toList(),
            selectedRootIndex = selectedIndex,
            backStacks = stacks,
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
public fun listDetailBackAction(onBack: () -> Unit): (() -> Unit)? =
    onBack.takeIf { LocalListDetailSceneScope.current == null }
