package net.mamby.androidkit.compose.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

private const val NavigationHighlightDurationMillis = 600

@Composable
internal fun rememberNavigationHighlightScale(selected: Boolean): Animatable<Float, AnimationVector1D> {
    val scale = remember { Animatable(if (selected) 1f else 0f) }
    LaunchedEffect(selected) {
        if (selected) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(NavigationHighlightDurationMillis, easing = FastOutSlowInEasing),
            )
        } else {
            scale.snapTo(0f)
        }
    }
    return scale
}
