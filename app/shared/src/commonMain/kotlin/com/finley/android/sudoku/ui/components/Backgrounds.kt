package com.finley.android.sudoku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Full-bleed screen background with a soft vertical gradient plus decorative
 * radial glow accents. Adapts automatically to light / dark theme.
 */
@Composable
fun AppScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val top = colorScheme.primaryContainer
    val bottom = colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(top, bottom)
                )
            )
    ) {
        // Decorative accent glow (top-right)
        RadialGlow(
            color = colorScheme.primary,
            size = 240.dp,
            alpha = if (isLightTheme()) 0.10f else 0.14f,
            modifier = Modifier.align(Alignment.TopEnd)
        )
        // Decorative accent glow (bottom-left)
        RadialGlow(
            color = colorScheme.secondary,
            size = 300.dp,
            alpha = if (isLightTheme()) 0.08f else 0.10f,
            modifier = Modifier.align(Alignment.BottomStart)
        )

        content()
    }
}

@Composable
fun isLightTheme(): Boolean =
    MaterialTheme.colorScheme.background.relativeLuminance() > 0.5f

fun Color.relativeLuminance(): Float {
    val lum = 0.299f * red + 0.587f * green + 0.114f * blue
    return lum
}

/**
 * A soft radial-gradient glow, used as a cross-platform stand-in for blur.
 */
@Composable
private fun RadialGlow(
    color: Color,
    size: Dp,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = alpha),
                        color.copy(alpha = 0f)
                    ),
                    center = Offset(size.value * 0.5f, size.value * 0.5f),
                    radius = size.value * 0.62f
                ),
                shape = CircleShape
            )
    )
}

/**
 * The brand logo — a rounded square with the "9" glyph and a soft glow.
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    glow: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    val primary = colorScheme.primary
    val shape = RoundedCornerShape(size * 0.28f)

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (glow) {
                    Modifier.shadow(
                        elevation = 24.dp,
                        shape = shape,
                        ambientColor = primary.copy(alpha = 0.45f),
                        spotColor = primary.copy(alpha = 0.45f)
                    )
                } else Modifier
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(primary, primary.copy(blue = primary.blue * 0.82f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.58f)
                .clip(RoundedCornerShape(size * 0.18f))
                .background(Color.White.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "9",
                fontSize = (size.value * 0.5f).sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

/** Small colored dot used by difficulty badges. */
@Composable
fun DifficultyDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}
