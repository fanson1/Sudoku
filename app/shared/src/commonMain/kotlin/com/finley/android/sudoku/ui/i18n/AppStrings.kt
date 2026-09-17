package com.finley.android.sudoku.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Keys for every localized UI string. Values live in zhDefaultStrings /
 * enFallbackStrings; the active language is selected by [appStringsFor] and
 * exposed to composables through [LocalAppStrings].
 */
enum class StrKey {
    DIFFICULTY_EASY, DIFFICULTY_MEDIUM, DIFFICULTY_HARD, DIFFICULTY_EXPERT, DIFFICULTY_MASTER,
    THEME_SYSTEM, THEME_LIGHT, THEME_DARK,
    WELCOME_TAGLINE, FEATURE_SYNC, FEATURE_LEADERBOARD, FEATURE_CROSS_PLATFORM,
    STANDALONE_MODE, ONLINE_MODE,
    CREATE_ACCOUNT_TITLE, WELCOME_BACK_TITLE, CREATE_ACCOUNT_SUBTITLE, SIGN_IN_SUBTITLE,
    USERNAME_LABEL, USERNAME_PLACEHOLDER, PASSWORD_LABEL, PASSWORD_PLACEHOLDER,
    FILL_ALL_FIELDS, AUTH_FAILED, REGISTER_BUTTON, SIGN_IN_BUTTON, GO_TO_SIGN_IN, GO_TO_REGISTER,
    PROFILE_TITLE, GUEST, MAX_LEVEL_LABEL, GLOBAL_RANK_LABEL, ONLINE_STATUS, ACCOUNT_STATUS_LABEL,
    CHANGE_USERNAME_TITLE, CHANGE_USERNAME_SUBTITLE, CHANGE_PASSWORD_TITLE, CHANGE_PASSWORD_SUBTITLE,
    LOGOUT,
    EDIT_PROFILE_TITLE, NEW_USERNAME_SYNCS, PROFILE_UPDATE_SUCCESS, PROFILE_UPDATE_FAILED, SAVE_CHANGES,
    CURRENT_PASSWORD, NEW_PASSWORD, PASSWORD_HINT_RULE, PASSWORD_CHANGE_SUCCESS,
    PASSWORD_CHANGE_FAILED, UPDATE_PASSWORD,
    LEADERBOARD_TITLE, NO_RANKING_YET, BE_FIRST, TOP_THREE, NOT_ON_BOARD, KEEP_GOING, POINTS_SUFFIX,
    CHOOSE_LEVEL, DAILY_CHALLENGE, DAILY_DONE_TODAY, DAILY_A_NEW_PUZZLE, TIMED_CHALLENGE,
    TIMED_10_MINUTES, LEVEL_PROGRESS,
    APPEARANCE_SETTINGS, APPEARANCE_SUBTITLE, THEME_MODE_LABEL, THEME_SYSTEM_SUBTITLE,
    THEME_LIGHT_SUBTITLE, THEME_DARK_SUBTITLE, GAME_SETTINGS, GAME_SETTINGS_SUBTITLE,
    AUTO_ERASE_NOTES, AUTO_ERASE_NOTES_TITLE, AUTO_ERASE_NOTES_SUBTITLE,
    SHOW_CONFLICTS, SHOW_CONFLICTS_TITLE, SHOW_CONFLICTS_SUBTITLE,
    GENERATING_PUZZLE, UNDO, REDO, NOTES, ERASE, PAUSED, COMBO_LABEL, PROGRESS_LABEL, ALL_COMPLETE,
    GAME_PAUSED, RESUME_GAME, VICTORY_DAILY, VICTORY_NORMAL,
    LEVEL_COLUMN, TIME_COLUMN, MISTAKES_COLUMN, MAX_COMBO_COLUMN, SCORE_COLUMN,
    BACK_TO_LEVELS, NEXT_LEVEL, TIME_UP, CHALLENGE_FAILED, NOT_FINISHED_IN_TIME,
    TOO_MANY_MISTAKES, RETRY,
    // Interpolated templates; {0}/{1} are substituted at the call site.
    HINT_BUTTON, LEVEL_TITLE, LEVEL_COMPLETED, LEVEL_RANGE, UNLOCKED_PROGRESS,
    CELLS_REMAINING, LEVELS_UNLOCKED, TOP_GLOBAL, ME_LABEL, DAILY_STREAK, LEVEL_ENTRY
}

/**
 * Localized string table. Composables read values through typed accessors;
 * interpolated templates ({0}, {1}) are expanded by the helper methods below.
 */
class AppStrings(private val t: Map<StrKey, String>) {

    val difficultyEasy: String get() = t[StrKey.DIFFICULTY_EASY]!!
    val difficultyMedium: String get() = t[StrKey.DIFFICULTY_MEDIUM]!!
    val difficultyHard: String get() = t[StrKey.DIFFICULTY_HARD]!!
    val difficultyExpert: String get() = t[StrKey.DIFFICULTY_EXPERT]!!
    val difficultyMaster: String get() = t[StrKey.DIFFICULTY_MASTER]!!

    val themeSystem: String get() = t[StrKey.THEME_SYSTEM]!!
    val themeLight: String get() = t[StrKey.THEME_LIGHT]!!
    val themeDark: String get() = t[StrKey.THEME_DARK]!!

    val welcomeTagline: String get() = t[StrKey.WELCOME_TAGLINE]!!
    val featureSync: String get() = t[StrKey.FEATURE_SYNC]!!
    val featureLeaderboard: String get() = t[StrKey.FEATURE_LEADERBOARD]!!
    val featureCrossPlatform: String get() = t[StrKey.FEATURE_CROSS_PLATFORM]!!
    val standaloneMode: String get() = t[StrKey.STANDALONE_MODE]!!
    val onlineMode: String get() = t[StrKey.ONLINE_MODE]!!

    val createAccountTitle: String get() = t[StrKey.CREATE_ACCOUNT_TITLE]!!
    val welcomeBackTitle: String get() = t[StrKey.WELCOME_BACK_TITLE]!!
    val createAccountSubtitle: String get() = t[StrKey.CREATE_ACCOUNT_SUBTITLE]!!
    val signInSubtitle: String get() = t[StrKey.SIGN_IN_SUBTITLE]!!
    val usernameLabel: String get() = t[StrKey.USERNAME_LABEL]!!
    val usernamePlaceholder: String get() = t[StrKey.USERNAME_PLACEHOLDER]!!
    val passwordLabel: String get() = t[StrKey.PASSWORD_LABEL]!!
    val passwordPlaceholder: String get() = t[StrKey.PASSWORD_PLACEHOLDER]!!
    val fillAllFields: String get() = t[StrKey.FILL_ALL_FIELDS]!!
    val authFailed: String get() = t[StrKey.AUTH_FAILED]!!
    val registerButton: String get() = t[StrKey.REGISTER_BUTTON]!!
    val signInButton: String get() = t[StrKey.SIGN_IN_BUTTON]!!
    val goToSignIn: String get() = t[StrKey.GO_TO_SIGN_IN]!!
    val goToRegister: String get() = t[StrKey.GO_TO_REGISTER]!!

    val profileTitle: String get() = t[StrKey.PROFILE_TITLE]!!
    val guest: String get() = t[StrKey.GUEST]!!
    val maxLevelLabel: String get() = t[StrKey.MAX_LEVEL_LABEL]!!
    val globalRankLabel: String get() = t[StrKey.GLOBAL_RANK_LABEL]!!
    val onlineStatus: String get() = t[StrKey.ONLINE_STATUS]!!
    val accountStatusLabel: String get() = t[StrKey.ACCOUNT_STATUS_LABEL]!!
    val changeUsernameTitle: String get() = t[StrKey.CHANGE_USERNAME_TITLE]!!
    val changeUsernameSubtitle: String get() = t[StrKey.CHANGE_USERNAME_SUBTITLE]!!
    val changePasswordTitle: String get() = t[StrKey.CHANGE_PASSWORD_TITLE]!!
    val changePasswordSubtitle: String get() = t[StrKey.CHANGE_PASSWORD_SUBTITLE]!!
    val logout: String get() = t[StrKey.LOGOUT]!!

    val editProfileTitle: String get() = t[StrKey.EDIT_PROFILE_TITLE]!!
    val newUsernameSyncsAcrossDevices: String get() = t[StrKey.NEW_USERNAME_SYNCS]!!
    val profileUpdateSuccess: String get() = t[StrKey.PROFILE_UPDATE_SUCCESS]!!
    val profileUpdateFailed: String get() = t[StrKey.PROFILE_UPDATE_FAILED]!!
    val saveChanges: String get() = t[StrKey.SAVE_CHANGES]!!

    val currentPassword: String get() = t[StrKey.CURRENT_PASSWORD]!!
    val newPassword: String get() = t[StrKey.NEW_PASSWORD]!!
    val passwordHintRule: String get() = t[StrKey.PASSWORD_HINT_RULE]!!
    val passwordChangeSuccess: String get() = t[StrKey.PASSWORD_CHANGE_SUCCESS]!!
    val passwordChangeFailed: String get() = t[StrKey.PASSWORD_CHANGE_FAILED]!!
    val updatePassword: String get() = t[StrKey.UPDATE_PASSWORD]!!

    val leaderboardTitle: String get() = t[StrKey.LEADERBOARD_TITLE]!!
    val noRankingYet: String get() = t[StrKey.NO_RANKING_YET]!!
    val beFirstOnBoard: String get() = t[StrKey.BE_FIRST]!!
    val topThree: String get() = t[StrKey.TOP_THREE]!!
    val notOnBoard: String get() = t[StrKey.NOT_ON_BOARD]!!
    val keepGoing: String get() = t[StrKey.KEEP_GOING]!!
    val pointsSuffix: String get() = t[StrKey.POINTS_SUFFIX]!!

    val chooseLevel: String get() = t[StrKey.CHOOSE_LEVEL]!!
    val dailyChallenge: String get() = t[StrKey.DAILY_CHALLENGE]!!
    val dailyDoneToday: String get() = t[StrKey.DAILY_DONE_TODAY]!!
    val dailyAnewPuzzle: String get() = t[StrKey.DAILY_A_NEW_PUZZLE]!!
    val timedChallenge: String get() = t[StrKey.TIMED_CHALLENGE]!!
    val timed10Minutes: String get() = t[StrKey.TIMED_10_MINUTES]!!
    val levelProgress: String get() = t[StrKey.LEVEL_PROGRESS]!!

    val appearanceSettings: String get() = t[StrKey.APPEARANCE_SETTINGS]!!
    val appearanceSubtitle: String get() = t[StrKey.APPEARANCE_SUBTITLE]!!
    val themeModeLabel: String get() = t[StrKey.THEME_MODE_LABEL]!!
    val themeSystemSubtitle: String get() = t[StrKey.THEME_SYSTEM_SUBTITLE]!!
    val themeLightSubtitle: String get() = t[StrKey.THEME_LIGHT_SUBTITLE]!!
    val themeDarkSubtitle: String get() = t[StrKey.THEME_DARK_SUBTITLE]!!
    val gameSettings: String get() = t[StrKey.GAME_SETTINGS]!!
    val gameSettingsSubtitle: String get() = t[StrKey.GAME_SETTINGS_SUBTITLE]!!
    val autoEraseNotes: String get() = t[StrKey.AUTO_ERASE_NOTES]!!
    val autoEraseNotesTitle: String get() = t[StrKey.AUTO_ERASE_NOTES_TITLE]!!
    val autoEraseNotesSubtitle: String get() = t[StrKey.AUTO_ERASE_NOTES_SUBTITLE]!!
    val showConflicts: String get() = t[StrKey.SHOW_CONFLICTS]!!
    val showConflictsTitle: String get() = t[StrKey.SHOW_CONFLICTS_TITLE]!!
    val showConflictsSubtitle: String get() = t[StrKey.SHOW_CONFLICTS_SUBTITLE]!!

    val generatingPuzzle: String get() = t[StrKey.GENERATING_PUZZLE]!!
    val undo: String get() = t[StrKey.UNDO]!!
    val redo: String get() = t[StrKey.REDO]!!
    val notes: String get() = t[StrKey.NOTES]!!
    val erase: String get() = t[StrKey.ERASE]!!
    val paused: String get() = t[StrKey.PAUSED]!!
    val comboLabel: String get() = t[StrKey.COMBO_LABEL]!!
    val progressLabel: String get() = t[StrKey.PROGRESS_LABEL]!!
    val allComplete: String get() = t[StrKey.ALL_COMPLETE]!!
    val gamePaused: String get() = t[StrKey.GAME_PAUSED]!!
    val resumeGame: String get() = t[StrKey.RESUME_GAME]!!
    val victoryTitleDaily: String get() = t[StrKey.VICTORY_DAILY]!!
    val victoryTitleNormal: String get() = t[StrKey.VICTORY_NORMAL]!!
    val levelColumn: String get() = t[StrKey.LEVEL_COLUMN]!!
    val timeColumn: String get() = t[StrKey.TIME_COLUMN]!!
    val mistakesColumn: String get() = t[StrKey.MISTAKES_COLUMN]!!
    val maxComboColumn: String get() = t[StrKey.MAX_COMBO_COLUMN]!!
    val scoreColumn: String get() = t[StrKey.SCORE_COLUMN]!!
    val backToLevels: String get() = t[StrKey.BACK_TO_LEVELS]!!
    val nextLevel: String get() = t[StrKey.NEXT_LEVEL]!!
    val timeUp: String get() = t[StrKey.TIME_UP]!!
    val challengeFailed: String get() = t[StrKey.CHALLENGE_FAILED]!!
    val notFinishedInTime: String get() = t[StrKey.NOT_FINISHED_IN_TIME]!!
    val tooManyMistakes: String get() = t[StrKey.TOO_MANY_MISTAKES]!!
    val retry: String get() = t[StrKey.RETRY]!!

    fun hintButton(count: Int): String = t[StrKey.HINT_BUTTON]!!.substitute(count)
    fun levelTitle(level: Int): String = t[StrKey.LEVEL_TITLE]!!.substitute(level)
    fun levelCompleted(level: Int): String = t[StrKey.LEVEL_COMPLETED]!!.substitute(level)
    fun levelRange(first: Int, last: Int): String = t[StrKey.LEVEL_RANGE]!!.substitute(first, last)
    fun unlockedProgress(count: Int, total: Int): String = t[StrKey.UNLOCKED_PROGRESS]!!.substitute(count, total)
    fun cellsRemaining(count: Int): String = t[StrKey.CELLS_REMAINING]!!.substitute(count)
    fun levelsUnlocked(count: Int): String = t[StrKey.LEVELS_UNLOCKED]!!.substitute(count)
    fun topGlobal(rank: Int): String = t[StrKey.TOP_GLOBAL]!!.substitute(rank)
    fun me(username: String): String = t[StrKey.ME_LABEL]!!.substitute(username)
    fun dailyStreak(days: Int): String = t[StrKey.DAILY_STREAK]!!.substitute(days)
    fun levelEntry(level: Int): String = t[StrKey.LEVEL_ENTRY]!!.substitute(level)
    fun difficultyName(level: Int): String = when (level) {
        1 -> difficultyEasy
        2 -> difficultyMedium
        3 -> difficultyHard
        4 -> difficultyExpert
        else -> difficultyMaster
    }
}

private fun String.substitute(vararg args: Any): String {
    var out = this
    args.forEachIndexed { i, arg -> out = out.replace("{$i}", arg.toString()) }
    return out
}

/** Simplified-Chinese strings, the source language of the product. */
val zhStrings: Map<StrKey, String> = buildMap {
    putAll(
        arrayOf(
            StrKey.DIFFICULTY_EASY to "简单", StrKey.DIFFICULTY_MEDIUM to "中等",
            StrKey.DIFFICULTY_HARD to "困难", StrKey.DIFFICULTY_EXPERT to "专家",
            StrKey.DIFFICULTY_MASTER to "大师",
            StrKey.THEME_SYSTEM to "跟随系统", StrKey.THEME_LIGHT to "浅色模式", StrKey.THEME_DARK to "深色模式",
            StrKey.WELCOME_TAGLINE to "全平台数独 · 挑战你的大脑",
            StrKey.FEATURE_SYNC to "多端同步", StrKey.FEATURE_LEADERBOARD to "全球排行",
            StrKey.FEATURE_CROSS_PLATFORM to "全平台", StrKey.STANDALONE_MODE to "单机模式",
            StrKey.ONLINE_MODE to "联网模式 · 登录同步进度",
            StrKey.CREATE_ACCOUNT_TITLE to "创建账号", StrKey.WELCOME_BACK_TITLE to "欢迎回来",
            StrKey.CREATE_ACCOUNT_SUBTITLE to "加入全球数独社区",
            StrKey.SIGN_IN_SUBTITLE to "登录以同步你的进度",
            StrKey.USERNAME_LABEL to "用户名", StrKey.USERNAME_PLACEHOLDER to "请输入用户名",
            StrKey.PASSWORD_LABEL to "密码", StrKey.PASSWORD_PLACEHOLDER to "请输入密码",
            StrKey.FILL_ALL_FIELDS to "请填写所有字段", StrKey.AUTH_FAILED to "认证失败",
            StrKey.REGISTER_BUTTON to "注册账号", StrKey.SIGN_IN_BUTTON to "登 录",
            StrKey.GO_TO_SIGN_IN to "已有账号？去登录", StrKey.GO_TO_REGISTER to "没有账号？立即注册",
            StrKey.PROFILE_TITLE to "个人中心", StrKey.GUEST to "游客",
            StrKey.MAX_LEVEL_LABEL to "最高关卡", StrKey.GLOBAL_RANK_LABEL to "全球排名",
            StrKey.ONLINE_STATUS to "在线", StrKey.ACCOUNT_STATUS_LABEL to "账号状态",
            StrKey.CHANGE_USERNAME_TITLE to "修改用户名", StrKey.CHANGE_USERNAME_SUBTITLE to "更新你的显示名称",
            StrKey.CHANGE_PASSWORD_TITLE to "修改密码", StrKey.CHANGE_PASSWORD_SUBTITLE to "定期更换密码更安全",
            StrKey.LOGOUT to "退出登录",
            StrKey.EDIT_PROFILE_TITLE to "编辑资料",
            StrKey.NEW_USERNAME_SYNCS to "新用户名将在所有设备上同步",
            StrKey.PROFILE_UPDATE_SUCCESS to "资料更新成功!", StrKey.PROFILE_UPDATE_FAILED to "更新失败，请稍后再试",
            StrKey.SAVE_CHANGES to "保存修改",
            StrKey.CURRENT_PASSWORD to "当前密码", StrKey.NEW_PASSWORD to "新密码",
            StrKey.PASSWORD_HINT_RULE to "新密码至少 6 位，建议混合字母和数字",
            StrKey.PASSWORD_CHANGE_SUCCESS to "密码修改成功!", StrKey.PASSWORD_CHANGE_FAILED to "修改失败，请检查当前密码",
            StrKey.UPDATE_PASSWORD to "更新密码",
            StrKey.LEADERBOARD_TITLE to "排行榜", StrKey.NO_RANKING_YET to "还没有排名",
            StrKey.BE_FIRST to "快来成为第一个上榜的人吧!", StrKey.TOP_THREE to "🏆 前三名",
            StrKey.NOT_ON_BOARD to "尚未上榜", StrKey.KEEP_GOING to "继续加油!",
            StrKey.POINTS_SUFFIX to "分",
            StrKey.CHOOSE_LEVEL to "选择关卡", StrKey.DAILY_CHALLENGE to "每日挑战",
            StrKey.DAILY_DONE_TODAY to "今日已完成", StrKey.DAILY_A_NEW_PUZZLE to "每天一道题",
            StrKey.TIMED_CHALLENGE to "限时挑战", StrKey.TIMED_10_MINUTES to "10 分钟",
            StrKey.LEVEL_PROGRESS to "关卡进度",
            StrKey.APPEARANCE_SETTINGS to "外观设置", StrKey.APPEARANCE_SUBTITLE to "选择你喜欢的界面主题",
            StrKey.THEME_MODE_LABEL to "主题模式", StrKey.THEME_SYSTEM_SUBTITLE to "根据设备自动切换",
            StrKey.THEME_LIGHT_SUBTITLE to "明亮清爽的界面", StrKey.THEME_DARK_SUBTITLE to "夜间护眼更舒适",
            StrKey.GAME_SETTINGS to "游戏设置", StrKey.GAME_SETTINGS_SUBTITLE to "自定义你的游戏体验",
            StrKey.AUTO_ERASE_NOTES to "自动擦除笔记", StrKey.AUTO_ERASE_NOTES_TITLE to "填入数字后自动删除相关候选数",
            StrKey.AUTO_ERASE_NOTES_SUBTITLE to "提高求解效率",
            StrKey.SHOW_CONFLICTS to "显示冲突", StrKey.SHOW_CONFLICTS_TITLE to "实时高亮错误单元格",
            StrKey.SHOW_CONFLICTS_SUBTITLE to "红色背景提示错误放置",
            StrKey.GENERATING_PUZZLE to "正在生成谜题…", StrKey.UNDO to "撤销", StrKey.REDO to "恢复",
            StrKey.NOTES to "笔记", StrKey.ERASE to "擦除", StrKey.PAUSED to "已暂停",
            StrKey.COMBO_LABEL to "连击", StrKey.PROGRESS_LABEL to "完成度", StrKey.ALL_COMPLETE to "全部完成",
            StrKey.GAME_PAUSED to "游戏已暂停", StrKey.RESUME_GAME to "继续游戏",
            StrKey.VICTORY_DAILY to "每日挑战完成!", StrKey.VICTORY_NORMAL to "恭喜通关!",
            StrKey.LEVEL_COLUMN to "关卡", StrKey.TIME_COLUMN to "用时", StrKey.MISTAKES_COLUMN to "失误",
            StrKey.MAX_COMBO_COLUMN to "最高连击", StrKey.SCORE_COLUMN to "得分",
            StrKey.BACK_TO_LEVELS to "返回关卡列表", StrKey.NEXT_LEVEL to "下一关",
            StrKey.TIME_UP to "时间到!", StrKey.CHALLENGE_FAILED to "挑战失败",
            StrKey.NOT_FINISHED_IN_TIME to "未在限定时间内完成", StrKey.TOO_MANY_MISTAKES to "失误次数已达上限",
            StrKey.RETRY to "再试一次",
            StrKey.HINT_BUTTON to "提示 ({0})", StrKey.LEVEL_TITLE to "第 {0} 关",
            StrKey.LEVEL_COMPLETED to "第 {0} 关已完成", StrKey.LEVEL_RANGE to "第 {0}-{1} 关",
            StrKey.UNLOCKED_PROGRESS to "已解锁 {0} / {1}", StrKey.CELLS_REMAINING to "剩余 {0} 格",
            StrKey.LEVELS_UNLOCKED to "已解锁 {0} 关", StrKey.TOP_GLOBAL to "已进入全球前 {0} 名",
            StrKey.ME_LABEL to "{0} (我)", StrKey.DAILY_STREAK to "连续 {0} 天",
            StrKey.LEVEL_ENTRY to "关卡 {0}"
        )
    )
}

/** English strings (fallback locale). */
val enStrings: Map<StrKey, String> = buildMap {
    putAll(
        arrayOf(
            StrKey.DIFFICULTY_EASY to "Easy", StrKey.DIFFICULTY_MEDIUM to "Medium",
            StrKey.DIFFICULTY_HARD to "Hard", StrKey.DIFFICULTY_EXPERT to "Expert",
            StrKey.DIFFICULTY_MASTER to "Master",
            StrKey.THEME_SYSTEM to "System Default", StrKey.THEME_LIGHT to "Light Mode", StrKey.THEME_DARK to "Dark Mode",
            StrKey.WELCOME_TAGLINE to "Sudoku on Every Platform · Challenge Your Brain",
            StrKey.FEATURE_SYNC to "Cross-device Sync", StrKey.FEATURE_LEADERBOARD to "Global Leaderboard",
            StrKey.FEATURE_CROSS_PLATFORM to "All Platforms", StrKey.STANDALONE_MODE to "Standalone Mode",
            StrKey.ONLINE_MODE to "Online Mode · Sign in to Sync Progress",
            StrKey.CREATE_ACCOUNT_TITLE to "Create Account", StrKey.WELCOME_BACK_TITLE to "Welcome Back",
            StrKey.CREATE_ACCOUNT_SUBTITLE to "Join the global Sudoku community",
            StrKey.SIGN_IN_SUBTITLE to "Sign in to sync your progress",
            StrKey.USERNAME_LABEL to "Username", StrKey.USERNAME_PLACEHOLDER to "Enter your username",
            StrKey.PASSWORD_LABEL to "Password", StrKey.PASSWORD_PLACEHOLDER to "Enter your password",
            StrKey.FILL_ALL_FIELDS to "Please fill in all fields", StrKey.AUTH_FAILED to "Authentication failed",
            StrKey.REGISTER_BUTTON to "Register", StrKey.SIGN_IN_BUTTON to "Sign In",
            StrKey.GO_TO_SIGN_IN to "Already have an account? Sign in",
            StrKey.GO_TO_REGISTER to "No account? Register now",
            StrKey.PROFILE_TITLE to "Profile", StrKey.GUEST to "Guest",
            StrKey.MAX_LEVEL_LABEL to "Highest Level", StrKey.GLOBAL_RANK_LABEL to "Global Rank",
            StrKey.ONLINE_STATUS to "Online", StrKey.ACCOUNT_STATUS_LABEL to "Account Status",
            StrKey.CHANGE_USERNAME_TITLE to "Change Username", StrKey.CHANGE_USERNAME_SUBTITLE to "Update your display name",
            StrKey.CHANGE_PASSWORD_TITLE to "Change Password", StrKey.CHANGE_PASSWORD_SUBTITLE to "Change regularly for security",
            StrKey.LOGOUT to "Log Out",
            StrKey.EDIT_PROFILE_TITLE to "Edit Profile",
            StrKey.NEW_USERNAME_SYNCS to "Your new username syncs across all devices",
            StrKey.PROFILE_UPDATE_SUCCESS to "Profile updated!", StrKey.PROFILE_UPDATE_FAILED to "Update failed, please try again",
            StrKey.SAVE_CHANGES to "Save Changes",
            StrKey.CURRENT_PASSWORD to "Current Password", StrKey.NEW_PASSWORD to "New Password",
            StrKey.PASSWORD_HINT_RULE to "At least 6 characters; mix letters and numbers",
            StrKey.PASSWORD_CHANGE_SUCCESS to "Password updated!", StrKey.PASSWORD_CHANGE_FAILED to "Update failed, check your current password",
            StrKey.UPDATE_PASSWORD to "Update Password",
            StrKey.LEADERBOARD_TITLE to "Leaderboard", StrKey.NO_RANKING_YET to "No ranking yet",
            StrKey.BE_FIRST to "Be the first to make the board!", StrKey.TOP_THREE to "🏆 Top 3",
            StrKey.NOT_ON_BOARD to "Not ranked", StrKey.KEEP_GOING to "Keep going!",
            StrKey.POINTS_SUFFIX to "pts",
            StrKey.CHOOSE_LEVEL to "Choose Level", StrKey.DAILY_CHALLENGE to "Daily Challenge",
            StrKey.DAILY_DONE_TODAY to "Done for today", StrKey.DAILY_A_NEW_PUZZLE to "A new puzzle every day",
            StrKey.TIMED_CHALLENGE to "Timed Challenge", StrKey.TIMED_10_MINUTES to "10 Minutes",
            StrKey.LEVEL_PROGRESS to "Level Progress",
            StrKey.APPEARANCE_SETTINGS to "Appearance", StrKey.APPEARANCE_SUBTITLE to "Pick the theme you like",
            StrKey.THEME_MODE_LABEL to "Theme Mode", StrKey.THEME_SYSTEM_SUBTITLE to "Automatically match your device",
            StrKey.THEME_LIGHT_SUBTITLE to "Bright and clean interface",
            StrKey.THEME_DARK_SUBTITLE to "Easier on the eyes at night",
            StrKey.GAME_SETTINGS to "Game Settings", StrKey.GAME_SETTINGS_SUBTITLE to "Customize your experience",
            StrKey.AUTO_ERASE_NOTES to "Auto-erase Notes",
            StrKey.AUTO_ERASE_NOTES_TITLE to "Delete related candidates after placing a number",
            StrKey.AUTO_ERASE_NOTES_SUBTITLE to "Solve faster",
            StrKey.SHOW_CONFLICTS to "Show Conflicts", StrKey.SHOW_CONFLICTS_TITLE to "Highlight wrong cells in real time",
            StrKey.SHOW_CONFLICTS_SUBTITLE to "Red background for placed errors",
            StrKey.GENERATING_PUZZLE to "Generating puzzle…", StrKey.UNDO to "Undo", StrKey.REDO to "Redo",
            StrKey.NOTES to "Notes", StrKey.ERASE to "Erase", StrKey.PAUSED to "Paused",
            StrKey.COMBO_LABEL to "Combo", StrKey.PROGRESS_LABEL to "Progress", StrKey.ALL_COMPLETE to "Complete",
            StrKey.GAME_PAUSED to "Game Paused", StrKey.RESUME_GAME to "Resume Game",
            StrKey.VICTORY_DAILY to "Daily Challenge Complete!", StrKey.VICTORY_NORMAL to "Congratulations!",
            StrKey.LEVEL_COLUMN to "Level", StrKey.TIME_COLUMN to "Time", StrKey.MISTAKES_COLUMN to "Mistakes",
            StrKey.MAX_COMBO_COLUMN to "Best Combo", StrKey.SCORE_COLUMN to "Score",
            StrKey.BACK_TO_LEVELS to "Back to Levels", StrKey.NEXT_LEVEL to "Next Level",
            StrKey.TIME_UP to "Time's Up!", StrKey.CHALLENGE_FAILED to "Challenge Failed",
            StrKey.NOT_FINISHED_IN_TIME to "Not finished within the time limit",
            StrKey.TOO_MANY_MISTAKES to "Mistakes reached the limit",
            StrKey.RETRY to "Try Again",
            StrKey.HINT_BUTTON to "Hint ({0})", StrKey.LEVEL_TITLE to "Level {0}",
            StrKey.LEVEL_COMPLETED to "Level {0} Complete", StrKey.LEVEL_RANGE to "Levels {0}-{1}",
            StrKey.UNLOCKED_PROGRESS to "Unlocked {0} / {1}", StrKey.CELLS_REMAINING to "{0} cells left",
            StrKey.LEVELS_UNLOCKED to "{0} levels unlocked", StrKey.TOP_GLOBAL to "Top {0} globally",
            StrKey.ME_LABEL to "{0} (Me)", StrKey.DAILY_STREAK to "{0}-day streak",
            StrKey.LEVEL_ENTRY to "Level {0}"
        )
    )
}

val zhAppStrings = AppStrings(zhStrings)
val enAppStrings = AppStrings(enStrings)

/** Composition-local provider of the active UI strings. Defaults to Chinese. */
val LocalAppStrings = staticCompositionLocalOf { zhAppStrings }

/** Picks a locale instance from the platform's language tag (e.g. "zh-CN"). */
fun appStringsFor(languageCode: String): AppStrings =
    if (languageCode.startsWith("zh")) zhAppStrings else enAppStrings

/** Platform's primary language tag (e.g. "zh-CN", "en-US"), or "en" when unavailable. */
expect fun platformLanguageCode(): String