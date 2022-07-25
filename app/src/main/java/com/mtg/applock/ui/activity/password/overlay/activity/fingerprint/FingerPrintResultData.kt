package com.mtg.applock.ui.activity.password.overlay.activity.fingerprint

import com.mtg.applock.ui.activity.password.overlay.activity.fingerprint.FingerPrintResult.*

data class FingerPrintResultData(val fingerPrintResult: FingerPrintResult, val availableTimes: Int = 0, val errorMessage: String = "") {
    fun isSuccess() = fingerPrintResult == SUCCESS

    fun isNotSuccess() = fingerPrintResult != SUCCESS

    companion object {
        fun matched() = FingerPrintResultData(SUCCESS)

        fun notMatched(availableTimes: Int) = FingerPrintResultData(
                fingerPrintResult = NOT_MATCHED, availableTimes = availableTimes
        )

        fun error(errorMessage: String) = FingerPrintResultData(
                fingerPrintResult = ERROR, errorMessage = errorMessage
        )
    }
}