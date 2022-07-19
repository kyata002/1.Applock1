package com.MTG.AppLock.control

import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemItemViewState

interface OnAppSelectedListener {
    fun onDeleteApp(position: Int, appLockItemItemViewState: AppLockItemItemViewState)
    fun onLockApp(position: Int, appLockItemItemViewState: AppLockItemItemViewState)
}