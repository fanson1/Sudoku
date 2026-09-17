package com.finley.android.sudoku.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finley.android.sudoku.generator.SudokuGenerator
import com.finley.android.sudoku.model.*
import com.finley.android.sudoku.solver.SudokuSolver
import com.finley.android.sudoku.util.DailyUtil
import com.finley.android.sudoku.util.Persistence
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.random.Random
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
                is GameIntent.StartDailyChallenge -> startDailyChallenge()
                is GameIntent.StartTimedChallenge -> startTimedChallenge(intent.timeLimitSeconds)
                is GameIntent.SetAutoEraseNotes -> {
                    val cur = _state.value
                    _state.value = cur.copy(autoEraseNotes = intent.enabled)
                }
                is GameIntent.SetShowConflicts -> {
                    val cur = _state.value
                    _state.value = cur.copy(showConflicts = intent.enabled)
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_state.value.isPaused && !_state.value.isCompleted && !_state.value.isLoading) {
                    val elapsed = _state.value.elapsedSeconds + 1
                    val timeLimit = _state.value.timeLimitSeconds
                    if (timeLimit != null && elapsed >= timeLimit) {
                        // Time's up: mark as completed-with-game-over.
                        recordGameLoss()
                        _state.value = _state.value.copy(
                            elapsedSeconds = timeLimit,
                            isCompleted = false
                        )
                        _effects.send(GameEffect.ShowGameOverDialog)
                        break
                    }
                    _state.value = _state.value.copy(elapsedSeconds = elapsed)
                }
            }
        }
    }

    private fun recordGameLoss() {
        val difficulty = _state.value.difficulty
        Persistence.recordGame(difficulty, won = false, score = 0)
    }

    private fun startNewGame() {
        when (_state.value.gameMode) {
            GameMode.DAILY -> startDailyChallenge()
            GameMode.TIMED -> startTimedChallenge(_state.value.timeLimitSeconds ?: DEFAULT_TIME_LIMIT)
            GameMode.NORMAL -> startLevel(_state.value.level)
        }
    }

    private fun startLevel(level: Int) {
        _state.value = _state.value.copy(isLoading = true, level = level, gameMode = GameMode.NORMAL)
        viewModelScope.launch {
            val difficulty = GameRules.getDifficultyForLevel(level)
            // Level-based puzzles are deterministic per level on purpose: the
            // same level always shows the same board, which keeps per-level
            // leaderboard scores comparable. "Restart" therefore replays the
            // exact same puzzle.
            val puzzle = generator.generatePuzzle(difficulty, seed = level.toLong())
            loadPuzzle(puzzle, level = level)
        }
    }

    private fun startDailyChallenge() {
        val dateKey = DailyUtil.todayDateKey()
        _state.value = _state.value.copy(
            isLoading = true,
            gameMode = GameMode.DAILY,
            dailyDate = dateKey
        )
        viewModelScope.launch {
            val difficulty = GameRules.getDifficultyForLevel(DAILY_LEVEL)
            val puzzle = generator.generatePuzzle(difficulty, seed = DailyUtil.dailySeed(dateKey))
            loadPuzzle(puzzle, level = DAILY_LEVEL)
        }
    }

    private fun startTimedChallenge(timeLimitSeconds: Int) {
        _state.value = _state.value.copy(
            isLoading = true,
            gameMode = GameMode.TIMED,
            timeLimitSeconds = timeLimitSeconds
        )
        viewModelScope.launch {
            val difficulty = GameRules.getDifficultyForLevel(TIMED_BASE_LEVEL)
            // Timed runs are randomized (not seeded by level) so every attempt
            // presents a fresh puzzle.
            val puzzle = generator.generatePuzzle(difficulty, seed = Random.nextLong())
            loadPuzzle(puzzle, level = TIMED_BASE_LEVEL)
        }
    }

    private fun loadPuzzle(puzzle: Puzzle, level: Int) {
        val board = parsePuzzleToBoard(puzzle)
        val dailyDate = _state.value.dailyDate
        _state.value = _state.value.copy(
            board = board,
            solution = puzzle.solution,
            level = level,
            dailyDate = dailyDate,
            difficulty = puzzle.difficulty,
            isLoading = false,
            isCompleted = false,
            mistakeCount = 0,
            maxMistakes = 5,
            comboCount = 0,
            hintsRemaining = GameRules.getInitialHintsForLevel(level),
            selectedCell = null,
            elapsedSeconds = 0,
            history = emptyList(),
            historyCursor = -1,
            canUndo = false,
            canRedo = false,
            completedNumbers = calculateCompletedNumbers(board),
            conflictingCells = emptySet(),
            hintCell = null,
            activeNumber = null,
            nakedSingleCell = null,
            nakedSingleValue = null,
            autoEraseNotes = Persistence.getAutoEraseNotes(),
            showConflicts = Persistence.getShowConflicts()
        )
        startTimer()
    }

    private companion object {
        const val DEFAULT_TIME_LIMIT = 600
        const val DAILY_LEVEL = 12 // MEDIUM difficulty
        const val TIMED_BASE_LEVEL = 8 // EASY difficulty
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
        val cell = _state.value.board.cells.getOrNull(row)?.getOrNull(col)
        _state.value = _state.value.copy(
            selectedCell = row to col,
            activeNumber = cell?.value,
            nakedSingleCell = if (cell?.value == null) recomputeNakedSingleCell(_state.value.board, row to col) else null,
            nakedSingleValue = if (cell?.value == null) recomputeNakedSingleValue(_state.value.board, row to col) else null
        )
    }

    private fun recomputeNakedSingleCell(board: Board, selectedCell: Pair<Int, Int>?): Pair<Int, Int>? {
        val (r, c) = selectedCell ?: return null
        val cell = board.cells.getOrNull(r)?.getOrNull(c) ?: return null
        if (cell.value != null) return null
        return if (solver.getCandidates(board, r, c).size == 1) r to c else null
    }

    private fun recomputeNakedSingleValue(board: Board, selectedCell: Pair<Int, Int>?): Int? {
        val (r, c) = selectedCell ?: return null
        val cell = board.cells.getOrNull(r)?.getOrNull(c) ?: return null
        if (cell.value != null) return null
        return solver.getCandidates(board, r, c).singleOrNull()
    }

    private fun inputNumber(value: Int) {
        _state.value = _state.value.copy(activeNumber = value)
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
        var newComboCount = _state.value.comboCount
        if (!isValid) {
            newMistakeCount++
            newComboCount = 0
            viewModelScope.launch {
                _effects.send(GameEffect.PlaySoundError)
                if (newMistakeCount >= _state.value.maxMistakes) {
                    recordGameLoss()
                    _effects.send(GameEffect.ShowGameOverDialog)
                }
            }
        } else {
            newComboCount++
        }

        val newBoard = _state.value.board.copy(cells = newCells)
        val previouslyCompleted = _state.value.completedNumbers
        updateStateWithMove(move, newBoard, newMistakeCount, conflicts, newComboCount)
        
        val nowCompleted = _state.value.completedNumbers
        val justCompleted = nowCompleted - previouslyCompleted
        if (justCompleted.isNotEmpty()) {
            viewModelScope.launch {
                justCompleted.forEach { _effects.send(GameEffect.NumberCompleted(it)) }
                _effects.send(GameEffect.PlaySoundSuccess)
            }
        }

        if (newMistakeCount < _state.value.maxMistakes) {
            handleBoardCompletion(newCells)
        }
    }

    private fun updateStateWithMove(
        move: Move, 
        newBoard: Board, 
        mistakeCount: Int? = null,
        conflicts: Set<Pair<Int, Int>> = emptySet(),
        comboCount: Int? = null
    ) {
        val currentState = _state.value
        val newHistory = currentState.history.take(currentState.historyCursor + 1) + move
        val newCursor = newHistory.lastIndex
        
        _state.value = currentState.copy(
            board = newBoard,
            mistakeCount = mistakeCount ?: currentState.mistakeCount,
            comboCount = comboCount ?: currentState.comboCount,
            history = newHistory,
            historyCursor = newCursor,
            canUndo = true,
            canRedo = false,
            completedNumbers = calculateCompletedNumbers(newBoard),
            conflictingCells = conflicts,
            nakedSingleCell = recomputeNakedSingleCell(newBoard, currentState.selectedCell),
            nakedSingleValue = recomputeNakedSingleValue(newBoard, currentState.selectedCell)
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

        // Standard Sudoku apps don't "refund" mistakes on undo; the isError
        // flags of the affected cells are recomputed by applyMove instead.

        _state.value = currentState.copy(
            board = newBoard,
            historyCursor = newCursor,
            canUndo = newCursor >= 0,
            canRedo = true,
            selectedCell = move.row to move.col,
            conflictingCells = emptySet(),
            completedNumbers = calculateCompletedNumbers(newBoard),
            nakedSingleCell = recomputeNakedSingleCell(newBoard, move.row to move.col),
            nakedSingleValue = recomputeNakedSingleValue(newBoard, move.row to move.col)
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
            selectedCell = move.row to move.col,
            conflictingCells = emptySet(),
            completedNumbers = calculateCompletedNumbers(newBoard),
            nakedSingleCell = recomputeNakedSingleCell(newBoard, move.row to move.col),
            nakedSingleValue = recomputeNakedSingleValue(newBoard, move.row to move.col)
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

        val hintCell = findHintCell(currentState.board, currentState.solution) ?: return
        val (r, c) = hintCell
        val cell = currentState.board.cells[r][c]
        val correctValue = currentState.solution[r * currentState.board.size + c].digitToInt()

        val move = Move.Place(r, c, correctValue, cell.value)

        val newCells = currentState.board.cells.mapIndexed { ri, row ->
            row.mapIndexed { ci, cell ->
                if (ri == r && ci == c) {
                    // A hint fills the value but the cell stays fully editable
                    // (not treated as a given clue) so it can still be undone/erased.
                    cell.copy(value = correctValue, isError = false, isGiven = cell.isGiven)
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
            canRedo = false,
            hintCell = r to c,
            conflictingCells = emptySet(),
            completedNumbers = calculateCompletedNumbers(newBoard),
            nakedSingleCell = recomputeNakedSingleCell(newBoard, currentState.selectedCell),
            nakedSingleValue = recomputeNakedSingleValue(newBoard, currentState.selectedCell)
        )

        viewModelScope.launch {
            delay(1800)
            _state.value = _state.value.copy(hintCell = null)
        }

        if (isBoardFullAndValid(newCells)) {
            handleBoardCompletion(newCells)
        }
    }

    /**
     * Picks the most instructive empty cell for a hint:
     * 1. a naked single (exactly one legal candidate),
     * 2. a hidden single (a digit with only one home in its row/col/box),
     * 3. any empty cell whose solution value does not conflict with the board.
     */
    private fun findHintCell(board: Board, solution: String): Pair<Int, Int>? {
        val empties = (0 until board.size).flatMap { r ->
            (0 until board.size).mapNotNull { c ->
                if (board.cells[r][c].value == null) r to c else null
            }
        }
        if (empties.isEmpty()) return null

        empties.firstOrNull { (r, c) -> solver.getCandidates(board, r, c).size == 1 }?.let { return it }
        empties.firstOrNull { (r, c) -> findHiddenSingleValue(board, r, c) != null }?.let { return it }
        return empties.firstOrNull { (r, c) ->
            val v = solution[r * board.size + c].digitToInt()
            solver.getConflictingCells(board, r, c, v).isEmpty()
        } ?: empties.first()
    }

    /** Returns the digit that only fits in this cell within its row/col/box, or null. */
    private fun findHiddenSingleValue(board: Board, row: Int, col: Int): Int? {
        val candidates = solver.getCandidates(board, row, col)
        if (candidates.isEmpty()) return null

        val rowBlocked: (Int) -> Boolean = { d ->
            (0 until board.size).any { cc ->
                cc != col && board.cells[row][cc].value == null && d in solver.getCandidates(board, row, cc)
            }
        }
        val colBlocked: (Int) -> Boolean = { d ->
            (0 until board.size).any { rr ->
                rr != row && board.cells[rr][col].value == null && d in solver.getCandidates(board, rr, col)
            }
        }
        val boxBlocked: (Int) -> Boolean = { d ->
            val startRow = (row / board.boxSize) * board.boxSize
            val startCol = (col / board.boxSize) * board.boxSize
            (startRow until startRow + board.boxSize).any { rr ->
                (startCol until startCol + board.boxSize).any { cc ->
                    (rr != row || cc != col) &&
                        board.cells[rr][cc].value == null &&
                        d in solver.getCandidates(board, rr, cc)
                }
            }
        }

        return candidates.firstOrNull { d -> !rowBlocked(d) && !colBlocked(d) && !boxBlocked(d) }
    }

    /** Shared win-path so both manual input and hints settle a completed board identically. */
    private fun handleBoardCompletion(newCells: List<List<Cell>>) {
        val current = _state.value
        if (current.isCompleted || current.mistakeCount >= current.maxMistakes) return
        if (!isBoardFullAndValid(newCells)) return

        if (current.gameMode == GameMode.DAILY) {
            Persistence.saveDailyCompleted(DailyUtil.todayDateKey())
        }
        val score = calculateScore(current.copy(isCompleted = true))
        Persistence.recordGame(current.difficulty, won = true, score = score)
        _state.value = current.copy(isCompleted = true)
        viewModelScope.launch { _effects.send(GameEffect.ShowVictoryDialog) }
    }

    private fun pauseGame() { _state.value = _state.value.copy(isPaused = true) }
    private fun resumeGame() { _state.value = _state.value.copy(isPaused = false) }
}
