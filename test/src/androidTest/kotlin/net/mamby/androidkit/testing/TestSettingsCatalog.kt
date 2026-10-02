package net.mamby.androidkit.testing

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import net.mamby.androidkit.compose.form.AndroidKitSettingsStoreMigration
import net.mamby.androidkit.compose.form.AndroidKitSettingsStorageProtection
import net.mamby.androidkit.compose.form.AndroidKitSettingsStore
import net.mamby.androidkit.compose.form.AndroidKitSearchHistorySnapshot
import androidx.compose.ui.platform.LocalContext
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import net.mamby.androidkit.compose.form.AndroidKitSettingsPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsPageScope
import net.mamby.androidkit.compose.form.AndroidKitSettingsSearchConfiguration
import net.mamby.androidkit.compose.form.androidKitSettingsCatalog

@Composable
internal fun TestSettingsPage(
    title: String = "Settings",
    onBack: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    content: AndroidKitSettingsPageScope.() -> Unit,
) {
    val catalog = androidKitSettingsCatalog(
        rememberTestSettingsSearchConfiguration({}, emptyList(), {}),
    ) {
        main(key = "test", title = title, content = content)
    }
    AndroidKitSettingsPage(catalog, "test", onBack = onBack, listState = listState)
}


@Composable
internal fun rememberTestSettingsSearchConfiguration(
    onOpenSearch: () -> Unit,
    recentQueries: List<String>,
    onRecentQueriesChange: (List<String>) -> Unit,
    recentQueriesVisible: Boolean = true,
    onRecentQueriesVisibleChange: (Boolean) -> Unit = {},
): AndroidKitSettingsSearchConfiguration {
    val context = LocalContext.current
    val store = remember {
        AndroidKitSettingsStore.open(context, "history-${UUID.randomUUID()}", AndroidKitSettingsStorageProtection.Plaintext,
            listOf(object : AndroidKitSettingsStoreMigration {
                override val id = "fixture"
                override suspend fun readHistories() = mapOf("settings" to AndroidKitSearchHistorySnapshot(recentQueries, recentQueriesVisible))
                override suspend fun cleanUp() = Unit
            }))
    }
    val history = remember(store) { store.searchHistory("settings") }
    val loaded = remember(history) { AtomicBoolean().also { ready ->
        SettingsPersistenceIdlingResource.track { ready.get() }
    } }
    LaunchedEffect(history) {
        history.snapshots.collect { snapshot ->
            onRecentQueriesChange(snapshot.recentQueries)
            onRecentQueriesVisibleChange(snapshot.visible)
            loaded.set(true)
        }
    }
    return AndroidKitSettingsSearchConfiguration(
        onOpenSearch = onOpenSearch,
        history = history,
        onStorageFailure = { throw it },
    )
}
