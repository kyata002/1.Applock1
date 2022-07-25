package com.mtg.applock.ui.activity.main.locked

import android.content.Context
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.ApplicationListUtils
import com.mtg.applock.util.preferences.AppLockerPreferences
import javax.inject.Inject

class LockedViewModel @Inject constructor(private val appLockerPreferences: AppLockerPreferences, private val appLockHelper: AppLockHelper) : RxAwareViewModel() {
    fun setReload(isReload: Boolean) {
        appLockerPreferences.setReload(isReload)
    }

    fun setLockSettingApp(lockSettingApp: Int) {
        appLockerPreferences.setLockSettingApp(lockSettingApp)
    }

    fun isApplicationGroupActive(packageName: String): Boolean {
        return appLockHelper.isApplicationGroupActive(packageName)
    }

    fun canUnlock(context: Context): Boolean {
        return ApplicationListUtils.instance?.canUnlock(context, appLockHelper) ?: false
    }
}