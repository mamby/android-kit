package net.mamby.androidkit.testing

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
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
        AndroidKitSettingsSearchConfiguration({}, emptyList(), {}),
    ) {
        main(key = "test", title = title, content = content)
    }
    AndroidKitSettingsPage(catalog, "test", onBack = onBack, listState = listState)
}
