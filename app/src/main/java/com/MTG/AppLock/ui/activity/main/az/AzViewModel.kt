package com.MTG.AppLock.ui.activity.main.az

import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.ApplicationListUtils
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import javax.inject.Inject

class AzViewModel @Inject constructor(private val appLockerPreferences: AppLockerPreferences, private val appLockHelper: AppLockHelper) : RxAwareViewModel() {

    init {
        ApplicationListUtils.instance?.loadDataFull()
    }

    fun setReload(isReload: Boolean) {
        appLockerPreferences.setReload(isReload)
    }

    fun setShowPasswordUninstall(isShowPasswordUninstall: Boolean) {
        appLockerPreferences.setShowPasswordUninstall(isShowPasswordUninstall)
    }

    fun setLockSettingApp(lockSettingApp: Int) {
        appLockerPreferences.setLockSettingApp(lockSettingApp)
    }

    fun isApplicationGroupActive(packageName: String): Boolean {
        return appLockHelper.isApplicationGroupActive(packageName)
    }
}