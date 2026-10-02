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
fun themeContractCase() = AndroidKitThemeTokens.dimensions.copy(spaceMedium = 1.dp)
