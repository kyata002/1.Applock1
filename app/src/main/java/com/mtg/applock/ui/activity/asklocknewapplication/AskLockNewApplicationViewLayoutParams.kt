package com.mtg.applock.ui.activity.asklocknewapplication

import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.view.WindowManager

object AskLockNewApplicationViewLayoutParams {
    fun get(): WindowManager.LayoutParams {
        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val width = Resources.getSystem().displayMetrics.widthPixels * 9 / 10
        val params = WindowManager.LayoutParams(width, WindowManager.LayoutParams.WRAP_CONTENT, layoutFlag, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT)
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        return params
    }
}