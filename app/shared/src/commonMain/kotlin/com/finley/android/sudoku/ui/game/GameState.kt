package com.finley.android.sudoku.ui.game

import com.finley.android.sudoku.model.Board
import com.finley.android.sudoku.model.Difficulty
import com.finley.android.sudoku.model.GameRules
import com.finley.android.sudoku.model.Move

data class GameState(
    val board: Board = Board(cells = emptyList()),
    val solution: String = "",
    val level: Int = 1,
    val difficulty: Difficulty = Difficulty.EASY,
    val selectedCell: Pair<Int, Int>? = null,
    val notesMode: Boolean = false,
    val elapsedSeconds: Int = 0,
    val mistakeCount: Int = 0,
    val maxMistakes: Int = 5,
    val history: List<Move> = emptyList(),
    val historyCursor: Int = -1, // Index of the last performed move
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val hintsRemaining: Int = GameRules.getInitialHintsForLevel(1),
    val isLoading: Boolean = true,
    val autoEraseNotes: Boolean = true,
    val completedNumbers: Set<Int> = emptySet(),
    val conflictingCells: Set<Pair<Int, Int>> = emptySet()
)
