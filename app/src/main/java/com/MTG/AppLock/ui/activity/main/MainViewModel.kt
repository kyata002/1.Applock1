package com.MTG.AppLock.ui.activity.main

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.MTG.AppLock.data.DataApp.AppDataProvider
import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.data.sqllite.model.ConfigurationModel
import com.MTG.AppLock.ui.activity.intruders.IntrudersPhotoItemViewState
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.extensions.plusAssign
import com.MTG.AppLock.util.file.FileExtension
import com.MTG.AppLock.util.file.FileManager
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject constructor(private val fileManager: FileManager, private val appLockerPreferences: AppLockerPreferences, private val appLockHelper: AppLockHelper, private val appDataProvider: AppDataProvider) : RxAwareViewModel() {
    private val intruderPhotoLiveData = MutableLiveData<IntrudersPhotoItemViewState>()

    init {
        if (isShowIntruderDialog()) {
            loadIntruderPhotos()
        }
    }

    fun getIntrudersCatcherEnabled(): Boolean {
        return appLockerPreferences.getIntrudersCatcherEnabled()
    }

    fun useThemeDownload(): Boolean {
        return !TextUtils.isEmpty(appLockHelper.getThemeDefault()?.backgroundDownload)
    }

    fun getIntruderPhotoLiveData(): LiveData<IntrudersPhotoItemViewState> = intruderPhotoLiveData

    private fun isShowIntruderDialog(): Boolean {
        return appLockerPreferences.getIntrudersCatcherEnabled() && appLockerPreferences.isShowIntruderDialog()
    }

    fun setShowIntruderDialog(isShowIntruderDialog: Boolean) {
        appLockerPreferences.setShowIntruderDialog(isShowIntruderDialog)
    }

    private fun loadIntruderPhotos() {
        val subFilesObservable = Single.create<List<File>> {
            val subFiles = fileManager.getSubFiles(fileManager.getExternalDirectory(FileManager.SubFolder.INTRUDERS), FileExtension.JPEG)
            it.onSuccess(subFiles)
        }
        disposables += subFilesObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ files ->
            val fileNews = files.sortedByDescending {
                it.lastModified()
            }
            if (!fileNews.isNullOrEmpty()) {
                compileName(fileNews[0])
            }
        }, { error -> Log.v("TAG1", "error : ${error.message}") })
    }

    private fun compileName(file: File) {
        try {
            appLockHelper.getIntruderPackageList().forEach { intruderPackage ->
                if (TextUtils.equals(file.absolutePath, intruderPackage.path)) {
                    val formatDay = SimpleDateFormat(Const.DATE_FORMAT1, Locale.getDefault())
                    val formatTime = SimpleDateFormat(Const.DATE_FORMAT2, Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = intruderPackage.time.toLong()
                    val day = formatDay.format(calendar.time)
                    val time = formatTime.format(calendar.time)
                    val packageApp = intruderPackage.intruderPackage
                    val intrudersPhotoItemViewState = IntrudersPhotoItemViewState(file)
                    intrudersPhotoItemViewState.day = day
                    intrudersPhotoItemViewState.time = time
                    intrudersPhotoItemViewState.packageApp = packageApp
                    intruderPhotoLiveData.postValue(intrudersPhotoItemViewState)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setReload(isReload: Boolean) {
        appLockerPreferences.setReload(isReload)
    }

    fun setRating(isRating: Boolean) {
        appLockerPreferences.setRating(isRating)
    }

    fun setTimeRateFirst(timeRateFirst: Long) {
        appLockerPreferences.setTimeRateFirst(timeRateFirst)
    }

    fun checkShowRateAfterLater(): Boolean {
        return appLockerPreferences.checkShowRateAfterLater()
    }

    fun isRating(): Boolean {
        return appLockerPreferences.isRating()
    }

    fun setLockSettingApp(lockSettingApp: Int) {
        appLockerPreferences.setLockSettingApp(lockSettingApp)
    }

    fun scanFileAndDelete(context: Context) {
        val pathList = appLockHelper.getScanFileList()
        try {
            MediaScannerConnection.scanFile(context, pathList.toTypedArray(), null) { _: String?, _: Uri? -> }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        appLockHelper.deleteScanFile()
    }

    fun getConfigurationActiveList(): MutableList<ConfigurationModel> {
        return appLockHelper.getConfigurationActiveList()
    }

    fun removePackageName(packageName: String) {
        appLockHelper.removePackageName(packageName)
    }

    fun hasSuperPassword(): Boolean {
        return !TextUtils.isEmpty(appLockerPreferences.getSuperPassword())
    }
}