package com.finley.android.sudoku.model

import kotlinx.serialization.Serializable

@Serializable
data class Cell(
    val row: Int,
    val col: Int,
    val value: Int? = null,        // null = 空格
    val isGiven: Boolean = false,  // 题目自带数字，不可被玩家修改
    val candidates: Set<Int> = emptySet(),
    val isError: Boolean = false   // 仅用于"实时检查"模式下的展示态，不影响核心判定
)

@Serializable
data class Board(
    val size: Int = 9,             // MVP 固定 9，预留扩展位
    val boxSize: Int = 3,          // 宫的边长，9x9 为 3
    val cells: List<List<Cell>>    // [row][col]
) {
    fun cellAt(row: Int, col: Int): Cell = cells[row][col]
}

@Serializable
enum class Difficulty { EASY, MEDIUM, HARD, EXPERT, MASTER }

@Serializable
data class Puzzle(
    val id: String,
    val seed: Long,                // 生成该题所用随机种子，保证可复现
    val size: Int,
    val difficulty: Difficulty,
    val difficultyScore: Int,      // 0-100 细分数值，用于排序/微调
    val initialClues: String,      // 81 位字符串，0 表示空格，便于存储/传输
    val solution: String,          // 81 位字符串，完整解
    val dateForDaily: String? = null, // 仅"每日挑战"题目赋值，格式 YYYY-MM-DD（UTC）
    val createdAt: Long
)

@Serializable
sealed class Move {
    @Serializable
    data class Place(val row: Int, val col: Int, val value: Int, val previous: Int?) : Move()
    @Serializable
    data class Erase(val row: Int, val col: Int, val previous: Int?) : Move()
    @Serializable
    data class ToggleCandidate(val row: Int, val col: Int, val value: Int) : Move()
}

@Serializable
enum class GameStatus { IN_PROGRESS, COMPLETED, FAILED, PAUSED }

@Serializable
data class GameSession(
    val sessionId: String,
    val puzzleId: String,
    val board: Board,
    val notesMode: Boolean = false,
    val elapsedSeconds: Int = 0,
    val mistakeCount: Int = 0,
    val hintUsedCount: Int = 0,
    val history: List<Move> = emptyList(),
    val historyCursor: Int = 0,    // 支持撤销/重做的指针
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val updatedAt: Long = 0,
    val version: Int = 1           // 用于服务端同步冲突解决
)
