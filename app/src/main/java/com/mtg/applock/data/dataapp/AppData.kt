package com.mtg.applock.data.dataapp

import android.graphics.drawable.Drawable

data class AppData(val appName: String, val packageName: String, val appIconDrawable: Drawable) {
    fun parsePackageName(): String {
        return packageName.substring(0, packageName.indexOf("/"))
    }
}