package com.finley.android.sudoku

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.finley.android.sudoku.ui.i18n.LocalAppStrings
import com.finley.android.sudoku.ui.i18n.appStringsFor
import com.finley.android.sudoku.ui.i18n.platformLanguageCode
import com.finley.android.sudoku.ui.WelcomeScreen
import com.finley.android.sudoku.ui.LoginScreen
import com.finley.android.sudoku.ui.ProfileScreen
import com.finley.android.sudoku.ui.EditProfileScreen
import com.finley.android.sudoku.ui.ChangePasswordScreen
import com.finley.android.sudoku.ui.LeaderboardScreen
import com.finley.android.sudoku.ui.LevelSelectionScreen
import com.finley.android.sudoku.ui.game.GameMode
import com.finley.android.sudoku.ui.game.GameScreen
import com.finley.android.sudoku.ui.theme.AppTheme
import com.finley.android.sudoku.ui.theme.ThemeMode
import com.finley.android.sudoku.util.Persistence
import com.finley.android.sudoku.util.DailyUtil
import com.finley.android.sudoku.util.BackHandler
import com.finley.android.sudoku.network.NetworkService
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

enum class Screen {
    WELCOME,
    LOGIN,
    PROFILE,
    EDIT_PROFILE,
    CHANGE_PASSWORD,
    LEVEL_SELECTION,
    GAME,
    LEADERBOARD
}

@Composable
@Preview
fun App() {
    var themeMode by remember { mutableStateOf(Persistence.getThemeMode()) }

    val appStrings = remember { appStringsFor(platformLanguageCode().lowercase()) }

    AppTheme(themeMode = themeMode) {
        CompositionLocalProvider(LocalAppStrings provides appStrings) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
            val navigationStack = remember { mutableStateListOf(Screen.WELCOME) }
            val currentScreen = navigationStack.lastOrNull() ?: Screen.WELCOME

            var isOnlineMode by remember { mutableStateOf(false) }
            var unlockedLevels by remember { mutableStateOf(Persistence.getUnlockedLevel()) }
            var selectedLevel by remember { mutableStateOf(1) }
            var selectedMode by remember { mutableStateOf(GameMode.NORMAL) }
            var selectedTimeLimit by remember { mutableStateOf<Int?>(null) }

            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                val savedToken = Persistence.getAuthToken()
                val savedUsername = Persistence.getUsername()
                if (savedToken != null && savedUsername != null) {
                    NetworkService.setAuthToken(savedToken)
                    scope.launch {
                        val user = NetworkService.getUserProfile()
                        if (user != null) {
                            val mergedLevel = maxOf(user.unlockedLevel, unlockedLevels)
                            unlockedLevels = mergedLevel
                            Persistence.saveUnlockedLevel(mergedLevel)

                            if (mergedLevel > user.unlockedLevel) {
                                NetworkService.updateUnlockedLevel(mergedLevel)
                            }

                            isOnlineMode = true
                            navigationStack.clear()
                            navigationStack.add(Screen.LEVEL_SELECTION)
                        } else {
                            NetworkService.logout()
                        }
                    }
                }
            }

            val navigateBack = {
                if (navigationStack.size > 1) {
                    navigationStack.removeAt(navigationStack.size - 1)
                }
            }

            BackHandler(enabled = navigationStack.size > 1) {
                navigateBack()
            }

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (slideInHorizontally(tween(320)) { it / 5 } + fadeIn(tween(320)))
                        .togetherWith(slideOutHorizontally(tween(220)) { -it / 8 } + fadeOut(tween(220)))
                        .using(
                            SizeTransform(clip = false)
                        )
                },
                label = "screen"
            ) { screen ->
                when (screen) {
                    Screen.WELCOME -> {
                        WelcomeScreen(
                            onStandaloneClick = {
                                isOnlineMode = false
                                unlockedLevels = Persistence.getUnlockedLevel()
                                navigationStack.add(Screen.LEVEL_SELECTION)
                            },
                            onOnlineClick = {
                                isOnlineMode = true
                                if (NetworkService.currentUser != null || Persistence.getAuthToken() != null) {
                                    navigationStack.add(Screen.LEVEL_SELECTION)
                                } else {
                                    navigationStack.add(Screen.LOGIN)
                                }
                            }
                        )
                    }
                    Screen.LOGIN -> {
                        LoginScreen(
                            onLoginSuccess = {
                                val user = NetworkService.currentUser
                                if (user != null) {
                                    val mergedLevel = maxOf(user.unlockedLevel, unlockedLevels)
                                    unlockedLevels = mergedLevel
                                    Persistence.saveUnlockedLevel(mergedLevel)

                                    if (mergedLevel > user.unlockedLevel) {
                                        scope.launch {
                                            NetworkService.updateUnlockedLevel(mergedLevel)
                                        }
                                    }
                                }
                                navigationStack.removeAt(navigationStack.size - 1)
                                navigationStack.add(Screen.LEVEL_SELECTION)
                            },
                            onBack = navigateBack
                        )
                    }
                    Screen.PROFILE -> {
                        ProfileScreen(
                            onEditUsername = {
                                navigationStack.add(Screen.EDIT_PROFILE)
                            },
                            onChangePassword = {
                                navigationStack.add(Screen.CHANGE_PASSWORD)
                            },
                            onBack = navigateBack,
                            onLogout = {
                                NetworkService.logout()
                                isOnlineMode = false
                                unlockedLevels = 1
                                navigationStack.clear()
                                navigationStack.add(Screen.WELCOME)
                            }
                        )
                    }
                    Screen.EDIT_PROFILE -> {
                        EditProfileScreen(onBack = navigateBack)
                    }
                    Screen.CHANGE_PASSWORD -> {
                        ChangePasswordScreen(onBack = navigateBack)
                    }
                    Screen.LEVEL_SELECTION -> {
                        LevelSelectionScreen(
                            unlockedLevels = unlockedLevels,
                            isOnlineMode = isOnlineMode,
                            themeMode = themeMode,
                            onThemeModeChange = {
                                themeMode = it
                                Persistence.saveThemeMode(it)
                            },
                            onLevelClick = { level ->
                                selectedMode = GameMode.NORMAL
                                selectedLevel = level
                                navigationStack.add(Screen.GAME)
                            },
                            onDailyChallengeClick = {
                                selectedMode = GameMode.DAILY
                                navigationStack.add(Screen.GAME)
                            },
                            onTimedChallengeClick = {
                                selectedMode = GameMode.TIMED
                                selectedTimeLimit = 600
                                navigationStack.add(Screen.GAME)
                            },
                            dailyCompletedToday = Persistence.isDailyCompleted(DailyUtil.todayDateKey()),
                            dailyChain = Persistence.getDailyChain(),
                            onLeaderboardClick = {
                                navigationStack.add(Screen.LEADERBOARD)
                            },
                            onProfileClick = {
                                navigationStack.add(Screen.PROFILE)
                            },
                            onBack = navigateBack
                        )
                    }
                    Screen.LEADERBOARD -> {
                        LeaderboardScreen(onBack = navigateBack)
                    }
                    Screen.GAME -> {
                        GameScreen(
                            level = selectedLevel,
                            mode = selectedMode,
                            timeLimitSeconds = selectedTimeLimit,
                            onBack = navigateBack,
                            onLevelCompleted = { completedLevel, score ->
                                val next = completedLevel + 1
                                if (selectedMode == GameMode.NORMAL && next > unlockedLevels) {
                                    unlockedLevels = next
                                    Persistence.saveUnlockedLevel(next)
                                }
                                if (isOnlineMode) {
                                    scope.launch {
                                        NetworkService.submitScore(completedLevel, score)
                                    }
                                }
                            },
                            onNextLevel = {
                                selectedLevel++
                            }
                        )
                    }
                }
            }
        }
    }
}
}
