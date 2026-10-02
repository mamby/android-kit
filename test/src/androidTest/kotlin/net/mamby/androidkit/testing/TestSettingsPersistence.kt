package net.mamby.androidkit.testing

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import androidx.compose.ui.test.IdlingResource
import net.mamby.androidkit.compose.form.AndroidKitPersistentSetting
import net.mamby.androidkit.compose.form.AndroidKitSettingsStore
import net.mamby.androidkit.compose.form.AndroidKitSettingsStorageProtection

internal val LocalTestSettingsPersistence = staticCompositionLocalOf<TestSettingsPersistence> { error("TestKitTheme is required") }

internal object SettingsPersistenceIdlingResource : IdlingResource {
    private val readiness = CopyOnWriteArrayList<() -> Boolean>()
    val pendingWrites = AtomicInteger()
    override val isIdleNow: Boolean get() = pendingWrites.get() == 0 && readiness.all { it() }
    fun track(ready: () -> Boolean) { readiness += ready }
    fun reset() { readiness.clear(); pendingWrites.set(0) }
}

/** Real persistent bindings, with explicit host-accepted source updates for existing fixtures. */
internal class TestSettingsPersistence(context: Context) {
    val store = AndroidKitSettingsStore.open(context, "test-${UUID.randomUUID()}", AndroidKitSettingsStorageProtection.Plaintext)
    private val sources = mutableMapOf<String, Any>()
    fun testSetting(key: String, value: String): AndroidKitPersistentSetting<String> =
        source(key, value, store.setting(stringPreferencesKey(key), value))
    fun testSetting(key: String, value: Boolean): AndroidKitPersistentSetting<Boolean> =
        source(key, value, store.setting(booleanPreferencesKey(key), value))
    fun testSetting(key: String, value: Float): AndroidKitPersistentSetting<Float> =
        source(key, value, store.setting(floatPreferencesKey(key), value))
    private fun <T : Any> source(key: String, value: T, binding: AndroidKitPersistentSetting<T>): AndroidKitPersistentSetting<T> {
        val previous = sources.put(key, value)
        if (previous == null) SettingsPersistenceIdlingResource.track { binding.loadedValue != null }
        if (previous != null && previous != value) {
            SettingsPersistenceIdlingResource.pendingWrites.incrementAndGet()
            binding.submit(value, { SettingsPersistenceIdlingResource.pendingWrites.decrementAndGet(); throw it },
                { SettingsPersistenceIdlingResource.pendingWrites.decrementAndGet() })
        }
        return binding
    }
}
