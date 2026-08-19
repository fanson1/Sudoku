package com.finley.android.sudoku

import com.finley.android.sudoku.ui.game.GameIntent
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RedoLogicTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // runCurrent only executes tasks due at the current virtual time, so the
    // viewmodel's infinite timer (delay(1000)) never blocks the test.
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

    private fun GameViewModel.redo() {
        dispatch(GameIntent.Redo)
        tick()
    }

    private fun GameViewModel.findEmptyCell(): Pair<Int, Int> {
        val board = state.value.board
        for (r in 0 until board.size) {
            for (c in 0 until board.size) {
                if (board.cells[r][c].value == null) return r to c
            }
        }
        throw IllegalStateException("No empty cell found")
    }

    @Test
    fun redoRestoresPlaceAfterUndo() {
        val vm = GameViewModel()
        vm.startLevelAndWait()
        assertFalse(vm.state.value.canUndo)
        assertFalse(vm.state.value.canRedo)

        val (r, c) = vm.findEmptyCell()
        vm.select(r, c)
        vm.input(5)

        assertTrue(vm.state.value.canUndo)
        assertFalse(vm.state.value.canRedo)
        assertEquals(5, vm.state.value.board.cells[r][c].value)

        vm.undo()
        assertNull(vm.state.value.board.cells[r][c].value)
        assertTrue(vm.state.value.canRedo)

        vm.redo()
        assertEquals(5, vm.state.value.board.cells[r][c].value)
        assertFalse(vm.state.value.canRedo)
    }

    @Test
    fun redoRestoresEraseAfterUndo() {
        val vm = GameViewModel()
        vm.startLevelAndWait()

        val (r, c) = vm.findEmptyCell()
        vm.select(r, c)
        vm.input(5)
        vm.dispatch(GameIntent.Erase)
        tick()
        assertNull(vm.state.value.board.cells[r][c].value)

        vm.undo()
        assertEquals(5, vm.state.value.board.cells[r][c].value)

        vm.redo()
        assertNull(vm.state.value.board.cells[r][c].value)
    }

    @Test
    fun redoRestoresToggleCandidateAfterUndo() {
        val vm = GameViewModel()
        vm.startLevelAndWait()

        val (r, c) = vm.findEmptyCell()
        vm.select(r, c)
        vm.dispatch(GameIntent.ToggleNotesMode)
        tick()
        vm.input(3)
        assertTrue(vm.state.value.board.cells[r][c].candidates.contains(3))

        vm.undo()
        assertFalse(vm.state.value.board.cells[r][c].candidates.contains(3))

        vm.redo()
        assertTrue(vm.state.value.board.cells[r][c].candidates.contains(3))
    }

    @Test
    fun newMoveAfterUndoClearsRedoHistory() {
        val vm = GameViewModel()
        vm.startLevelAndWait()

        val (r, c) = vm.findEmptyCell()
        vm.select(r, c)
        vm.input(5)
        vm.undo()
        assertTrue(vm.state.value.canRedo)

        vm.input(7)
        assertFalse(vm.state.value.canRedo)
        assertEquals(7, vm.state.value.board.cells[r][c].value)
    }

    @Test
    fun nakedSingleIsDetectedForSingleCandidateCell() {
        val vm = GameViewModel()
        vm.startLevelAndWait()

        // Scan the board for any cell with exactly one legal candidate.
        val board = vm.state.value.board
        val target = (0 until 9).flatMap { r -> (0 until 9).map { c -> r to c } }
            .firstOrNull { (r, c) ->
                val cell = board.cells[r][c]
                cell.value == null &&
                    com.finley.android.sudoku.solver.SudokuSolver().getCandidates(board, r, c).size == 1
            }
            ?: return // No naked single at puzzle start; nothing to assert.

        vm.select(target.first, target.second)
        assertNotNull(vm.state.value.nakedSingleCell)
        assertEquals(target, vm.state.value.nakedSingleCell)
        assertNotNull(vm.state.value.nakedSingleValue)

        // Placing the suggested value consumes the naked single.
        vm.input(vm.state.value.nakedSingleValue!!)
        assertNull(vm.state.value.nakedSingleCell)
    }

    @Test
    fun comboCountIncrementsOnValidMovesAndResetsOnMistake() {
        val vm = GameViewModel()
        vm.startLevelAndWait()

        // Place two valid numbers into empty cells.
        val empty = vm.findEmptyCell()
        val solver = com.finley.android.sudoku.solver.SudokuSolver()
        var placed = 0
        for ((r, c) in (0 until 9).flatMap { r -> (0 until 9).map { c -> r to c } }) {
            if (placed >= 2) break
            if (vm.state.value.board.cells[r][c].value != null) continue
            val candidates = solver.getCandidates(vm.state.value.board, r, c)
            if (candidates.isEmpty()) continue
            vm.select(r, c)
            vm.input(candidates.first())
            placed++
        }
        assertEquals(2, placed)
        assertEquals(2, vm.state.value.comboCount)

        // A wrong move resets the combo.
        vm.select(empty.first, empty.second)
        val wrongValue = (1..9).first { n ->
            n != vm.state.value.board.cells[empty.first][empty.second].value &&
                solver.getConflictingCells(vm.state.value.board, empty.first, empty.second, n).isNotEmpty()
        }
        vm.input(wrongValue)
        assertEquals(0, vm.state.value.comboCount)
        assertEquals(1, vm.state.value.mistakeCount)
    }
}