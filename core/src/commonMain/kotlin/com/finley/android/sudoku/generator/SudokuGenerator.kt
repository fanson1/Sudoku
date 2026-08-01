package com.finley.android.sudoku.generator

import com.finley.android.sudoku.grading.DifficultyGrader
import com.finley.android.sudoku.model.Board
import com.finley.android.sudoku.model.Cell
import com.finley.android.sudoku.model.Difficulty
import com.finley.android.sudoku.model.Puzzle
import com.finley.android.sudoku.solver.SudokuSolver
import kotlin.random.Random

class SudokuGenerator(
    private val solver: SudokuSolver = SudokuSolver(),
    private val grader: DifficultyGrader = DifficultyGrader()
) {
    fun generateSolvedBoard(size: Int = 9, seed: Long = Random.nextLong()): Board {
        val random = Random(seed)
        val grid = Array(size) { IntArray(size) { 0 } }
        val boxSize = 3 

        fun isValid(row: Int, col: Int, num: Int): Boolean {
            for (i in 0 until size) {
                if (grid[row][i] == num || grid[i][col] == num) return false
            }
            val startRow = (row / boxSize) * boxSize
            val startCol = (col / boxSize) * boxSize
            for (i in 0 until boxSize) {
                for (j in 0 until boxSize) {
                    if (grid[startRow + i][startCol + j] == num) return false
                }
            }
            return true
        }

        fun fillRecursive(row: Int, col: Int): Boolean {
            var nextRow = row
            var nextCol = col + 1
            if (nextCol == size) {
                nextRow++
                nextCol = 0
            }
            if (row == size) return true

            val nums = (1..size).shuffled(random)
            for (num in nums) {
                if (isValid(row, col, num)) {
                    grid[row][col] = num
                    if (fillRecursive(nextRow, nextCol)) return true
                    grid[row][col] = 0
                }
            }
            return false
        }

        fillRecursive(0, 0)
        val cells = List(size) { r ->
            List(size) { c ->
                Cell(r, c, grid[r][c], isGiven = true)
            }
        }
        return Board(size, boxSize, cells)
    }

    fun generatePuzzle(
        targetDifficulty: Difficulty,
        seed: Long = Random.nextLong(),
        size: Int = 9
    ): Puzzle {
        val solvedBoard = generateSolvedBoard(size, seed)
        val random = Random(seed)
        
        val currentCells = solvedBoard.cells.map { row -> 
            row.map { it.copy() }.toMutableList() 
        }.toMutableList()
        
        val positions = (0 until size * size).shuffled(random)
        val boxSize = 3

        val targetEmptyCells = when (targetDifficulty) {
            Difficulty.EASY -> random.nextInt(20, 30)
            Difficulty.MEDIUM -> random.nextInt(30, 40)
            Difficulty.HARD -> random.nextInt(40, 50)
            Difficulty.EXPERT -> random.nextInt(50, 60)
            Difficulty.MASTER -> random.nextInt(60, 70)
        }
        
        var currentEmptyCells = 0
        for (pos in positions) {
            if (currentEmptyCells >= targetEmptyCells) break

            val r = pos / size
            val c = pos % size
            
            val oldValue = currentCells[r][c].value ?: continue
            
            currentCells[r][c] = currentCells[r][c].copy(value = null, isGiven = false)
            
            val boardToTest = Board(size, boxSize, currentCells.map { it.toList() })
            if (solver.hasUniqueSolution(boardToTest)) {
                currentEmptyCells++
            } else {
                currentCells[r][c] = currentCells[r][c].copy(value = oldValue, isGiven = true)
            }
        }
        
        val finalBoard = Board(size, boxSize, currentCells.map { it.toList() })
        val finalGrade = grader.gradeDifficulty(finalBoard)
        
        return Puzzle(
            id = seed.toString(),
            seed = seed,
            size = size,
            difficulty = finalGrade.difficulty,
            difficultyScore = finalGrade.score,
            initialClues = serializeBoard(finalBoard),
            solution = serializeBoard(solvedBoard),
            createdAt = 0L // Placeholder
        )
    }

    private fun serializeBoard(board: Board): String {
        return board.cells.flatten().joinToString("") { it.value?.toString() ?: "0" }
    }
}
