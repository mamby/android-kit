package net.mamby.androidkit.demo.ui

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource

@Composable
internal fun materialSymbol(@DrawableRes resource: Int): ImageVector =
    ImageVector.vectorResource(resource)
