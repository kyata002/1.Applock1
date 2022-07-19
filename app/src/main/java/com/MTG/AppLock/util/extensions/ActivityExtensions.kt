package com.MTG.AppLock.util.extensions

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_IMPLICIT_ONLY
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

fun Activity.showKeyboard() {
    try {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(0, HIDE_IMPLICIT_ONLY)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Activity.hideKeyboard(editText: EditText) {
    try {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Activity.dialogLayout(dialog: AlertDialog?) {
    // retrieve display dimensions
    val displayRectangle = Rect()
    val window: Window = window
    window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
    dialog?.window?.setLayout((displayRectangle.width() * 0.85f).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
}

fun Activity.dialogLayoutSony(dialog: AlertDialog?) {
    // retrieve display dimensions
    val displayRectangle = Rect()
    val window: Window = window
    window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
    if (isSony()) {
        dialog?.window?.setLayout((displayRectangle.width() * 0.85f).toInt(), LinearLayout.LayoutParams.MATCH_PARENT)
    } else {
        dialog?.window?.setLayout((displayRectangle.width() * 0.85f).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
    }
}

private fun isSony(): Boolean {
    val device = Build.MANUFACTURER
    if (device == "Sony") {
        return true
    }
    return false
}