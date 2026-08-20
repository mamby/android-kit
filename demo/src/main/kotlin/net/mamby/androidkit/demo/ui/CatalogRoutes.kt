package net.mamby.androidkit.demo.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface CatalogRoute : NavKey

sealed interface CatalogRootRoute : CatalogRoute

@Serializable
data object ComponentsRoute : CatalogRootRoute

@Serializable
data object LayoutsRoute : CatalogRootRoute

@Serializable
data object FormsRoute : CatalogRootRoute

@Serializable
data object SettingsRoute : CatalogRootRoute

@Serializable
data class LayoutDetailRoute(val sampleId: Int) : CatalogRoute
