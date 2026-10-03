package net.mamby.androidkit.testing

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamby.androidkit.compose.form.AndroidKitSearchHistorySnapshot
import net.mamby.androidkit.compose.form.AndroidKitSettingsStorageProtection
import net.mamby.androidkit.compose.form.AndroidKitSettingsStore
import net.mamby.androidkit.compose.form.AndroidKitSettingsStoreMigration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsPersistenceTest {
    @Test
    fun encryptedHistoryAndPreferencesSurviveStoreRecreationAndMigrateOnce() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "persistence-${UUID.randomUUID()}"
        val file = File(context.cacheDir, "$name/settings")
        val theme = stringPreferencesKey("theme")
        val language = stringPreferencesKey("language")
        val opacity = floatPreferencesKey("opacity")
        val appLock = booleanPreferencesKey("app-lock")
        var historyReads = 0
        var preferenceReads = 0
        var cleanups = 0
        val migration = object : AndroidKitSettingsStoreMigration {
            override val id = "legacy"
            override suspend fun readHistories(): Map<String, AndroidKitSearchHistorySnapshot> {
                historyReads++
                return mapOf("settings" to AndroidKitSearchHistorySnapshot(listOf("private-query-4821"), false))
            }
            override suspend fun readPreferences(): Preferences {
                preferenceReads++
                return preferencesOf(theme to "dark", language to "fr", opacity to 75f, appLock to true)
            }
            override suspend fun cleanUp() { cleanups++ }
        }
        suspend fun session(check: suspend (AndroidKitSettingsStore) -> Unit) {
            val job = SupervisorJob()
            val store = AndroidKitSettingsStore.create(file, name, AndroidKitSettingsStorageProtection.Encrypted,
                listOf(migration), CoroutineScope(job + Dispatchers.IO))
            try { check(store) } finally { job.cancelAndJoin() }
        }
        session { store ->
            val history = store.searchHistory("settings")
            assertEquals(emptyList<String>(), history.snapshots.first().recentQueries)
            assertFalse(history.snapshots.first().enabled)
            assertEquals("dark", store.setting(theme, "system").values.first())
            assertEquals("fr", store.setting(language, "system").values.first())
            assertEquals(75f, store.setting(opacity, 0f).values.first())
            assertTrue(store.setting(appLock, false).values.first())
            history.record("ignored-while-disabled")
            assertEquals(emptyList<String>(), history.snapshots.first().recentQueries)
            history.setEnabled(true)
            history.clear()
            coroutineScope {
                launch { history.record("private-query-4821") }
                launch { history.record("second-private-query-9916") }
            }
            store.searchHistory("content").record("content-only")
            store.setting(opacity, 0f).set(40f)
        }
        assertFalse(file.readBytes().toString(Charsets.ISO_8859_1).contains("private-query"))
        assertFalse(File(file.parentFile, "${file.name}.preferences").readBytes().toString(Charsets.ISO_8859_1).contains("dark"))
        session { store ->
            val saved = store.searchHistory("settings").snapshots.first()
            assertEquals(setOf("private-query-4821", "second-private-query-9916"), saved.recentQueries.toSet())
            assertTrue(saved.enabled)
            assertEquals(listOf("content-only"), store.searchHistory("content").snapshots.first().recentQueries)
            assertEquals(40f, store.setting(opacity, 0f).values.first())
            assertEquals(1, historyReads)
            assertEquals(1, preferenceReads)
            assertEquals(1, cleanups)
        }
    }

    @Test
    fun disablingIsAtomicWithConcurrentRecordingAndSurvivesRecreation() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "disabled-${UUID.randomUUID()}"
        val file = File(context.cacheDir, "$name/settings")
        suspend fun session(check: suspend (AndroidKitSettingsStore) -> Unit) {
            val job = SupervisorJob()
            val store = AndroidKitSettingsStore.create(file, name, AndroidKitSettingsStorageProtection.Encrypted,
                emptyList(), CoroutineScope(job + Dispatchers.IO))
            try { check(store) } finally { job.cancelAndJoin() }
        }
        session { store ->
            val history = store.searchHistory("settings")
            history.record("existing")
            store.searchHistory("content").record("independent")
            coroutineScope {
                launch { history.setEnabled(false) }
                launch { repeat(20) { history.record("pending-$it") } }
            }
            assertEquals(AndroidKitSearchHistorySnapshot(emptyList(), false), history.snapshots.first())
        }
        session { store ->
            val history = store.searchHistory("settings")
            history.record("still-disabled")
            assertEquals(AndroidKitSearchHistorySnapshot(emptyList(), false), history.snapshots.first())
            assertEquals(listOf("independent"), store.searchHistory("content").snapshots.first().recentQueries)
            history.setEnabled(true)
            assertEquals(emptyList<String>(), history.snapshots.first().recentQueries)
            history.record("new")
            history.clear()
            assertTrue(history.snapshots.first().enabled)
            history.record("after-clear")
            assertEquals(listOf("after-clear"), history.snapshots.first().recentQueries)
        }
    }

    @Test
    fun legacyHiddenHistoryIsErasedOnDiskBeforeItIsExposed() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "legacy-${UUID.randomUUID()}"
        val file = File(context.cacheDir, "$name/settings")
        file.parentFile!!.mkdirs()
        file.writeText("""{"version":1,"histories":{"settings":{"queries":["hidden-sensitive"],"visible":false},"content":{"queries":["retained"],"visible":true}},"migrations":["legacy-import"]}""")
        val job = SupervisorJob()
        val store = AndroidKitSettingsStore.create(file, name, AndroidKitSettingsStorageProtection.Plaintext,
            emptyList(), CoroutineScope(job + Dispatchers.IO))
        try {
            assertEquals(AndroidKitSearchHistorySnapshot(emptyList(), false), store.searchHistory("settings").snapshots.first())
            assertEquals(listOf("retained"), store.searchHistory("content").snapshots.first().recentQueries)
            val persisted = org.json.JSONObject(file.readText())
            assertEquals(2, persisted.getInt("version"))
            assertFalse(file.readText().contains("hidden-sensitive"))
            assertEquals("legacy-import", persisted.getJSONArray("migrations").getString(0))
        } finally { job.cancelAndJoin() }
    }

    @Test
    fun tamperedEncryptedHistoryFailsWithoutReplacingTheFile() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "tamper-${UUID.randomUUID()}"
        val file = File(context.cacheDir, "$name/settings")
        var job = SupervisorJob()
        var store = AndroidKitSettingsStore.create(file, name, AndroidKitSettingsStorageProtection.Encrypted,
            emptyList(), CoroutineScope(job + Dispatchers.IO))
        store.searchHistory("settings").record("private-query")
        job.cancelAndJoin()
        val damaged = file.readBytes().also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }
        file.writeBytes(damaged)
        job = SupervisorJob()
        store = AndroidKitSettingsStore.create(file, name, AndroidKitSettingsStorageProtection.Encrypted,
            emptyList(), CoroutineScope(job + Dispatchers.IO))
        try {
            assertTrue(runCatching { store.searchHistory("settings").snapshots.first() }.isFailure)
            assertTrue(file.readBytes().contentEquals(damaged))
        } finally { job.cancelAndJoin() }
    }
}
