package net.mamby.androidkit.compose.form

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import okio.Buffer
import okio.buffer
import okio.source
import okio.sink

/** Explicit storage policy. Encrypted uses a non-exportable, app-owned Android Keystore key. */
public enum class AndroidKitSettingsStorageProtection { Plaintext, Encrypted }

/** One logical page's durable history and privacy preference. */
public data class AndroidKitSearchHistorySnapshot(
    public val recentQueries: List<String> = emptyList(),
    public val visible: Boolean = true,
)

/** Import existing host preferences once; cleanup runs only after the Kit transaction succeeds. */
public interface AndroidKitSettingsStoreMigration {
    public val id: String
    public suspend fun readHistories(): Map<String, AndroidKitSearchHistorySnapshot>
    public suspend fun cleanUp()
    public suspend fun readPreferences(): Preferences = emptyPreferences()
    public suspend fun cleanUpPreferences(): Unit = Unit
}

/**
 * Kit-owned, transactional storage independent of navigation and composition lifetimes.
 * Files live in noBackupFilesDir. Unreadable data is reported, never replaced with empty history.
 * Hosts apply preference effects and authorize protected changes. Kit owns durable storage.
 */
public class AndroidKitSettingsStore private constructor(
    private val dataStore: DataStore<StoredSettings>,
    private val scope: CoroutineScope,
    public val preferences: DataStore<Preferences>,
) {
    private val settings = mutableMapOf<Preferences.Key<*>, AndroidKitPersistentSetting<*>>()
    /** A typed binding backed only by this store's real DataStore, never arbitrary callbacks. */
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> setting(key: Preferences.Key<T>, defaultValue: T): AndroidKitPersistentSetting<T> = synchronized(settings) {
        val existing = settings[key]
        if (existing != null) {
            require(existing.defaultValue::class == defaultValue::class) { "Setting key type changed." }
            existing as AndroidKitPersistentSetting<T>
        } else {
            AndroidKitPersistentSetting(this, key, defaultValue).also { settings[key] = it }
        }
    }

    internal fun observe(operation: suspend () -> Unit) { scope.launch { operation() } }
    /** Use a stable, untranslated key for each logical search page. */
    public fun searchHistory(pageKey: String): AndroidKitPersistentSearchHistory {
        require(pageKey.isNotBlank()) { "Search page keys must not be blank." }
        return AndroidKitPersistentSearchHistory(this, pageKey)
    }

    /** Erases all queries and visibility preferences, without resetting migration markers. */
    public suspend fun clearSearchHistories(): Unit {
        dataStore.updateData { it.copy(histories = emptyMap()) }
    }

    internal fun history(pageKey: String): Flow<AndroidKitSearchHistorySnapshot> =
        dataStore.data.map { it.histories[pageKey] ?: AndroidKitSearchHistorySnapshot() }
            .distinctUntilChanged()

    internal suspend fun updateHistory(
        pageKey: String,
        update: (AndroidKitSearchHistorySnapshot) -> AndroidKitSearchHistorySnapshot,
    ) {
        dataStore.updateData { current ->
            val updated = update(current.histories[pageKey] ?: AndroidKitSearchHistorySnapshot())
            current.copy(histories = current.histories + (pageKey to updated.sanitized()))
        }
    }

    internal fun submit(onFailure: (Throwable) -> Unit, operation: suspend () -> Unit) {
        scope.launch {
            try {
                operation()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                withContext(Dispatchers.Main.immediate) { onFailure(failure) }
            }
        }
    }

    public companion object {
        private val instances = mutableMapOf<String, Pair<AndroidKitSettingsStorageProtection, AndroidKitSettingsStore>>()

        /**
         * Returns one process-wide store per name. Configure migrations on the first call,
         * normally in the application's singleton DI provider. Protection cannot change in place.
         */
        public fun open(
            context: Context,
            name: String,
            protection: AndroidKitSettingsStorageProtection,
            migrations: List<AndroidKitSettingsStoreMigration> = emptyList(),
        ): AndroidKitSettingsStore = synchronized(instances) {
            require(name.matches(Regex("[a-zA-Z0-9_-]+"))) { "Invalid settings store name." }
            require(migrations.map { it.id }.distinct().size == migrations.size && migrations.all { it.id.isNotBlank() })
            val file = File(context.applicationContext.noBackupFilesDir, "androidkit/$name.settings")
            val existing = instances[file.absolutePath]
            if (existing != null) {
                require(existing.first == protection) { "Settings storage protection cannot change in place." }
                existing.second
            } else {
                val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                create(file, name, protection, migrations, scope).also {
                    instances[file.absolutePath] = protection to it
                }
            }
        }

        internal fun create(
            file: File,
            name: String,
            protection: AndroidKitSettingsStorageProtection,
            migrations: List<AndroidKitSettingsStoreMigration>,
            scope: CoroutineScope,
        ): AndroidKitSettingsStore = AndroidKitSettingsStore(
            DataStoreFactory.create(
                serializer = SettingsSerializer(name, protection),
                migrations = migrations.map { migration ->
                    object : DataMigration<StoredSettings> {
                        override suspend fun shouldMigrate(currentData: StoredSettings) = migration.id !in currentData.migrations
                        override suspend fun migrate(currentData: StoredSettings): StoredSettings = currentData.copy(
                            histories = migration.readHistories().mapValues { it.value.sanitized() } + currentData.histories,
                            migrations = currentData.migrations + migration.id,
                        )
                        override suspend fun cleanUp() = migration.cleanUp()
                    }
                },
                scope = scope,
                produceFile = { file },
            ),
            scope,
            DataStoreFactory.create(
                serializer = PersistentPreferencesSerializer(name, protection),
                migrations = migrations.map { migration ->
                    object : DataMigration<Preferences> {
                        private val marker = booleanPreferencesKey("androidkit.migration.${migration.id}")
                        override suspend fun shouldMigrate(currentData: Preferences) = currentData[marker] != true
                        override suspend fun migrate(currentData: Preferences): Preferences {
                            val imported = migration.readPreferences().toMutablePreferences()
                            imported.putAll(*currentData.asMap().map { (key, value) -> preferencePair(key, value) }.toTypedArray())
                            imported[marker] = true
                            return imported.toPreferences()
                        }
                        override suspend fun cleanUp() = migration.cleanUpPreferences()
                    }
                },
                scope = scope,
                produceFile = { File(file.parentFile, "${file.name}.preferences") },
            ),
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun preferencePair(key: Preferences.Key<*>, value: Any): Preferences.Pair<Any> =
    (key as Preferences.Key<Any>) to value

public class AndroidKitPersistentSetting<T : Any> internal constructor(
    internal val store: AndroidKitSettingsStore,
    public val key: Preferences.Key<T>,
    public val defaultValue: T,
) {
    public val values: Flow<T> = store.preferences.data.map { it[key] ?: defaultValue }.distinctUntilChanged()
    internal var loadedValue: T? by mutableStateOf(null)
        private set
    internal var failure: Throwable? by mutableStateOf(null)
        private set
    private var preview: T? by mutableStateOf(null)
    internal val currentValue: T get() = preview ?: loadedValue ?: defaultValue
    init {
        store.observe {
            try { values.collect { value -> withContext(Dispatchers.Main.immediate) { loadedValue = value } } }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { withContext(Dispatchers.Main.immediate) { failure = error } }
        }
    }
    public suspend fun set(value: T): Unit { store.preferences.edit { it[key] = value } }
    internal fun preview(value: T) { preview = value }
    internal fun savePreview(onFailure: (Throwable) -> Unit, onSaved: () -> Unit) {
        val value = preview ?: return
        store.submit(onFailure) {
            try {
                set(value)
                withContext(Dispatchers.Main.immediate) { onSaved() }
            } finally {
                withContext(Dispatchers.Main.immediate) { if (preview == value) preview = null }
            }
        }
    }
    internal fun submit(value: T, onFailure: (Throwable) -> Unit, onSaved: () -> Unit) {
        store.submit(onFailure) {
            set(value)
            withContext(Dispatchers.Main.immediate) { onSaved() }
        }
    }
}

private class PersistentPreferencesSerializer(
    name: String,
    protection: AndroidKitSettingsStorageProtection,
) : Serializer<Preferences> {
    private val encryption = if (protection == AndroidKitSettingsStorageProtection.Encrypted) SettingsEncryption("$name-preferences") else null
    override val defaultValue: Preferences = emptyPreferences()
    override suspend fun readFrom(input: InputStream): Preferences {
        if (encryption == null) return PreferencesSerializer.readFrom(input.source().buffer())
        val plaintext = encryption.decrypt(input.readBytes())
        val buffer = Buffer().write(plaintext)
        try { return PreferencesSerializer.readFrom(buffer) } finally { plaintext.fill(0); buffer.clear() }
    }
    override suspend fun writeTo(t: Preferences, output: OutputStream) {
        if (encryption == null) {
            val sink = output.sink().buffer()
            PreferencesSerializer.writeTo(t, sink)
            sink.flush()
            return
        }
        val buffer = Buffer()
        PreferencesSerializer.writeTo(t, buffer)
        val plaintext = buffer.readByteArray()
        try { output.write(encryption.encrypt(plaintext)) } finally { plaintext.fill(0) }
    }
}

/** Constructible only by the persistent Kit store; cannot be backed by screen-local state. */
public class AndroidKitPersistentSearchHistory internal constructor(
    private val store: AndroidKitSettingsStore,
    private val pageKey: String,
) {
    public val snapshots: Flow<AndroidKitSearchHistorySnapshot> = store.history(pageKey)

    public suspend fun record(query: String): Unit {
        val value = query.trim()
        if (value.isEmpty()) return
        store.updateHistory(pageKey) { current ->
            current.copy(recentQueries = listOf(value) + current.recentQueries.filterNot {
                normalizeSearchText(it) == normalizeSearchText(value)
            })
        }
    }

    public suspend fun remove(query: String): Unit = store.updateHistory(pageKey) { current ->
        current.copy(recentQueries = current.recentQueries.filterNot {
            normalizeSearchText(it) == normalizeSearchText(query)
        })
    }

    public suspend fun clear(): Unit = store.updateHistory(pageKey) { it.copy(recentQueries = emptyList()) }
    public suspend fun setVisible(visible: Boolean): Unit = store.updateHistory(pageKey) { it.copy(visible = visible) }

    internal fun submit(onFailure: (Throwable) -> Unit, operation: suspend AndroidKitPersistentSearchHistory.() -> Unit) =
        store.submit(onFailure) { operation() }
}

private fun AndroidKitSearchHistorySnapshot.sanitized(): AndroidKitSearchHistorySnapshot {
    val seen = mutableSetOf<String>()
    return copy(recentQueries = recentQueries.map(String::trim).filter {
        it.isNotEmpty() && seen.add(normalizeSearchText(it))
    }.take(MaximumRecentQueries))
}

private const val MaximumRecentQueries = 10
private const val StorageVersion = 1

private data class StoredSettings(
    val histories: Map<String, AndroidKitSearchHistorySnapshot> = emptyMap(),
    val migrations: Set<String> = emptySet(),
)

private class SettingsSerializer(
    name: String,
    protection: AndroidKitSettingsStorageProtection,
) : Serializer<StoredSettings> {
    private val encryption = if (protection == AndroidKitSettingsStorageProtection.Encrypted) SettingsEncryption(name) else null
    override val defaultValue = StoredSettings()

    override suspend fun readFrom(input: InputStream): StoredSettings {
        val encoded = input.readBytes()
        val plaintext = encryption?.decrypt(encoded) ?: encoded
        try {
            val root = JSONObject(plaintext.toString(Charsets.UTF_8))
            if (root.getInt("version") != StorageVersion) throw CorruptionException("Unsupported settings storage version.")
            val histories = root.getJSONObject("histories")
            val values = histories.keys().asSequence().associateWith { key ->
                val history = histories.getJSONObject(key)
                AndroidKitSearchHistorySnapshot(history.getJSONArray("queries").strings(), history.getBoolean("visible")).sanitized()
            }
            return StoredSettings(values, root.getJSONArray("migrations").strings().toSet())
        } catch (failure: JSONException) {
            throw CorruptionException("Unreadable settings storage.", failure)
        } finally {
            plaintext.fill(0)
        }
    }

    override suspend fun writeTo(t: StoredSettings, output: OutputStream) {
        val histories = JSONObject()
        t.histories.forEach { (key, history) ->
            histories.put(key, JSONObject().put("queries", JSONArray(history.recentQueries)).put("visible", history.visible))
        }
        val plaintext = JSONObject().put("version", StorageVersion).put("histories", histories)
            .put("migrations", JSONArray(t.migrations.toList())).toString().toByteArray(Charsets.UTF_8)
        try {
            output.write(encryption?.encrypt(plaintext) ?: plaintext)
        } finally {
            plaintext.fill(0)
        }
    }
}

private fun JSONArray.strings(): List<String> = (0 until length()).map(::getString)

private class SettingsEncryption(name: String) {
    private val alias = "net.mamby.androidkit.settings.$name.v1"
    private val associatedData = alias.toByteArray(Charsets.UTF_8)

    private fun key(create: Boolean): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(alias, null) as? SecretKey)?.let { return it }
        check(create) { "Settings encryption key is unavailable." }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KeySizeBits)
                .setRandomizedEncryptionRequired(true)
                .build())
        }.generateKey()
    }

    fun encrypt(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, key(create = true))
            updateAAD(associatedData)
        }
        return byteArrayOf(EnvelopeVersion, cipher.iv.size.toByte()) + cipher.iv + cipher.doFinal(plaintext)
    }

    fun decrypt(envelope: ByteArray): ByteArray {
        if (envelope.size < HeaderSize || envelope[0] != EnvelopeVersion) throw CorruptionException("Invalid encrypted settings envelope.")
        val nonceSize = envelope[1].toInt() and 0xff
        if (nonceSize != NonceSize || envelope.size < HeaderSize + nonceSize + TagSizeBytes) throw CorruptionException("Truncated encrypted settings envelope.")
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(Cipher.DECRYPT_MODE, key(create = false), GCMParameterSpec(TagSizeBits, envelope.copyOfRange(HeaderSize, HeaderSize + nonceSize)))
                updateAAD(associatedData)
            }
            return cipher.doFinal(envelope, HeaderSize + nonceSize, envelope.size - HeaderSize - nonceSize)
        } catch (failure: GeneralSecurityException) {
            throw CorruptionException("Encrypted settings could not be authenticated.", failure)
        } catch (failure: IllegalStateException) {
            throw CorruptionException("Settings encryption key is unavailable.", failure)
        }
    }

    private companion object {
        const val EnvelopeVersion: Byte = 1
        const val HeaderSize = 2
        const val KeySizeBits = 256
        const val NonceSize = 12
        const val TagSizeBits = 128
        const val TagSizeBytes = TagSizeBits / Byte.SIZE_BITS
    }
}
