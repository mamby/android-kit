package net.mamby.androidkit.demo.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.form.AndroidKitAppLockSetting
import net.mamby.androidkit.compose.form.AndroidKitAppLockTimeoutSetting
import net.mamby.androidkit.compose.form.AndroidKitSettingsOption
import net.mamby.androidkit.compose.form.AndroidKitSettingsPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsSearchConfiguration
import net.mamby.androidkit.compose.form.androidKitSettingsCatalog
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import net.mamby.androidkit.demo.R

/** Interactive UI fixture; the catalog's real authentication remains host-owned. */
@Preview(showBackground = true)
@Composable
private fun AppLockSettingsPreview() {
    var checked by rememberSaveable { mutableStateOf(true) }
    var selected by rememberSaveable { mutableStateOf("5") }
    val title = stringResource(R.string.settings_app_lock)
    val options = listOf(
        AndroidKitSettingsOption("0", stringResource(R.string.settings_lock_immediately)),
        AndroidKitSettingsOption("1", stringResource(R.string.settings_lock_after_one_minute)),
        AndroidKitSettingsOption("5", stringResource(R.string.settings_lock_after_five_minutes)),
        AndroidKitSettingsOption("15", stringResource(R.string.settings_lock_after_fifteen_minutes)),
    )
    AndroidKitTheme {
        val catalog = androidKitSettingsCatalog(
            AndroidKitSettingsSearchConfiguration({}, emptyList(), {}),
        ) {
            main("preview", title) {
                section("security") {
                    appLock(AndroidKitAppLockSetting(
                        checked = checked, onCheckedChange = { checked = it },
                        timeout = AndroidKitAppLockTimeoutSetting(
                            options = options,
                            selectedId = selected, onSelected = { selected = it },
                        ),
                        onLockNow = {},
                    ))
                }
            }
        }
        AndroidKitSettingsPage(catalog, "preview")
    }
}
