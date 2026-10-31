package com.finley.android.sudoku.i18n

/**
 * Every user-visible string in the app. Adding a key here forces all language
 * packs to provide a translation (missing keys fall back to English at runtime,
 * but `TranslationsCoverageTest` fails the build if a pack is incomplete).
 */
enum class StringKey {
    // Common
    APP_NAME,
    OK,
    CANCEL,
    CLOSE,
    BACK,
    CONTINUE,
    RETRY,
    VERSION,
    OFFLINE_TAGLINE,

    // Home / level selection
    LEVEL_SELECTION,
    PROGRESS_TITLE,
    PROGRESS_UNLOCKED,
    LEVEL_RANGE,
    DIFFICULTY_EASY,
    DIFFICULTY_MEDIUM,
    DIFFICULTY_HARD,
    DIFFICULTY_EXPERT,
    DIFFICULTY_MASTER,
    DAILY_CHALLENGE,
    DAILY_DONE,
    DAILY_SUBTITLE,
    DAILY_STREAK,
    TIMED_CHALLENGE,
    TIMED_SUBTITLE,
    PRO_BADGE,
    PRO_ONLY,
    STATS_TITLE,
    STATS_LOCKED,
    GAMES_PLAYED,
    GAMES_WON,
    WIN_RATE,
    BEST_SCORE,
    DAILY_STREAK_LABEL,
    PRO_ENTRY_TITLE,
    PRO_ENTRY_SUBTITLE,
    PRO_ACTIVE,
    PRO_ACTIVE_SUBTITLE,
    LOCKED_LEVEL_MESSAGE,
    CHALLENGE_LOCKED_MESSAGE,
    SETTINGS,

    // Settings
    APPEARANCE_SETTINGS,
    APPEARANCE_SUBTITLE,
    THEME_MODE,
    THEME_SYSTEM,
    THEME_SYSTEM_SUB,
    THEME_LIGHT,
    THEME_LIGHT_SUB,
    THEME_DARK,
    THEME_DARK_SUB,
    LANGUAGE,
    LANGUAGE_SUBTITLE,
    LANGUAGE_SYSTEM,
    GAME_SETTINGS,
    GAME_SETTINGS_SUBTITLE,
    AUTO_ERASE_NOTES,
    AUTO_ERASE_NOTES_TITLE,
    AUTO_ERASE_NOTES_SUB,
    SHOW_CONFLICTS,
    SHOW_CONFLICTS_TITLE,
    SHOW_CONFLICTS_SUB,
    PRO_SECTION,
    RESTORE_PURCHASE,

    // Paywall
    PAYWALL_TITLE,
    PAYWALL_SUBTITLE,
    PAYWALL_FEATURE_LEVELS,
    PAYWALL_FEATURE_DAILY,
    PAYWALL_FEATURE_TIMED,
    PAYWALL_FEATURE_HINTS,
    PAYWALL_FEATURE_STATS,
    PAYWALL_FEATURE_FOREVER,
    PAYWALL_BUY,
    PAYWALL_BUY_FALLBACK,
    PAYWALL_RESTORE,
    PAYWALL_NOTE,
    PAYWALL_PROCESSED_BY,
    PAYWALL_ALREADY_PRO,
    PAYWALL_START,
    PAYWALL_UNAVAILABLE,
    PAYWALL_PRICE_LOADING,

    // Purchase / restore messages
    MSG_PURCHASE_SUCCESS,
    MSG_PURCHASE_CANCELLED,
    MSG_PURCHASE_FAILED,
    MSG_PURCHASE_PENDING,
    MSG_RESTORE_SUCCESS,
    MSG_RESTORE_NOTHING,
    MSG_BILLING_UNAVAILABLE,

    // Game
    LEVEL_N,
    GENERATING_PUZZLE,
    UNDO,
    REDO,
    HINT,
    HINT_UNLIMITED,
    PAUSED,
    GAME_PAUSED,
    RESUME_GAME,
    COMBO,
    COMBO_X,
    PROGRESS,
    REMAINING_CELLS,
    ALL_DONE,
    NOTES,
    ERASE,
    VICTORY,
    DAILY_VICTORY,
    LEVEL_COMPLETED,
    RESULT_LEVEL,
    RESULT_TIME,
    RESULT_MISTAKES,
    RESULT_MAX_COMBO,
    SCORE,
    BACK_TO_LEVELS,
    NEXT_LEVEL,
    TIME_UP,
    CHALLENGE_FAILED,
    TIME_UP_SUBTITLE,
    MISTAKES_LIMIT_SUBTITLE,
    TRY_AGAIN,
    RESTART,
    PRO_UNLIMITED_HINTS,

    // Statistics
    STATS_SUBTITLE,
    TOTAL_GAMES,
    TOTAL_WINS,
    WIN_RATE_LABEL,
    BEST_SCORE_LABEL,
    BEST_BY_DIFFICULTY,
    DAILY_STREAK_TITLE,
    DAYS,
    NO_GAMES_YET,
    BACK_TO_HOME,

    // Content descriptions
    CD_BACK,
    CD_SETTINGS,
    CD_RESTART,
    CD_LOCKED,
    CD_STATS
}

/**
 * A language pack. Missing keys transparently fall back to English so a partial
 * pack never crashes the UI.
 */
class Strings(private val values: Map<StringKey, String>) {
    operator fun get(key: StringKey): String = values[key] ?: EnglishValues[key] ?: key.name

    /** Replaces `{name}` placeholders, e.g. `fmt(LEVEL_N, "n" to 7)`. */
    fun fmt(key: StringKey, vararg args: Pair<String, Any>): String {
        var text = get(key)
        args.forEach { (name, value) -> text = text.replace("{$name}", value.toString()) }
        return text
    }
}
