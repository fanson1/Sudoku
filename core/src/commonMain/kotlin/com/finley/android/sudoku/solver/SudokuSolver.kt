package com.finley.android.sudoku.solver

import com.finley.android.sudoku.model.Board
import com.finley.android.sudoku.model.Cell

class SudokuSolver {
    fun hasUniqueSolution(board: Board): Boolean {
        return countSolutions(board, limit = 2) == 1
    }

    fun countSolutions(board: Board, limit: Int = 2): Int {
        val grid = Array(board.size) { r -> IntArray(board.size) { c -> board.cells[r][c].value ?: 0 } }
        var count = 0

        fun isValid(row: Int, col: Int, num: Int): Boolean {
            for (i in 0 until board.size) {
                if (grid[row][i] == num || grid[i][col] == num) return false
            }
            val startRow = (row / board.boxSize) * board.boxSize
            val startCol = (col / board.boxSize) * board.boxSize
            for (i in 0 until board.boxSize) {
                for (j in 0 until board.boxSize) {
                    if (grid[startRow + i][startCol + j] == num) return false
                }
            }
            return true
        }

        fun solveRecursive(row: Int, col: Int): Boolean {
            if (count >= limit) return true
            
            var nextRow = row
            var nextCol = col + 1
            if (nextCol == board.size) {
                nextRow++
                nextCol = 0
            }

            if (row == board.size) {
                count++
                return count >= limit
            }

            if (grid[row][col] != 0) {
                return solveRecursive(nextRow, nextCol)
            }

            for (num in 1..board.size) {
                if (isValid(row, col, num)) {
                    grid[row][col] = num
                    if (solveRecursive(nextRow, nextCol)) return true
                    grid[row][col] = 0
                }
            }
            return false
        }

        solveRecursive(0, 0)
        return count
    }

    fun solve(board: Board): Board? {
        val grid = Array(board.size) { r -> IntArray(board.size) { c -> board.cells[r][c].value ?: 0 } }

        fun isValid(row: Int, col: Int, num: Int): Boolean {
            for (i in 0 until board.size) {
                if (grid[row][i] == num || grid[i][col] == num) return false
            }
            val startRow = (row / board.boxSize) * board.boxSize
            val startCol = (col / board.boxSize) * board.boxSize
            for (i in 0 until board.boxSize) {
                for (j in 0 until board.boxSize) {
                    if (grid[startRow + i][startCol + j] == num) return false
                }
            }
            return true
        }

        fun solveRecursive(row: Int, col: Int): Boolean {
            var nextRow = row
            var nextCol = col + 1
            if (nextCol == board.size) {
                nextRow++
                nextCol = 0
            }

            if (row == board.size) return true

            if (grid[row][col] != 0) {
                return solveRecursive(nextRow, nextCol)
            }

            for (num in 1..board.size) {
                if (isValid(row, col, num)) {
                    grid[row][col] = num
                    if (solveRecursive(nextRow, nextCol)) return true
                    grid[row][col] = 0
                }
            }
            return false
        }

        if (solveRecursive(0, 0)) {
            val solvedCells = List(board.size) { r ->
                List(board.size) { c ->
                    Cell(r, c, grid[r][c], isGiven = board.cells[r][c].isGiven)
                }
            }
            return Board(board.size, board.boxSize, solvedCells)
        }
        return null
    }

    fun getConflictingCells(board: Board, row: Int, col: Int, num: Int): Set<Pair<Int, Int>> {
        val conflicts = mutableSetOf<Pair<Int, Int>>()
        for (c in 0 until board.size) {
            if (c != col && board.cells[row][c].value == num) conflicts.add(row to c)
        }
        for (r in 0 until board.size) {
            if (r != row && board.cells[r][col].value == num) conflicts.add(r to col)
        }
        val startRow = (row / board.boxSize) * board.boxSize
        val startCol = (col / board.boxSize) * board.boxSize
        for (r in startRow until startRow + board.boxSize) {
            for (c in startCol until startCol + board.boxSize) {
                if ((r != row || c != col) && board.cells[r][c].value == num) conflicts.add(r to c)
            }
        }
        return conflicts
    }

    /** Returns the valid candidates (1..9) that can legally go into cell (row, col). */
    fun getCandidates(board: Board, row: Int, col: Int): Set<Int> {
        if (board.cells[row][col].value != null) return emptySet()
        val used = mutableSetOf<Int>()
        for (c in 0 until board.size) {
            board.cells[row][c].value?.let { used.add(it) }
        }
        for (r in 0 until board.size) {
            board.cells[r][col].value?.let { used.add(it) }
        }
        val startRow = (row / board.boxSize) * board.boxSize
        val startCol = (col / board.boxSize) * board.boxSize
        for (r in startRow until startRow + board.boxSize) {
            for (c in startCol until startCol + board.boxSize) {
                board.cells[r][c].value?.let { used.add(it) }
            }
        }
        return (1..board.size).filter { it !in used }.toSet()
    }
}
