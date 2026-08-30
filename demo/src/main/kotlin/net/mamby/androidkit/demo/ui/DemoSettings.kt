package net.mamby.androidkit.demo.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal data class DemoSettings(
    val themeChoice: DemoThemeChoice = DemoThemeChoice.Light,
    val floatingSurfacesTransparent: Boolean = false,
    val floatingNavigationLayout: DemoFloatingNavigationLayout =
        DemoFloatingNavigationLayout.FiveItemsWithMore,
    val showCompactNavigationLabels: Boolean = false,
)

internal enum class DemoFloatingNavigationLayout(
    val storedValue: String,
) {
    ThreeItemsWithoutMore("three_items_without_more"),
    FiveItemsWithMore("five_items_with_more"),
    SevenItemsWithMore("seven_items_with_more"),
    ;

    companion object {
        fun fromStoredValue(value: String?): DemoFloatingNavigationLayout = when (value) {
            ThreeItemsWithoutMore.storedValue,
            "three_destinations",
            -> ThreeItemsWithoutMore

            SevenItemsWithMore.storedValue -> SevenItemsWithMore

            else -> FiveItemsWithMore
        }
    }
}

internal class DemoSettingsRepository(context: Context) {
    private val dataStore = context.applicationContext.demoSettingsDataStore

    val settings: Flow<DemoSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            DemoSettings(
                themeChoice = DemoThemeChoice.fromStoredValue(
                    preferences[ThemeChoiceKey],
                ),
                floatingSurfacesTransparent = preferences[FloatingSurfacesTransparentKey]
                    ?: preferences[FloatingSurfaceOpacityKey]?.let { opacity ->
                        opacity < OpaqueFloatingSurfaceOpacity
                    }
                    ?: false,
                floatingNavigationLayout = DemoFloatingNavigationLayout.fromStoredValue(
                    preferences[FloatingNavigationLayoutKey],
                ),
                showCompactNavigationLabels = preferences[ShowCompactNavigationLabelsKey]
                    ?: false,
            )
        }

    suspend fun setThemeChoice(choice: DemoThemeChoice) {
        dataStore.edit { preferences ->
            preferences[ThemeChoiceKey] = choice.storedValue
        }
    }

    suspend fun setFloatingSurfacesTransparent(isTransparent: Boolean) {
        dataStore.edit { preferences ->
            preferences[FloatingSurfacesTransparentKey] = isTransparent
            preferences.remove(FloatingSurfaceOpacityKey)
        }
    }

    suspend fun setFloatingNavigationLayout(layout: DemoFloatingNavigationLayout) {
        dataStore.edit { preferences ->
            preferences[FloatingNavigationLayoutKey] = layout.storedValue
        }
    }

    suspend fun setShowCompactNavigationLabels(showLabels: Boolean) {
        dataStore.edit { preferences ->
            preferences[ShowCompactNavigationLabelsKey] = showLabels
        }
    }

}

internal class DemoSettingsViewModel(
    private val repository: DemoSettingsRepository,
) : ViewModel() {
    val settings = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DemoSettings(),
    )

    fun setThemeChoice(choice: DemoThemeChoice) {
        viewModelScope.launch {
            repository.setThemeChoice(choice)
        }
    }

    fun setFloatingSurfacesTransparent(isTransparent: Boolean) {
        viewModelScope.launch {
            repository.setFloatingSurfacesTransparent(isTransparent)
        }
    }

    fun setFloatingNavigationLayout(layout: DemoFloatingNavigationLayout) {
        viewModelScope.launch {
            repository.setFloatingNavigationLayout(layout)
        }
    }

    fun setShowCompactNavigationLabels(showLabels: Boolean) {
        viewModelScope.launch {
            repository.setShowCompactNavigationLabels(showLabels)
        }
    }

}

private val Context.demoSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "demo_settings",
)

private val ThemeChoiceKey = stringPreferencesKey("theme_choice")
private val FloatingSurfaceOpacityKey = floatPreferencesKey("floating_surface_opacity")
private val FloatingSurfacesTransparentKey = booleanPreferencesKey(
    "floating_surfaces_transparent",
)
private val FloatingNavigationLayoutKey = stringPreferencesKey(
    "floating_navigation_layout",
)
private val ShowCompactNavigationLabelsKey = booleanPreferencesKey(
    "show_compact_navigation_labels",
)
internal const val TransparentFloatingSurfaceOpacity = 0.92f
internal const val OpaqueFloatingSurfaceOpacity = 1f
