package net.mamby.androidkit.navigation3

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay

/**
 * Displays Kit pages immediately. Forward, Back and predictive Back have no page motion.
 * Hosts own destinations, back stacks, entry state and adaptive scene selection.
 */
@Composable
public fun <T : Any> AndroidKitNavDisplay(
    entries: List<NavEntry<T>>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    sceneStrategies: List<SceneStrategy<T>> = emptyList(),
) {
    val immediatePages = remember { ImmediatePageSceneDecorator<T>() }
    NavDisplay(
        entries = entries,
        onBack = onBack,
        modifier = modifier,
        sceneStrategies = sceneStrategies,
        sceneDecoratorStrategies = listOf(immediatePages),
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        predictivePopTransitionSpec = {
            EnterTransition.None togetherWith ExitTransition.KeepUntilTransitionsFinished
        },
    )
}

/** Back-stack overload with Navigation 3's cached entry decoration and saved-state ownership. */
@Composable
public fun <T : Any> AndroidKitNavDisplay(
    backStack: List<T>,
    onBack: () -> Unit,
    entryProvider: (T) -> NavEntry<T>,
    modifier: Modifier = Modifier,
    sceneStrategies: List<SceneStrategy<T>> = emptyList(),
    entryDecorators: List<NavEntryDecorator<T>> =
        listOf(rememberSaveableStateHolderNavEntryDecorator()),
) {
    val entries = rememberDecoratedNavEntries(backStack, entryDecorators, entryProvider)
    AndroidKitNavDisplay(entries, onBack, modifier, sceneStrategies)
}

// Scene metadata otherwise takes priority over NavDisplay's transition parameters.
private val immediatePageMetadata: Map<String, Any> = metadata {
    put(NavDisplay.TransitionKey) { EnterTransition.None togetherWith ExitTransition.None }
    put(NavDisplay.PopTransitionKey) { EnterTransition.None togetherWith ExitTransition.None }
    put(NavDisplay.PredictivePopTransitionKey) {
        EnterTransition.None togetherWith ExitTransition.KeepUntilTransitionsFinished
    }
}

private class ImmediatePageSceneDecorator<T : Any> : SceneDecoratorStrategy<T> {
    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> =
        ImmediatePageScene(scene)
}

private data class ImmediatePageScene<T : Any>(val scene: Scene<T>) : Scene<T> by scene {
    override val metadata: Map<String, Any> = scene.metadata + immediatePageMetadata
}
