package com.finley.android.sudoku.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finley.android.sudoku.model.Board
import com.finley.android.sudoku.model.Cell
import com.finley.android.sudoku.model.Difficulty

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.foundation.focusable

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material.icons.automirrored.filled.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun GameScreen(
    level: Int = 1,
    onBack: () -> Unit = {},
    onLevelCompleted: (Int, Int) -> Unit = { _, _ -> },
    onNextLevel: () -> Unit = {},
    viewModel: GameViewModel = viewModel { GameViewModel() }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(level) {
        viewModel.dispatch(GameIntent.StartLevel(level))
        focusRequester.requestFocus()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Level $level", style = MaterialTheme.typography.titleLarge)
                        Text(state.difficulty.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.dispatch(GameIntent.StartNewGame) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .focusRequester(focusRequester)
                    .focusable()
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.NumPad1, Key.One -> { viewModel.dispatch(GameIntent.InputNumber(1)); true }
                                Key.NumPad2, Key.Two -> { viewModel.dispatch(GameIntent.InputNumber(2)); true }
                                Key.NumPad3, Key.Three -> { viewModel.dispatch(GameIntent.InputNumber(3)); true }
                                Key.NumPad4, Key.Four -> { viewModel.dispatch(GameIntent.InputNumber(4)); true }
                                Key.NumPad5, Key.Five -> { viewModel.dispatch(GameIntent.InputNumber(5)); true }
                                Key.NumPad6, Key.Six -> { viewModel.dispatch(GameIntent.InputNumber(6)); true }
                                Key.NumPad7, Key.Seven -> { viewModel.dispatch(GameIntent.InputNumber(7)); true }
                                Key.NumPad8, Key.Eight -> { viewModel.dispatch(GameIntent.InputNumber(8)); true }
                                Key.NumPad9, Key.Nine -> { viewModel.dispatch(GameIntent.InputNumber(9)); true }
                                Key.Backspace, Key.Delete -> { viewModel.dispatch(GameIntent.Erase); true }
                                Key.Z -> {
                                    if (event.isCtrlPressed || event.isMetaPressed) {
                                        if (event.isShiftPressed) viewModel.dispatch(GameIntent.Redo)
                                        else viewModel.dispatch(GameIntent.Undo)
                                        true
                                    } else false
                                }
                                Key.Y -> {
                                    if (event.isCtrlPressed || event.isMetaPressed) {
                                        viewModel.dispatch(GameIntent.Redo)
                                        true
                                    } else false
                                }
                                Key.N -> { viewModel.dispatch(GameIntent.ToggleNotesMode); true }
                                Key.H -> { viewModel.dispatch(GameIntent.RequestHint); true }
                                else -> false
                            }
                        } else false
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Stats Row (Time and Mistakes)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatTime(state.elapsedSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Surface(
                        color = if (state.mistakeCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Mistakes: ${state.mistakeCount}/${state.maxMistakes}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.mistakeCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    SudokuBoard(
                        board = state.board,
                        selectedCell = state.selectedCell,
                        conflictingCells = state.conflictingCells,
                        onCellClick = { r, c -> viewModel.dispatch(GameIntent.SelectCell(r, c)) }
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f)) // Push keyboard down

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    NumberPad(
                        onNumberClick = { n -> viewModel.dispatch(GameIntent.InputNumber(n)) },
                        onEraseClick = { viewModel.dispatch(GameIntent.Erase) },
                        notesMode = state.notesMode,
                        onNotesToggle = { viewModel.dispatch(GameIntent.ToggleNotesMode) },
                        completedNumbers = state.completedNumbers
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.dispatch(GameIntent.Undo) },
                            enabled = state.canUndo && !state.isCompleted,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Undo", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = { viewModel.dispatch(GameIntent.Redo) },
                            enabled = state.canRedo && !state.isCompleted,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Redo, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Redo", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.dispatch(GameIntent.RequestHint) },
                            enabled = state.hintsRemaining > 0 && !state.isCompleted,
                            modifier = Modifier.weight(1.5f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Lightbulb, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hint (${state.hintsRemaining})", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (state.isCompleted) {
        val score = calculateScore(state)
        LaunchedEffect(Unit) {
            onLevelCompleted(state.level, score)
        }
        AlertDialog(
            onDismissRequest = { },
            title = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("🎉", fontSize = 48.sp)
                    Text("Victory!", style = MaterialTheme.typography.headlineMedium)
                }
            },
            text = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Level ${state.level} Completed", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Time: ${formatTime(state.elapsedSeconds)}")
                    Text("Mistakes: ${state.mistakeCount}")
                    Text("Score: $score", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 20.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { onNextLevel() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Next Level")
                }
            }
        )
    }

    if (state.mistakeCount >= state.maxMistakes) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Game Over") },
            text = { Text("You've made too many mistakes.") },
            confirmButton = {
                Button(onClick = { viewModel.dispatch(GameIntent.StartNewGame) }) {
                    Text("Try Again")
                }
            }
        )
    }
}

fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}

fun calculateScore(state: GameState): Int {
    val baseScore = when (state.difficulty) {
        Difficulty.EASY -> 1000
        Difficulty.MEDIUM -> 2000
        Difficulty.HARD -> 3000
        Difficulty.EXPERT -> 5000
        Difficulty.MASTER -> 8000
    }
    
    val timePenalty = state.elapsedSeconds * 2
    val mistakePenalty = state.mistakeCount * 100
    
    return (baseScore - timePenalty - mistakePenalty).coerceAtLeast(100)
}

@Composable
fun SudokuBoard(
    board: Board,
    selectedCell: Pair<Int, Int>?,
    conflictingCells: Set<Pair<Int, Int>> = emptySet(),
    onCellClick: (Int, Int) -> Unit
) {
    if (board.cells.isEmpty()) {
        Box(modifier = Modifier.aspectRatio(1f))
        return
    }

    val selectedValue = selectedCell?.let { (r, c) -> 
        board.cells.getOrNull(r)?.getOrNull(c)?.value 
    }
    val selectedRow = selectedCell?.first
    val selectedCol = selectedCell?.second

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(2.dp, Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (r in 0 until board.size) {
                Row(modifier = Modifier.weight(1f)) {
                    for (c in 0 until board.size) {
                        val cell = board.cells[r][c]
                        val isSelected = selectedRow == r && selectedCol == c
                        val isSameValue = selectedValue != null && cell.value == selectedValue
                        val isConflict = conflictingCells.contains(r to c)
                        
                        val isInSameRowOrCol = selectedRow == r || selectedCol == c
                        val isInSameBox = if (selectedRow != null && selectedCol != null) {
                            (r / board.boxSize == selectedRow / board.boxSize) &&
                            (c / board.boxSize == selectedCol / board.boxSize)
                        } else false

                        SudokuCell(
                            cell = cell,
                            isSelected = isSelected,
                            isSameValue = isSameValue,
                            isHighlighted = isInSameRowOrCol || isInSameBox,
                            isConflict = isConflict,
                            onClick = { onCellClick(r, c) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Draw thick borders for 3x3 boxes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val blockSize = size.width / 3
            for (i in 1..2) {
                drawLine(
                    color = Color.Black,
                    start = Offset(blockSize * i, 0f),
                    end = Offset(blockSize * i, size.height),
                    strokeWidth = 4f
                )
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, blockSize * i),
                    end = Offset(size.width, blockSize * i),
                    strokeWidth = 4f
                )
            }
        }
    }
}

@Composable
fun SudokuCell(
    cell: Cell,
    isSelected: Boolean,
    isSameValue: Boolean,
    isHighlighted: Boolean,
    isConflict: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetBackgroundColor = when {
        isSelected -> Color(0xFFBBDEFB)
        cell.isError -> Color(0xFFFFCDD2)
        isConflict -> Color(0xFFFFEBEE)
        isSameValue && cell.value != null -> Color(0xFFE3F2FD)
        isHighlighted -> Color(0xFFF5F5F5)
        else -> Color.White
    }
    
    val backgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 200)
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .border(0.2.dp, Color.LightGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (cell.value != null) {
            val textColor = when {
                cell.isGiven -> Color.Black
                cell.isError -> Color(0xFFD32F2F)
                else -> Color(0xFF1976D2)
            }
            
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(initialScale = 0.5f) + fadeIn(),
            ) {
                Text(
                    text = cell.value.toString(),
                    fontSize = 26.sp,
                    fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.Normal,
                    color = textColor
                )
            }
        } else if (cell.candidates.isNotEmpty()) {
            CellNotes(cell.candidates)
        }
    }
}

@Composable
fun CellNotes(candidates: Set<Int>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(2.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (row in 0 until 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until 3) {
                    val num = row * 3 + col + 1
                    if (num in candidates) {
                        Text(
                            text = num.toString(),
                            fontSize = 9.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Light
                        )
                    } else {
                        Spacer(modifier = Modifier.size(9.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onEraseClick: () -> Unit,
    notesMode: Boolean,
    onNotesToggle: () -> Unit,
    completedNumbers: Set<Int> = emptySet()
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Function buttons at the top
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InputButton(
                onClick = onNotesToggle,
                modifier = Modifier.weight(1f),
                containerColor = if (notesMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(if (notesMode) Icons.Default.Edit else Icons.Default.EditNote, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (notesMode) "Notes ON" else "Notes OFF", fontSize = 12.sp)
            }
            
            InputButton(
                onClick = onEraseClick,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Erase", fontSize = 12.sp)
            }
        }

        // 1-9 Number Grid (3x3)
        Column(modifier = Modifier.widthIn(max = 240.dp)) {
            for (row in 0 until 3) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 3) {
                        val n = row * 3 + col + 1
                        val isCompleted = completedNumbers.contains(n)
                        NumberButton(n, onNumberClick, Modifier.weight(1f), isCompleted = isCompleted)
                    }
                }
            }
        }
    }
}

@Composable
fun NumberButton(
    number: Int, 
    onClick: (Int) -> Unit, 
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false
) {
    InputButton(
        onClick = { if (!isCompleted) onClick(number) }, 
        modifier = modifier.graphicsLayer { alpha = if (isCompleted) 0.3f else 1.0f },
        containerColor = if (isCompleted) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant,
        elevation = if (isCompleted) 0.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Text(
                number.toString(), 
                fontSize = 20.sp,
                color = if (isCompleted) Color.Gray else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            if (isCompleted) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(10.dp), tint = Color.Green)
            }
        }
    }
}

@Composable
fun InputButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    elevation: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.padding(2.dp).height(44.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = containerColor, 
            contentColor = contentColorFor(containerColor)
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = elevation),
        contentPadding = PaddingValues(0.dp)
    ) {
        content()
    }
}
