package com.finley.android.sudoku.ui.game

sealed class GameEffect {
    data object ShowVictoryDialog : GameEffect()
    data object ShowGameOverDialog : GameEffect()
    data class ShowHintExplanation(val text: String) : GameEffect()
    data object PlaySoundError : GameEffect()
    data object PlaySoundSuccess : GameEffect()
    data object VibrateError : GameEffect()
    data class NumberCompleted(val number: Int) : GameEffect()
}
