package com.finley.android.sudoku

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.finley.android.sudoku.ui.WelcomeScreen
import com.finley.android.sudoku.ui.LoginScreen
import com.finley.android.sudoku.ui.ProfileScreen
import com.finley.android.sudoku.ui.EditProfileScreen
import com.finley.android.sudoku.ui.ChangePasswordScreen
import com.finley.android.sudoku.ui.LeaderboardScreen
import com.finley.android.sudoku.ui.LevelSelectionScreen
import com.finley.android.sudoku.ui.game.GameScreen
import com.finley.android.sudoku.util.Persistence
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
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val navigationStack = remember { mutableStateListOf(Screen.WELCOME) }
            val currentScreen = navigationStack.lastOrNull() ?: Screen.WELCOME
            
            var isOnlineMode by remember { mutableStateOf(false) }
            var unlockedLevels by remember { mutableStateOf(Persistence.getUnlockedLevel()) }
            var selectedLevel by remember { mutableStateOf(1) }
            
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                val savedToken = Persistence.getAuthToken()
                val savedUsername = Persistence.getUsername()
                if (savedToken != null && savedUsername != null) {
                    NetworkService.setAuthToken(savedToken)
                    // Optionally refresh profile to verify token
                    scope.launch {
                        val user = NetworkService.getUserProfile()
                        if (user != null) {
                            // 融合本地和服务器的进度，取最大值
                            val mergedLevel = maxOf(user.unlockedLevel, unlockedLevels)
                            unlockedLevels = mergedLevel
                            Persistence.saveUnlockedLevel(mergedLevel)
                            
                            // 如果本地进度更高，同步给服务器
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

            when (currentScreen) {
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
                                // 登录成功后也要融合进度
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
                            unlockedLevels = 1 // 退出登录时重置本地进度变量
                            navigationStack.clear()
                            navigationStack.add(Screen.WELCOME)
                        }
                    )
                }
                Screen.EDIT_PROFILE -> {
                    EditProfileScreen(
                        onBack = navigateBack
                    )
                }
                Screen.CHANGE_PASSWORD -> {
                    ChangePasswordScreen(
                        onBack = navigateBack
                    )
                }
                Screen.LEVEL_SELECTION -> {
                    LevelSelectionScreen(
                        unlockedLevels = unlockedLevels,
                        isOnlineMode = isOnlineMode,
                        onLevelClick = { level ->
                            selectedLevel = level
                            navigationStack.add(Screen.GAME)
                        },
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
                    LeaderboardScreen(
                        onBack = navigateBack
                    )
                }
                Screen.GAME -> {
                    GameScreen(
                        level = selectedLevel,
                        onBack = navigateBack,
                        onLevelCompleted = { completedLevel, score ->
                            val next = completedLevel + 1
                            if (next > unlockedLevels) {
                                unlockedLevels = next
                                Persistence.saveUnlockedLevel(next)
                            }
                            if (isOnlineMode) {
                                // 使用独立的协程，避免被组件销毁影响
                                kotlinx.coroutines.GlobalScope.launch {
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
