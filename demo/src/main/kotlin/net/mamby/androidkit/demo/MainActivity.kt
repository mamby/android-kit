package net.mamby.androidkit.demo

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import net.mamby.androidkit.demo.ui.AndroidKitCatalogApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyEdgeToEdge(isDarkTheme = false)
        setContent {
            AndroidKitCatalogApp(onThemeDarknessChanged = ::applyEdgeToEdge)
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
