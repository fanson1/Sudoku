package com.finley.android.sudoku.pro

import com.finley.android.sudoku.util.Persistence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Offline fallback used by desktop, web and iOS builds. There is no store on
 * those targets yet, so a purchase simply unlocks Pro locally.
 */
class LocalProController : ProController {
    private val _isPro = MutableStateFlow(Persistence.isPro())
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _priceLabel = MutableStateFlow<String?>(null)
    override val priceLabel: StateFlow<String?> = _priceLabel.asStateFlow()

    private val _busy = MutableStateFlow(false)
    override val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _available = MutableStateFlow(true)
    override val available: StateFlow<Boolean> = _available.asStateFlow()

    private val _message = MutableStateFlow<ProMessage?>(null)
    override val message: StateFlow<ProMessage?> = _message.asStateFlow()

    override fun purchase() {
        Persistence.setPro(true)
        _isPro.value = true
        _message.value = ProMessage.PURCHASE_SUCCESS
    }

    override fun restore() {
        _isPro.value = Persistence.isPro()
        _message.value = if (_isPro.value) ProMessage.RESTORE_SUCCESS else ProMessage.RESTORE_NOTHING
    }

    override fun consumeMessage() {
        _message.value = null
    }
}
