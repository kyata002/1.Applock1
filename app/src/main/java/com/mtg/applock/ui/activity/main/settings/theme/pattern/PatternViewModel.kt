package com.mtg.applock.ui.activity.main.settings.theme.pattern

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.model.ThemeModel
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.ThemeUtils
import com.mtg.applock.util.extensions.doOnBackground
import com.mtg.applock.util.preferences.AppLockerPreferences
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