package com.finley.android.sudoku.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.sudoku.i18n.Language
import com.finley.android.sudoku.i18n.LocalStrings
import com.finley.android.sudoku.i18n.StringKey
import com.finley.android.sudoku.model.Difficulty
import com.finley.android.sudoku.model.GameRules
import com.finley.android.sudoku.ui.components.AppScreenBackground
import com.finley.android.sudoku.ui.components.DifficultyDot
import com.finley.android.sudoku.ui.components.PaywallReason
import com.finley.android.sudoku.ui.components.SettingsSheetContent
import com.finley.android.sudoku.ui.components.difficultyColor
import com.finley.android.sudoku.ui.components.difficultyLabel
import com.finley.android.sudoku.ui.components.pressScale
import com.finley.android.sudoku.ui.theme.ThemeMode
import com.finley.android.sudoku.util.FreeTier
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectionScreen(
    isPro: Boolean,
    unlockedLevels: Int,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    language: Language,
    onLanguageChange: (Language) -> Unit,
    dailyCompletedToday: Boolean,
    dailyChain: Int,
    onLevelClick: (Int) -> Unit,
    onProRequired: (PaywallReason) -> Unit,
    onDailyChallengeClick: () -> Unit,
    onTimedChallengeClick: () -> Unit,
    onStatsClick: () -> Unit,
    onProClick: () -> Unit
) {
    val s = LocalStrings.current
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = s[StringKey.APP_NAME],
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                },
                actions = {
                    IconButton(onClick = onStatsClick) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = s[StringKey.CD_STATS]
                        )
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = s[StringKey.CD_SETTINGS]
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        AppScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(84.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(key = "progress", span = { GridItemSpan(maxLineSpan) }) {
                    LevelProgressHeader(unlockedLevels = unlockedLevels)
                }

                item(key = "pro", span = { GridItemSpan(maxLineSpan) }) {
                    if (isPro) {
                        ProActiveCard()
                    } else {
                        ProBanner(onClick = onProClick)
                    }
                }

                item(key = "challenges", span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ChallengeCard(
                            title = s[StringKey.DAILY_CHALLENGE],
                            subtitle = if (dailyCompletedToday) {
                                s[StringKey.DAILY_DONE]
                            } else {
                                s[StringKey.DAILY_SUBTITLE]
                            },
                            icon = Icons.Default.CalendarMonth,
                            badge = if (dailyCompletedToday) "✓" else null,
                            goalLabel = if (dailyChain > 0) {
                                s.fmt(StringKey.DAILY_STREAK, "n" to dailyChain)
                            } else {
                                null
                            },
                            locked = !isPro,
                            modifier = Modifier.weight(1f),
                            onClick = onDailyChallengeClick
                        )
                        ChallengeCard(
                            title = s[StringKey.TIMED_CHALLENGE],
                            subtitle = s[StringKey.TIMED_SUBTITLE],
                            icon = Icons.Default.Timer,
                            locked = !isPro,
                            modifier = Modifier.weight(1f),
                            onClick = onTimedChallengeClick
                        )
                    }
                }

                Difficulty.entries.forEachIndexed { diffIndex, difficulty ->
                    val start = diffIndex * 10 + 1
                    val end = minOf(start + 9, FreeTier.TOTAL_LEVELS)

                    item(key = "header-$difficulty", span = { GridItemSpan(maxLineSpan) }) {
                        DifficultySectionHeader(
                            difficulty = difficulty,
                            range = start..end,
                            unlockedLevels = unlockedLevels
                        )
                    }

                    items((start..end).toList(), key = { it }) { level ->
                        LevelItem(
                            level = level,
                            unlockedLevels = unlockedLevels,
                            isPro = isPro,
                            modifier = Modifier.animateItem(),
                            onClick = { onLevelClick(level) },
                            onProRequired = { onProRequired(PaywallReason.LEVELS) }
                        )
                    }
                }
            }
        }
    }

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        ) {
            SettingsSheetContent(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                language = language,
                onLanguageChange = onLanguageChange,
                isPro = isPro,
                onUpgradeClick = {
                    showSettings = false
                    onProClick()
                }
            )
            Spacer(modifier = Modifier.navigationBarsPadding().height(16.dp))
        }
    }
}

@Composable
private fun ProBanner(onClick: () -> Unit) {
    val s = LocalStrings.current
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, ambientColor = colorScheme.primary.copy(alpha = 0.3f))
            .pressScale(interactionSource),
        interactionSource = interactionSource,
        shape = shape,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(colorScheme.primary, colorScheme.secondary)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = s[StringKey.PRO_ENTRY_TITLE],
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = s[StringKey.PRO_ENTRY_SUBTITLE],
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ProActiveCard() {
    val s = LocalStrings.current
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.primaryContainer.copy(alpha = 0.55f),
        contentColor = colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = s[StringKey.PRO_ACTIVE],
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = s[StringKey.PRO_ACTIVE_SUBTITLE],
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colorScheme.primary
            )
        }
    }
}

@Composable
private fun DifficultySectionHeader(
    difficulty: Difficulty,
    range: IntRange,
    unlockedLevels: Int
) {
    val s = LocalStrings.current
    val colorScheme = MaterialTheme.colorScheme
    val diffColor = difficultyColor(difficulty)
    val total = range.count()
    val completed = range.count { it < unlockedLevels }

    Column(modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(diffColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                DifficultyDot(color = diffColor, size = 10.dp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = difficultyLabel(difficulty),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = s.fmt(StringKey.LEVEL_RANGE, "a" to range.first, "b" to range.last),
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$completed/$total",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (completed == total) diffColor else colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(diffColor.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun LevelProgressHeader(unlockedLevels: Int) {
    val s = LocalStrings.current
    val progress = (unlockedLevels.toFloat() / FreeTier.TOTAL_LEVELS).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(progress, label = "progress")
    val animatedCount by animateIntAsState(unlockedLevels, label = "count")
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.padding(bottom = 10.dp, top = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = s[StringKey.PROGRESS_TITLE],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = s.fmt(
                        StringKey.PROGRESS_UNLOCKED,
                        "done" to animatedCount,
                        "total" to FreeTier.TOTAL_LEVELS
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(colorScheme.primary, colorScheme.secondary)
                        )
                    )
            )
        }
    }
}

@Composable
private fun ChallengeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    badge: String? = null,
    goalLabel: String? = null,
    locked: Boolean = false,
    onClick: () -> Unit
) {
    val s = LocalStrings.current
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(16.dp)

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(100.dp)
            .shadow(6.dp, shape)
            .pressScale(interactionSource),
        interactionSource = interactionSource,
        shape = shape,
        color = colorScheme.primaryContainer.copy(alpha = 0.55f),
        contentColor = colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = colorScheme.primary.copy(alpha = 0.15f),
                contentColor = colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (badge != null) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                if (goalLabel != null) {
                    Text(
                        text = goalLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.secondary
                    )
                } else if (locked) {
                    ProPill()
                }
            }
        }
    }
}

@Composable
private fun ProPill() {
    val s = LocalStrings.current
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Text(
            text = s[StringKey.PRO_BADGE],
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun LevelItem(
    level: Int,
    unlockedLevels: Int,
    isPro: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onProRequired: () -> Unit
) {
    val s = LocalStrings.current
    val colorScheme = MaterialTheme.colorScheme
    val difficulty = GameRules.getDifficultyForLevel(level)
    val diffColor = difficultyColor(difficulty)
    val shape = RoundedCornerShape(16.dp)

    val requiresPro = !FreeTier.isLevelFree(level) && !isPro
    val unlockedByProgress = level <= unlockedLevels
    val playable = unlockedByProgress && !requiresPro
    val isCurrent = playable && level == unlockedLevels
    val isCompleted = unlockedByProgress && level != unlockedLevels

    val background = when {
        isCurrent -> Brush.linearGradient(
            listOf(
                colorScheme.primary,
                colorScheme.primary.copy(blue = colorScheme.primary.blue * 0.78f)
            )
        )

        playable -> Brush.linearGradient(
            listOf(
                colorScheme.surface.copy(alpha = 0.92f),
                colorScheme.surfaceVariant.copy(alpha = 0.9f)
            )
        )

        else -> Brush.linearGradient(
            listOf(
                colorScheme.surfaceVariant.copy(alpha = 0.45f),
                colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        )
    }

    val scaleAnim by animateFloatAsState(
        targetValue = if (isCurrent) 1f else 0.96f,
        label = "levelScale"
    )

    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((level % 10) * 35L)
        appeared = true
    }
    val entranceScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "levelEntranceScale"
    )
    val entranceAlpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(280),
        label = "levelEntranceAlpha"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                elevation = if (isCurrent) 12.dp else 2.dp,
                shape = shape,
                ambientColor = if (isCurrent) colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent,
                spotColor = if (isCurrent) colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent
            )
            .clip(shape)
            .background(background)
            .graphicsLayer {
                scaleX = scaleAnim * entranceScale
                scaleY = scaleAnim * entranceScale
                alpha = entranceAlpha
            }
            .clickable(enabled = playable || requiresPro) {
                if (playable) onClick() else onProRequired()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                requiresPro -> {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = s[StringKey.CD_LOCKED],
                        tint = colorScheme.primary.copy(alpha = 0.75f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = s[StringKey.PRO_BADGE],
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.primary
                    )
                }

                playable -> {
                    DifficultyDot(
                        color = diffColor,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = level.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) colorScheme.onPrimary else colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                else -> {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = s[StringKey.CD_LOCKED],
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isCompleted) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(16.dp),
                shape = CircleShape,
                color = com.finley.android.sudoku.ui.theme.SuccessGreen,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(3.dp)
                )
            }
        }
    }
}
