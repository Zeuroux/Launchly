package com.zeuroux.launchly.extensions

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.shimmerLoadingAnimation(
    loading: Boolean = true,
    widthOfShadowBrush: Int = 500,
    angleOfAxisY: Float = 270f,
    durationMillis: Int = 1000,
): Modifier {
    return composed {
        if (!loading) return@composed this
        val shimmerColors = listOf(
            Color.White.copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.5f),
            Color.White.copy(alpha = 1.0f),
            Color.White.copy(alpha = 0.5f),
            Color.White.copy(alpha = 0.3f),
        )

        val transition = rememberInfiniteTransition(label = "")

        val translateAnimation = transition.animateFloat(
            0f, (durationMillis + widthOfShadowBrush).toFloat(),
            infiniteRepeatable(
                tween(durationMillis, easing = LinearEasing,),
                RepeatMode.Restart,
            )
        )

        this.background(
            brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(translateAnimation.value - widthOfShadowBrush,0.0f),
                end = Offset(translateAnimation.value, angleOfAxisY),
            ),
        ).graphicsLayer(alpha = 0f)
    }
}