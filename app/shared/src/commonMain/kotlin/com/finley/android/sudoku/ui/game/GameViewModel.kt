package com.finley.android.sudoku.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finley.android.sudoku.generator.SudokuGenerator
import com.finley.android.sudoku.model.*
import com.finley.android.sudoku.solver.SudokuSolver
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel(
    private val generator: SudokuGenerator = SudokuGenerator(),
    private val solver: SudokuSolver = SudokuSolver()
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _effects = Channel<GameEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var timerJob: Job? = null

    fun dispatch(intent: GameIntent) {
        viewModelScope.launch {
            when (intent) {
                is GameIntent.StartNewGame -> startNewGame()
                is GameIntent.SelectCell -> selectCell(intent.row, intent.col)
                is GameIntent.InputNumber -> inputNumber(intent.value)
                is GameIntent.Erase -> erase()
                is GameIntent.ToggleNotesMode -> toggleNotesMode()
                is GameIntent.Undo -> undo()
                is GameIntent.Redo -> redo()
                is GameIntent.RequestHint -> requestHint()
                is GameIntent.PauseGame -> pauseGame()
                is GameIntent.ResumeGame -> resumeGame()
                is GameIntent.StartLevel -> startLevel(intent.level)
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_state.value.isPaused && !_state.value.isCompleted && !_state.value.isLoading) {
                    _state.value = _state.value.copy(elapsedSeconds = _state.value.elapsedSeconds + 1)
                }
            }
        }
    }

    private fun startNewGame() {
        startLevel(_state.value.level)
    }

    private fun startLevel(level: Int) {
        _state.value = _state.value.copy(isLoading = true, level = level)
        viewModelScope.launch {
            // Map level to difficulty: 1-10 Easy, 11-20 Medium, etc.
            val difficulty = GameRules.getDifficultyForLevel(level)
            // Use level as seed for deterministic level generation
            val puzzle = generator.generatePuzzle(difficulty, seed = level.toLong())
            val board = parsePuzzleToBoard(puzzle)
            
            _state.value = _state.value.copy(
                board = board,
                solution = puzzle.solution,
                difficulty = puzzle.difficulty,
                isLoading = false,
                isCompleted = false,
                mistakeCount = 0,
                maxMistakes = 5,
                hintsRemaining = GameRules.getInitialHintsForLevel(level),
                selectedCell = null,
                elapsedSeconds = 0,
                history = emptyList(),
                historyCursor = -1,
                canUndo = false,
                canRedo = false,
                completedNumbers = calculateCompletedNumbers(board),
                conflictingCells = emptySet()
            )
            startTimer()
        }
    }

    private fun parsePuzzleToBoard(puzzle: Puzzle): Board {
        val size = puzzle.size
        val boxSize = 3
        val clues = puzzle.initialClues
        val cells = List(size) { r ->
            List(size) { c ->
                val char = clues[r * size + c]
                val value = if (char == '0') null else char.digitToInt()
                Cell(r, c, value, isGiven = value != null)
            }
        }
        return Board(size, boxSize, cells)
    }

    private fun selectCell(row: Int, col: Int) {
        _state.value = _state.value.copy(selectedCell = row to col)
    }

    private fun inputNumber(value: Int) {
        val selected = _state.value.selectedCell ?: return
        val (r, c) = selected
        val cell = _state.value.board.cells[r][c]
        
        if (cell.isGiven) return

        if (_state.value.notesMode) {
            val currentNotes = cell.candidates
            val newNotes = if (value in currentNotes) currentNotes - value else currentNotes + value
            
            val move = Move.ToggleCandidate(r, c, value)
            
            val newCells = _state.value.board.cells.mapIndexed { ri, row ->
                row.mapIndexed { ci, cCell ->
                    if (ri == r && ci == c) cCell.copy(candidates = newNotes, value = null)
                    else cCell
                }
            }
            updateStateWithMove(move, _state.value.board.copy(cells = newCells))
            return
        }

        if (cell.value == value) return

        val conflicts = solver.getConflictingCells(_state.value.board, r, c, value)
        val isValid = conflicts.isEmpty()
        val move = Move.Place(r, c, value, cell.value)
        
        var newCells = _state.value.board.cells.mapIndexed { ri, row ->
            row.mapIndexed { ci, cell ->
                if (ri == r && ci == c) {
                    cell.copy(value = value, isError = !isValid)
                } else cell
            }
        }

        if (isValid && _state.value.autoEraseNotes) {
            newCells = autoEraseNotes(newCells, r, c, value)
        }
        
        var newMistakeCount = _state.value.mistakeCount
        if (!isValid) {
            newMistakeCount++
            viewModelScope.launch {
                _effects.send(GameEffect.PlaySoundError)
                if (newMistakeCount >= _state.value.maxMistakes) {
                    _effects.send(GameEffect.ShowGameOverDialog)
                }
            }
        }

        val newBoard = _state.value.board.copy(cells = newCells)
        val previouslyCompleted = _state.value.completedNumbers
        updateStateWithMove(move, newBoard, newMistakeCount, conflicts)
        
        val nowCompleted = _state.value.completedNumbers
        val justCompleted = nowCompleted - previouslyCompleted
        if (justCompleted.isNotEmpty()) {
            viewModelScope.launch {
                justCompleted.forEach { _effects.send(GameEffect.NumberCompleted(it)) }
                _effects.send(GameEffect.PlaySoundSuccess)
            }
        }

        if (newMistakeCount < _state.value.maxMistakes && isBoardFullAndValid(newCells)) {
            _state.value = _state.value.copy(isCompleted = true)
            viewModelScope.launch {
                _effects.send(GameEffect.ShowVictoryDialog)
            }
        }
    }

    private fun updateStateWithMove(
        move: Move, 
        newBoard: Board, 
        mistakeCount: Int? = null,
        conflicts: Set<Pair<Int, Int>> = emptySet()
    ) {
        val currentState = _state.value
        val newHistory = currentState.history.take(currentState.historyCursor + 1) + move
        val newCursor = newHistory.lastIndex
        
        _state.value = currentState.copy(
            board = newBoard,
            mistakeCount = mistakeCount ?: currentState.mistakeCount,
            history = newHistory,
            historyCursor = newCursor,
            canUndo = true,
            canRedo = false,
            completedNumbers = calculateCompletedNumbers(newBoard),
            conflictingCells = conflicts
        )
    }

    private fun calculateCompletedNumbers(board: Board): Set<Int> {
        val counts = IntArray(10)
        board.cells.flatten().forEach { cell ->
            cell.value?.let { value ->
                if (!cell.isError) {
                    counts[value]++
                }
            }
        }
        return (1..9).filter { counts[it] == 9 }.toSet()
    }

    private fun autoEraseNotes(cells: List<List<Cell>>, row: Int, col: Int, value: Int): List<List<Cell>> {
        val boxSize = 3
        val startRow = (row / boxSize) * boxSize
        val startCol = (col / boxSize) * boxSize
        
        return cells.mapIndexed { ri, rowList ->
            rowList.mapIndexed { ci, cell ->
                val isInRow = ri == row
                val isInCol = ci == col
                val isInBox = (ri in startRow until startRow + boxSize) && (ci in startCol until startCol + boxSize)
                
                if ((isInRow || isInCol || isInBox) && cell.candidates.contains(value)) {
                    cell.copy(candidates = cell.candidates - value)
                } else cell
            }
        }
    }

    private fun isBoardFullAndValid(cells: List<List<Cell>>): Boolean {
        return cells.flatten().all { it.value != null && !it.isError }
    }

    private fun erase() {
        val selected = _state.value.selectedCell ?: return
        val (r, c) = selected
        val cell = _state.value.board.cells[r][c]
        if (cell.isGiven || cell.value == null) return

        val move = Move.Erase(r, c, cell.value)
        val newCells = _state.value.board.cells.mapIndexed { ri, row ->
            row.mapIndexed { ci, cell ->
                if (ri == r && ci == c) cell.copy(value = null, isError = false)
                else cell
            }
        }
        updateStateWithMove(move, _state.value.board.copy(cells = newCells))
    }

    private fun toggleNotesMode() {
        _state.value = _state.value.copy(notesMode = !_state.value.notesMode)
    }

    private fun undo() {
        val currentState = _state.value
        if (!currentState.canUndo) return

        val move = currentState.history[currentState.historyCursor]
        val newBoard = applyMove(currentState.board, move, undo = true)
        val newCursor = currentState.historyCursor - 1
        
        // If the move being undone was an error, we should ideally decrement mistake count
        // However, standard Sudoku apps usually don't "refund" mistakes on undo.
        // For now, let's keep it simple.

        _state.value = currentState.copy(
            board = newBoard,
            historyCursor = newCursor,
            canUndo = newCursor >= 0,
            canRedo = true,
            selectedCell = move.row to move.col
        )
    }

    private fun redo() {
        val currentState = _state.value
        if (!currentState.canRedo) return

        val newCursor = currentState.historyCursor + 1
        val move = currentState.history[newCursor]
        val newBoard = applyMove(currentState.board, move, undo = false)

        _state.value = currentState.copy(
            board = newBoard,
            historyCursor = newCursor,
            canUndo = true,
            canRedo = newCursor < currentState.history.lastIndex,
            selectedCell = move.row to move.col
        )
    }

    private fun applyMove(board: Board, move: Move, undo: Boolean): Board {
        val newCells = board.cells.mapIndexed { r, row ->
            row.mapIndexed { c, cell ->
                if (r == move.row && c == move.col) {
                    when (move) {
                        is Move.Place -> {
                            if (undo) cell.copy(value = move.previous, isError = false)
                            else {
                                val isValid = solver.getConflictingCells(board, r, c, move.value).isEmpty()
                                cell.copy(value = move.value, isError = !isValid)
                            }
                        }
                        is Move.Erase -> {
                            if (undo) {
                                val isValid = solver.getConflictingCells(board, r, c, move.previous!!).isEmpty()
                                cell.copy(value = move.previous, isError = !isValid)
                            }
                            else cell.copy(value = null, isError = false)
                        }
                        is Move.ToggleCandidate -> {
                            val currentNotes = cell.candidates
                            val newNotes = if (move.value in currentNotes) currentNotes - move.value else currentNotes + move.value
                            cell.copy(candidates = newNotes)
                        }
                    }
                } else cell
            }
        }
        return board.copy(cells = newCells)
    }

    private val Move.row: Int get() = when(this) {
        is Move.Place -> row
        is Move.Erase -> row
        is Move.ToggleCandidate -> row
    }

    private val Move.col: Int get() = when(this) {
        is Move.Place -> col
        is Move.Erase -> col
        is Move.ToggleCandidate -> col
    }
    private fun requestHint() {
        val currentState = _state.value
        if (currentState.hintsRemaining <= 0 || currentState.isCompleted) return

        val emptyCells = currentState.board.cells.flatten().filter { it.value == null }
        if (emptyCells.isEmpty()) return

        val cellToFill = emptyCells.random()
        val solution = currentState.solution
        val correctValue = solution[cellToFill.row * currentState.board.size + cellToFill.col].digitToInt()

        val move = Move.Place(cellToFill.row, cellToFill.col, correctValue, cellToFill.value)

        val newCells = currentState.board.cells.mapIndexed { ri, row ->
            row.mapIndexed { ci, cell ->
                if (ri == cellToFill.row && ci == cellToFill.col) {
                    cell.copy(value = correctValue, isGiven = true) // Treat hint as given
                } else cell
            }
        }

        val newBoard = currentState.board.copy(cells = newCells)
        val newHistory = currentState.history.take(currentState.historyCursor + 1) + move
        
        _state.value = currentState.copy(
            board = newBoard,
            hintsRemaining = currentState.hintsRemaining - 1,
            history = newHistory,
            historyCursor = newHistory.lastIndex,
            canUndo = true,
            canRedo = false
        )
        
        if (isBoardFullAndValid(newCells)) {
            _state.value = _state.value.copy(isCompleted = true)
        }
    }
    private fun pauseGame() { _state.value = _state.value.copy(isPaused = true) }
    private fun resumeGame() { _state.value = _state.value.copy(isPaused = false) }
}
