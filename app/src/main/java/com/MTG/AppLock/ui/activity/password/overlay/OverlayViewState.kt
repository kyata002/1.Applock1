package com.MTG.AppLock.ui.activity.password.overlay

import android.content.Context
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.activity.password.overlay.OverlayValidateType.TYPE_FINGERPRINT
import com.MTG.AppLock.ui.activity.password.overlay.OverlayValidateType.TYPE_PATTERN
import com.MTG.AppLock.ui.activity.password.overlay.activity.fingerprint.FingerPrintResult.*
import com.MTG.AppLock.ui.activity.password.overlay.activity.fingerprint.FingerPrintResultData
import es.MTG.toasty.Toasty

data class OverlayViewState(
        val overlayValidateType: OverlayValidateType? = null,

        val isDrawnCorrect: Boolean? = null,

        val fingerPrintResultData: FingerPrintResultData? = null,

        val isFingerPrintMode: Boolean = false,

        val isIntrudersCatcherMode: Boolean = false
) {
    fun showNotification(context: Context) {
        when (overlayValidateType) {
            TYPE_PATTERN -> {
                when (isDrawnCorrect) {
                    true -> {
                        Toasty.hideToast()
                    }
                    false -> {
                        Toasty.showToast(context, R.string.overlay_prompt_pattern_title_wrong, Toasty.ERROR)
                    }
                    null -> {
                        Toasty.showToast(context, R.string.overlay_prompt_pattern_title, Toasty.NORMAL)
                    }
                }
            }
            TYPE_FINGERPRINT -> {
                when (fingerPrintResultData?.fingerPrintResult) {
                    SUCCESS -> {
                        Toasty.hideToast()
                    }
                    NOT_MATCHED -> {
                        Toasty.showToast(context, String.format(context.getString(R.string.overlay_prompt_fingerprint_title_wrong) + "%s%s ", fingerPrintResultData.availableTimes.toString()), Toasty.ERROR)
                    }
                    ERROR -> {
                        Toasty.showToast(context, R.string.overlay_prompt_fingerprint_title_error, Toasty.ERROR)
                    }
                    else -> {

                    }
                }
            }
            else -> {
                // nothing
            }
        }
    }
}