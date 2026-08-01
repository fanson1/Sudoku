package com.finley.android.sudoku.model

object GameRules {
    /**
     * 根据关卡计算初始 Hint 次数
     * 1-10关: 3次
     * 11-20关: 4次
     * 21-30关: 5次
     * 以此类推：每增加10关，Hint次数增加1次
     */
    fun getInitialHintsForLevel(level: Int): Int {
        if (level <= 0) return 3
        return 3 + (level - 1) / 10
    }

    /**
     * 根据关卡映射难度
     */
    fun getDifficultyForLevel(level: Int): Difficulty {
        return when {
            level <= 10 -> Difficulty.EASY
            level <= 20 -> Difficulty.MEDIUM
            level <= 30 -> Difficulty.HARD
            level <= 40 -> Difficulty.EXPERT
            else -> Difficulty.MASTER
        }
    }
}
