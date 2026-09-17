package com.finley.android.sudoku.ui.i18n

import java.util.Locale

actual fun platformLanguageCode(): String =
    Locale.getDefault().language.ifEmpty { "en" }