package com.finley.android.sudoku

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finley.android.sudoku.i18n.LocalStrings
import com.finley.android.sudoku.i18n.resolveLanguage
import com.finley.android.sudoku.i18n.stringKey
import com.finley.android.sudoku.i18n.stringsFor
import com.finley.android.sudoku.pro.rememberProController
import com.finley.android.sudoku.ui.LevelSelectionScreen
import com.finley.android.sudoku.ui.StatsScreen
import com.finley.android.sudoku.ui.components.PaywallDialog
import com.finley.android.sudoku.ui.components.PaywallReason
import com.finley.android.sudoku.ui.game.GameMode
import com.finley.android.sudoku.ui.game.GameScreen
import com.finley.android.sudoku.ui.theme.AppTheme
import com.finley.android.sudoku.util.BackHandler
import com.finley.android.sudoku.util.DailyUtil
import com.finley.android.sudoku.util.Persistence
import com.finley.android.sudoku.util.FreeTier

enum class Screen {
    HOME,
    GAME,
    STATS
}

@Composable
@Preview
fun App() {
    var language by remember { mutableStateOf(Persistence.getLanguage()) }
    var themeMode by remember { mutableStateOf(Persistence.getThemeMode()) }
    val strings = remember(language) { stringsFor(resolveLanguage(language)) }

    val pro = rememberProController()
    val isPro by pro.isPro.collectAsStateWithLifecycle()
    val priceLabel by pro.priceLabel.collectAsStateWithLifecycle()
    val busy by pro.busy.collectAsStateWithLifecycle()
    val available by pro.available.collectAsStateWithLifecycle()
    val proMessage by pro.message.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalStrings provides strings) {
        AppTheme(themeMode = themeMode) {
            Box(modifier = Modifier.fillMaxSize()) {
                val navigationStack = remember { mutableStateListOf(Screen.HOME) }
                val currentScreen = navigationStack.lastOrNull() ?: Screen.HOME

                var unlockedLevels by remember { mutableIntStateOf(Persistence.getUnlockedLevel()) }
                var selectedLevel by remember { mutableIntStateOf(1) }
                var selectedMode by remember { mutableStateOf(GameMode.NORMAL) }
                var selectedTimeLimit by remember { mutableStateOf<Int?>(null) }

                var showPaywall by remember { mutableStateOf(false) }
                var paywallReason by remember { mutableStateOf(PaywallReason.LEVELS) }
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(proMessage) {
                    val msg = proMessage ?: return@LaunchedEffect
                    snackbarHostState.showSnackbar(strings[msg.stringKey()])
                    pro.consumeMessage()
                }

                val navigateBack = {
                    if (navigationStack.size > 1) {
                        navigationStack.removeAt(navigationStack.lastIndex)
                    }
                }

                BackHandler(enabled = navigationStack.size > 1) { navigateBack() }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        (slideInHorizontally(tween(320)) { it / 5 } + fadeIn(tween(320)))
                            .togetherWith(slideOutHorizontally(tween(220)) { -it / 8 } + fadeOut(tween(220)))
                            .using(SizeTransform(clip = false))
                    },
                    label = "screen"
                ) { screen ->
                    when (screen) {
                        Screen.HOME -> LevelSelectionScreen(
                            isPro = isPro,
                            unlockedLevels = unlockedLevels,
                            themeMode = themeMode,
                            onThemeModeChange = {
                                themeMode = it
                                Persistence.saveThemeMode(it)
                            },
                            language = language,
                            onLanguageChange = {
                                language = it
                                Persistence.saveLanguage(it)
                            },
                            dailyCompletedToday = Persistence.isDailyCompleted(DailyUtil.todayDateKey()),
                            dailyChain = Persistence.getDailyChain(),
                            onLevelClick = { level ->
                                selectedMode = GameMode.NORMAL
                                selectedLevel = level
                                navigationStack.add(Screen.GAME)
                            },
                            onProRequired = { reason ->
                                paywallReason = reason
                                showPaywall = true
                            },
                            onDailyChallengeClick = {
                                if (!isPro) {
                                    paywallReason = PaywallReason.CHALLENGE
                                    showPaywall = true
                                } else {
                                    selectedMode = GameMode.DAILY
                                    navigationStack.add(Screen.GAME)
                                }
                            },
                            onTimedChallengeClick = {
                                if (!isPro) {
                                    paywallReason = PaywallReason.CHALLENGE
                                    showPaywall = true
                                } else {
                                    selectedMode = GameMode.TIMED
                                    selectedTimeLimit = 600
                                    navigationStack.add(Screen.GAME)
                                }
                            },
                            onStatsClick = {
                                if (isPro) {
                                    navigationStack.add(Screen.STATS)
                                } else {
                                    paywallReason = PaywallReason.STATS
                                    showPaywall = true
                                }
                            },
                            onProClick = {
                                paywallReason = PaywallReason.GENERAL
                                showPaywall = true
                            }
                        )

                        Screen.STATS -> StatsScreen(
                            onBack = navigateBack,
                            onUnlockPro = {
                                paywallReason = PaywallReason.STATS
                                showPaywall = true
                            }
                        )

                        Screen.GAME -> GameScreen(
                            level = selectedLevel,
                            mode = selectedMode,
                            timeLimitSeconds = selectedTimeLimit,
                            onBack = navigateBack,
                            onLevelCompleted = { completedLevel, _ ->
                                val next = completedLevel + 1
                                if (selectedMode == GameMode.NORMAL && next > unlockedLevels) {
                                    unlockedLevels = next
                                    Persistence.saveUnlockedLevel(next)
                                }
                            },
                            onNextLevel = {
                                val next = selectedLevel + 1
                                if (next > FreeTier.MAX_FREE_LEVEL && !isPro) {
                                    paywallReason = PaywallReason.LEVELS
                                    showPaywall = true
                                } else if (next <= FreeTier.TOTAL_LEVELS) {
                                    selectedLevel = next
                                }
                            }
                        )
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                if (showPaywall) {
                    PaywallDialog(
                        reason = paywallReason,
                        isPro = isPro,
                        priceLabel = priceLabel,
                        busy = busy,
                        available = available,
                        onPurchase = pro::purchase,
                        onRestore = pro::restore,
                        onDismiss = { showPaywall = false }
                    )
                }
            }
        }
    }
}
