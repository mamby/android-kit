package net.mamby.androidkit.demo

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import net.mamby.androidkit.demo.ui.AndroidKitCatalogApp

class MainActivity : AppCompatActivity() {
    private var isContentReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isContentReady }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidKitCatalogApp { isDarkTheme ->
                applyEdgeToEdge(isDarkTheme)
                isContentReady = true
            }
        }
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
