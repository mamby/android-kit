package net.mamby.androidkit.demo.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.form.AndroidKitSettingsAbout
import net.mamby.androidkit.compose.form.AndroidKitSettingsAction
import net.mamby.androidkit.compose.form.AndroidKitSettingsPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsPageConfiguration
import net.mamby.androidkit.demo.BuildConfig
import net.mamby.androidkit.demo.R

internal const val CatalogRepository = "https://github.com/mamby/android-kit"

@Composable
internal fun catalogAbout(): AndroidKitSettingsAbout {
    val uriHandler = LocalUriHandler.current
    return AndroidKitSettingsAbout(
        title = stringResource(R.string.about_section),
        appName = stringResource(R.string.app_name),
        description = stringResource(R.string.about_body),
        maintainer = stringResource(R.string.about_maintainer),
        versionLabel = stringResource(R.string.about_version),
        version = BuildConfig.VERSION_NAME,
        openSourceTitle = stringResource(R.string.about_open_source),
        informationTitle = stringResource(R.string.about_information),
        sourceCode = AndroidKitSettingsAction(stringResource(R.string.about_source), { uriHandler.openUri(CatalogRepository) }),
        contributors = AndroidKitSettingsAction(stringResource(R.string.about_contributors), {
            uriHandler.openUri("$CatalogRepository/graphs/contributors")
        }),
        license = AndroidKitSettingsAction(stringResource(R.string.about_license), {
            uriHandler.openUri("$CatalogRepository/blob/main/LICENSE")
        }),
    )
}

@Composable
internal fun AboutScreen(onBack: () -> Unit) {
    AndroidKitSettingsPage(
        configuration = AndroidKitSettingsPageConfiguration.About(catalogAbout()),
        onBack = onBack,
    )
}
