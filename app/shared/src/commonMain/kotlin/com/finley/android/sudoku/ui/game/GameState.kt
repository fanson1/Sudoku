package com.finley.android.sudoku.ui.game

import com.finley.android.sudoku.model.Board
import com.finley.android.sudoku.model.Difficulty
import com.finley.android.sudoku.model.GameRules
import com.finley.android.sudoku.model.Move

data class GameState(
    val gameMode: GameMode = GameMode.NORMAL,
    val board: Board = Board(cells = emptyList()),
    val solution: String = "",
    val level: Int = 1,
    val dailyDate: String? = null,
    val difficulty: Difficulty = Difficulty.EASY,
    val selectedCell: Pair<Int, Int>? = null,
    val notesMode: Boolean = false,
    val elapsedSeconds: Int = 0,
    val timeLimitSeconds: Int? = null,
    val mistakeCount: Int = 0,
    val maxMistakes: Int = 5,
    val comboCount: Int = 0,
    val history: List<Move> = emptyList(),
    val historyCursor: Int = -1, // Index of the last performed move
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val hintsRemaining: Int = GameRules.getInitialHintsForLevel(1),
    val isLoading: Boolean = true,
    val autoEraseNotes: Boolean = true,
    val showConflicts: Boolean = true,
    val completedNumbers: Set<Int> = emptySet(),
    val conflictingCells: Set<Pair<Int, Int>> = emptySet(),
    val hintCell: Pair<Int, Int>? = null,
    val activeNumber: Int? = null,
    val nakedSingleCell: Pair<Int, Int>? = null,
    val nakedSingleValue: Int? = null
)

fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}

fun calculateScore(state: GameState): Int {
    val baseScore = when (state.difficulty) {
        Difficulty.EASY -> 1000
        Difficulty.MEDIUM -> 2000
        Difficulty.HARD -> 3000
        Difficulty.EXPERT -> 5000
        Difficulty.MASTER -> 8000
    }

    val timePenalty = state.elapsedSeconds * 2
    val mistakePenalty = state.mistakeCount * 100

    // Combo multiplier: each mistake-free move extends a streak that
    // boosts the final score, rewarding consecutive smart placements.
    val comboMultiplier = 1.0 + (state.comboCount * 0.05).coerceIn(0.0, 1.0)

    return ((baseScore - timePenalty - mistakePenalty) * comboMultiplier).toInt().coerceAtLeast(100)
}
