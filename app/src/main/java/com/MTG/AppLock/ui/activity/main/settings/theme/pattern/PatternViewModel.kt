package com.MTG.AppLock.ui.activity.main.settings.theme.pattern

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.model.ThemeModel
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.ThemeUtils
import com.MTG.AppLock.util.extensions.doOnBackground
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import javax.inject.Inject

class PatternViewModel @Inject constructor(private val appLockerPreferences: AppLockerPreferences, private val appLockHelper: AppLockHelper) : RxAwareViewModel() {
    private val mPatternLiveData = MutableLiveData<MutableList<ThemeModel>>()
    private val mStopReloadLiveData = MutableLiveData<Boolean>()


    init {
        doOnBackground {
            appLockHelper.checkExistThemeDownloaded()
            mPatternLiveData.postValue(appLockHelper.getThemeList(ThemeUtils.TYPE_PATTERN))
        }

    }



    fun reloadPattern() {
        mStopReloadLiveData.postValue(false)
        doOnBackground {
            mPatternLiveData.postValue(appLockHelper.getThemeList(ThemeUtils.TYPE_PATTERN))
        }
    }

    fun setReload(isReload: Boolean) {
        appLockerPreferences.setReload(isReload)
    }

    fun getPatternLiveData(): LiveData<MutableList<ThemeModel>> = mPatternLiveData

    fun getStopReloadLiveData(): LiveData<Boolean> = mStopReloadLiveData
}