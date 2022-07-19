package com.MTG.AppLock.ui.activity.main.settings.theme.keypad

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.model.ThemeModel
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.ThemeUtils
import com.MTG.AppLock.util.extensions.doOnBackground
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import javax.inject.Inject

class KeypadViewModel @Inject constructor(private val appLockerPreferences: AppLockerPreferences, private val appLockHelper: AppLockHelper) : RxAwareViewModel() {
    private val mKeypadLiveData = MutableLiveData<MutableList<ThemeModel>>()
    private val mStopReloadLiveData = MutableLiveData<Boolean>()


    init {
        doOnBackground {
            mKeypadLiveData.postValue(appLockHelper.getThemeList(ThemeUtils.TYPE_PIN))
        }

    }



    fun reloadPin() {
        mStopReloadLiveData.postValue(false)
        doOnBackground {
            mKeypadLiveData.postValue(appLockHelper.getThemeList(ThemeUtils.TYPE_PIN))

        }
    }

    fun setReload(isReload: Boolean) {
        appLockerPreferences.setReload(isReload)
    }

    fun getKeypadLiveData(): LiveData<MutableList<ThemeModel>> = mKeypadLiveData

    fun getStopReloadLiveData(): LiveData<Boolean> = mStopReloadLiveData
}