package net.mamby.androidkit.demo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigation
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigationItem
import net.mamby.androidkit.compose.theme.AndroidKitTheme
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
    val settingsState by settingsViewModel.settings.collectAsStateWithLifecycle()
    val settings = settingsState ?: return
    var previewedFloatingSurfaceOpacityLevel by remember {
        mutableFloatStateOf(settings.floatingSurfaceOpacityLevel)
    }
    LaunchedEffect(settings.floatingSurfaceOpacityLevel) {
        previewedFloatingSurfaceOpacityLevel = settings.floatingSurfaceOpacityLevel
    }
    val themeDefinition = settings.themeChoice.definition().copy(
        floatingSurfaceOpacityLevel = previewedFloatingSurfaceOpacityLevel,
    )
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
    val navigationDemoConfiguration = floatingNavigationDemoConfiguration(settings)

    AndroidKitTheme(
        definition = themeDefinition,
        strings = androidKitStrings(),
    ) {
        val dummyNavigationIcons = listOf(
            materialSymbol(R.drawable.ic_symbol_home),
            materialSymbol(R.drawable.ic_symbol_favorite),
            materialSymbol(R.drawable.ic_symbol_notifications),
            materialSymbol(R.drawable.ic_symbol_person),
            materialSymbol(R.drawable.ic_symbol_search),
            materialSymbol(R.drawable.ic_symbol_info),
            materialSymbol(R.drawable.ic_symbol_dashboard_customize),
        )
        val navigationItems: List<AndroidKitFloatingNavigationItem<CatalogRootRoute>> = (
            listOf(
                AndroidKitFloatingNavigationItem<CatalogRootRoute>(
                    key = ComponentsRoute,
                    label = stringResource(R.string.nav_components),
                    icon = materialSymbol(R.drawable.ic_symbol_dashboard_customize),
                ),
                AndroidKitFloatingNavigationItem<CatalogRootRoute>(
                    key = LocalizationRoute,
                    label = stringResource(R.string.nav_localization),
                    icon = materialSymbol(R.drawable.ic_symbol_language),
                ),
                AndroidKitFloatingNavigationItem<CatalogRootRoute>(
                    key = SettingsRoute,
                    label = stringResource(R.string.nav_settings),
                    icon = materialSymbol(R.drawable.ic_symbol_settings),
                ),
            ) + roots.filterIsInstance<DemoRootRoute>().map { route ->
                AndroidKitFloatingNavigationItem<CatalogRootRoute>(
                    key = route,
                    label = stringResource(R.string.nav_demo, route.index),
                    icon = dummyNavigationIcons[route.index - 1],
                )
            }
        ).take(navigationDemoConfiguration.itemCount)
        val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

        BackHandler(enabled = !navigation.isAtRoot || navigation.selectedRoot != roots.first()) {
            navigation.goBack()
        }

        AndroidKitFloatingNavigation(
            items = navigationItems,
            selectedKey = navigation.selectedRoot,
            onSelected = navigation::openRoot,
            modifier = Modifier.semantics { testTagsAsResourceId = true },
            compactVisibleDestinationCount =
                navigationDemoConfiguration.visibleDestinationCount,
            showCompactLabels = navigationDemoConfiguration.showLabels,
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
                                floatingNavigationLayout = settings.floatingNavigationLayout,
                                onFloatingNavigationLayoutChange =
                                    settingsViewModel::setFloatingNavigationLayout,
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
                                floatingSurfaceOpacityLevel =
                                    previewedFloatingSurfaceOpacityLevel,
                                onFloatingSurfaceOpacityLevelChange = { level ->
                                    previewedFloatingSurfaceOpacityLevel = level
                                },
                                onFloatingSurfaceOpacityLevelChangeFinished = {
                                    settingsViewModel.setFloatingSurfaceOpacityLevel(
                                        previewedFloatingSurfaceOpacityLevel,
                                    )
                                },
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

private const val DummyNavigationDestinationCount = 7
private data class FloatingNavigationDemoConfiguration(
    val itemCount: Int = 10,
    val visibleDestinationCount: Int = 4,
    val showLabels: Boolean = false,
)

private fun floatingNavigationDemoConfiguration(
    settings: DemoSettings,
): FloatingNavigationDemoConfiguration {
    val layoutConfiguration = when (settings.floatingNavigationLayout) {
        DemoFloatingNavigationLayout.ThreeItemsWithoutMore ->
            FloatingNavigationDemoConfiguration(itemCount = 3, visibleDestinationCount = 3)
        DemoFloatingNavigationLayout.FiveItemsWithMore ->
            FloatingNavigationDemoConfiguration(itemCount = 10, visibleDestinationCount = 4)
        DemoFloatingNavigationLayout.SevenItemsWithMore ->
            FloatingNavigationDemoConfiguration(itemCount = 7, visibleDestinationCount = 4)
    }
    return layoutConfiguration.copy(showLabels = settings.showCompactNavigationLabels)
}
