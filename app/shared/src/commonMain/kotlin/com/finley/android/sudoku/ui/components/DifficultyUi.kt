package com.finley.android.sudoku.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.finley.android.sudoku.model.Difficulty

data class DifficultyUi(
    val label: String,
    val color: Color
)

@Composable
fun difficultyUi(difficulty: Difficulty): DifficultyUi {
    val colorScheme = MaterialTheme.colorScheme
    return when (difficulty) {
        Difficulty.EASY -> DifficultyUi("简单", Color(0xFF22C55E))
        Difficulty.MEDIUM -> DifficultyUi("中等", Color(0xFFF59E0B))
        Difficulty.HARD -> DifficultyUi("困难", Color(0xFFF97316))
        Difficulty.EXPERT -> DifficultyUi("专家", Color(0xFFEF4444))
        Difficulty.MASTER -> DifficultyUi("大师", Color(0xFFA855F7))
    }.also { }
}

fun difficultyLabel(difficulty: Difficulty): String = when (difficulty) {
    Difficulty.EASY -> "简单"
    Difficulty.MEDIUM -> "中等"
    Difficulty.HARD -> "困难"
    Difficulty.EXPERT -> "专家"
    Difficulty.MASTER -> "大师"
}

fun difficultyColor(difficulty: Difficulty): Color = when (difficulty) {
    Difficulty.EASY -> Color(0xFF22C55E)
    Difficulty.MEDIUM -> Color(0xFFF59E0B)
    Difficulty.HARD -> Color(0xFFF97316)
    Difficulty.EXPERT -> Color(0xFFEF4444)
    Difficulty.MASTER -> Color(0xFFA855F7)
}
