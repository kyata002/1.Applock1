package com.MTG.AppLock.ui.activity.intruders

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.data.sqllite.model.IntruderPhoto
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.extensions.plusAssign
import com.MTG.AppLock.util.file.EncryptionFileManager
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

class IntrudersPhotosViewModel @Inject constructor(private val fileManager: FileManager, private val preferences: AppLockerPreferences, private val appLockHelper: AppLockHelper, val appLockerPreferences: AppLockerPreferences) : RxAwareViewModel() {
    private val mIntruderListViewState = MutableLiveData<IntrudersViewState>()

    init {
        loadIntruderPhotos()
    }

    fun getIntruderListViewState(): LiveData<IntrudersViewState> = mIntruderListViewState

    private fun loadIntruderPhotos() {
        val subFilesObservable = Single.create<List<File>> {
            val subFiles = fileManager.getSubFiles(
                    fileManager.getExternalDirectory(FileManager.SubFolder.INTRUDERS), FileExtension.JPEG
            )
            it.onSuccess(subFiles)
        }
        disposables += subFilesObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ files ->
            mIntruderListViewState.postValue(IntrudersViewState(mapToViewState(files), appLockHelper.getIntruderPackageList()))
        }, { })
    }

    private fun mapToViewState(files: List<File>): MutableList<IntrudersPhotoItemViewState> {
        val viewStateList = arrayListOf<IntrudersPhotoItemViewState>()
        files.forEach { viewStateList.add((IntrudersPhotoItemViewState(it))) }
        //
        viewStateList.sortWith { photo1, photo2 ->
            if (TextUtils.isEmpty(photo1.time) || TextUtils.isEmpty(photo2.time)) {
                var name1 = File(photo1.filePath).name
                var name2 = File(photo2.filePath).name
                if (name1.startsWith(".IMG_")) {
                    val indexStart = name1.indexOf(".IMG_") + ".IMG_".length
                    val indexEnd = name1.indexOf(FileExtension.JPEG.extension)
                    name1 = name1.substring(indexStart, indexEnd)
                } else if (name1.startsWith("IMG_")) {
                    val indexStart = name1.indexOf("IMG_") + "IMG_".length
                    val indexEnd = name1.indexOf(FileExtension.JPEG.extension)
                    name1 = name1.substring(indexStart, indexEnd)
                }
                //
                if (name2.startsWith(".IMG_")) {
                    val indexStart = name2.indexOf(".IMG_") + ".IMG_".length
                    val indexEnd = name2.indexOf(FileExtension.JPEG.extension)
                    name2 = name2.substring(indexStart, indexEnd)
                } else if (name2.startsWith("IMG_")) {
                    val indexStart = name2.indexOf("IMG_") + "IMG_".length
                    val indexEnd = name2.indexOf(FileExtension.JPEG.extension)
                    name2 = name2.substring(indexStart, indexEnd)
                }
                name1.compareTo(name2)
            } else {
                photo1.time.compareTo(photo2.time)
            }
        }
        return viewStateList.asReversed()
    }

    fun setIntrudersCatcherEnable(intrudersCatcherEnabled: Boolean) {
        preferences.setIntrudersCatcherEnable(intrudersCatcherEnabled)
    }

    fun getIntrudersCatcherEnabled(): Boolean {
        return preferences.getIntrudersCatcherEnabled()
    }

    fun deleteAll() {
        // xóa data ở trong database
        appLockHelper.deleteAllIntruder()
        // xóa hết thư mục ảnh chụp kẻ trộm
        EncryptionFileManager.deleteAllIntruderFolder()
        //
        val intrudersViewState = mIntruderListViewState.value as IntrudersViewState
        intrudersViewState.intruderPackageList.clear()
        intrudersViewState.intrudersPhotoItemViewStateList.clear()
        mIntruderListViewState.postValue(intrudersViewState)
    }

    fun deletePath(path: String) {
        // xóa data ở trong database
        appLockHelper.deleteIntruder(path)
        //
        val intrudersViewState = mIntruderListViewState.value as IntrudersViewState
        //
        val deleteFailedList = mutableListOf<IntrudersPhotoItemViewState>()
        val deleteFailedIntruderPhotoList = mutableListOf<IntruderPhoto>()
        intrudersViewState.intrudersPhotoItemViewStateList.forEach {
            if (!TextUtils.equals(path, it.filePath)) {
                deleteFailedList.add(it)
            }
        }
        intrudersViewState.intruderPackageList.forEach { intruderPackage ->
            deleteFailedList.forEach { deleteFailed ->
                if (TextUtils.equals(intruderPackage.path, deleteFailed.filePath)) {
                    deleteFailedIntruderPhotoList.add(intruderPackage)
                }
            }
        }
        if (deleteFailedList.size != intrudersViewState.intrudersPhotoItemViewStateList.size) {
            intrudersViewState.intrudersPhotoItemViewStateList.clear()
            // cập nhật lại số lượng chỉ nhừng file xóa lỗi nếu có
            intrudersViewState.intrudersPhotoItemViewStateList.addAll(deleteFailedList)
            //
            intrudersViewState.intruderPackageList.clear()
            intrudersViewState.intruderPackageList.addAll(deleteFailedIntruderPhotoList)
            mIntruderListViewState.postValue(intrudersViewState)
        }
    }

    fun deleteFile(context: Context, path: String): Boolean {
        val file = File(path)
        val success = file.delete()
        MediaScannerConnection.scanFile(context, arrayOf(path), null) { _: String?, _: Uri? -> }
        return success
    }

    fun compileName() {
        val intrudersViewState = mIntruderListViewState.value as IntrudersViewState
//        Log.d("TAG1", "size = " + intrudersViewState.intrudersPhotoItemViewStateList.size)
        intrudersViewState.intrudersPhotoItemViewStateList.forEach { intruderPhotoItemViewState ->
            try {
                val path = intruderPhotoItemViewState.filePath
                val file = File(path)
                val name = file.name
                if (name.startsWith(".IMG_") && name.contains(FileExtension.JPEG.extension)) {
                    val indexStart = name.indexOf(".IMG_") + ".IMG_".length
                    var indexEnd = name.indexOf(FileExtension.JPEG.extension)
                    var time = name.substring(indexStart, indexEnd)
                    val formatDay1 = SimpleDateFormat(Const.DATE_FORMAT1, Locale.getDefault())
                    val formatTime1 = SimpleDateFormat(Const.DATE_FORMAT2, Locale.getDefault())
                    val calendar1 = Calendar.getInstance()
                    while (time.contains("(") || time.contains(")")) {
                        indexEnd = name.indexOf("(")
                        time = name.substring(indexStart, indexEnd)
                    }
                    calendar1.timeInMillis = time.toLong()
                    intruderPhotoItemViewState.day = formatDay1.format(calendar1.time)
                    intruderPhotoItemViewState.time = formatTime1.format(calendar1.time)
                }
                //
                if (name.startsWith("IMG_") && name.contains(FileExtension.JPEG.extension)) {
                    val indexStart = name.indexOf("IMG_") + "IMG_".length
                    var indexEnd = name.indexOf(FileExtension.JPEG.extension)
                    var time = name.substring(indexStart, indexEnd)
                    val formatDay1 = SimpleDateFormat(Const.DATE_FORMAT1, Locale.getDefault())
                    val formatTime1 = SimpleDateFormat(Const.DATE_FORMAT2, Locale.getDefault())
                    val calendar1 = Calendar.getInstance()
                    while (time.contains("(") || time.contains(")")) {
                        indexEnd = name.indexOf("(")
                        time = name.substring(indexStart, indexEnd)
                    }
                    calendar1.timeInMillis = time.toLong()
                    intruderPhotoItemViewState.day = formatDay1.format(calendar1.time)
                    intruderPhotoItemViewState.time = formatTime1.format(calendar1.time)
                }
                //
                intrudersViewState.intruderPackageList.forEach { intruderPackage ->
                    if (TextUtils.equals(intruderPhotoItemViewState.filePath, intruderPackage.path)) {
                        val formatDay = SimpleDateFormat(Const.DATE_FORMAT1, Locale.getDefault())
                        val formatTime = SimpleDateFormat(Const.DATE_FORMAT2, Locale.getDefault())
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = intruderPackage.time.toLong()
                        intruderPhotoItemViewState.id = intruderPackage.id
                        intruderPhotoItemViewState.day = formatDay.format(calendar.time)
                        intruderPhotoItemViewState.time = formatTime.format(calendar.time)
                        intruderPhotoItemViewState.packageApp = intruderPackage.intruderPackage
                        intruderPhotoItemViewState.intruderTime = intruderPackage.time
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setReload(isReload: Boolean) {
        appLockerPreferences.setReload(isReload)
    }

    fun updateIntruder(intruderPhoto: IntruderPhoto): Boolean {
        return appLockHelper.updateIntruder(intruderPhoto)
    }
}