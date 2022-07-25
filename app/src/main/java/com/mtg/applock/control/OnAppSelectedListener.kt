package com.mtg.applock.control

import com.mtg.applock.ui.activity.main.az.model.AppLockItemItemViewState

interface OnAppSelectedListener {
    fun onDeleteApp(position: Int, appLockItemItemViewState: AppLockItemItemViewState)
    fun onLockApp(position: Int, appLockItemItemViewState: AppLockItemItemViewState)
}