package com.finley.android.sudoku.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.focusable
import androidx.compose.ui.input.key.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finley.android.sudoku.model.Board
import com.finley.android.sudoku.model.Cell
import com.finley.android.sudoku.model.Difficulty
import com.finley.android.sudoku.ui.components.AppScreenBackground
import com.finley.android.sudoku.ui.components.AppLogo
import com.finley.android.sudoku.ui.components.ConfettiRain
import com.finley.android.sudoku.ui.components.GhostButton
import com.finley.android.sudoku.ui.components.GradientButton
import com.finley.android.sudoku.ui.components.PillBadge
import com.finley.android.sudoku.ui.components.difficultyColor
import com.finley.android.sudoku.ui.components.difficultyLabel
import com.finley.android.sudoku.ui.components.isLightTheme
import com.finley.android.sudoku.ui.components.pressScale
import com.finley.android.sudoku.ui.theme.AppColors
import com.finley.android.sudoku.ui.theme.AppTheme

import com.finley.android.sudoku.util.DailyUtil
import com.finley.android.sudoku.util.Persistence

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    level: Int = 1,
    mode: GameMode = GameMode.NORMAL,
    timeLimitSeconds: Int? = null,
    onBack: () -> Unit = {},
    onLevelCompleted: (Int, Int) -> Unit = { _, _ -> },
    onNextLevel: () -> Unit = {},
    viewModel: GameViewModel = viewModel { GameViewModel() }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    var showTimeUpDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is GameEffect.ShowGameOverDialog -> showTimeUpDialog = true
                else -> Unit
            }
        }
    }

    LaunchedEffect(level, mode, timeLimitSeconds) {
        when (mode) {
            GameMode.DAILY -> viewModel.dispatch(GameIntent.StartDailyChallenge)
            GameMode.TIMED -> viewModel.dispatch(GameIntent.StartTimedChallenge(timeLimitSeconds ?: 600))
            GameMode.NORMAL -> viewModel.dispatch(GameIntent.StartLevel(level))
        }
        focusRequester.requestFocus()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (mode) {
                                GameMode.DAILY -> "每日挑战"
                                GameMode.TIMED -> "限时挑战"
                                GameMode.NORMAL -> "第 $level 关"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        DifficultyPill(state.difficulty)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.dispatch(GameIntent.StartNewGame) }) {
                        Icon(Icons.Default.Refresh, "Restart")
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, "Game Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        AppScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                val boardWidth = minOf(
                    maxWidth - 32.dp,
                    360.dp,
                    (maxHeight - 250.dp).coerceAtLeast(150.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 520.dp)
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
                                    Key.Spacebar -> { togglePause(viewModel); true }
                                    else -> false
                                }
                            } else false
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GameStatsRow(
                        elapsedSeconds = state.elapsedSeconds,
                        mistakeCount = state.mistakeCount,
                        maxMistakes = state.maxMistakes,
                        isPaused = state.isPaused,
                        comboCount = state.comboCount,
                        onPauseToggle = { togglePause(viewModel) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.isLoading) {
                        Box(
                            modifier = Modifier
                                .width(boardWidth)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val pulse = rememberInfiniteTransition(label = "loadingPulse")
                                val logoScale by pulse.animateFloat(
                                    initialValue = 0.92f,
                                    targetValue = 1.06f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(900),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "loadingScale"
                                )
                                val logoAlpha by pulse.animateFloat(
                                    initialValue = 0.75f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(900),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "loadingAlpha"
                                )
                                AppLogo(
                                    size = 72.dp,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = logoScale
                                        scaleY = logoScale
                                        alpha = logoAlpha
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "正在生成谜题…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        SudokuBoard(
                            board = state.board,
                            selectedCell = state.selectedCell,
                            conflictingCells = state.conflictingCells,
                            hintCell = state.hintCell,
                            activeNumber = state.activeNumber,
                            nakedSingleCell = state.nakedSingleCell,
                            onCellClick = { r, c -> viewModel.dispatch(GameIntent.SelectCell(r, c)) },
                            modifier = Modifier
                                .width(boardWidth)
                                .aspectRatio(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    BoardProgressBar(
                        board = state.board,
                        modifier = Modifier
                            .width(boardWidth)
                            .padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val digitCounts = IntArray(10)
                        state.board.cells.flatten().forEach { cell ->
                            cell.value?.let { v ->
                                if (v in 1..9 && !cell.isError) digitCounts[v]++
                            }
                        }

                        NumberPad(
                            onNumberClick = { n -> viewModel.dispatch(GameIntent.InputNumber(n)) },
                            onEraseClick = { viewModel.dispatch(GameIntent.Erase) },
                            notesMode = state.notesMode,
                            onNotesToggle = { viewModel.dispatch(GameIntent.ToggleNotesMode) },
                            completedNumbers = state.completedNumbers,
                            digitCounts = digitCounts,
                            activeNumber = state.activeNumber,
                            nakedSingleValue = state.nakedSingleValue
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ActionButton(
                                label = "撤销",
                                enabled = state.canUndo && !state.isCompleted,
                                onClick = { viewModel.dispatch(GameIntent.Undo) },
                                modifier = Modifier.weight(1f)
                            )
                            ActionButton(
                                label = "恢复",
                                enabled = state.canRedo && !state.isCompleted,
                                onClick = { viewModel.dispatch(GameIntent.Redo) },
                                modifier = Modifier.weight(1f)
                            )
                            ActionButton(
                                icon = { Icon(Icons.Default.Lightbulb, null, modifier = Modifier.size(18.dp), tint = AppColors.hintGold) },
                                label = "提示 (${state.hintsRemaining})",
                                enabled = state.hintsRemaining > 0 && !state.isCompleted,
                                onClick = { viewModel.dispatch(GameIntent.RequestHint) },
                                modifier = Modifier.weight(1.4f),
                                emphasize = true
                            )
                        }
                    }
                }

                // Pause overlay
                AnimatedVisibility(
                    visible = state.isPaused && !state.isCompleted,
                    enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.98f),
                    exit = fadeOut(tween(200))
                ) {
                    PauseOverlay(
                        onResume = { viewModel.dispatch(GameIntent.ResumeGame) }
                    )
                }
            }
        }
    }

    if (state.isCompleted) {
        val score = calculateScore(state)
        LaunchedEffect(Unit) {
            onLevelCompleted(state.level, score)
        }
        VictoryDialog(
            level = state.level,
            time = formatTime(state.elapsedSeconds),
            mistakes = state.mistakeCount,
            comboCount = state.comboCount,
            score = score,
            isDaily = mode == GameMode.DAILY,
            onNextLevel = onNextLevel,
            onBackToLevels = onBack
        )
    }

    if ((state.mistakeCount >= state.maxMistakes && !state.isCompleted) || showTimeUpDialog) {
        GameOverDialog(
            isTimeUp = showTimeUpDialog,
            onRetry = {
                showTimeUpDialog = false
                viewModel.dispatch(GameIntent.StartNewGame)
            },
            onBackToLevels = onBack
        )
    }

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        ) {
            com.finley.android.sudoku.ui.components.GameSettingsSheet(
                autoEraseNotes = state.autoEraseNotes,
                showConflicts = state.showConflicts,
                onAutoEraseNotesChange = {
                    Persistence.saveAutoEraseNotes(it)
                    viewModel.dispatch(GameIntent.SetAutoEraseNotes(it))
                },
                onShowConflictsChange = {
                    Persistence.saveShowConflicts(it)
                    viewModel.dispatch(GameIntent.SetShowConflicts(it))
                }
            )
            Spacer(modifier = Modifier.navigationBarsPadding().height(16.dp))
        }
    }
}

private fun togglePause(viewModel: GameViewModel) {
    if (viewModel.state.value.isPaused) {
        viewModel.dispatch(GameIntent.ResumeGame)
    } else {
        viewModel.dispatch(GameIntent.PauseGame)
    }
}

// ---------------------------------------------------------------------------
// Stats row
// ---------------------------------------------------------------------------
@Composable
private fun GameStatsRow(
    elapsedSeconds: Int,
    mistakeCount: Int,
    maxMistakes: Int,
    isPaused: Boolean,
    comboCount: Int,
    onPauseToggle: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val mistakesExceeded = mistakeCount > 0
    val mistakesColor = if (mistakesExceeded) colorScheme.error else colorScheme.onSurfaceVariant

    // Bump the mistake chip whenever the counter goes up.
    val mistakeBump = remember { Animatable(1f) }
    var prevMistakes by remember { mutableStateOf(mistakeCount) }
    LaunchedEffect(mistakeCount) {
        if (mistakeCount > prevMistakes) {
            mistakeBump.snapTo(1.18f)
            mistakeBump.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        prevMistakes = mistakeCount
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatChip(
            icon = Icons.Default.Timer,
            text = if (isPaused) "已暂停" else formatTime(elapsedSeconds),
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    scaleX = mistakeBump.value
                    scaleY = mistakeBump.value
                }
        ) {
        StatChip(
            icon = Icons.Default.ErrorOutline,
            text = "$mistakeCount/$maxMistakes",
            iconTint = mistakesColor,
            textColor = if (mistakesExceeded) colorScheme.error else colorScheme.onSurfaceVariant,
            containerColor = if (mistakesExceeded) colorScheme.errorContainer.copy(alpha = 0.7f) else colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        }

        ComboChip(comboCount)

        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = colorScheme.surface,
            contentColor = colorScheme.onSurface
        ) {
            IconButton(onClick = onPauseToggle, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = textColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ComboChip(comboCount: Int) {
    val colorScheme = MaterialTheme.colorScheme
    val active = comboCount >= 2
    val scale = remember { Animatable(1f) }
    var prevCombo by remember { mutableStateOf(comboCount) }
    LaunchedEffect(comboCount) {
        if (comboCount > prevCombo) {
            scale.snapTo(1.25f)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        prevCombo = comboCount
    }

    val comboColor = when {
        comboCount >= 8 -> Color(0xFFFF9800)
        comboCount >= 4 -> colorScheme.error
        else -> AppColors.hintGold
    }

    Surface(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .alpha(if (active) 1f else 0.55f),
        shape = RoundedCornerShape(12.dp),
        color = if (active) comboColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
        contentColor = if (active) comboColor else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .height(40.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = if (active) comboColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (comboCount >= 2) "x${comboCount}" else "连击",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Board
// ---------------------------------------------------------------------------
@Composable
fun SudokuBoard(
    board: Board,
    selectedCell: Pair<Int, Int>?,
    conflictingCells: Set<Pair<Int, Int>> = emptySet(),
    hintCell: Pair<Int, Int>? = null,
    activeNumber: Int? = null,
    nakedSingleCell: Pair<Int, Int>? = null,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (board.cells.isEmpty()) {
        Box(modifier = modifier)
        return
    }

    val colorScheme = MaterialTheme.colorScheme
    val light = isLightTheme()

    val selectedValue = selectedCell?.let { (r, c) ->
        board.cells.getOrNull(r)?.getOrNull(c)?.value
    }
    val selectedRow = selectedCell?.first
    val selectedCol = selectedCell?.second
    val boxSize = board.boxSize

    val boxBlockBg = if (light) {
        colorScheme.surfaceVariant.copy(alpha = 0.6f)
    } else {
        colorScheme.surfaceVariant.copy(alpha = 0.9f)
    }
    val boxShape = RoundedCornerShape(9.dp)

    Surface(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = colorScheme.primary.copy(alpha = 0.18f),
                spotColor = colorScheme.primary.copy(alpha = 0.18f)
            )
            .clip(RoundedCornerShape(20.dp)),
        color = colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            for (br in 0 until boxSize) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    for (bc in 0 until boxSize) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(boxShape)
                                .background(boxBlockBg)
                                .padding(1.5.dp)
                        ) {
                            for (r in 0 until boxSize) {
                                Row(modifier = Modifier.weight(1f)) {
                                    for (c in 0 until boxSize) {
                                        val globalR = br * boxSize + r
                                        val globalC = bc * boxSize + c
                                        val cell = board.cells[globalR][globalC]
                                        val isSelected = selectedRow == globalR && selectedCol == globalC
                                        val isSameValue = selectedValue != null && cell.value == selectedValue
                                        val isActiveMatch = activeNumber != null && cell.value == activeNumber
                                        val isConflict = conflictingCells.contains(globalR to globalC)
                                        val isHighlighted = selectedCell != null && (
                                            selectedRow == globalR || selectedCol == globalC ||
                                            (br == selectedRow!! / boxSize && bc == selectedCol!! / boxSize)
                                            )
                                        val isHinted = hintCell == globalR to globalC
                                        val isDimmed = activeNumber != null && activeNumber != selectedValue &&
                                            cell.value != null && cell.value != activeNumber &&
                                            !isConflict && !cell.isError
                                        val isNakedSingle = nakedSingleCell == globalR to globalC

                                        SudokuCell(
                                            cell = cell,
                                            isSelected = isSelected,
                                            isSameValue = isSameValue || isActiveMatch,
                                            isHighlighted = isHighlighted && !isSelected,
                                            isConflict = isConflict,
                                            isHinted = isHinted,
                                            isDimmed = isDimmed,
                                            isNakedSingle = isNakedSingle,
                                            onClick = { onCellClick(globalR, globalC) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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
    isHinted: Boolean = false,
    isDimmed: Boolean = false,
    isNakedSingle: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val targetBackground = when {
        isSelected -> colorScheme.primaryContainer
        isHinted -> colorScheme.primaryContainer.copy(alpha = 0.35f)
        isConflict || cell.isError -> colorScheme.errorContainer
        isSameValue && cell.value != null -> colorScheme.primaryContainer.copy(alpha = 0.55f)
        isHighlighted -> colorScheme.onSurface.copy(alpha = 0.055f)
        else -> Color.Transparent
    }
    val backgroundColor by animateColorAsState(
        targetValue = targetBackground,
        animationSpec = tween(durationMillis = 180),
        label = "cellBg"
    )

    // Pop-in when a value is entered (skips initial render so given clues don't pop).
    val popScale = remember { Animatable(1f) }
    var hadValue by remember { mutableStateOf(cell.value != null) }
    LaunchedEffect(cell.value) {
        val nowHasValue = cell.value != null
        if (nowHasValue && !hadValue) {
            popScale.snapTo(0.55f)
            popScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        hadValue = nowHasValue
    }

    // Shake when a wrong value is placed.
    val shakeOffset = remember { Animatable(0f) }
    var wasError by remember { mutableStateOf(cell.isError) }
    LaunchedEffect(cell.isError) {
        val becameError = cell.isError && !wasError
        wasError = cell.isError
        if (becameError) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 320
                    -6f at 40
                    6f at 100
                    -5f at 170
                    5f at 230
                    -2f at 290
                    0f at 320
                }
            )
        }
    }

    // Pulsing ring on the cell just revealed by a hint.
    val hintPulse = rememberInfiniteTransition(label = "hintPulse")
    val hintAlpha by hintPulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "hintPulseAlpha"
    )

    // Gentle pulsing highlight on a naked-single cell (only one legal value).
    val singlePulse = rememberInfiniteTransition(label = "singlePulse")
    val singleAlpha by singlePulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "singlePulseAlpha"
    )

    val cellShape = RoundedCornerShape(5.dp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(cellShape)
            .background(backgroundColor)
            .then(
                when {
                    isHinted -> Modifier.border(2.dp, AppColors.hintGold.copy(alpha = hintAlpha), cellShape)
                    isNakedSingle -> Modifier.border(
                        2.dp,
                        colorScheme.successGreen().copy(alpha = singleAlpha.coerceAtLeast(0.55f)),
                        cellShape
                    )
                    isSelected -> Modifier.border(2.dp, colorScheme.primary, cellShape)
                    else -> Modifier
                }
            )
            .graphicsLayer {
                scaleX = popScale.value
                scaleY = popScale.value
                translationX = shakeOffset.value
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (cell.value != null) {
            val textColor = when {
                isDimmed -> colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                cell.isGiven -> colorScheme.onSurface
                cell.isError -> colorScheme.error
                isSelected -> colorScheme.onPrimaryContainer
                else -> colorScheme.primary
            }

            AnimatedVisibility(
                visible = true,
                enter = scaleIn(initialScale = 0.5f) + fadeIn(),
                label = "cellValue"
            ) {
                Text(
                    text = cell.value.toString(),
                    fontSize = 22.sp,
                    fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.SemiBold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        } else if (cell.candidates.isNotEmpty()) {
            CellNotes(cell.candidates, isSelected)
        }
    }
}

@Composable
fun CellNotes(candidates: Set<Int>, isSelected: Boolean = false) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp),
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
                            fontSize = 8.sp,
                            color = if (isSelected) colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            else colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Board progress
// ---------------------------------------------------------------------------
@Composable
private fun BoardProgressBar(
    board: Board,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val toFill = board.cells.flatten().count { !it.isGiven }
    val filled = board.cells.flatten().count { it.value != null && !it.isGiven }
    val remaining = (toFill - filled).coerceAtLeast(0)
    val progress = if (toFill > 0) filled.toFloat() / toFill else 1f
    val animatedProgress by animateFloatAsState(progress, tween(400), label = "boardProgress")

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "完成度",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = if (remaining > 0) "剩余 $remaining 格" else "全部完成",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (remaining > 0) colorScheme.primary else colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(50))
                .background(colorScheme.surfaceVariant.copy(alpha = 0.7f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(colorScheme.primary, colorScheme.secondary)
                        )
                    )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Number pad
// ---------------------------------------------------------------------------
@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onEraseClick: () -> Unit,
    notesMode: Boolean,
    onNotesToggle: () -> Unit,
    completedNumbers: Set<Int> = emptySet(),
    digitCounts: IntArray = IntArray(10),
    activeNumber: Int? = null,
    nakedSingleValue: Int? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PadToggleButton(
                label = "笔记",
                active = notesMode,
                onClick = onNotesToggle,
                modifier = Modifier.weight(1f)
            )
            PadToggleButton(
                label = "擦除",
                active = false,
                destructive = true,
                onClick = onEraseClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.widthIn(max = 260.dp)) {
            for (row in 0 until 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0 until 3) {
                        val n = row * 3 + col + 1
                        NumberButton(
                            number = n,
                            onClick = onNumberClick,
                            isCompleted = completedNumbers.contains(n),
                            placedCount = digitCounts[n],
                            isActive = activeNumber == n,
                            isSuggested = nakedSingleValue == n,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (row < 2) Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PadToggleButton(
    icon: (@Composable () -> Unit)? = null,
    label: String,
    active: Boolean,
    destructive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val container = when {
        active -> colorScheme.primary
        destructive -> colorScheme.errorContainer.copy(alpha = 0.5f)
        else -> colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = when {
        active -> colorScheme.onPrimary
        destructive -> colorScheme.error
        else -> colorScheme.onSurfaceVariant
    }

    val shape = RoundedCornerShape(14.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .pressScale(interactionSource),
        interactionSource = interactionSource,
        shape = shape,
        color = container,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.let {
                it()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberButton(
    number: Int,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false,
    placedCount: Int = 0,
    isActive: Boolean = false,
    isSuggested: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val numberColor = when {
        isCompleted -> colorScheme.onSurface
        isActive -> colorScheme.onPrimaryContainer
        isSuggested -> colorScheme.successGreen()
        else -> colorScheme.primary
    }
    val buttonColor = when {
        isCompleted -> colorScheme.surfaceVariant.copy(alpha = 0.45f)
        isActive -> colorScheme.primaryContainer
        isSuggested -> colorScheme.successGreen().copy(alpha = 0.16f)
        else -> colorScheme.surface.copy(alpha = 0.92f)
    }

    Surface(
        onClick = { if (!isCompleted) onClick(number) },
        enabled = !isCompleted,
        modifier = modifier
            .height(46.dp)
            .pressScale(interactionSource, pressedScale = 0.9f),
        interactionSource = interactionSource,
        shape = shape,
        color = buttonColor,
        contentColor = numberColor,
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = number.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = numberColor
            )
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    tint = colorScheme.successGreen(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(10.dp)
                )
            } else if (placedCount > 0) {
                Text(
                    text = "$placedCount/9",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp)
                )
            }
        }
    }
}

@Composable
private fun androidx.compose.material3.ColorScheme.successGreen(): Color =
    if (isLightTheme()) Color(0xFF22C55E) else Color(0xFF4ADE80)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionButton(
    icon: (@Composable () -> Unit)? = null,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasize: Boolean = false
) {
    if (emphasize) {
        GradientButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            height = 44.dp
        ) {
            icon?.let {
                it()
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        val colorScheme = MaterialTheme.colorScheme
        val shape = RoundedCornerShape(13.dp)
        val interactionSource = remember { MutableInteractionSource() }
        Surface(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .height(44.dp)
                .pressScale(interactionSource),
            interactionSource = interactionSource,
            shape = shape,
            color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = colorScheme.onSurfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                icon?.let {
                    it()
                    Spacer(modifier = Modifier.width(5.dp))
                }
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Difficulty pill
// ---------------------------------------------------------------------------
@Composable
private fun DifficultyPill(difficulty: Difficulty) {
    val color = difficultyColor(difficulty)
    PillBadge(
        text = difficultyLabel(difficulty),
        containerColor = color.copy(alpha = 0.15f),
        contentColor = color,
        fontWeight = FontWeight.SemiBold
    )
}

// ---------------------------------------------------------------------------
// Pause overlay
// ---------------------------------------------------------------------------
@Composable
private fun PauseOverlay(onResume: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "游戏已暂停",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))
            GradientButton(onClick = onResume, modifier = Modifier.padding(horizontal = 48.dp)) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("继续游戏", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Dialogs
// ---------------------------------------------------------------------------
@Composable
private fun VictoryDialog(
    level: Int,
    time: String,
    mistakes: Int,
    comboCount: Int,
    score: Int,
    isDaily: Boolean = false,
    onNextLevel: () -> Unit,
    onBackToLevels: () -> Unit
) {
    val dialogShape = RoundedCornerShape(28.dp)
    Dialog(onDismissRequest = {}) {
        DialogEntrance {
            Surface(
                shape = dialogShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 24.dp
            ) {
                Box {
                    ConfettiRain(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(dialogShape)
                    )
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                modifier = Modifier.size(88.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isDaily) "每日挑战完成!" else "恭喜通关!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "第 $level 关已完成",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                ResultRow("关卡", "$level")
                                Spacer(modifier = Modifier.height(8.dp))
                                ResultRow("用时", time)
                                Spacer(modifier = Modifier.height(8.dp))
                                ResultRow("失误", mistakes.toString())
                                if (comboCount >= 2) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ResultRow("最高连击", "x$comboCount")
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "得分",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = score.toString(),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (isDaily) {
                            GhostButton(onClick = onBackToLevels, modifier = Modifier.fillMaxWidth(), height = 48.dp) {
                                Text("返回关卡列表", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        } else {
                            GradientButton(
                                onClick = onNextLevel,
                                modifier = Modifier.fillMaxWidth(),
                                height = 54.dp
                            ) {
                                Text("下一关", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            GhostButton(onClick = onBackToLevels, modifier = Modifier.fillMaxWidth(), height = 48.dp) {
                                Text("返回关卡列表", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun GameOverDialog(
    isTimeUp: Boolean = false,
    onRetry: () -> Unit,
    onBackToLevels: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        DialogEntrance {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(88.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SentimentDissatisfied,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isTimeUp) "时间到!" else "挑战失败",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (isTimeUp) "未在限定时间内完成" else "失误次数已达上限",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GradientButton(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                        height = 54.dp
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("再试一次", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    GhostButton(onClick = onBackToLevels, modifier = Modifier.fillMaxWidth(), height = 48.dp) {
                        Text("返回关卡列表", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Dialog entrance helper
// ---------------------------------------------------------------------------
@Composable
private fun DialogEntrance(
    content: @Composable () -> Unit
) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.82f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dialogScale"
    )
    val dialogAlpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(220),
        label = "dialogAlpha"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            alpha = dialogAlpha
        }
    ) {
        content()
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------
@Composable
@Preview(showBackground = true, backgroundColor = 0xFFEDF1F6, widthDp = 411, heightDp = 500)
private fun GameControlsPreview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column {
                NumberPad(
                    onNumberClick = {},
                    onEraseClick = {},
                    notesMode = false,
                    onNotesToggle = {}
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionButton(
                        label = "撤销",
                        enabled = true,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        label = "恢复",
                        enabled = true,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        icon = {
                            Icon(
                                Icons.Default.Lightbulb,
                                null,
                                modifier = Modifier.size(18.dp),
                                tint = AppColors.hintGold
                            )
                        },
                        label = "提示 (3)",
                        enabled = true,
                        onClick = {},
                        modifier = Modifier.weight(1.4f),
                        emphasize = true
                    )
                }
            }
        }
    }
}
