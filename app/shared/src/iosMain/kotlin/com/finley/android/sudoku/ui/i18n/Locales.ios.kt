package com.finley.android.sudoku.ui.i18n

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

actual fun platformLanguageCode(): String =
    NSLocale.currentLocale.languageCode?.lowercase() ?: "en"