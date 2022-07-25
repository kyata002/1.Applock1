package com.mtg.applock.ui.activity.main

interface OnMainListener {
    fun onAppSelected(): Boolean

    fun onChangeStatusGroupLock()

    fun showAnimation()

    fun hideAnimation()
}