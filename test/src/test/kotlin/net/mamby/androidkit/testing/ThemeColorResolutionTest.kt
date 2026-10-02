package net.mamby.androidkit.testing

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import net.mamby.androidkit.compose.theme.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ThemeColorResolutionTest {
    @Test
    fun paletteCopyRecomputesUnspecifiedComponentColors() {
        val original = AndroidKitThemeDefinition(lightColorScheme(surface = Color.Red), isDark = false)
        val updated = original.copy(colorScheme = original.colorScheme.copy(surface = Color.Blue))
        assertEquals(Color.Blue, updated.cardStyle.containerColor)
        assertEquals(Color.Blue, updated.settingSectionStyle.containerColor)
        assertEquals(Color.Blue, updated.bottomSheetStyle.containerColor)
        assertEquals(Color.Blue, updated.floatingNavigationStyle.navigationRailContainerColor)
        assertEquals(original.cardStyle.shape, updated.cardStyle.shape)
        assertEquals(original.cardStyle.borderWidth, updated.cardStyle.borderWidth)
    }

    @Test
    fun explicitComponentAndInstanceColorsSurvivePaletteChanges() {
        val definition = AndroidKitThemeDefinition(
            colorScheme = lightColorScheme(surface = Color.Red),
            isDark = false,
            componentColors = AndroidKitComponentColors(card = AndroidKitCardColors(borderColor = Color.Green)),
        ).copy(colorScheme = lightColorScheme(surface = Color.Blue))
        val instance = definition.cardStyle.withColors(AndroidKitCardColors(containerColor = Color.Yellow))
        assertEquals(Color.Yellow, instance.containerColor)
        assertEquals(Color.Green, instance.borderColor)
        assertEquals(definition.colorScheme.onSurface, instance.contentColor)
        assertEquals(definition.cardStyle.shape, instance.shape)
    }

    @Test
    fun floatingColorsMergeSharedComponentAndInstanceRolesIndependently() {
        val definition = AndroidKitThemeDefinition(
            colorScheme = lightColorScheme(),
            isDark = false,
            floatingSurfaceColors = AndroidKitFloatingSurfaceColors(borderColor = Color.Red, contentColor = Color.Green),
            componentColors = AndroidKitComponentColors(
                floatingActionButton = AndroidKitFloatingActionButtonColors(
                    surfaceColors = AndroidKitFloatingSurfaceColors(containerColor = Color.Blue),
                ),
            ),
        )
        val instance = definition.floatingActionButtonStyle.withColors(
            AndroidKitFloatingActionButtonColors(surfaceColors = AndroidKitFloatingSurfaceColors(contentColor = Color.Yellow)),
            definition.floatingSurfaceStyle,
        )
        assertEquals(Color.Blue, instance.surfaceStyle!!.containerColor)
        assertEquals(Color.Yellow, instance.surfaceStyle.contentColor)
        assertEquals(Color.Red, instance.surfaceStyle.borderColor)
        assertEquals(definition.floatingActionButtonStyle.visualSize, instance.visualSize)
    }

    @Test
    fun unspecifiedAdaptiveNavigationRetainsMaterialDefaults() {
        val definition = AndroidKitThemeDefinition(lightColorScheme(), isDark = false)
        assertNull(definition.floatingNavigationStyle.adaptiveItemStyle)
    }
}
