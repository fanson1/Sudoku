package com.finley.android.sudoku

import com.finley.android.sudoku.ui.game.GameIntent
import com.finley.android.sudoku.ui.game.GameMode
import com.finley.android.sudoku.ui.game.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Regression tests for the P0 correctness fixes:
 * - hints must keep the cell editable (undo must not leave an unfillable cell),
 * - timed challenges must not replay a fixed puzzle,
 * - undo/redo must refresh completedNumbers/conflictingCells.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameCorrectnessRegressionTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun tick() = dispatcher.scheduler.runCurrent()

    private fun GameViewModel.startLevelAndWait(level: Int = 1) {
        dispatch(GameIntent.StartLevel(level))
        tick()
    }

    private fun GameViewModel.select(row: Int, col: Int) {
        dispatch(GameIntent.SelectCell(row, col))
        tick()
    }

    private fun GameViewModel.input(value: Int) {
        dispatch(GameIntent.InputNumber(value))
        tick()
    }

    private fun GameViewModel.undo() {
        dispatch(GameIntent.Undo)
        tick()
    }

    private fun GameViewModel.hint() {
        dispatch(GameIntent.RequestHint)
        tick()
    }

    private fun GameViewModel.copyEmptyCount(): Int =
        state.value.board.cells.sumOf { row -> row.count { it.value == null } }

    @Test
    fun hintFillsValueWithoutTurningCellIntoGiven() {
        val vm = GameViewModel()
        vm.startLevelAndWait()
        vm.hint()

        assertEquals(2, vm.state.value.hintsRemaining)
        val (r, c) = vm.state.value.hintCell ?: throw AssertionError("hint should highlight a cell")
        val cell = vm.state.value.board.cells[r][c]
        assertEquals(
            vm.state.value.solution[r * 9 + c].digitToInt(),
            cell.value,
            "hint must reveal the solution value"
        )
        assertFalse(cell.isGiven, "a hinted cell must stay a regular (editable) cell")
    }

    @Test
    fun undoAfterHintLeavesCellFillableAgain() {
        val vm = GameViewModel()
        vm.startLevelAndWait()

        vm.hint()
        val (r, c) = vm.state.value.hintCell!!
        val hintedValue = vm.state.value.board.cells[r][c].value!!

        vm.undo()
        assertNull(vm.state.value.board.cells[r][c].value, "undo must clear the hinted value")
        assertFalse(vm.state.value.board.cells[r][c].isGiven)
        assertTrue(vm.state.value.canRedo)

        // Without the fix the cell kept isGiven=true and rejected any input forever.
        vm.select(r, c)
        vm.input(hintedValue)
        assertEquals(hintedValue, vm.state.value.board.cells[r][c].value)

        vm.undo()
        assertTrue(vm.state.value.canRedo)
        vm.dispatch(GameIntent.Redo)
        tick()
        assertEquals(hintedValue, vm.state.value.board.cells[r][c].value)
    }

    @Test
    fun exhaustedHintsAreNoOps() {
        val vm = GameViewModel()
        vm.startLevelAndWait()
        repeat(3) {
            vm.hint()
        }
        assertEquals(0, vm.state.value.hintsRemaining)

        val emptyBefore = vm.copyEmptyCount()
        vm.hint()
        assertEquals(emptyBefore, vm.copyEmptyCount(), "no more hints may fill cells")
    }

    @Test
    fun placingLastOfDigitMarksItCompletedAndUndoRefreshesIt() {
        val vm = GameViewModel()
        vm.startLevelAndWait()

        val (placed, lastCell) = vm.placeAllOfDigit(1)
        assertTrue(placed.isNotEmpty(), "level 1 puzzles must leave some empty 1-cells")
        assertTrue(
            1 in vm.state.value.completedNumbers,
            "after placing every empty 1 the digit must be counted as completed"
        )
        assertEquals(9, vm.state.value.board.cells.sumOf { row -> row.count { it.value == 1 } })

        vm.select(lastCell.first, lastCell.second)
        vm.undo()
        assertFalse(
            1 in vm.state.value.completedNumbers,
            "undoing the final placement must refresh completedNumbers"
        )
        assertTrue(vm.state.value.conflictingCells.isEmpty())
    }

    @Test
    fun timedChallengesGenerateFreshPuzzles() {
        val vm = GameViewModel()
        vm.dispatch(GameIntent.StartTimedChallenge(300))
        tick()
        val firstSolution = vm.state.value.solution
        assertEquals(GameMode.TIMED, vm.state.value.gameMode)
        assertEquals(300, vm.state.value.timeLimitSeconds)
        assertNotNull(vm.state.value.timeLimitSeconds)

        vm.dispatch(GameIntent.StartTimedChallenge(300))
        tick()
        assertNotEquals(
            firstSolution,
            vm.state.value.solution,
            "each timed run should present a fresh puzzle, not a fixed board"
        )
    }

    private data class CellPlacement(val row: Int, val col: Int, val digit: Int)

    /** Places all non-given cells of [digit] using the solution values. */
    private fun GameViewModel.placeAllOfDigit(digit: Int): Pair<List<CellPlacement>, Pair<Int, Int>> {
        val solution = state.value.solution
        val placements = mutableListOf<CellPlacement>()
        var lastCell: Pair<Int, Int>? = null
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val cell = state.value.board.cells[r][c]
                if (cell.value == null && solution[r * 9 + c].digitToInt() == digit) {
                    select(r, c)
                    input(digit)
                    placements += CellPlacement(r, c, digit)
                    lastCell = r to c
                }
            }
        }
        return placements to (lastCell ?: throw AssertionError("no empty cell for digit $digit"))
    }
}