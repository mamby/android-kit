package net.mamby.androidkit.demo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewQuilt
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import net.mamby.androidkit.compose.navigation.AdaptiveNavigationScaffold
import net.mamby.androidkit.compose.navigation.AndroidKitNavigationItem
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import net.mamby.androidkit.compose.theme.FloatingSurfaceStyle
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.screen.ComponentDetailScreen
import net.mamby.androidkit.demo.ui.screen.ComponentPlaceholder
import net.mamby.androidkit.demo.ui.screen.ComponentsScreen
import net.mamby.androidkit.demo.ui.screen.FloatingActionsDemoScreen
import net.mamby.androidkit.demo.ui.screen.FloatingCatalogScreen
import net.mamby.androidkit.demo.ui.screen.FloatingNavigationDemoScreen
import net.mamby.androidkit.demo.ui.screen.FormsScreen
import net.mamby.androidkit.demo.ui.screen.LayoutDetailScreen
import net.mamby.androidkit.demo.ui.screen.LayoutPlaceholder
import net.mamby.androidkit.demo.ui.screen.LayoutsScreen
import net.mamby.androidkit.demo.ui.screen.LocalizationScreen
import net.mamby.androidkit.demo.ui.screen.SettingsScreen
import net.mamby.androidkit.navigation3.rememberMultiBackStackNavigationState

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AndroidKitCatalogApp() {
    var themeChoice by rememberSaveable { mutableStateOf(DemoThemeChoice.Light) }
    var floatingSurfaceOpacity by rememberSaveable { mutableFloatStateOf(0.92f) }
    val roots = remember {
        listOf(
            ComponentsRoute,
            LayoutsRoute,
            FormsRoute,
            FloatingRoute,
            LocalizationRoute,
            SettingsRoute,
        )
    }
    val navigation = rememberMultiBackStackNavigationState(roots)

    AndroidKitTheme(
        definition = themeChoice.definition(),
        strings = androidKitStrings(),
        floatingSurfaceStyle = FloatingSurfaceStyle(
            opacity = floatingSurfaceOpacity,
        ),
    ) {
        val navigationItems: List<AndroidKitNavigationItem<CatalogRootRoute>> = listOf(
            AndroidKitNavigationItem(
                key = ComponentsRoute,
                label = stringResource(R.string.nav_components),
                icon = Icons.Default.DashboardCustomize,
            ),
            AndroidKitNavigationItem(
                key = LayoutsRoute,
                label = stringResource(R.string.nav_layouts),
                icon = Icons.AutoMirrored.Filled.ViewQuilt,
            ),
            AndroidKitNavigationItem(
                key = FormsRoute,
                label = stringResource(R.string.nav_forms),
                icon = Icons.Default.EditNote,
            ),
            AndroidKitNavigationItem(
                key = FloatingRoute,
                label = stringResource(R.string.nav_floating),
                icon = Icons.Default.Navigation,
            ),
            AndroidKitNavigationItem(
                key = LocalizationRoute,
                label = stringResource(R.string.nav_localization),
                icon = Icons.Default.Language,
            ),
            AndroidKitNavigationItem(
                key = SettingsRoute,
                label = stringResource(R.string.nav_settings),
                icon = Icons.Default.Settings,
                showDividerAfterInFlyout = true,
            ),
        )
        val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
        val currentRoute = navigation.currentBackStack.last()
        val showCompactLabels = (currentRoute as? FloatingNavigationDemoRoute)?.showLabels == true

        BackHandler(enabled = !navigation.isAtRoot || navigation.selectedRoot != roots.first()) {
            navigation.goBack()
        }

        AdaptiveNavigationScaffold(
            items = navigationItems,
            selectedKey = navigation.selectedRoot,
            onSelected = navigation::openRoot,
            compactVisibleDestinationCount = 3,
            showCompactLabels = showCompactLabels,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavDisplay(
                    backStack = navigation.currentBackStack,
                    onBack = { navigation.goBack() },
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
                                    navigation.navigate(ComponentDetailRoute(componentId = it))
                                },
                            )
                        }
                        entry<ComponentDetailRoute>(
                            metadata = ListDetailSceneStrategy.detailPane(),
                        ) { route ->
                            ComponentDetailScreen(
                                componentId = route.componentId,
                                onBack = navigation::goBack,
                            )
                        }
                        entry<LayoutsRoute>(
                            metadata = ListDetailSceneStrategy.listPane(
                                detailPlaceholder = { LayoutPlaceholder() },
                            ),
                        ) {
                            LayoutsScreen(onSelected = { navigation.navigate(LayoutDetailRoute(it)) })
                        }
                        entry<LayoutDetailRoute>(
                            metadata = ListDetailSceneStrategy.detailPane(),
                        ) { route ->
                            LayoutDetailScreen(sampleId = route.sampleId, onBack = navigation::goBack)
                        }
                        entry<FormsRoute> { FormsScreen() }
                        entry<FloatingRoute> {
                            FloatingCatalogScreen(
                                onOpenFloatingNavigation = {
                                    navigation.navigate(FloatingNavigationDemoRoute(showLabels = it))
                                },
                                onOpenFloatingActions = {
                                    navigation.navigate(FloatingActionsDemoRoute(variant = it))
                                },
                            )
                        }
                        entry<LocalizationRoute> { LocalizationScreen() }
                        entry<FloatingNavigationDemoRoute> { route ->
                            FloatingNavigationDemoScreen(
                                showLabels = route.showLabels,
                                onBack = navigation::goBack,
                            )
                        }
                        entry<FloatingActionsDemoRoute> { route ->
                            FloatingActionsDemoScreen(
                                variant = route.variant,
                                onBack = navigation::goBack,
                            )
                        }
                        entry<SettingsRoute> {
                            SettingsScreen(
                                themeChoice = themeChoice,
                                onThemeChoice = { themeChoice = it },
                                floatingSurfaceOpacity = floatingSurfaceOpacity,
                                onFloatingSurfaceOpacity = { floatingSurfaceOpacity = it },
                            )
                        }
                    },
                )
            }
        }
    }
}
