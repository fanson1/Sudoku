package com.finley.android.sudoku.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.finley.android.sudoku.i18n.LocalStrings
import com.finley.android.sudoku.i18n.StringKey
import com.finley.android.sudoku.model.Difficulty

fun difficultyColor(difficulty: Difficulty): Color = when (difficulty) {
    Difficulty.EASY -> Color(0xFF22C55E)
    Difficulty.MEDIUM -> Color(0xFFF59E0B)
    Difficulty.HARD -> Color(0xFFF97316)
    Difficulty.EXPERT -> Color(0xFFEF4444)
    Difficulty.MASTER -> Color(0xFFA855F7)
}

fun difficultyKey(difficulty: Difficulty): StringKey = when (difficulty) {
    Difficulty.EASY -> StringKey.DIFFICULTY_EASY
    Difficulty.MEDIUM -> StringKey.DIFFICULTY_MEDIUM
    Difficulty.HARD -> StringKey.DIFFICULTY_HARD
    Difficulty.EXPERT -> StringKey.DIFFICULTY_EXPERT
    Difficulty.MASTER -> StringKey.DIFFICULTY_MASTER
}

@Composable
fun difficultyLabel(difficulty: Difficulty): String =
    LocalStrings.current[difficultyKey(difficulty)]
