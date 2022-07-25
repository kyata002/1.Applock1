package com.mtg.applock.ui.activity.main.configuration.detail

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mtg.applock.R
import com.mtg.applock.control.OnLockListener
import com.mtg.applock.data.dataapp.AppDataProvider
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.data.sqllite.ConstantDB
import com.mtg.applock.data.sqllite.model.ConfigurationModel
import com.mtg.applock.ui.activity.main.az.function.AddSectionHeaderViewStateFunction
import com.mtg.applock.ui.activity.main.az.model.AppLockItemBaseViewState
import com.mtg.applock.ui.activity.main.az.model.AppLockItemHeaderViewState
import com.mtg.applock.ui.activity.main.az.model.AppLockItemItemViewState
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.Const
import com.mtg.applock.util.extensions.doOnBackground
import com.mtg.applock.util.extensions.plusAssign
import com.mtg.applock.util.preferences.AppLockerPreferences
import com.mtg.applock.util.text.VNCharacterUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class DetailConfigurationViewModel @Inject constructor(private val appDataProvider: AppDataProvider, private val appLockHelper: AppLockHelper, private val appLockerPreferences: AppLockerPreferences) : RxAwareViewModel() {
    private var appDataFullList: List<AppLockItemBaseViewState> = ArrayList()
    private val appDataViewStateListLiveData = MutableLiveData<List<AppLockItemBaseViewState>>()
    private val nameLiveData = MutableLiveData<String>()
    private var configurationModel: ConfigurationModel? = null
    private var mId: Int = ConstantDB.CONFIGURATION_ID_CREATE

    fun setId(id: Int) {
        mId = id
    }

    fun getNameLiveData(): LiveData<String> = nameLiveData

    fun getAppDataListLiveData(): LiveData<List<AppLockItemBaseViewState>> = appDataViewStateListLiveData

    fun lockApp(appLockItemViewState: AppLockItemItemViewState, onLockListener: OnLockListener) {
        disposables += doOnBackground {
            configurationModel?.let {
                if (isIdDefault()) {
                    it.addPackageAppLock(appLockItemViewState.appData.parsePackageName())
                } else {
                    it.addPackageAppLockTemp(appLockItemViewState.appData.parsePackageName())
                }
                val appDataViewStateList = appDataViewStateListLiveData.value
                appDataViewStateList?.forEachIndexed { index, appLockItemViewStateNew ->
                    if (appLockItemViewStateNew is AppLockItemHeaderViewState) {
                        // nothing
                    } else if (appLockItemViewStateNew is AppLockItemItemViewState) {
                        if (TextUtils.equals(appLockItemViewStateNew.appData.parsePackageName(), appLockItemViewState.appData.parsePackageName())) {
                            appLockItemViewStateNew.isLocked = true
                            onLockListener.onLockListener(true, index)
                        }
                    }
                }
                appDataViewStateListLiveData.postValue(appDataViewStateList)
            }
        }
    }

    fun unlockApp(appLockItemViewState: AppLockItemItemViewState, onLockListener: OnLockListener) {
        disposables += doOnBackground {
            configurationModel?.let {
                if (isIdDefault()) {
                    it.removePackageAppLock(appLockItemViewState.appData.parsePackageName())
                } else {
                    it.removePackageAppLockTemp(appLockItemViewState.appData.parsePackageName())
                }
                it.removePackageAppLock(appLockItemViewState.appData.parsePackageName())
                val appDataViewStateList = appDataViewStateListLiveData.value
                appDataViewStateList?.forEachIndexed { index, appLockItemViewStateNew ->
                    if (appLockItemViewStateNew is AppLockItemHeaderViewState) {
                        // nothing
                    } else if (appLockItemViewStateNew is AppLockItemItemViewState) {
                        if (TextUtils.equals(appLockItemViewStateNew.appData.parsePackageName(), appLockItemViewState.appData.parsePackageName())) {
                            appLockItemViewStateNew.isLocked = false
                            onLockListener.onLockListener(false, index)
                        }
                    }
                }
                appDataViewStateListLiveData.postValue(appDataViewStateList)
            }
        }
    }

    fun loadConfiguration() {
        doOnBackground {
            configurationModel = getConfiguration()
            configurationModel?.let {
                val name = if (TextUtils.isEmpty(it.name)) {
                    nameDefault
                } else {
                    it.name
                }
                nameLiveData.postValue(name)
            }
            val installedAppsObservable = appDataProvider.fetchInstalledAppList().toObservable()
            installedAppsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                val appDataViewStateList = mutableListOf<AppLockItemItemViewState>()
                it.forEach { appData ->
                    val lockApp: Boolean = if (isIdDefault()) {
                        if (TextUtils.equals(appData.parsePackageName(), Const.CH_PLAY_PACKAGE) || TextUtils.equals(appData.parsePackageName(), Const.SETTINGS_PACKAGE)) {
                            true
                        } else {
                            configurationModel?.packageAppLock?.contains(appData.parsePackageName())
                                    ?: false
                        }
                    } else {
                        if (TextUtils.equals(appData.parsePackageName(), Const.CH_PLAY_PACKAGE) || TextUtils.equals(appData.parsePackageName(), Const.SETTINGS_PACKAGE)) {
                            true
                        } else {
                            configurationModel?.packageAppLockTemp?.contains(appData.parsePackageName())
                                    ?: false
                        }
                    }
                    if (!lockApp) {
                        appDataViewStateList.add(AppLockItemItemViewState(appData, lockApp))
                    }
                }
                appDataFullList = AddSectionHeaderViewStateFunction(appDataProvider.context).applyV2(appDataViewStateList)
                appDataViewStateListLiveData.postValue(appDataFullList)
            }
        }
    }

    private fun getConfiguration(): ConfigurationModel? {
        return if (isIdDefault()) {
            ConfigurationModel.createEmpty()
        } else {
            appLockHelper.getConfiguration(mId)
        }
    }

    fun saveConfiguration(name: String): Boolean {
        return configurationModel?.let {
            it.name = name
            if (it.id == ConstantDB.CONFIGURATION_ID_CREATE) {
                appLockHelper.addConfiguration(it)
            } else {
                appLockHelper.updateConfiguration(it)
            }
        } ?: false
    }

    fun hasAppLock(context: Context): Boolean {
        configurationModel?.let {
            var hasAppLock = false
            if (isIdDefault()) {
                it.getPackageAppLockList().forEach { packageAppLock ->
                    try {
                        context.packageManager.getApplicationIcon(packageAppLock)
                        hasAppLock = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                it.getPackageAppLockTempList().forEach { packageAppLock ->
                    try {
                        context.packageManager.getApplicationIcon(packageAppLock)
                        hasAppLock = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return hasAppLock
        }
        return false
    }

    fun isIdDefault(): Boolean {
        return mId == ConstantDB.CONFIGURATION_ID_CREATE
    }

    fun search(searchData: String) {
        val appList = appDataFullList.filter {
            when (it) {
                is AppLockItemHeaderViewState -> {
                    true
                }
                is AppLockItemItemViewState -> {
                    VNCharacterUtils.removeAccent(it.appName().toLowerCase(Locale.getDefault())).contains(VNCharacterUtils.removeAccent(searchData.toLowerCase(Locale.getDefault())))
                }
                else -> {
                    true
                }
            }
        }
        var numberAll = 0
        var numberRecommended = 0
        appList.forEach {
            when (it) {
                is AppLockItemHeaderViewState -> {
                    // nothing
                }
                is AppLockItemItemViewState -> {
                    if (AddSectionHeaderViewStateFunction(appDataProvider.context).containsRecommended(it.appData.parsePackageName())) {
                        numberRecommended++
                    } else {
                        numberAll++
                    }
                }
            }
        }
        val appListNew = appList.filter {
            when (it) {
                is AppLockItemHeaderViewState -> {
                    if (it.headerTextResource == R.string.text_sensitive_applications) {
                        numberRecommended != 0
                    } else {
                        numberAll != 0
                    }
                }
                is AppLockItemItemViewState -> {
                    true
                }
                else -> {
                    true
                }
            }
        }
        appDataViewStateListLiveData.postValue(appListNew)
    }

    fun getCountSelected(): Int {
        var count = 0
        appDataFullList.forEach {
            if (it is AppLockItemItemViewState) {
                if (it.isLocked) {
                    count++
                }
            }
        }
        return count
    }

    companion object {
        const val nameDefault = "Group 1"
    }
}