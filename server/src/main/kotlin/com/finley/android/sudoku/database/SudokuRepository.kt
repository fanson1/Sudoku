package com.finley.android.sudoku.database

import com.finley.android.sudoku.database.DatabaseFactory.dbQuery
import com.finley.android.sudoku.model.*
import com.finley.android.sudoku.security.PasswordHasher
import org.jetbrains.exposed.sql.*
import java.util.*

class SudokuRepository {

    suspend fun registerUser(request: AuthRequest): User? = dbQuery {
        val userId = UUID.randomUUID().toString()
        val hashedPassword = PasswordHasher.hash(request.password)
        
        val insertStatement = UsersTable.insert {
            it[id] = userId
            it[username] = request.username
            it[password] = hashedPassword
            it[token] = null
            it[unlockedLevel] = 1
        }

        insertStatement.resultedValues?.singleOrNull()?.let {
            User(it[UsersTable.id], it[UsersTable.username], it[UsersTable.token], it[UsersTable.unlockedLevel])
        }
    }

    suspend fun findUser(username: String): String? = dbQuery {
        UsersTable.select(UsersTable.password)
            .where { UsersTable.username eq username }
            .map { it[UsersTable.password] }
            .singleOrNull()
    }

    suspend fun getUserByUsername(username: String): User? = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.username eq username }
            .map { User(it[UsersTable.id], it[UsersTable.username], it[UsersTable.token], it[UsersTable.unlockedLevel]) }
            .singleOrNull()
    }

    suspend fun updateUnlockedLevel(username: String, level: Int): Boolean = dbQuery {
        val currentLevel = UsersTable.select(UsersTable.unlockedLevel)
            .where { UsersTable.username eq username }
            .map { it[UsersTable.unlockedLevel] }
            .singleOrNull() ?: 1
            
        if (level > currentLevel) {
            UsersTable.update({ UsersTable.username eq username }) {
                it[unlockedLevel] = level
            } > 0
        } else {
            false
        }
    }

    suspend fun updateUsername(oldUsername: String, newUsername: String): Boolean = dbQuery {
        // 更新用户表
        val userUpdated = UsersTable.update({ UsersTable.username eq oldUsername }) {
            it[username] = newUsername
        } > 0
        
        // 同步更新分数表中的用户名，确保排行榜显示最新名称
        if (userUpdated) {
            ScoresTable.update({ ScoresTable.username eq oldUsername }) {
                it[username] = newUsername
            }
        }
        
        userUpdated
    }

    suspend fun updatePassword(username: String, newHashedPassword: String): Boolean = dbQuery {
        UsersTable.update({ UsersTable.username eq username }) {
            it[password] = newHashedPassword
        } > 0
    }

    suspend fun getLeaderboard(): List<LeaderboardEntry> = dbQuery {
        val totalScore = ScoresTable.score.sum()
        val maxLevel = ScoresTable.level.max()
        
        ScoresTable
            .select(ScoresTable.username, totalScore, maxLevel)
            .groupBy(ScoresTable.username)
            .orderBy(totalScore, SortOrder.DESC)
            .limit(100)
            .mapIndexed { index, row ->
                LeaderboardEntry(
                    rank = index + 1,
                    username = row[ScoresTable.username],
                    score = row[totalScore] ?: 0,
                    level = row[maxLevel] ?: 0,
                )
            }
    }

    suspend fun addScore(entry: LeaderboardEntry) = dbQuery {
        // 检查该用户该关卡是否已经有分数
        val existingScore = ScoresTable.select(ScoresTable.score)
            .where { (ScoresTable.username eq entry.username) and (ScoresTable.level eq entry.level) }
            .map { it[ScoresTable.score] }
            .singleOrNull()

        if (existingScore == null) {
            // 如果没玩过这一关，直接插入
            ScoresTable.insert {
                it[username] = entry.username
                it[score] = entry.score
                it[level] = entry.level
            }
        } else if (entry.score > existingScore) {
            // 如果玩过，但这次分数更高，则更新为最高分
            ScoresTable.update({ (ScoresTable.username eq entry.username) and (ScoresTable.level eq entry.level) }) {
                it[score] = entry.score
            }
        }
    }

    suspend fun getAllUsers(): List<User> = dbQuery {
        UsersTable.selectAll()
            .map { User(it[UsersTable.id], it[UsersTable.username], it[UsersTable.token], it[UsersTable.unlockedLevel]) }
    }
}
