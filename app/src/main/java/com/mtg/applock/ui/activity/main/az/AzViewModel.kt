package com.mtg.applock.ui.activity.main.az

import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.ApplicationListUtils
import com.mtg.applock.util.preferences.AppLockerPreferences
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