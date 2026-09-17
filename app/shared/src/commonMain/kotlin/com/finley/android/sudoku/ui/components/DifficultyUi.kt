package com.finley.android.sudoku.ui.components

import androidx.compose.ui.graphics.Color
import com.finley.android.sudoku.model.Difficulty
import com.finley.android.sudoku.ui.i18n.AppStrings

fun difficultyLabel(difficulty: Difficulty, strings: AppStrings): String = when (difficulty) {
    Difficulty.EASY -> strings.difficultyEasy
    Difficulty.MEDIUM -> strings.difficultyMedium
    Difficulty.HARD -> strings.difficultyHard
    Difficulty.EXPERT -> strings.difficultyExpert
    Difficulty.MASTER -> strings.difficultyMaster
}

fun difficultyColor(difficulty: Difficulty): Color = when (difficulty) {
    Difficulty.EASY -> Color(0xFF22C55E)
    Difficulty.MEDIUM -> Color(0xFFF59E0B)
    Difficulty.HARD -> Color(0xFFF97316)
    Difficulty.EXPERT -> Color(0xFFEF4444)
    Difficulty.MASTER -> Color(0xFFA855F7)
}