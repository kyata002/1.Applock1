package com.MTG.AppLock.ui.activity.splash

import com.MTG.AppLock.data.DataApp.AppDataProvider
import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.ApplicationListUtils
import javax.inject.Inject

class SplashModel @Inject constructor(val appLockHelper: AppLockHelper, val appDataProvider: AppDataProvider) : RxAwareViewModel() {
//    private val typeThemeLiveData = MutableLiveData<Boolean>()
//    fun getTypeThemeLiveData(): LiveData<Boolean> = typeThemeLiveData

    init {
//        appLockHelper.getThemeDefault()?.let { theme ->
//            typeThemeLiveData.postValue(theme.typeTheme == ThemeUtils.TYPE_PATTERN)
//        }
        ApplicationListUtils.instance?.reload(appDataProvider.context)
    }
}