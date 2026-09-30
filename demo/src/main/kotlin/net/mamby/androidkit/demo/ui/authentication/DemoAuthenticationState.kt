package net.mamby.androidkit.demo.ui.authentication

import java.util.concurrent.atomic.AtomicLong

/** Attempts belong to the main thread; persistence also checks authorization from DataStore. */
internal class DemoAuthenticationState {
    internal data class Authorization(val generation: Long, val enabled: Boolean?)
    private data class Attempt(val generation: Long, val explicitGeneration: Long, val enabled: Boolean?)

    private val generation = AtomicLong()
    private var explicitGeneration = 0L
    private var attempt: Attempt? = null

    fun begin(enabled: Boolean?): Boolean {
        if (attempt != null) return false
        attempt = Attempt(generation.get(), explicitGeneration, enabled)
        return true
    }

    fun invalidate(explicit: Boolean) {
        generation.incrementAndGet()
        if (explicit) explicitGeneration++
    }

    fun complete(deviceCredential: Boolean): Authorization? {
        val completed = attempt ?: return null
        attempt = null
        if (completed.explicitGeneration != explicitGeneration) return null
        // On older Android versions the official credential flow opens another activity.
        // Its fresh successful result may cross activity background/foreground callbacks.
        val currentGeneration = generation.get()
        if (completed.generation != currentGeneration && !deviceCredential) return null
        return Authorization(currentGeneration, completed.enabled)
    }

    fun fail() {
        attempt = null
    }

    fun isCurrent(authorization: Authorization): Boolean = authorization.generation == generation.get()
}
