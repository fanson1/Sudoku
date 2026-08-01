package com.finley.android.sudoku.grading

import com.finley.android.sudoku.model.Board
import com.finley.android.sudoku.model.Difficulty

data class DifficultyScore(
    val tier: Int,
    val difficulty: Difficulty,
    val score: Int
)

class DifficultyGrader {
    fun gradeDifficulty(board: Board): DifficultyScore {
        // Placeholder implementation based on empty cell count
        // Tier system should be implemented here as described in 4.4
        val emptyCells = board.cells.flatten().count { it.value == null }
        val difficulty = when {
            emptyCells < 35 -> Difficulty.EASY
            emptyCells < 45 -> Difficulty.MEDIUM
            emptyCells < 55 -> Difficulty.HARD
            emptyCells < 65 -> Difficulty.EXPERT
            else -> Difficulty.MASTER
        }
        return DifficultyScore(1, difficulty, emptyCells)
    }
}
