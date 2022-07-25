package com.mtg.applock.ui.activity.main.configuration

import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mtg.applock.data.dataapp.AppDataProvider
import com.mtg.applock.data.sqllite.ConstantDB
import com.mtg.applock.data.sqllite.model.ConfigurationModel
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.extensions.doOnBackground
import javax.inject.Inject


class ConfigurationViewModel @Inject constructor(private val appDataProvider: AppDataProvider) : RxAwareViewModel() {
    private val configurationListLiveData = MutableLiveData<List<ConfigurationModel>>()

    init {
        loadConfiguration(appDataProvider.context)
    }

    fun getConfigurationListLiveData(): LiveData<List<ConfigurationModel>> {
        return configurationListLiveData
    }

    fun renameConfiguration(configurationModel: ConfigurationModel): Boolean {
        return appDataProvider.appLockHelper.updateConfiguration(configurationModel)
    }

    fun removeAppLockInConfiguration(packageName: String) {
        appDataProvider.appLockHelper.removePackageName(packageName)
    }

    fun deleteConfiguration(configurationModel: ConfigurationModel) {
        appDataProvider.appLockHelper.deleteConfiguration(configurationModel)
        val configurationList: List<ConfigurationModel>? = configurationListLiveData.value
        configurationList?.let {
            val configurationModelList = mutableListOf<ConfigurationModel>()
            it.forEach { configuration ->
                if (configurationModel.id != configuration.id) {
                    configurationModelList.add(configuration)
                }
            }
            configurationListLiveData.postValue(configurationModelList)
        }
    }

    fun loadConfiguration(context: Context) {
        doOnBackground {
            val packageManager = context.packageManager
            val configurationList = appDataProvider.appLockHelper.getConfigurationList()
            val configurationFixList = mutableListOf<ConfigurationModel>()
            configurationList.forEach { configuration ->
                configuration.getPackageAppLockList().forEach { packageName ->
                    if (!isPackageInstalled(packageName, packageManager)) {
                        configuration.removePackageAppLock(packageName)
                    }
                }
                if (TextUtils.isEmpty(configuration.packageAppLock)) {
                    deleteConfiguration(configuration)
                } else {
                    configurationFixList.add(configuration)
                }
            }
            configurationListLiveData.postValue(configurationFixList)
        }
    }

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun setReload(isReload: Boolean) {
        appDataProvider.appLockerPreferences.setReload(isReload)
    }

    fun updateConfiguration(configurationModel: ConfigurationModel): Boolean {
        return appDataProvider.appLockHelper.updateConfiguration(configurationModel)
    }

    fun getNumberActiveGroup(): Int {
        return appDataProvider.appLockHelper.getConfigurationActiveList().size
    }

    fun getConfigurationDefault(): ConfigurationModel? {
        return appDataProvider.appLockHelper.getConfiguration(ConstantDB.CONFIGURATION_ID_DEFAULT)
    }
}