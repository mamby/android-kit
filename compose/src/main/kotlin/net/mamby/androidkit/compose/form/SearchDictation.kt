package net.mamby.androidkit.compose.form

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

internal enum class DictationPhase { Idle, Starting, Listening, Finishing }
internal enum class DictationError { Unavailable, Permission, NoSpeech }

/** Internal seam for testing device callbacks without recording audio. */
internal interface SearchSpeechInput {
    fun start(listener: RecognitionListener)
    fun stop()
    fun cancel()
    fun destroy()
}

internal val LocalSearchSpeechInputFactory = staticCompositionLocalOf<(Context) -> SearchSpeechInput> {
    { context -> AndroidSearchSpeechInput(context) }
}

internal fun searchRecognitionIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        putExtra(RecognizerIntent.EXTRA_ENABLE_LANGUAGE_SWITCH, RecognizerIntent.LANGUAGE_SWITCH_BALANCED)
    }
}

private class AndroidSearchSpeechInput(context: Context) : SearchSpeechInput {
    private val recognizer: SpeechRecognizer
    init {
        check(SpeechRecognizer.isRecognitionAvailable(context)) { "Speech recognition unavailable" }
        recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    }
    override fun start(listener: RecognitionListener) {
        recognizer.setRecognitionListener(listener)
        recognizer.startListening(searchRecognitionIntent())
    }
    override fun stop() = recognizer.stopListening()
    override fun cancel() = recognizer.cancel()
    override fun destroy() = recognizer.destroy()
}

/** One foreground dictation session. Every provider callback is scoped to its session generation. */
internal class SearchDictation(
    private val createInput: () -> SearchSpeechInput,
    private val canPublish: () -> Boolean,
    private val onQueryChange: (String) -> Unit,
) {
    var phase: DictationPhase by mutableStateOf(DictationPhase.Idle)
        private set
    var error: DictationError? by mutableStateOf(null)
        private set
    private var input: SearchSpeechInput? = null
    private var generation = 0
    private var prefix = ""
    private var publishedQuery = ""
    val active: Boolean get() = phase != DictationPhase.Idle

    fun permissionDenied() { error = DictationError.Permission }
    fun clearError() { error = null }

    fun start(query: String) {
        if (active || !canPublish()) return
        error = null
        prefix = query
        publishedQuery = query
        val session = ++generation
        phase = DictationPhase.Starting
        try {
            val created = createInput()
            input = created
            created.start(listener(session))
        } catch (_: SecurityException) {
            fail(DictationError.Permission)
        } catch (_: RuntimeException) {
            fail(DictationError.Unavailable)
        }
    }

    fun stop() {
        if (!active || phase == DictationPhase.Finishing) return
        phase = DictationPhase.Finishing
        try { input?.stop() } catch (_: RuntimeException) { fail(DictationError.Unavailable) }
    }

    fun cancel() {
        generation++
        phase = DictationPhase.Idle
        release(cancel = true)
    }

    fun hostQueryChanged(query: String) {
        if (active && query != publishedQuery) cancel()
    }

    private fun fail(reason: DictationError) {
        cancel()
        error = reason
    }

    private fun release(cancel: Boolean) {
        val current = input
        input = null
        try {
            if (cancel) current?.cancel()
        } catch (_: RuntimeException) {
            // Cleanup must still destroy a disconnected recognizer.
        } finally {
            try { current?.destroy() } catch (_: RuntimeException) { /* Already disconnected. */ }
        }
    }

    private fun listener(session: Int): RecognitionListener = object : RecognitionListener {
        private fun current(): Boolean {
            if (session != generation || !active) return false
            if (!canPublish()) { cancel(); return false }
            return true
        }

        private fun publish(results: Bundle?) {
            val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() } ?: return
            val separator = if (prefix.isNotEmpty() && !prefix.last().isWhitespace()) " " else ""
            val updated = prefix + separator + phrase
            if (updated != publishedQuery) {
                publishedQuery = updated
                onQueryChange(updated)
            }
        }

        override fun onReadyForSpeech(params: Bundle?) {
            if (current() && phase == DictationPhase.Starting) phase = DictationPhase.Listening
        }
        override fun onBeginningOfSpeech() {
            if (current() && phase == DictationPhase.Starting) phase = DictationPhase.Listening
        }
        override fun onEndOfSpeech() {
            if (current()) phase = DictationPhase.Finishing
        }
        override fun onPartialResults(partialResults: Bundle?) {
            if (current()) publish(partialResults)
        }
        override fun onResults(results: Bundle?) {
            if (!current()) return
            publish(results)
            generation++
            phase = DictationPhase.Idle
            release(cancel = false)
        }
        override fun onError(error: Int) {
            if (!current()) return
            fail(when (error) {
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> DictationError.Permission
                SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> DictationError.NoSpeech
                else -> DictationError.Unavailable
            })
        }
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }
}
