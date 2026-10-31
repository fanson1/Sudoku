package com.finley.android.sudoku.i18n

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

actual fun systemLanguageTag(): String =
    (NSLocale.preferredLanguages.firstOrNull() as? String) ?: "en"
