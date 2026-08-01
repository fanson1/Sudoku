package com.finley.android.sudoku

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getBaseUrl(): String
