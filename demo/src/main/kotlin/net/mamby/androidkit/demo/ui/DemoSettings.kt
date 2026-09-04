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
import kotlin.math.roundToInt
import net.mamby.androidkit.compose.theme.AndroidKitFloatingSurfaceDefaults

internal data class DemoSettings(
    val themeChoice: DemoThemeChoice = DemoThemeChoice.Light,
    val floatingSurfaceOpacityLevel: Float = DefaultFloatingSurfaceOpacityLevel,
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
                floatingSurfaceOpacityLevel = normalizeFloatingSurfaceOpacityLevel(
                    preferences[FloatingSurfaceOpacityLevelKey]
                        ?: DefaultFloatingSurfaceOpacityLevel,
                ),
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

    suspend fun setFloatingSurfaceOpacityLevel(level: Float) {
        dataStore.edit { preferences ->
            preferences[FloatingSurfaceOpacityLevelKey] =
                normalizeFloatingSurfaceOpacityLevel(level)
            preferences.remove(FloatingSurfaceOpacityKey)
            preferences.remove(FloatingSurfacesTransparentKey)
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
    val settings = repository.settings
        .map<DemoSettings, DemoSettings?> { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    fun setThemeChoice(choice: DemoThemeChoice) {
        viewModelScope.launch {
            repository.setThemeChoice(choice)
        }
    }

    fun setFloatingSurfaceOpacityLevel(level: Float) {
        viewModelScope.launch {
            repository.setFloatingSurfaceOpacityLevel(level)
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
private val FloatingSurfaceOpacityLevelKey = floatPreferencesKey(
    "floating_surface_opacity_level",
)
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

internal const val MinimumFloatingSurfaceOpacityLevel: Float =
    AndroidKitFloatingSurfaceDefaults.MinimumOpacityLevel
internal const val MaximumFloatingSurfaceOpacityLevel: Float =
    AndroidKitFloatingSurfaceDefaults.MaximumOpacityLevel
internal const val DefaultFloatingSurfaceOpacityLevel: Float =
    AndroidKitFloatingSurfaceDefaults.DefaultOpacityLevel
internal const val FloatingSurfaceOpacityLevelStep: Float = 5f

internal fun normalizeFloatingSurfaceOpacityLevel(level: Float): Float {
    if (!level.isFinite()) return DefaultFloatingSurfaceOpacityLevel

    val clampedLevel = level.coerceIn(
        MinimumFloatingSurfaceOpacityLevel,
        MaximumFloatingSurfaceOpacityLevel,
    )
    return (
        clampedLevel / FloatingSurfaceOpacityLevelStep
    ).roundToInt() * FloatingSurfaceOpacityLevelStep
}
