package com.finley.android.sudoku.pro

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

/** One-time, lifetime product. The ID must match Play Console exactly. */
object ProProduct {
    const val ID = "sudoku_pro_lifetime"
    const val FALLBACK_PRICE = "$2.99"
}

enum class ProMessage {
    PURCHASE_SUCCESS,
    PURCHASE_CANCELLED,
    PURCHASE_FAILED,
    PURCHASE_PENDING,
    RESTORE_SUCCESS,
    RESTORE_NOTHING,
    BILLING_UNAVAILABLE
}

/**
 * Platform billing facade. Android is backed by Google Play Billing; every
 * other target uses a local unlock so the desktop/web builds stay usable.
 */
interface ProController {
    val isPro: StateFlow<Boolean>
    val priceLabel: StateFlow<String?>
    val busy: StateFlow<Boolean>
    val available: StateFlow<Boolean>
    val message: StateFlow<ProMessage?>

    fun purchase()
    fun restore()
    fun consumeMessage()
}

@Composable
expect fun rememberProController(): ProController
