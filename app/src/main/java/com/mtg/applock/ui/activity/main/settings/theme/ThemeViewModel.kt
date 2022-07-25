package com.mtg.applock.ui.activity.main.settings.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.model.ThemeModel
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.ThemeUtils
import com.mtg.applock.util.extensions.doOnBackground
import com.mtg.applock.util.preferences.AppLockerPreferences
import javax.inject.Inject

class ThemeViewModel @Inject constructor(private val appLockerPreferences: AppLockerPreferences, private val appLockHelper: AppLockHelper) : RxAwareViewModel() {
    private val mPatternLiveData = MutableLiveData<MutableList<ThemeModel>>()
    private val mPinLiveData = MutableLiveData<MutableList<ThemeModel>>()
    private val mBackgroundLiveData = MutableLiveData<MutableList<ThemeModel>>()
    private val mStopReloadLiveData = MutableLiveData<Boolean>()

    init {
        doOnBackground {
            appLockHelper.checkExistThemeDownloaded()
            mPatternLiveData.postValue(appLockHelper.getThemeList(ThemeUtils.TYPE_PATTERN))
            mPinLiveData.postValue(appLockHelper.getThemeList(ThemeUtils.TYPE_PIN))
            mBackgroundLiveData.postValue(appLockHelper.getThemeList(ThemeUtils.TYPE_WALLPAPER))
        }

    }



    fun setReload(isReload: Boolean) {
        appLockerPreferences.setReload(isReload)
    }

    fun updateThemeDefault(backgroundResId: Int, backgroundUrl: String, backgroundDownload: String) {
        appLockHelper.updateThemeDefault(backgroundResId, backgroundUrl, backgroundDownload)
    }

    fun reloadPattern() {
        mStopReloadLiveData.postValue(false)
        doOnBackground {
            mPatternLiveData.postValue(appLockHelper.getThemeList(ThemeUtils.TYPE_PATTERN))

        }
    }

    fun reloadPin() {
        mStopReloadLiveData.postValue(false)
        doOnBackground {
            mPinLiveData.postValue(appLockHelper.getThemeList(ThemeUtils.TYPE_PIN))

        }
    }

    fun reloadBackground() {
        mStopReloadLiveData.postValue(false)
        doOnBackground {
            mBackgroundLiveData.postValue(appLockHelper.getThemeList(ThemeUtils.TYPE_WALLPAPER))
        }
    }

    fun getPinLiveData(): LiveData<MutableList<ThemeModel>> = mPinLiveData
    fun getPatternLiveData(): LiveData<MutableList<ThemeModel>> = mPatternLiveData
    fun getBackgroundLiveData(): LiveData<MutableList<ThemeModel>> = mBackgroundLiveData
    fun getStopReloadLiveData(): LiveData<Boolean> = mStopReloadLiveData
}