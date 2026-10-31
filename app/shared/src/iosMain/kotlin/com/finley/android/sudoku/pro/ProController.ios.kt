package com.finley.android.sudoku.pro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberProController(): ProController = remember { LocalProController() }
