package net.mamby.androidkit.demo.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import net.mamby.androidkit.compose.form.AndroidKitAppLockSetting
import net.mamby.androidkit.compose.form.AndroidKitAppLockTimeoutSetting
import net.mamby.androidkit.compose.form.AndroidKitSettingsOption
import net.mamby.androidkit.compose.form.AndroidKitSettingsPageConfiguration
import net.mamby.androidkit.compose.form.AndroidKitSettingsPage
import net.mamby.androidkit.compose.theme.AndroidKitTheme

/** Interactive UI fixture; the catalog's real authentication remains host-owned. */
@Preview(showBackground = true)
@Composable
private fun AppLockSettingsPreview() {
    var checked by rememberSaveable { mutableStateOf(true) }
    var selected by rememberSaveable { mutableStateOf("5") }
    AndroidKitTheme {
        AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.Subpage, title = "App lock UI preview") {
            securitySection(
                appLock = AndroidKitAppLockSetting(
                    checked = checked, onCheckedChange = { checked = it },
                    timeout = AndroidKitAppLockTimeoutSetting(
                        options = listOf(
                            AndroidKitSettingsOption("0", "Immediately"),
                            AndroidKitSettingsOption("1", "After 1 minute"),
                            AndroidKitSettingsOption("5", "After 5 minutes"),
                            AndroidKitSettingsOption("15", "After 15 minutes"),
                        ),
                        selectedId = selected, onSelected = { selected = it },
                    ),
                    onLockNow = {},
                ),
            )
        }
    }
}
