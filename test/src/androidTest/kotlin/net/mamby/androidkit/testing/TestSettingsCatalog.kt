package net.mamby.androidkit.testing

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
): AndroidKitSettingsSearchConfiguration {
    var visible by rememberSaveable { mutableStateOf(true) }
    return AndroidKitSettingsSearchConfiguration(
        onOpenSearch = onOpenSearch,
        recentQueries = recentQueries,
        onRecentQueriesChange = onRecentQueriesChange,
        recentQueriesVisible = visible,
        onRecentQueriesVisibleChange = { visible = it },
    )
}
