package com.MTG.AppLock.ui.activity.intruders.detail

import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.data.sqllite.model.IntruderPhoto
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import java.io.File
import javax.inject.Inject

class IntrudersPhotosDetailViewModel @Inject constructor(private val appLockerPreferences: AppLockerPreferences, private val appLockHelper: AppLockHelper) : RxAwareViewModel() {
    fun deleteFile(path: String): Boolean {
        val file = File(path)
        return file.delete()
    }

    fun setReLoad(isReload: Boolean) {
        appLockerPreferences.setReload(isReload)
    }

    fun updateIntruder(intruderPhoto: IntruderPhoto): Boolean {
        return appLockHelper.updateIntruder(intruderPhoto)
    }
}