package net.mamby.androidkit.demo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import net.mamby.androidkit.compose.navigation.AndroidKitNavigationItem
import net.mamby.androidkit.compose.navigation.FloatingNavigation
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import net.mamby.androidkit.compose.theme.FloatingSurfaceStyle
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.screen.ComponentDemoScreen
import net.mamby.androidkit.demo.ui.screen.ComponentPlaceholder
import net.mamby.androidkit.demo.ui.screen.ComponentsScreen
import net.mamby.androidkit.demo.ui.screen.DummyNavigationScreen
import net.mamby.androidkit.demo.ui.screen.LocalizationScreen
import net.mamby.androidkit.demo.ui.screen.SettingsScreen
import net.mamby.androidkit.navigation3.rememberMultiBackStackNavigationState

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AndroidKitCatalogApp(
    onThemeDarknessChanged: (Boolean) -> Unit,
) {
    val applicationContext = LocalContext.current.applicationContext
    val settingsViewModel: DemoSettingsViewModel = viewModel {
        DemoSettingsViewModel(DemoSettingsRepository(applicationContext))
    }
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val themeDefinition = settings.themeChoice.definition()
    LaunchedEffect(themeDefinition.isDark) {
        onThemeDarknessChanged(themeDefinition.isDark)
    }
    val roots: List<CatalogRootRoute> = remember {
        listOf<CatalogRootRoute>(
            ComponentsRoute,
            LocalizationRoute,
            SettingsRoute,
        ) + (1..DummyNavigationDestinationCount).map(::DemoRootRoute)
    }
    val navigation = rememberMultiBackStackNavigationState(roots)

    AndroidKitTheme(
        definition = themeDefinition,
        strings = androidKitStrings(),
        floatingSurfaceStyle = FloatingSurfaceStyle(
            opacity = if (settings.floatingSurfacesTransparent) {
                TransparentFloatingSurfaceOpacity
            } else {
                OpaqueFloatingSurfaceOpacity
            },
        ),
    ) {
        val dummyNavigationIcons = listOf(
            Icons.Default.Home,
            Icons.Default.Favorite,
            Icons.Default.Notifications,
            Icons.Default.Person,
            Icons.Default.Search,
            Icons.Default.Info,
            Icons.Default.DashboardCustomize,
        )
        val navigationItems: List<AndroidKitNavigationItem<CatalogRootRoute>> =
            listOf(
                AndroidKitNavigationItem<CatalogRootRoute>(
                    key = ComponentsRoute,
                    label = stringResource(R.string.nav_components),
                    icon = Icons.Default.DashboardCustomize,
                ),
                AndroidKitNavigationItem<CatalogRootRoute>(
                    key = LocalizationRoute,
                    label = stringResource(R.string.nav_localization),
                    icon = Icons.Default.Language,
                ),
                AndroidKitNavigationItem<CatalogRootRoute>(
                    key = SettingsRoute,
                    label = stringResource(R.string.nav_settings),
                    icon = Icons.Default.Settings,
                    showDividerAfterInFlyout = true,
                ),
            ) + roots.filterIsInstance<DemoRootRoute>().map { route ->
                AndroidKitNavigationItem<CatalogRootRoute>(
                    key = route,
                    label = stringResource(R.string.nav_demo, route.index),
                    icon = dummyNavigationIcons[route.index - 1],
                )
            }
        val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

        BackHandler(enabled = !navigation.isAtRoot || navigation.selectedRoot != roots.first()) {
            navigation.goBack()
        }

        FloatingNavigation(
            items = navigationItems,
            selectedKey = navigation.selectedRoot,
            onSelected = navigation::openRoot,
            compactVisibleDestinationCount = if (settings.showCompactNavigationLabels) {
                CompactLabeledDestinationCount
            } else {
                CompactIconDestinationCount
            },
            showCompactLabels = settings.showCompactNavigationLabels,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavDisplay(
                    backStack = navigation.currentBackStack,
                    onBack = navigation::goBack,
                    sceneStrategies = listOf(listDetailStrategy),
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider = entryProvider {
                        entry<ComponentsRoute>(
                            metadata = ListDetailSceneStrategy.listPane(
                                detailPlaceholder = { ComponentPlaceholder() },
                            ),
                        ) {
                            ComponentsScreen(
                                onSelected = {
                                    navigation.navigate(ComponentDemoRoute(demo = it))
                                },
                            )
                        }
                        entry<ComponentDemoRoute>(
                            metadata = ListDetailSceneStrategy.detailPane(),
                        ) { route ->
                            ComponentDemoScreen(
                                demo = route.demo,
                                showCompactNavigationLabels =
                                    settings.showCompactNavigationLabels,
                                onShowCompactNavigationLabelsChange =
                                    settingsViewModel::setShowCompactNavigationLabels,
                                onBack = navigation::goBack,
                            )
                        }
                        entry<LocalizationRoute> { LocalizationScreen() }
                        entry<SettingsRoute> {
                            SettingsScreen(
                                themeChoice = settings.themeChoice,
                                onThemeChoice = settingsViewModel::setThemeChoice,
                                floatingSurfacesTransparent =
                                    settings.floatingSurfacesTransparent,
                                onFloatingSurfacesTransparent =
                                    settingsViewModel::setFloatingSurfacesTransparent,
                            )
                        }
                        entry<DemoRootRoute> { route ->
                            DummyNavigationScreen(index = route.index)
                        }
                    },
                )
            }
        }
    }
}

private const val TransparentFloatingSurfaceOpacity = 0.8f
private const val OpaqueFloatingSurfaceOpacity = 1f
private const val DummyNavigationDestinationCount = 7
private const val CompactLabeledDestinationCount = 2
private const val CompactIconDestinationCount = 4
