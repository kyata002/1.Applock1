package com.MTG.AppLock.ui.activity.main.configuration.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.MTG.AppLock.data.DataApp.AppDataProvider
import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.data.sqllite.ConstantDB
import com.MTG.AppLock.data.sqllite.model.ConfigurationModel
import com.MTG.AppLock.ui.activity.main.az.function.AddSectionHeaderViewStateFunction
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemBaseViewState
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemItemViewState
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.extensions.doOnBackground
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class EditConfigurationViewModel @Inject constructor(private val appDataProvider: AppDataProvider, private val appLockHelper: AppLockHelper, private val appLockerPreferences: AppLockerPreferences) : RxAwareViewModel() {
    private val appDataViewStateListLiveData = MutableLiveData<List<AppLockItemBaseViewState>>()
    private val nameLiveData = MutableLiveData<String>()
    private var appDataFullList: List<AppLockItemBaseViewState> = ArrayList()
    private var configurationModel: ConfigurationModel? = null
    private var mId: Int = ConstantDB.CONFIGURATION_ID_CREATE

    fun setId(id: Int) {
        mId = id
    }

    fun getNameLiveData(): LiveData<String> = nameLiveData

    fun getAppDataListLiveData(): LiveData<List<AppLockItemBaseViewState>> = appDataViewStateListLiveData

    fun loadConfiguration(isLoadMain: Boolean) {
        doOnBackground {
            configurationModel = appLockHelper.getConfiguration(mId)
            configurationModel?.let {
                nameLiveData.postValue(it.name)
            }
            val installedAppsObservable = appDataProvider.fetchInstalledAppList().toObservable()
            installedAppsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                val appDataViewStateList = mutableListOf<AppLockItemItemViewState>()
                it.forEach { appData ->
                    configurationModel?.let { configuration ->
                        if (isLoadMain) {
                            configuration.packageAppLockTemp = configuration.packageAppLock
                        }
                        if (configuration.packageAppLockTemp.contains(appData.parsePackageName())) {
                            appDataViewStateList.add(AppLockItemItemViewState(appData, true))
                        }
                    }
                }
                appDataFullList = AddSectionHeaderViewStateFunction(appDataProvider.context).applyV2(appDataViewStateList)
                appDataViewStateListLiveData.postValue(appDataFullList)
            }
        }
    }

    fun updateConfiguration(): Boolean {
        return configurationModel?.let {
            it.packageAppLock = it.packageAppLockTemp
            appLockHelper.updateConfiguration(it)
        } ?: false
    }

    fun removeData(selectedApp: AppLockItemItemViewState) {
        configurationModel?.removePackageAppLockTemp(selectedApp.appData.parsePackageName())
    }

    fun setReload(isReload: Boolean) {
        appLockerPreferences.setReload(isReload)
    }

    fun isConfigurationActive(): Boolean {
        return try {
            if (mId == ConstantDB.CONFIGURATION_ID_CREATE) {
                false
            } else {
                appLockHelper.isConfigurationActive(mId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}