package com.finley.android.sudoku.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.withTransform
import com.finley.android.sudoku.ui.theme.AppColors
import kotlin.math.sin

/**
 * Lightweight looping confetti rain drawn on a canvas — used as a pure-Compose
 * celebration effect behind victory dialogs (no external dependencies).
 */
@Composable
fun ConfettiRain(
    modifier: Modifier = Modifier,
    particleCount: Int = 42
) {
    val colorScheme = MaterialTheme.colorScheme
    val colors = listOf(
        colorScheme.primary,
        colorScheme.secondary,
        colorScheme.tertiary,
        AppColors.hintGold,
        colorScheme.primaryContainer,
        colorScheme.secondaryContainer
    )

    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProgress"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        for (i in 0 until particleCount) {
            // Deterministic per-particle values derived from its index so the
            // rain looks organic without needing any RNG state.
            val lane = (i * 7 + 3) % 11
            val phase = (i * 0.061f) % 1f
            val p = (progress + phase) % 1f

            val baseX = (lane + 0.5f) / 11f * w
            val sway = sin(p * 10.0 * (if (i % 2 == 0) 1.0 else -1.0)).toFloat()
            val x = baseX + sway * w * 0.045f
            val y = p * (h + 40f) - 20f

            val rotation = p * 720f + i * 47f
            val rectWidth = w * (0.016f + (i % 3) * 0.006f)
            val rectHeight = h * (0.012f + (i % 2) * 0.006f)
            val color = colors[i % colors.size]

            withTransform({
                rotate(degrees = rotation, pivot = Offset(x, y))
            }) {
                drawRect(
                    color = color,
                    topLeft = Offset(x - rectWidth / 2, y - rectHeight / 2),
                    size = Size(rectWidth, rectHeight)
                )
            }
        }
    }
}
