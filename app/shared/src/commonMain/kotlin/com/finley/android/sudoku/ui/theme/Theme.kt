package com.finley.android.sudoku.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色")
}

private val LightColors = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = IndigoOnPrimary,
    primaryContainer = IndigoPrimaryContainer,
    onPrimaryContainer = IndigoOnPrimaryContainer,
    secondary = IndigoSecondary,
    onSecondary = IndigoOnSecondary,
    secondaryContainer = IndigoSecondaryContainer,
    onSecondaryContainer = IndigoOnSecondaryContainer,
    tertiary = IndigoTertiary,
    onTertiary = IndigoOnTertiary,
    tertiaryContainer = IndigoTertiaryContainer,
    onTertiaryContainer = IndigoOnTertiaryContainer,
    error = IndigoError,
    onError = IndigoOnError,
    errorContainer = IndigoErrorContainer,
    onErrorContainer = IndigoOnErrorContainer,
    background = IndigoSurface,
    onBackground = IndigoOnSurface,
    surface = IndigoSurface,
    onSurface = IndigoOnSurface,
    surfaceVariant = IndigoSurfaceVariant,
    onSurfaceVariant = IndigoOnSurfaceVariant,
    outline = IndigoOutline,
    outlineVariant = IndigoOutlineVariant,
    surfaceContainer = IndigoSurfaceContainer,
    surfaceContainerHigh = IndigoSurfaceContainerHigh,
    surfaceContainerHighest = IndigoSurfaceContainerHigh,
    inverseSurface = IndigoInverseSurface,
    inverseOnSurface = IndigoInverseOnSurface,
    inversePrimary = IndigoPrimary
)

private val DarkColors = darkColorScheme(
    primary = NebulaPrimary,
    onPrimary = NebulaOnPrimary,
    primaryContainer = NebulaPrimaryContainer,
    onPrimaryContainer = NebulaOnPrimaryContainer,
    secondary = NebulaSecondary,
    onSecondary = NebulaOnSecondary,
    secondaryContainer = NebulaSecondaryContainer,
    onSecondaryContainer = NebulaOnSecondaryContainer,
    tertiary = NebulaTertiary,
    onTertiary = NebulaOnTertiary,
    tertiaryContainer = NebulaTertiaryContainer,
    onTertiaryContainer = NebulaOnTertiaryContainer,
    error = NebulaError,
    onError = NebulaOnError,
    errorContainer = NebulaErrorContainer,
    onErrorContainer = NebulaOnErrorContainer,
    background = NebulaBackground,
    onBackground = NebulaOnBackground,
    surface = NebulaSurface,
    onSurface = NebulaOnSurface,
    surfaceVariant = NebulaSurfaceVariant,
    onSurfaceVariant = NebulaOnSurfaceVariant,
    outline = NebulaOutline,
    outlineVariant = NebulaOutlineVariant,
    surfaceContainer = NebulaSurfaceContainer,
    surfaceContainerHigh = NebulaSurfaceContainerHigh,
    surfaceContainerHighest = NebulaSurfaceContainerHigh,
    inverseSurface = NebulaInverseSurface,
    inverseOnSurface = NebulaInverseOnSurface,
    inversePrimary = IndigoPrimary
)

object AppColors {
    val hintGold: Color
        @Composable get() = if (isDarkTheme()) NebulaHintGold else IndigoHintGold

    val success: Color
        @Composable get() = if (isDarkTheme()) SuccessGreenDark else SuccessGreen

    val lock: Color
        @Composable get() = if (isDarkTheme()) LockGray.copy(alpha = 0.8f) else LockGray
}

@Composable
fun isDarkTheme(): Boolean {
    val bg = MaterialTheme.colorScheme.background
    val lum = 0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue
    return lum < 0.5f
}

@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        shapes = androidx.compose.material3.Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
            small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
        ),
        content = content
    )
}
