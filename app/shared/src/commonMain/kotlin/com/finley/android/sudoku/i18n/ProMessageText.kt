package com.finley.android.sudoku.i18n

import com.finley.android.sudoku.pro.ProMessage

fun ProMessage.stringKey(): StringKey = when (this) {
    ProMessage.PURCHASE_SUCCESS -> StringKey.MSG_PURCHASE_SUCCESS
    ProMessage.PURCHASE_CANCELLED -> StringKey.MSG_PURCHASE_CANCELLED
    ProMessage.PURCHASE_FAILED -> StringKey.MSG_PURCHASE_FAILED
    ProMessage.PURCHASE_PENDING -> StringKey.MSG_PURCHASE_PENDING
    ProMessage.RESTORE_SUCCESS -> StringKey.MSG_RESTORE_SUCCESS
    ProMessage.RESTORE_NOTHING -> StringKey.MSG_RESTORE_NOTHING
    ProMessage.BILLING_UNAVAILABLE -> StringKey.MSG_BILLING_UNAVAILABLE
}
