package net.mamby.androidkit.demo.ui

import android.content.Context
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import net.mamby.androidkit.demo.ui.authentication.DemoAuthenticationState
import net.mamby.androidkit.compose.theme.AndroidKitFloatingSurfaceDefaults
import org.json.JSONArray
import net.mamby.androidkit.compose.form.AndroidKitSettingsStore
import net.mamby.androidkit.compose.form.AndroidKitSettingsStoreMigration
import net.mamby.androidkit.compose.form.AndroidKitSettingsStorageProtection
import net.mamby.androidkit.compose.form.AndroidKitSearchHistorySnapshot
import kotlinx.coroutines.flow.first

internal data class DemoSettings(
    val demoToggles: Set<DemoToggle> = DemoToggle.entries.filter { it.defaultValue }.toSet(),
    val selectedPageAction: DemoPageAction? = null,
    val pageHeaderActionPresentation: DemoHeaderActionPresentation =
        DemoHeaderActionPresentation.Mixed,
    val sheetHeaderActionPresentation: DemoHeaderActionPresentation =
        DemoHeaderActionPresentation.Mixed,
    val appLockEnabled: Boolean = false,
    val appLockTimeout: DemoAppLockTimeout = DemoAppLockTimeout.Immediately,
    val themeChoice: DemoThemeChoice = DemoThemeChoice.System,
    val floatingSurfaceOpacityLevel: Float = DefaultFloatingSurfaceOpacityLevel,
    val floatingNavigationLayout: DemoFloatingNavigationLayout =
        DemoFloatingNavigationLayout.FiveItemsWithMore,
    val showCompactNavigationLabels: Boolean = false,
    val recentSettingsSearches: List<String> = emptyList(),
    val recentContentSearches: List<String> = emptyList(),
    val recentSettingsSearchesVisible: Boolean = true,
    val recentContentSearchesVisible: Boolean = true,
)

enum class DemoAppLockTimeout(val duration: Duration) {
    Immediately(Duration.ZERO),
    OneMinute(1.minutes),
    FiveMinutes(5.minutes),
    FifteenMinutes(15.minutes),
}

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
    private val legacyDataStore = context.applicationContext.demoSettingsDataStore
    val kitSettingsStore = AndroidKitSettingsStore.open(
        context, "demo-settings", AndroidKitSettingsStorageProtection.Plaintext,
        listOf(object : AndroidKitSettingsStoreMigration {
            override val id = "demo-settings-v1"
            override suspend fun readHistories(): Map<String, AndroidKitSearchHistorySnapshot> {
                val old = legacyDataStore.data.first()
                return mapOf("settings" to AndroidKitSearchHistorySnapshot(
                    old[RecentSettingsSearchesKey]?.let(::decodeStringList).orEmpty(),
                    old[RecentSettingsSearchesVisibleKey] ?: true,
                ))
            }
            override suspend fun readPreferences(): Preferences {
                val preferences = legacyDataStore.data.first().toMutablePreferences()
                preferences[stringPreferencesKey("selected_language_tag")] =
                    androidx.core.app.LocaleManagerCompat.getApplicationLocales(context).get(0)?.language ?: "system"
                return preferences.toPreferences()
            }
            override suspend fun cleanUp() = Unit
        }),
    )
    private val dataStore = kitSettingsStore.preferences

    val settings: Flow<DemoSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                // A read failure must never silently disable a persisted app lock.
                emit(preferencesOf(
                    AppLockEnabledKey to true,
                    RecentSettingsSearchesVisibleKey to false,
                    RecentContentSearchesVisibleKey to false,
                ))
            } else {
                throw exception
            }
        }
        .map { preferences ->
            DemoSettings(
                demoToggles = DemoToggle.entries.filter { toggle ->
                    preferences[booleanPreferencesKey("demo_toggle_${toggle.name}")]
                        ?: toggle.defaultValue
                }.toSet(),
                selectedPageAction = DemoPageAction.entries.firstOrNull {
                    it.name == preferences[SelectedPageActionKey]
                },
                pageHeaderActionPresentation = DemoHeaderActionPresentation.entries.firstOrNull {
                    it.name == preferences[PageHeaderActionPresentationKey]
                } ?: DemoHeaderActionPresentation.Mixed,
                sheetHeaderActionPresentation = DemoHeaderActionPresentation.entries.firstOrNull {
                    it.name == preferences[SheetHeaderActionPresentationKey]
                } ?: DemoHeaderActionPresentation.Mixed,
                appLockEnabled = preferences[AppLockEnabledKey] ?: false,
                appLockTimeout = DemoAppLockTimeout.entries.firstOrNull {
                    it.name == preferences[AppLockTimeoutKey]
                } ?: DemoAppLockTimeout.Immediately,
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
                recentSettingsSearchesVisible = preferences[RecentSettingsSearchesVisibleKey] ?: true,
                recentContentSearchesVisible = preferences[RecentContentSearchesVisibleKey] ?: true,
                recentSettingsSearches = preferences[RecentSettingsSearchesKey]
                    ?.let(::decodeStringList)
                    .orEmpty(),
                recentContentSearches = preferences[RecentContentSearchesKey]
                    ?.let(::decodeStringList)
                    .orEmpty(),
            )
        }

    suspend fun setDemoToggle(toggle: DemoToggle, enabled: Boolean) {
        dataStore.edit { it[booleanPreferencesKey("demo_toggle_${toggle.name}")] = enabled }
    }

    suspend fun setSelectedPageAction(action: DemoPageAction) {
        dataStore.edit { it[SelectedPageActionKey] = action.name }
    }

    suspend fun setPageHeaderActionPresentation(
        presentation: DemoHeaderActionPresentation,
    ) {
        dataStore.edit { it[PageHeaderActionPresentationKey] = presentation.name }
    }

    suspend fun setSheetHeaderActionPresentation(
        presentation: DemoHeaderActionPresentation,
    ) {
        dataStore.edit { it[SheetHeaderActionPresentationKey] = presentation.name }
    }

    suspend fun setAppLockEnabled(enabled: Boolean, isAuthorized: () -> Boolean) {
        dataStore.edit { if (isAuthorized()) it[AppLockEnabledKey] = enabled }
    }

    suspend fun setAppLockTimeout(timeout: DemoAppLockTimeout) {
        dataStore.edit { it[AppLockTimeoutKey] = timeout.name }
    }

    suspend fun setRecentSettingsSearchesVisible(visible: Boolean) {
        dataStore.edit { it[RecentSettingsSearchesVisibleKey] = visible }
    }

    suspend fun setRecentContentSearchesVisible(visible: Boolean) {
        dataStore.edit { it[RecentContentSearchesVisibleKey] = visible }
    }

    suspend fun setRecentSettingsSearches(queries: List<String>) {
        dataStore.edit { it[RecentSettingsSearchesKey] = JSONArray(queries).toString() }
    }

    suspend fun setRecentContentSearches(queries: List<String>) {
        dataStore.edit { it[RecentContentSearchesKey] = JSONArray(queries).toString() }
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
    val kitSettingsStore = repository.kitSettingsStore
    var settingsStorageFailure by mutableStateOf<Throwable?>(null)
        private set
    fun settingsStorageFailed(@Suppress("UNUSED_PARAMETER") failure: Throwable) {
        settingsStorageFailure = failure
    }
    fun dismissSettingsStorageFailure() { settingsStorageFailure = null }
    var unlocked by mutableStateOf(false)
        private set
    var authenticating by mutableStateOf(false)
        private set
    var authenticationError by mutableStateOf<String?>(null)
        private set
    private val authentication = DemoAuthenticationState()
    private var authenticationCommit: Job? = null
    private var backgroundedAt: Long? = null
    var settingsWriteFailure by mutableStateOf<DemoSettingsWriteFailure?>(null)
        private set
    private var writeRequest = 0L

    fun onBackground() {
        authentication.invalidate(explicit = false)
        authenticationCommit?.cancel()
        backgroundedAt = SystemClock.elapsedRealtime()
        if ((settings.value?.appLockTimeout ?: DemoAppLockTimeout.Immediately) ==
            DemoAppLockTimeout.Immediately
        ) {
            unlocked = false
        }
    }

    fun onForeground() {
        val leftAt = backgroundedAt ?: return
        backgroundedAt = null
        val timeout = settings.value?.appLockTimeout ?: DemoAppLockTimeout.Immediately
        if (SystemClock.elapsedRealtime() - leftAt >= timeout.duration.inWholeMilliseconds) {
            authentication.invalidate(explicit = false)
            authenticationCommit?.cancel()
            unlocked = false
        }
    }

    fun lock() {
        authentication.invalidate(explicit = true)
        authenticationCommit?.cancel()
        unlocked = false
    }

    fun beginAuthentication(enabled: Boolean?): Boolean {
        if (authenticating) return false
        if (!authentication.begin(enabled)) return false
        authenticationError = null
        authenticating = true
        return true
    }

    fun authenticationSucceeded(deviceCredential: Boolean = false) {
        if (!authenticating) return
        val authorization = authentication.complete(deviceCredential)
        if (authorization == null) {
            authenticating = false
            return
        }
        authenticationCommit = viewModelScope.launch {
            try {
                authorization.enabled?.let { enabled ->
                    repository.setAppLockEnabled(enabled) { authentication.isCurrent(authorization) }
                }
                unlocked = authentication.isCurrent(authorization)
            } catch (exception: IOException) {
                authenticationError = exception.localizedMessage
            } finally {
                authenticating = false
            }
        }
    }

    fun authenticationFailed(message: String) {
        authentication.fail()
        authenticating = false
        authenticationError = message
    }

    val settings = repository.settings
        .map<DemoSettings, DemoSettings?> { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    fun setDemoToggle(toggle: DemoToggle, enabled: Boolean) {
        persist { repository.setDemoToggle(toggle, enabled) }
    }

    fun setSelectedPageAction(action: DemoPageAction) {
        persist { repository.setSelectedPageAction(action) }
    }

    fun setPageHeaderActionPresentation(presentation: DemoHeaderActionPresentation) {
        persist { repository.setPageHeaderActionPresentation(presentation) }
    }

    fun setSheetHeaderActionPresentation(presentation: DemoHeaderActionPresentation) {
        persist { repository.setSheetHeaderActionPresentation(presentation) }
    }

    fun setThemeChoice(choice: DemoThemeChoice) {
        persist { repository.setThemeChoice(choice) }
    }

    fun setAppLockTimeout(timeout: DemoAppLockTimeout) {
        persist { repository.setAppLockTimeout(timeout) }
    }

    fun setFloatingSurfaceOpacityLevel(level: Float) {
        persist { repository.setFloatingSurfaceOpacityLevel(level) }
    }

    fun setFloatingNavigationLayout(layout: DemoFloatingNavigationLayout) {
        persist { repository.setFloatingNavigationLayout(layout) }
    }

    fun setShowCompactNavigationLabels(showLabels: Boolean) {
        persist { repository.setShowCompactNavigationLabels(showLabels) }
    }

    fun setRecentSettingsSearchesVisible(visible: Boolean) {
        persist { repository.setRecentSettingsSearchesVisible(visible) }
    }

    fun setRecentContentSearchesVisible(visible: Boolean) {
        persist { repository.setRecentContentSearchesVisible(visible) }
    }

    fun setRecentSettingsSearches(queries: List<String>) {
        persist { repository.setRecentSettingsSearches(queries) }
    }

    fun setRecentContentSearches(queries: List<String>) {
        persist { repository.setRecentContentSearches(queries) }
    }

    fun dismissSettingsWriteFailure(failure: DemoSettingsWriteFailure) {
        if (settingsWriteFailure === failure) settingsWriteFailure = null
    }

    private fun persist(update: suspend () -> Unit) {
        val request = ++writeRequest
        settingsWriteFailure = null
        viewModelScope.launch {
            try {
                update()
            } catch (_: IOException) {
                if (request == writeRequest) {
                    settingsWriteFailure = DemoSettingsWriteFailure { persist(update) }
                }
            }
        }
    }
}

internal class DemoSettingsWriteFailure(val retry: () -> Unit)

private val Context.demoSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "demo_settings",
)

private val SelectedPageActionKey = stringPreferencesKey("selected_page_action")
private val PageHeaderActionPresentationKey = stringPreferencesKey(
    "page_header_action_presentation",
)
private val SheetHeaderActionPresentationKey = stringPreferencesKey(
    "sheet_header_action_presentation",
)
private val AppLockEnabledKey = booleanPreferencesKey("app_lock_enabled")
private val AppLockTimeoutKey = stringPreferencesKey("app_lock_timeout")
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
private val RecentSettingsSearchesVisibleKey = booleanPreferencesKey("search.settings.recents_visible")
private val RecentContentSearchesVisibleKey = booleanPreferencesKey("search.content.recents_visible")
private val RecentSettingsSearchesKey = stringPreferencesKey("recent_settings_searches")
private val RecentContentSearchesKey = stringPreferencesKey("recent_content_searches")

private fun decodeStringList(value: String): List<String> = runCatching {
    val array = JSONArray(value)
    List(array.length()) { index -> array.getString(index) }
}.getOrDefault(emptyList())

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
