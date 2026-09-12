package net.mamby.androidkit.demo.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface CatalogRoute : NavKey

sealed interface CatalogRootRoute : CatalogRoute

@Serializable
data object ComponentsRoute : CatalogRootRoute

@Serializable
data object LocalizationRoute : CatalogRootRoute

@Serializable
data object SettingsRoute : CatalogRootRoute

@Serializable
data class DemoRootRoute(val index: Int) : CatalogRootRoute

@Serializable
data class ComponentDemoRoute(val demo: ComponentDemo) : CatalogRoute
