package com.finley.android.sudoku.ui.i18n

import web.navigator.navigator

actual fun platformLanguageCode(): String =
    navigator.language.ifEmpty { "en" }