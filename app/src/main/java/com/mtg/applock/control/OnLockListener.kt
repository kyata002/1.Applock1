package com.mtg.applock.control

interface OnLockListener {
    fun onLockListener(lock: Boolean, positionChange: Int)
}