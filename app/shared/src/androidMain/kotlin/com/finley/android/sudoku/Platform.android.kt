package com.finley.android.sudoku

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
actual fun getBaseUrl(): String {
    return "http://192.168.50.39:8080"
}
