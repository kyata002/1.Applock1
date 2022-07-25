package com.mtg.applock.ui.activity.main.settings.theme.background

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.model.ThemeModel
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.ThemeUtils
import com.mtg.applock.util.extensions.doOnBackground
import com.mtg.applock.util.preferences.AppLockerPreferences
import javax.inject.Inject

class BackgroundViewModel @Inject constructor(private val appLockerPreferences: AppLockerPreferences, private val appLockHelper: AppLockHelper) : RxAwareViewModel() {
    private val mBackgroundLiveData = MutableLiveData<MutableList<ThemeModel>>()
    private val mStopReloadLiveData = MutableLiveData<Boolean>()

    init {
        doOnBackground {
            mBackgroundLiveData.postValue(appLockHelper.getThemeList(ThemeUtils.TYPE_WALLPAPER))
        }

    }



    fun reloadBackground() {
        mStopReloadLiveData.postValue(false)
        doOnBackground {
            mBackgroundLiveData.postValue(appLockHelper.getThemeList(ThemeUtils.TYPE_WALLPAPER))
        }
    }

    fun setReload(isReload: Boolean) {
        appLockerPreferences.setReload(isReload)
    }

    fun updateThemeDefault(backgroundResId: Int, backgroundUrl: String, backgroundDownload: String) {
        appLockHelper.updateThemeDefault(backgroundResId, backgroundUrl, backgroundDownload)
    }

    fun getBackgroundLiveData(): LiveData<MutableList<ThemeModel>> = mBackgroundLiveData

    fun getStopReloadLiveData(): LiveData<Boolean> = mStopReloadLiveData
}



