package net.mamby.androidkit.testing

import net.mamby.androidkit.demo.ui.authentication.DemoAuthenticationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoAuthenticationStateTest {
    @Test
    fun backgroundedBiometricCompletionCannotUnlockOrChangeLockSettings() {
        val state = DemoAuthenticationState()
        assertTrue(state.begin(false))
        state.invalidate(explicit = false)
        assertNull(state.complete(deviceCredential = false))
        assertTrue(state.begin(null))
    }

    @Test
    fun freshSystemCredentialCompletionSurvivesItsActivityRoundTrip() {
        val state = DemoAuthenticationState()
        state.begin(true)
        state.invalidate(explicit = false)
        state.invalidate(explicit = false)
        val authorization = state.complete(deviceCredential = true)
        assertNotNull(authorization)
        assertEquals(true, authorization!!.enabled)
        assertTrue(state.isCurrent(authorization))
    }

    @Test
    fun explicitLockRejectsEvenSuccessfulDeviceCredentials() {
        val state = DemoAuthenticationState()
        state.begin(false)
        state.invalidate(explicit = true)
        assertNull(state.complete(deviceCredential = true))
    }

    @Test
    fun invalidationDuringPersistenceRevokesAuthorization() {
        val state = DemoAuthenticationState()
        state.begin(false)
        val authorization = state.complete(deviceCredential = false)!!
        state.invalidate(explicit = false)
        assertFalse(state.isCurrent(authorization))
        assertNull(state.complete(deviceCredential = false))
    }

    @Test
    fun oneAttemptAtATimeAndFailureAllowsRetry() {
        val state = DemoAuthenticationState()
        assertTrue(state.begin(true))
        assertFalse(state.begin(false))
        state.fail()
        assertNull(state.complete(deviceCredential = true))
        assertTrue(state.begin(false))
        assertEquals(false, state.complete(deviceCredential = false)!!.enabled)
    }
}
