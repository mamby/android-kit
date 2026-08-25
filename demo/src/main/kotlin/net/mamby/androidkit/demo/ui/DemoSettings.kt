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
    val floatingSurfaceOpacity: Float = DefaultFloatingSurfaceOpacity,
    val showCompactNavigationLabels: Boolean = false,
)

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
                floatingSurfaceOpacity = preferences[FloatingSurfaceOpacityKey]
                    ?.coerceIn(MinimumFloatingSurfaceOpacity, MaximumFloatingSurfaceOpacity)
                    ?: preferences[FloatingSurfacesTransparentKey]?.let { wasTransparent ->
                        if (wasTransparent) {
                            DefaultFloatingSurfaceOpacity
                        } else {
                            MaximumFloatingSurfaceOpacity
                        }
                    }
                    ?: DefaultFloatingSurfaceOpacity,
                showCompactNavigationLabels = preferences[ShowCompactNavigationLabelsKey] ?: false,
            )
        }

    suspend fun setThemeChoice(choice: DemoThemeChoice) {
        dataStore.edit { preferences ->
            preferences[ThemeChoiceKey] = choice.storedValue
        }
    }

    suspend fun setFloatingSurfaceOpacity(opacity: Float) {
        dataStore.edit { preferences ->
            preferences[FloatingSurfaceOpacityKey] = opacity.coerceIn(
                MinimumFloatingSurfaceOpacity,
                MaximumFloatingSurfaceOpacity,
            )
            preferences.remove(FloatingSurfacesTransparentKey)
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

    fun setFloatingSurfaceOpacity(opacity: Float) {
        viewModelScope.launch {
            repository.setFloatingSurfaceOpacity(opacity)
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
private val ShowCompactNavigationLabelsKey = booleanPreferencesKey(
    "show_compact_navigation_labels",
)

internal const val MinimumFloatingSurfaceOpacity = 0f
internal const val MaximumFloatingSurfaceOpacity = 1f
internal const val DefaultFloatingSurfaceOpacity = 0.7f
