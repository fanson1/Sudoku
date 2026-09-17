package com.finley.android.sudoku.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

object UsersTable : Table("users") {
    val id = varchar("id", 50)
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 100)
    val token = varchar("token", 100).nullable()
    // 新增：记录用户通关的最大关卡，默认为1
    val unlockedLevel = integer("unlocked_level").default(1)

    override val primaryKey = PrimaryKey(id)
}

object ScoresTable : Table("scores") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50)
    val score = integer("score")
    val level = integer("level")

    override val primaryKey = PrimaryKey(id)
}

object DatabaseFactory {
    fun init() {
        // 添加 AUTO_SERVER=TRUE 允许 H2 数据库被多个进程（如 IDE 的 DB 浏览器）同时访问，防止文件锁定
        init("jdbc:h2:file:./build/db;AUTO_SERVER=TRUE")
    }

    fun init(jdbcURL: String) {
        val driverClassName = "org.h2.Driver"
        val database = Database.connect(jdbcURL, driverClassName)
        
        transaction(database) {
            // SchemaUtils.create 会自动检测缺失的表，但在某些版本中对新字段的自动添加支持有限
            SchemaUtils.create(UsersTable, ScoresTable)
            
            // 手动执行迁移：如果 unlocked_level 字段不存在，则添加它
            try {
                exec("ALTER TABLE users ADD COLUMN IF NOT EXISTS unlocked_level INT DEFAULT 1")
            } catch (e: Exception) {
                // 如果数据库不支持 IF NOT EXISTS，忽略错误或根据驱动细化处理
                println("Migration info: ${e.message}")
            }
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
