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
import net.mamby.androidkit.compose.form.AndroidKitSettings
import net.mamby.androidkit.compose.form.AndroidKitSettingsSearchPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsCatalog
import net.mamby.androidkit.compose.form.AndroidKitSettingsCatalogScope
import net.mamby.androidkit.compose.form.androidKitSettingsCatalog

@Composable
internal fun TestSettingsPage(
    title: String = "Settings",
    onBack: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    content: AndroidKitSettingsPageScope.() -> Unit,
) {
    val catalog = testSettingsCatalog(
        rememberTestSettingsSearchConfiguration({}, emptyList(), {}),
    ) {
        main(key = "test", title = title, content = content)
    }
    TestCatalogPage(catalog, "test", onBack = onBack, listState = listState)
}


@Composable
internal fun rememberTestSettingsSearchConfiguration(
    onOpenSearch: () -> Unit,
    recentQueries: List<String>,
    onRecentQueriesChange: (List<String>) -> Unit,
    searchHistoryEnabled: Boolean = true,
    onSearchHistoryEnabledChange: (Boolean) -> Unit = {},
): TestSettingsConfiguration {
    val context = LocalContext.current
    val store = remember {
        AndroidKitSettingsStore.open(context, "history-${UUID.randomUUID()}", AndroidKitSettingsStorageProtection.Plaintext,
            listOf(object : AndroidKitSettingsStoreMigration {
                override val id = "fixture"
                override suspend fun readHistories() = mapOf("settings" to AndroidKitSearchHistorySnapshot(recentQueries, searchHistoryEnabled))
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
            onSearchHistoryEnabledChange(snapshot.enabled)
            loaded.set(true)
        }
    }
    return TestSettingsConfiguration(
        onOpenSearch = onOpenSearch,
        store = store,
        onStorageFailure = { throw it },
    )
}

internal class TestSettingsConfiguration(
    val onOpenSearch: () -> Unit,
    val store: AndroidKitSettingsStore,
    val onStorageFailure: (Throwable) -> Unit,
)

internal class TestCatalog(val catalog: AndroidKitSettingsCatalog, val configuration: TestSettingsConfiguration)

internal fun testSettingsCatalog(
    configuration: TestSettingsConfiguration,
    content: AndroidKitSettingsCatalogScope.() -> Unit,
): TestCatalog = TestCatalog(androidKitSettingsCatalog(content), configuration)

@Composable
internal fun TestCatalogOwner(catalog: TestCatalog, content: @Composable () -> Unit) {
    AndroidKitSettings(catalog.catalog, catalog.configuration.store,
        catalog.configuration.onOpenSearch, catalog.configuration.onStorageFailure, content)
}

@Composable
internal fun TestCatalogPage(
    catalog: TestCatalog,
    pageKey: String,
    onBack: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
) {
    TestCatalogOwner(catalog) { AndroidKitSettingsPage(pageKey, onBack = onBack, listState = listState) }
}

@Composable
internal fun TestCatalogSearchPage(catalog: TestCatalog) {
    TestCatalogOwner(catalog) { AndroidKitSettingsSearchPage() }
}
