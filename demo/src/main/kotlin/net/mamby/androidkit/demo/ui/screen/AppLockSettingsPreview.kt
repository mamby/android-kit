package net.mamby.androidkit.demo.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import net.mamby.androidkit.compose.form.AndroidKitSettingsStore
import net.mamby.androidkit.compose.form.AndroidKitSettingsStorageProtection
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.form.AndroidKitAppLockSetting
import net.mamby.androidkit.compose.form.AndroidKitAppLockTimeoutSetting
import net.mamby.androidkit.compose.form.AndroidKitSettingsOption
import net.mamby.androidkit.compose.form.AndroidKitSettingsPage
import net.mamby.androidkit.compose.form.AndroidKitSettings
import net.mamby.androidkit.compose.form.androidKitSettingsCatalog
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import net.mamby.androidkit.demo.R

/** Interactive UI fixture; the catalog's real authentication remains host-owned. */
@Preview(showBackground = true)
@Composable
private fun AppLockSettingsPreview() {
    val context = LocalContext.current
    val store = remember(context) { AndroidKitSettingsStore.open(context, "app-lock-preview", AndroidKitSettingsStorageProtection.Plaintext) }
    val coroutineScope = rememberCoroutineScope()
    val appLock = store.setting(booleanPreferencesKey("app-lock"), true)
    val title = stringResource(R.string.settings_app_lock)
    val options = listOf(
        AndroidKitSettingsOption("0", stringResource(R.string.settings_lock_immediately)),
        AndroidKitSettingsOption("1", stringResource(R.string.settings_lock_after_one_minute)),
        AndroidKitSettingsOption("5", stringResource(R.string.settings_lock_after_five_minutes)),
        AndroidKitSettingsOption("15", stringResource(R.string.settings_lock_after_fifteen_minutes)),
    )
    AndroidKitTheme {
        val catalog = androidKitSettingsCatalog {
            main("preview", title) {
                section("security") {
                    appLock(AndroidKitAppLockSetting(
                        persistence = appLock, onCheckedChange = { coroutineScope.launch { appLock.set(it) } },
                        timeout = AndroidKitAppLockTimeoutSetting(
                            persistence = store.setting(stringPreferencesKey("timeout"), "5"),
                            options = options,
                            onSelected = {},
                        ),
                        onLockNow = {},
                    ))
                }
            }
        }
        AndroidKitSettings(catalog, store, onOpenSearch = {}, onStorageFailure = { throw it }) {
            AndroidKitSettingsPage("preview")
        }
    }
}
