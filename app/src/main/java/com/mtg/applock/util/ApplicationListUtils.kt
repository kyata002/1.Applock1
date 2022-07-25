package com.mtg.applock.util

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mtg.applock.R
import com.mtg.applock.data.dataapp.AppDataProvider
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.data.sqllite.ConstantDB
import com.mtg.applock.ui.activity.main.az.function.AddSectionHeaderViewStateFunction
import com.mtg.applock.ui.activity.main.az.model.AppLockItemBaseViewState
import com.mtg.applock.ui.activity.main.az.model.AppLockItemDataViewState
import com.mtg.applock.ui.activity.main.az.model.AppLockItemHeaderViewState
import com.mtg.applock.ui.activity.main.az.model.AppLockItemItemViewState
import com.mtg.applock.util.extensions.doOnBackground
import com.mtg.applock.util.preferences.AppLockerPreferences
import com.mtg.applock.util.text.VNCharacterUtils
import com.mtg.applock.util.uninstall.UninstallUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class ApplicationListUtils private constructor() {
    private var appDataFullList: MutableList<AppLockItemBaseViewState> = mutableListOf()
    private val appDataLockedViewStateListLiveData = MutableLiveData<List<AppLockItemBaseViewState>>()

    private val appDataFullViewStateLiveData = MutableLiveData<AppLockItemDataViewState>()

    fun getAppDataLockedListLiveData(): LiveData<List<AppLockItemBaseViewState>> = appDataLockedViewStateListLiveData

    fun getAppDataFullLiveData(): LiveData<AppLockItemDataViewState> = appDataFullViewStateLiveData

    private fun setAppDataLockList(appDataFullList: List<AppLockItemBaseViewState>) {
        this.appDataFullList.clear()
        this.appDataFullList.addAll(appDataFullList)
    }

    fun loadDataLocked() {
        val appDataList = mutableListOf<AppLockItemBaseViewState>()
        appDataFullList.forEach {
            if (it is AppLockItemItemViewState) {
                if (it.isLocked) {
                    appDataList.add(it)
                }
            }
        }
        appDataLockedViewStateListLiveData.postValue(appDataList)
    }

    fun loadDataFull() {
        appDataFullViewStateLiveData.postValue(AppLockItemDataViewState(appDataFullList, false))
    }

    fun reload(context: Context, onAppDataListener: OnAppDataListener?) {
        doOnBackground {
            val appLockHelper = AppLockHelper(context)
            val appDataProvider = AppDataProvider(context, appLockHelper, AppLockerPreferences(context))
            val configurationModel = appLockHelper.getConfiguration(ConstantDB.CONFIGURATION_ID_DEFAULT)
            // configuration active list
            val configurationActiveList = appLockHelper.getConfigurationActiveList()
            if (CommonUtils.enableSettings(context)) {
                configurationModel?.let {
                    if (!it.getPackageAppLockList().contains(Const.SETTINGS_PACKAGE)) {
                        it.addPackageAppLock(Const.SETTINGS_PACKAGE)
                    }
                    if (!it.getPackageAppLockList().contains(Const.CH_PLAY_PACKAGE)) {
                        it.addPackageAppLock(Const.CH_PLAY_PACKAGE)
                    }
                }
            }
            //
            val installedAppsObservable = appDataProvider.fetchInstalledAppList().toObservable()
            installedAppsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                val appDataViewStateList = mutableListOf<AppLockItemItemViewState>()
                it.forEach { appData ->
                    var lockApp = false
                    configurationModel?.getPackageAppLockList()?.forEach { packageAppLock ->
                        if (TextUtils.equals(appData.parsePackageName(), packageAppLock)) {
                            lockApp = true
                        }
                    }
                    configurationActiveList.forEach { configuration ->
                        configuration.getPackageAppLockList().forEach { packageAppLock ->
                            if (TextUtils.equals(appData.parsePackageName(), packageAppLock)) {
                                if (configuration.isCurrentTimeAboutStartEnd()) {
                                    lockApp = true
                                }
                            }
                        }
                    }
                    val hasRemove = !UninstallUtils.isSignedBySystem(context, appData.parsePackageName()) && !UninstallUtils.isAppInSystemPartition(context, appData.parsePackageName())
                    appDataViewStateList.add(AppLockItemItemViewState(appData, lockApp, hasRemove))
                }
                onAppDataListener?.onAppDataFullList()
                onAppDataListener?.onAppDataLockedList()
            }
        }
    }

    fun reload(context: Context) {
        doOnBackground {
            val appLockHelper = AppLockHelper(context)
            val appDataProvider = AppDataProvider(context, appLockHelper, AppLockerPreferences(context))
            val configurationModel = appLockHelper.getConfiguration(ConstantDB.CONFIGURATION_ID_DEFAULT)
            // configuration active list
            val configurationActiveList = appLockHelper.getConfigurationActiveList()
            if (CommonUtils.enableSettings(context)) {
                configurationModel?.let {
                    if (!it.getPackageAppLockList().contains(Const.SETTINGS_PACKAGE)) {
                        it.addPackageAppLock(Const.SETTINGS_PACKAGE)
                    }
                    if (!it.getPackageAppLockList().contains(Const.CH_PLAY_PACKAGE)) {
                        it.addPackageAppLock(Const.CH_PLAY_PACKAGE)
                    }
                }
            }
            //
            val installedAppsObservable = appDataProvider.fetchInstalledAppList().toObservable()
            installedAppsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                val appDataViewStateList = mutableListOf<AppLockItemItemViewState>()
                it.forEach { appData ->
                    var lockApp = false
                    configurationModel?.getPackageAppLockList()?.forEach { packageAppLock ->
                        if (TextUtils.equals(appData.parsePackageName(), packageAppLock)) {
                            lockApp = true
                        }
                    }
                    configurationActiveList.forEach { configuration ->
                        configuration.getPackageAppLockList().forEach { packageAppLock ->
                            if (TextUtils.equals(appData.parsePackageName(), packageAppLock)) {
                                if (configuration.isCurrentTimeAboutStartEnd()) {
                                    lockApp = true
                                }
                            }
                        }
                    }
                    val hasRemove = !UninstallUtils.isSignedBySystem(context, appData.parsePackageName()) && !UninstallUtils.isAppInSystemPartition(context, appData.parsePackageName())
                    appDataViewStateList.add(AppLockItemItemViewState(appData, lockApp, hasRemove))
                }
                setAppDataLockList(AddSectionHeaderViewStateFunction(context).apply(appDataViewStateList))
                loadDataFull()
                loadDataLocked()
            }
        }
    }

    fun removePackageName(packageName: String?) {
        packageName?.let { packageNameRemove ->
            doOnBackground {
                val appLockLockedItemViewStateList = mutableListOf<AppLockItemBaseViewState>()
                val listIterator = appDataFullList.listIterator()
                while (listIterator.hasNext()) {
                    when (val appData = listIterator.next()) {
                        is AppLockItemItemViewState -> {
                            if (TextUtils.equals(appData.appData.parsePackageName(), packageNameRemove)) {
                                listIterator.remove()
                            } else {
                                if (appData.isLocked) {
                                    appLockLockedItemViewStateList.add(appData)
                                }
                            }
                        }
                        else -> {
                            // nothing
                        }
                    }
                }
                appDataFullViewStateLiveData.postValue(AppLockItemDataViewState(appDataFullList, true))
                appDataLockedViewStateListLiveData.postValue(appLockLockedItemViewStateList)
            }
        }
    }

    fun hasLocked(): Boolean {
        return getNumberLock() > 0
    }

    fun canUnlock(context: Context, appLockHelper: AppLockHelper): Boolean {
        val appLockList = mutableListOf<AppLockItemItemViewState>()
        appDataFullList.forEach {
            if (it is AppLockItemItemViewState) {
                if (it.isLocked) {
                    appLockList.add(it)
                }
            }
        }
        var canUnlock = false
        appLockList.forEach {
            if (!appLockHelper.isApplicationGroupActive(it.appData.parsePackageName())) {
                if (CommonUtils.enableSettings(context)) {
                    if (!TextUtils.equals(it.appData.parsePackageName(), Const.SETTINGS_PACKAGE) && !TextUtils.equals(it.appData.parsePackageName(), Const.CH_PLAY_PACKAGE)) {
                        canUnlock = true
                    }
                } else {
                    canUnlock = true
                }
                return canUnlock
            }
        }
        return canUnlock
    }

    fun search(context: Context, searchData: String) {
        val appList: List<AppLockItemBaseViewState> = appDataFullList.filter {
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
                    if (AddSectionHeaderViewStateFunction(context).containsRecommended(it.appData.parsePackageName())) {
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
        // full
        appDataFullViewStateLiveData.postValue((AppLockItemDataViewState(appListNew, false)))
        // locked
        val appLockLockedItemViewStateList = mutableListOf<AppLockItemBaseViewState>()
        appListNew.forEach { appData ->
            when (appData) {
                is AppLockItemItemViewState -> {
                    if (appData.isLocked) {
                        appLockLockedItemViewStateList.add(appData)
                    }
                }
                else -> {
                    // nothing
                }
            }
        }
        appDataLockedViewStateListLiveData.postValue(appLockLockedItemViewStateList)
    }

    fun lockApp(context: Context, appLockItemViewState: AppLockItemItemViewState) {
        doOnBackground {
            val appLockHelper = AppLockHelper(context)
            val configurationModel = appLockHelper.getConfiguration(ConstantDB.CONFIGURATION_ID_DEFAULT)
            configurationModel?.let {
                it.addPackageAppLock(appLockItemViewState.appData.parsePackageName())
                appLockHelper.updateConfiguration(it)
                var appLockItemItemViewStateSettings: AppLockItemItemViewState? = null
                var appLockItemItemViewStateChPlay: AppLockItemItemViewState? = null
                val isSettings = TextUtils.equals(appLockItemViewState.appData.parsePackageName(), Const.SETTINGS_PACKAGE)
                val isChPlay = TextUtils.equals(appLockItemViewState.appData.parsePackageName(), Const.CH_PLAY_PACKAGE)
                appDataFullList.forEachIndexed { _, appLockItemViewStateNew ->
                    if (appLockItemViewStateNew is AppLockItemHeaderViewState) {
                        // nothing
                    } else if (appLockItemViewStateNew is AppLockItemItemViewState) {
                        if (TextUtils.equals(appLockItemViewStateNew.appData.parsePackageName(), Const.SETTINGS_PACKAGE)) {
                            appLockItemItemViewStateSettings = appLockItemViewStateNew
                        } else if (TextUtils.equals(appLockItemViewStateNew.appData.parsePackageName(), Const.CH_PLAY_PACKAGE)) {
                            appLockItemItemViewStateChPlay = appLockItemViewStateNew
                        }
                        if (TextUtils.equals(appLockItemViewStateNew.appData.parsePackageName(), appLockItemViewState.appData.parsePackageName())) {
                            if (!appLockHelper.isApplicationGroupActive(appLockItemViewStateNew.appData.parsePackageName())) {
                                appLockItemViewStateNew.isLocked = true
                            }
                        }
                    }
                }
                var updateLockSetting = false
                appLockItemItemViewStateSettings?.let { appItem ->
                    if (appItem.isLocked) {
                        // nothing
                    } else {
                        if (isSettings) {
                            // nothing
                        } else {
                            appItem.isLocked = getNumberLock() == 1
                            if (appItem.isLocked) {
                                it.addPackageAppLock(appItem.appData.parsePackageName())
                                appLockHelper.updateConfiguration(it)
                                updateLockSetting = true
                            }
                        }
                    }
                }
                appLockItemItemViewStateChPlay?.let { appItem ->
                    if (appItem.isLocked) {
                        // nothing
                    } else {
                        if (isChPlay) {
                            // nothing
                        } else {
                            if (CommonUtils.isAdminActive(context)) {
                                appItem.isLocked = getNumberLock() == 2
                            } else {
                                if (updateLockSetting) {
                                    appItem.isLocked = getNumberLock() == 2
                                } else {
                                    appItem.isLocked = getNumberLock() == 1
                                }
                            }
                            if (appItem.isLocked) {
                                it.addPackageAppLock(appItem.appData.parsePackageName())
                                appLockHelper.updateConfiguration(it)
                            }
                        }
                    }
                }
                // full
                val appLockFullItemViewStateList = appDataFullViewStateLiveData.value?.appDataList
                        ?: mutableListOf()
                appLockFullItemViewStateList.forEach { appLockItemViewState ->
                    appDataFullList.forEach { appData ->
                        if (appLockItemViewState is AppLockItemItemViewState && appData is AppLockItemItemViewState && TextUtils.equals(appLockItemViewState.appData.parsePackageName(), appData.appData.parsePackageName())) {
                            appLockItemViewState.isLocked = appData.isLocked
                        }
                    }
                }
                appDataFullViewStateLiveData.postValue((AppLockItemDataViewState(appLockFullItemViewStateList, false)))
                // locked
                val appLockLockedItemViewStateList = mutableListOf<AppLockItemBaseViewState>()
                appDataFullList.forEach { appData ->
                    when (appData) {
                        is AppLockItemItemViewState -> {
                            if (appData.isLocked) {
                                appLockLockedItemViewStateList.add(appData)
                            }
                        }
                        else -> {
                            // nothing
                        }
                    }
                }
                appDataLockedViewStateListLiveData.postValue(appLockLockedItemViewStateList)
            }
        }
    }

    fun unlockApp(context: Context, appLockItemViewState: AppLockItemItemViewState) {
        doOnBackground {
            val appLockHelper = AppLockHelper(context)
            val configurationModel = appLockHelper.getConfiguration(ConstantDB.CONFIGURATION_ID_DEFAULT)
            configurationModel?.let {
                it.removePackageAppLock(appLockItemViewState.appData.parsePackageName())
                appLockHelper.updateConfiguration(it)
                val appDataViewStateList = appDataFullViewStateLiveData.value?.appDataList
                        ?: mutableListOf()
                appDataViewStateList.forEachIndexed { _, appLockItemViewStateNew ->
                    if (appLockItemViewStateNew is AppLockItemHeaderViewState) {
                        // nothing
                    } else if (appLockItemViewStateNew is AppLockItemItemViewState) {
                        if (TextUtils.equals(appLockItemViewStateNew.appData.parsePackageName(), appLockItemViewState.appData.parsePackageName())) {
                            if (!appLockHelper.isApplicationGroupActive(appLockItemViewStateNew.appData.parsePackageName())) {
                                appLockItemViewStateNew.isLocked = false
                            }
                        }
                    }
                }
                appDataFullViewStateLiveData.postValue((AppLockItemDataViewState(appDataViewStateList, false)))
                // locked
                val appLockLockedItemViewStateList = mutableListOf<AppLockItemBaseViewState>()
                appDataFullList.forEach { appData ->
                    when (appData) {
                        is AppLockItemItemViewState -> {
                            if (appData.isLocked) {
                                appLockLockedItemViewStateList.add(appData)
                            }
                        }
                        else -> {
                            // nothing
                        }
                    }
                }
                appDataLockedViewStateListLiveData.postValue(appLockLockedItemViewStateList)
            }
        }
    }

    fun unlockAll(context: Context) {
        doOnBackground {
            val unlockList = mutableListOf<AppLockItemItemViewState>()
            appDataLockedViewStateListLiveData.value?.forEach {
                if (it is AppLockItemItemViewState) {
                    unlockList.add(it)
                }
            }
            val appLockHelper = AppLockHelper(context)
            val configurationModel = appLockHelper.getConfiguration(ConstantDB.CONFIGURATION_ID_DEFAULT)
            val appDataViewStateList = appDataFullViewStateLiveData.value?.appDataList
                    ?: mutableListOf()
            configurationModel?.let { configuration ->
                unlockList.forEach { appLockItemViewState ->
                    configuration.removePackageAppLock(appLockItemViewState.appData.parsePackageName())
                    appLockHelper.updateConfiguration(configuration)
                    appDataViewStateList.forEachIndexed { _, appLockItemViewStateNew ->
                        if (appLockItemViewStateNew is AppLockItemHeaderViewState) {
                            // nothing
                        } else if (appLockItemViewStateNew is AppLockItemItemViewState) {
                            if (TextUtils.equals(appLockItemViewStateNew.appData.parsePackageName(), appLockItemViewState.appData.parsePackageName())) {
                                if (!appLockHelper.isApplicationGroupActive(appLockItemViewStateNew.appData.parsePackageName())) {
                                    appLockItemViewStateNew.isLocked = false
                                }
                            }
                        }
                    }
                }
                val configurationActiveList = appLockHelper.getConfigurationActiveList()
                if (configurationActiveList.isNullOrEmpty()) {
                    appDataFullViewStateLiveData.postValue(AppLockItemDataViewState(appDataViewStateList, false))
                    appDataLockedViewStateListLiveData.postValue(mutableListOf())
                } else {
                    reload(context)
                }
            }
        }
    }

    private fun getNumberLock(): Int {
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


    fun destroy() {
        singleton = null
    }

    companion object {
        private var singleton: ApplicationListUtils? = null

        val instance: ApplicationListUtils?
            get() {
                if (singleton == null) {
                    singleton = ApplicationListUtils()
                }
                return singleton
            }
    }

    interface OnAppDataListener {
        fun onAppDataFullList(): MutableList<AppLockItemBaseViewState>

        fun onAppDataFullSearchList(): MutableList<AppLockItemBaseViewState>

        fun onAppDataLockedList(): MutableList<AppLockItemBaseViewState>
    }
}