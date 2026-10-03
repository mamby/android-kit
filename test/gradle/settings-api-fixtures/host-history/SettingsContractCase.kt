package net.mamby.androidkit.settingscontract

import androidx.compose.runtime.Composable
import net.mamby.androidkit.compose.form.*

@Composable
fun settingsContractCase(store: AndroidKitSettingsStore) {
    val catalog = androidKitSettingsCatalog { main("main", "Settings") }
    AndroidKitSettings(catalog, store, onOpenSearch = {}, onStorageFailure = {},
        history = store.searchHistory("about")) {}
}
