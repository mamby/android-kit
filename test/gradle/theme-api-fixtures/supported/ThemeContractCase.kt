package net.mamby.androidkit.themecontract

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.theme.*
import net.mamby.androidkit.compose.presentation.AndroidKitCard
import net.mamby.androidkit.compose.action.AndroidKitFloatingToolbar
import net.mamby.androidkit.compose.form.AndroidKitBottomSheet

@Composable
fun themeContractCase() {
    val definition = AndroidKitThemes.Light.copy(
        floatingSurfaceOpacityLevel = 75f,
        floatingSurfaceColors = AndroidKitFloatingSurfaceColors(borderColor = Color.Red),
        componentColors = AndroidKitComponentColors(card = AndroidKitCardColors(containerColor = Color.Blue)),
    )
    AndroidKitTheme(definition) {
        val padding = AndroidKitThemeTokens.dimensions.spaceMedium
        val shape = AndroidKitThemeTokens.shapes.extraLarge
        val typography = AndroidKitThemeTokens.typography.bodyLarge
        AndroidKitCard(colors = AndroidKitThemeTokens.cardColors.copy(contentColor = Color.White)) {}
        AndroidKitFloatingToolbar { text({}, "Action") }
        AndroidKitBottomSheet(false, "Sheet", {}, sheetMaxWidth = padding) {}
    }
}
