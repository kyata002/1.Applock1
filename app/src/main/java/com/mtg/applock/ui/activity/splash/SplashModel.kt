package com.mtg.applock.ui.activity.splash

import com.mtg.applock.data.dataapp.AppDataProvider
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.ApplicationListUtils
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