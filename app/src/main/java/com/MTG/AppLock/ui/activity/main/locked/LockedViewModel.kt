package com.MTG.AppLock.ui.activity.main.locked

import android.content.Context
import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.ApplicationListUtils
import com.MTG.AppLock.util.preferences.AppLockerPreferences
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