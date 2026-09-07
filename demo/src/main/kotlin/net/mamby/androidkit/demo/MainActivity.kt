package net.mamby.androidkit.demo

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import net.mamby.androidkit.demo.ui.AndroidKitCatalogApp
import net.mamby.androidkit.demo.ui.DemoSettingsRepository
import net.mamby.androidkit.demo.ui.DemoSettingsViewModel

class MainActivity : AppCompatActivity() {
    private val settingsViewModel: DemoSettingsViewModel by viewModels {
        viewModelFactory {
            initializer { DemoSettingsViewModel(DemoSettingsRepository(applicationContext)) }
        }
    }
    private lateinit var biometricPrompt: BiometricPrompt
    private var isContentReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isContentReady }
        super.onCreate(savedInstanceState)
        biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    settingsViewModel.authenticationSucceeded()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    settingsViewModel.authenticationFailed(errString.toString())
                }
            },
        )
        enableEdgeToEdge()
        setContent {
            AndroidKitCatalogApp(
                settingsViewModel = settingsViewModel,
                onAuthenticate = ::authenticate,
            ) { isDarkTheme ->
                applyEdgeToEdge(isDarkTheme)
                isContentReady = true
            }
        }
    }

    override fun onStop() {
        if (!isChangingConfigurations) settingsViewModel.lock()
        super.onStop()
    }

    private fun authenticate(enabled: Boolean?) {
        if (!settingsViewModel.beginAuthentication(enabled)) return
        val authenticators = BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        if (BiometricManager.from(this).canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            settingsViewModel.authenticationFailed(getString(R.string.app_lock_unavailable))
            return
        }
        biometricPrompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.settings_app_lock))
                .setSubtitle(getString(R.string.app_lock_authenticate))
                .setAllowedAuthenticators(authenticators)
                .build(),
        )
    }

    private fun applyEdgeToEdge(isDarkTheme: Boolean) {
        val systemBarStyle = if (isDarkTheme) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            )
        }
        enableEdgeToEdge(
            statusBarStyle = systemBarStyle,
            navigationBarStyle = systemBarStyle,
        )
    }
}
