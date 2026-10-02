package net.mamby.androidkit.demo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import net.mamby.androidkit.compose.layout.AndroidKitLockPage
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigation
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigationItem
import net.mamby.androidkit.navigation3.AndroidKitNavDisplay
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.screen.ComponentDemoScreen
import net.mamby.androidkit.demo.ui.screen.ComponentPlaceholder
import net.mamby.androidkit.demo.ui.screen.ComponentsScreen
import net.mamby.androidkit.demo.ui.screen.DummyNavigationScreen
import net.mamby.androidkit.demo.ui.screen.LocalizationScreen
import net.mamby.androidkit.demo.ui.screen.SettingsScreen
import net.mamby.androidkit.demo.ui.screen.AboutScreen
import net.mamby.androidkit.demo.ui.screen.SettingsSearchScreen
import net.mamby.androidkit.demo.ui.screen.demoSettingsCatalog
import net.mamby.androidkit.navigation3.rememberMultiBackStackNavigationState

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun AndroidKitCatalogApp(
    settingsViewModel: DemoSettingsViewModel,
    onAuthenticate: (Boolean?) -> Unit,
    onThemeDarknessChanged: (Boolean) -> Unit,
) {
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
    val snackbarState = remember { SnackbarHostState() }
    val writeFailure = settingsViewModel.settingsWriteFailure
    val saveFailedMessage = stringResource(R.string.settings_save_failed)
    val retryLabel = stringResource(R.string.action_retry)
    val storageFailure = settingsViewModel.settingsStorageFailure
    LaunchedEffect(storageFailure) {
        if (storageFailure != null) {
            previewedFloatingSurfaceOpacityLevel = settings.floatingSurfaceOpacityLevel
            snackbarState.showSnackbar(saveFailedMessage)
            settingsViewModel.dismissSettingsStorageFailure()
        }
    }
    LaunchedEffect(writeFailure, saveFailedMessage, retryLabel) {
        if (writeFailure != null) {
            previewedFloatingSurfaceOpacityLevel = settings.floatingSurfaceOpacityLevel
            val result = snackbarState.showSnackbar(
                message = saveFailedMessage,
                actionLabel = retryLabel,
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite,
            )
            settingsViewModel.dismissSettingsWriteFailure(writeFailure)
            if (result == SnackbarResult.ActionPerformed) writeFailure.retry()
        }
    }

    AndroidKitTheme(
        definition = themeDefinition,
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
        val settingsCatalog = demoSettingsCatalog(
            onAbout = { navigation.navigate(AboutRoute) },
            onSearch = { navigation.navigate(SettingsSearchRoute) },
            settingsStore = settingsViewModel.kitSettingsStore,
            onSettingsStorageFailure = settingsViewModel::settingsStorageFailed,
            appLockEnabled = settings.appLockEnabled,
            appLockTimeout = settings.appLockTimeout,
            onAppLockTimeoutChange = settingsViewModel::setAppLockTimeout,
            onAppLockChange = { onAuthenticate(it) },
            appLockBusy = settingsViewModel.authenticating,
            appLockError = settingsViewModel.authenticationError,
            onLockNow = settingsViewModel::lock,
            themeChoice = settings.themeChoice,
            onThemeChoice = settingsViewModel::setThemeChoice,
            floatingSurfaceOpacityLevel = previewedFloatingSurfaceOpacityLevel,
            onFloatingSurfaceOpacityLevelChange = { level ->
                previewedFloatingSurfaceOpacityLevel = level
            },
            onFloatingSurfaceOpacityLevelChangeFinished = {
                settingsViewModel.setFloatingSurfaceOpacityLevel(
                    previewedFloatingSurfaceOpacityLevel,
                )
            },
        )

        val currentSettings by rememberUpdatedState(settings)
        val currentCatalog by rememberUpdatedState(settingsCatalog)
        val provider = remember(navigation, settingsViewModel) {
            entryProvider<NavKey> {
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
                        demoToggles = currentSettings.demoToggles,
                        onDemoToggleChange = settingsViewModel::setDemoToggle,
                        selectedPageAction = currentSettings.selectedPageAction,
                        onPageActionSelected = settingsViewModel::setSelectedPageAction,
                        pageHeaderActionPresentation =
                            currentSettings.pageHeaderActionPresentation,
                        onPageHeaderActionPresentationChange =
                            settingsViewModel::setPageHeaderActionPresentation,
                        sheetHeaderActionPresentation =
                            currentSettings.sheetHeaderActionPresentation,
                        onSheetHeaderActionPresentationChange =
                            settingsViewModel::setSheetHeaderActionPresentation,
                        floatingNavigationLayout = currentSettings.floatingNavigationLayout,
                        onFloatingNavigationLayoutChange =
                            settingsViewModel::setFloatingNavigationLayout,
                        showCompactNavigationLabels =
                            currentSettings.showCompactNavigationLabels,
                        onShowCompactNavigationLabelsChange =
                            settingsViewModel::setShowCompactNavigationLabels,
                        recentContentSearchesVisible = currentSettings.recentContentSearchesVisible,
                        onRecentContentSearchesVisibleChange = settingsViewModel::setRecentContentSearchesVisible,
                        recentContentSearches = currentSettings.recentContentSearches,
                        onRecentContentSearchesChange = settingsViewModel::setRecentContentSearches,
                        onOpenDemo = { navigation.navigate(ComponentDemoRoute(demo = it)) },
                        onBack = navigation::goBack,
                    )
                }
                entry<LocalizationRoute> {
                    LocalizationScreen()
                }
                entry<SettingsRoute> {
                    SettingsScreen(currentCatalog)
                }
                entry<AboutRoute> {
                    AboutScreen(currentCatalog, onBack = navigation::goBack)
                }
                entry<SettingsSearchRoute> {
                    SettingsSearchScreen(currentCatalog, onBack = navigation::goBack)
                }
                entry<DemoRootRoute> { route ->
                    DummyNavigationScreen(index = route.index)
                }
            }
        }
        // Keep decoration and pop cleanup alive across shell changes and the lock gate.
        // Each root owns its decorators, including while another root is displayed.
        val entriesByRoot = roots.associateWith { root ->
            key(root) {
                rememberDecoratedNavEntries(
                    backStack = navigation.backStackFor(root),
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<NavKey>()),
                    entryProvider = provider,
                )
            }
        }
        val entries = entriesByRoot.getValue(navigation.selectedRoot)
        if (settings.appLockEnabled && !settingsViewModel.unlocked) {
            AndroidKitLockPage(
                message = stringResource(R.string.lock_page_message),
                unlockLabel = stringResource(R.string.lock_page_unlock),
                onUnlock = { onAuthenticate(null) },
                isUnlocking = settingsViewModel.authenticating,
                errorMessage = settingsViewModel.authenticationError,
            )
            return@AndroidKitTheme
        }
        BackHandler(enabled = !navigation.isAtRoot || navigation.selectedRoot != roots.first()) {
            navigation.goBack()
        }
        val currentContent by rememberUpdatedState<@Composable () -> Unit> {
            Box(modifier = Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
                AndroidKitNavDisplay(
                    entries = entries,
                    onBack = navigation::goBack,
                    sceneStrategies = listOf(listDetailStrategy),
                )
            }
        }
        // Move one NavDisplay instead of creating overlapping saved-state owners in subcomposition.
        val content = remember { movableContentOf { currentContent() } }

        Box(Modifier.fillMaxSize()) {
            if (navigation.isAtRoot) {
                AndroidKitFloatingNavigation(
                    items = navigationItems,
                    selectedKey = navigation.selectedRoot,
                    onSelected = navigation::openRoot,
                    compactVisibleDestinationCount =
                        navigationDemoConfiguration.visibleDestinationCount,
                    showCompactLabels = navigationDemoConfiguration.showLabels,
                    content = content,
                )
            } else {
                content()
            }
            SnackbarHost(
                hostState = snackbarState,
                modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding().imePadding(),
            )
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
