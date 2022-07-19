package com.MTG.AppLock.ui.activity.main.settings.theme.background

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.model.ThemeModel
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.ThemeUtils
import com.MTG.AppLock.util.extensions.doOnBackground
import com.MTG.AppLock.util.preferences.AppLockerPreferences
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



