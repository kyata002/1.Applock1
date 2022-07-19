package com.MTG.pinlock.extension

import android.util.Base64

fun String.decodeBase64(): String {
    return try {
        val stringReplace = this.replace("*", "/")
        Base64.decode(stringReplace, Base64.DEFAULT).toString(charset("UTF-8"))
    } catch (e: Exception) {
        e.printStackTrace()
        this
    }
}

fun String.encodeBase64(): String {
    return try {
        val encode = Base64.encodeToString(this.toByteArray(charset("UTF-8")), Base64.DEFAULT)
        encode.replace("/", "*")
        encode.replace("\n", "")
    } catch (e: Exception) {
        e.printStackTrace()
        return this
    }
}