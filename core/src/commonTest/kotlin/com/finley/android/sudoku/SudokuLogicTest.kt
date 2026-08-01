package com.finley.android.sudoku

import com.finley.android.sudoku.model.Board
import com.finley.android.sudoku.model.Cell
import com.finley.android.sudoku.solver.SudokuSolver
import com.finley.android.sudoku.generator.SudokuGenerator
import com.finley.android.sudoku.model.Difficulty
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SudokuLogicTest {
    private val solver = SudokuSolver()

    @Test
    fun testUniqueSolution() {
        // Test with a known unique solution
        val boardStr = "530070000600195000098000060800060003400803001700020006060000280000419005000080079"
        val board = parseBoard(boardStr)
        assertTrue(solver.hasUniqueSolution(board), "Board should have a unique solution")
    }

    @Test
    fun testEmptyBoard() {
        val boardStr = "0".repeat(81)
        val board = parseBoard(boardStr)
        assertFalse(solver.hasUniqueSolution(board), "Empty board should not have a unique solution")
    }

    @Test
    fun testGenerator() {
        val generator = SudokuGenerator(solver)
        val solvedBoard = generator.generateSolvedBoard(seed = 12345L)
        
        // Verify solved board is valid
        assertTrue(isBoardValid(solvedBoard), "Generated solved board should be valid")
        
        val puzzle = generator.generatePuzzle(Difficulty.EASY, seed = 12345L)
        val initialBoard = parseBoard(puzzle.initialClues)
        
        assertTrue(solver.hasUniqueSolution(initialBoard), "Generated puzzle should have a unique solution")
    }

    private fun isBoardValid(board: Board): Boolean {
        val grid = board.cells.map { row -> row.map { it.value ?: 0 } }
        for (r in 0 until board.size) {
            for (c in 0 until board.size) {
                val valAt = grid[r][c]
                if (valAt == 0) return false
                if (!isValValidInGrid(grid, r, c, valAt, board.size, board.boxSize)) return false
            }
        }
        return true
    }

    private fun isValValidInGrid(grid: List<List<Int>>, row: Int, col: Int, num: Int, size: Int, boxSize: Int): Boolean {
        for (i in 0 until size) {
            if (i != col && grid[row][i] == num) return false
            if (i != row && grid[i][col] == num) return false
        }
        val startRow = (row / boxSize) * boxSize
        val startCol = (col / boxSize) * boxSize
        for (i in 0 until boxSize) {
            for (j in 0 until boxSize) {
                val r = startRow + i
                val c = startCol + j
                if ((r != row || c != col) && grid[r][c] == num) return false
            }
        }
        return true
    }

    private fun parseBoard(clues: String): Board {
        val size = 9
        val boxSize = 3
        val cells = List(size) { r ->
            List(size) { c ->
                val char = clues[r * size + c]
                val value = if (char == '0') null else char.toString().toInt()
                Cell(r, c, value, isGiven = value != null)
            }
        }
        return Board(size, boxSize, cells)
    }
}
