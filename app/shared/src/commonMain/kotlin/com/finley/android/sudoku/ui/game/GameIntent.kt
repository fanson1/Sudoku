package com.finley.android.sudoku.ui.game

sealed class GameIntent {
    data class SelectCell(val row: Int, val col: Int) : GameIntent()
    data class InputNumber(val value: Int) : GameIntent()
    data object Erase : GameIntent()
    data object ToggleNotesMode : GameIntent()
    data object Undo : GameIntent()
    data object Redo : GameIntent()
    data object RequestHint : GameIntent()
    data object PauseGame : GameIntent()
    data object ResumeGame : GameIntent()
    data object StartNewGame : GameIntent()
    data class StartLevel(val level: Int) : GameIntent()
}
