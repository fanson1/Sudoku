package com.finley.android.sudoku.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.sudoku.ui.components.AppLogo
import com.finley.android.sudoku.ui.components.AppScreenBackground
import com.finley.android.sudoku.ui.components.GhostButton
import com.finley.android.sudoku.ui.components.GradientButton
import com.finley.android.sudoku.ui.i18n.LocalAppStrings

@Composable
fun WelcomeScreen(
    onStandaloneClick: () -> Unit,
    onOnlineClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val strings = LocalAppStrings.current

    AppScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1.2f))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    initialOffsetY = { it / 2 }
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppLogo(size = 108.dp)

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Sudoku",
                        style = TextStyle(
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Black,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                    )
                    Text(
                        text = strings.welcomeTagline,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FeatureChip(strings.featureSync, Icons.Default.CloudSync)
                        FeatureChip(strings.featureLeaderboard, Icons.Default.EmojiEvents)
                        FeatureChip(strings.featureCrossPlatform, Icons.Default.PhoneAndroid)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(modifier = Modifier.fillMaxWidth()) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(360, delayMillis = 280)) + slideInVertically(
                        animationSpec = tween(360, delayMillis = 280),
                        initialOffsetY = { it / 3 }
                    ),
                    exit = fadeOut(tween(200))
                ) {
                    GradientButton(
                        onClick = onStandaloneClick,
                        modifier = Modifier.fillMaxWidth(),
                        height = 58.dp
                    ) {
                        Text(
                            text = strings.standaloneMode,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(360, delayMillis = 400)) + slideInVertically(
                        animationSpec = tween(360, delayMillis = 400),
                        initialOffsetY = { it / 3 }
                    ),
                    exit = fadeOut(tween(200))
                ) {
                    GhostButton(
                        onClick = onOnlineClick,
                        modifier = Modifier.fillMaxWidth(),
                        height = 58.dp
                    ) {
                        Text(
                            text = strings.onlineMode,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(360, delayMillis = 540)),
                exit = fadeOut(tween(200))
            ) {
                Text(
                    text = "v1.0.0",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FeatureChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colorScheme.surface.copy(alpha = 0.7f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant
        )
    }
}
