package com.finley.android.sudoku

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()
actual fun getBaseUrl(): String {
    return "http://localhost:8080"
}
