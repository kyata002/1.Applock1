package com.mtg.applock.ui.activity.main.settings.theme.keypad

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.model.ThemeModel
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.ThemeUtils
import com.mtg.applock.util.extensions.doOnBackground
import com.mtg.applock.util.preferences.AppLockerPreferences
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