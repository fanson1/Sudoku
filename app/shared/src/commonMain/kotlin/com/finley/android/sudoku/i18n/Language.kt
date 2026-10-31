package com.finley.android.sudoku.i18n

import androidx.compose.runtime.staticCompositionLocalOf

/** Languages with a full in-app translation. */
enum class Language(val code: String, val nativeName: String) {
    SYSTEM("system", "Follow system"),
    ENGLISH("en", "English"),
    CHINESE_SIMPLIFIED("zh-Hans", "简体中文"),
    CHINESE_TRADITIONAL("zh-Hant", "繁體中文"),
    JAPANESE("ja", "日本語"),
    KOREAN("ko", "한국어"),
    SPANISH("es", "Español"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch");

    companion object {
        fun fromCode(code: String?): Language =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
    }
}

val LocalStrings = staticCompositionLocalOf { Strings(EnglishValues) }

/** BCP-47 tag of the device language, e.g. `zh-Hans-CN`. */
expect fun systemLanguageTag(): String

/** Resolves a preference (possibly [Language.SYSTEM]) to a concrete language. */
fun resolveLanguage(preference: Language): Language =
    if (preference == Language.SYSTEM) languageFromTag(systemLanguageTag()) else preference

fun languageFromTag(tag: String): Language {
    val normalized = tag.replace('_', '-').lowercase()
    val primary = normalized.substringBefore('-')
    return when {
        primary == "zh" -> {
            val traditional = normalized.contains("hant") ||
                normalized.contains("tw") ||
                normalized.contains("hk") ||
                normalized.contains("mo")
            if (traditional) Language.CHINESE_TRADITIONAL else Language.CHINESE_SIMPLIFIED
        }
        primary == "ja" -> Language.JAPANESE
        primary == "ko" -> Language.KOREAN
        primary == "es" -> Language.SPANISH
        primary == "fr" -> Language.FRENCH
        primary == "de" -> Language.GERMAN
        else -> Language.ENGLISH
    }
}

fun stringsFor(language: Language): Strings = when (language) {
    Language.SYSTEM, Language.ENGLISH -> Strings(EnglishValues)
    Language.CHINESE_SIMPLIFIED -> Strings(ZhHansValues)
    Language.CHINESE_TRADITIONAL -> Strings(ZhHantValues)
    Language.JAPANESE -> Strings(JaValues)
    Language.KOREAN -> Strings(KoValues)
    Language.SPANISH -> Strings(EsValues)
    Language.FRENCH -> Strings(FrValues)
    Language.GERMAN -> Strings(DeValues)
}
