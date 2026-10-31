package com.finley.android.sudoku.i18n

import java.util.Locale

actual fun systemLanguageTag(): String = Locale.getDefault().toLanguageTag()
