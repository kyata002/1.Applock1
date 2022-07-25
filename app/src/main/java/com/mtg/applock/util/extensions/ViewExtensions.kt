package com.mtg.applock.util.extensions

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.core.content.ContextCompat.getSystemService


fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Vibrates the device. Used for providing feedback when the user performs an action.
 */
fun vibrate(context: Context, isVibrate: Boolean) {
    if (!isVibrate) return
    val vibrator = getSystemService(context, Vibrator::class.java)
    vibrator?.let {
        if (!it.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= 26) {
            it.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            it.vibrate(500)
        }
    }
}