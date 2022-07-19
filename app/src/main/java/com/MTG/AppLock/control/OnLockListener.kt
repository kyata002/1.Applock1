package com.MTG.AppLock.control

interface OnLockListener {
    fun onLockListener(lock: Boolean, positionChange: Int)
}