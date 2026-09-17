package com.finley.android.sudoku.ui.i18n

import kotlinx.browser.window

actual fun platformLanguageCode(): String =
    window.navigator.language.ifEmpty { "en" }