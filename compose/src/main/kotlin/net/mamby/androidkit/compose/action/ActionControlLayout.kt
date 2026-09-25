package net.mamby.androidkit.compose.action

import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified

@Composable
internal fun actionControlMinimumWidth(visualSize: Dp = 0.dp): Dp {
    val interactiveSize = LocalMinimumInteractiveComponentSize.current
    return maxOf(visualSize, if (interactiveSize.isSpecified) interactiveSize else 0.dp)
}
