package net.mamby.androidkit.compose.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.compositionLocalOf

/**
 * One complete Settings hierarchy, search history and privacy preference.
 * Place above the host's Settings navigation destinations. Pages cannot replace
 * this catalog or select a different search history. Nested owners are rejected.
 */
@Composable
public fun AndroidKitSettings(
    catalog: AndroidKitSettingsCatalog,
    store: AndroidKitSettingsStore,
    onOpenSearch: () -> Unit,
    onStorageFailure: (Throwable) -> Unit,
    content: @Composable () -> Unit,
): Unit {
    check(LocalSettingsOwner.current == null) {
        "Settings pages must share their enclosing AndroidKitSettings owner; nested owners are not supported."
    }
    val history = remember(store) { store.searchHistory("settings") }
    val identity = remember(store) { Any() }
    DisposableEffect(store, identity) {
        store.attachSettingsOwner(identity)
        onDispose { store.detachSettingsOwner(identity) }
    }
    val owner = SettingsOwner(catalog, SettingsSearchConfiguration(onOpenSearch, history, onStorageFailure))
    CompositionLocalProvider(LocalSettingsOwner provides owner, content = content)
}

internal class SettingsOwner(
    val catalog: AndroidKitSettingsCatalog,
    val search: SettingsSearchConfiguration,
)

internal class SettingsSearchConfiguration(
    val onOpenSearch: () -> Unit,
    val history: AndroidKitPersistentSearchHistory,
    val onStorageFailure: (Throwable) -> Unit,
)

private val LocalSettingsOwner = compositionLocalOf<SettingsOwner?> { null }

@Composable
internal fun currentSettingsOwner(): SettingsOwner = checkNotNull(LocalSettingsOwner.current) {
    "Settings pages and search must be rendered inside one AndroidKitSettings owner above Settings navigation."
}
