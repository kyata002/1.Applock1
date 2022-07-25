package com.mtg.applock.ui.activity.detail

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mtg.applock.R
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.model.ItemDetail
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.Const
import com.mtg.applock.util.file.EncryptionFileManager
import com.mtg.applock.util.preferences.AppLockerPreferences
import javax.inject.Inject

class DetailListViewModel @Inject constructor(val appLockHelper: AppLockHelper, private val appLockerPreferences: AppLockerPreferences) : RxAwareViewModel() {
    private val mDetailListLiveData = MutableLiveData<MutableList<ItemDetail>>()

    private val mProgressLiveData = MutableLiveData<Int>()

    fun getProgressLiveData(): LiveData<Int> = mProgressLiveData

    fun loadData(pathFolder: String?, type: Int, extension: String) {
        when (type) {
            Const.TYPE_FILES -> {
                val detailList = EncryptionFileManager.getListFileWithType(
                        EncryptionFileManager.getFolderWithType(type).absolutePath,
                        type = type,
                        isDecode = true,
                        extension = extension,
                        allHide = true,
                        object : EncryptionFileManager.ProgressCallback {
                            override fun progress(progress: Int) {
                                mProgressLiveData.postValue(progress)
                            }
                        })
                if (detailList.isNullOrEmpty()) {
                    mDetailListLiveData.postValue(mutableListOf())
                } else {
                    mDetailListLiveData.postValue(detailList)
                }
            }
            else -> {
                pathFolder?.let {
                    val albumList = EncryptionFileManager.getListFileWithType(
                            pathFolder = it,
                            type = type,
                            isDecode = true,
                            extension = extension,
                            allHide = true,
                            object : EncryptionFileManager.ProgressCallback {
                                override fun progress(progress: Int) {
                                    mProgressLiveData.postValue(progress)
                                }
                            })
                    if (albumList.isNullOrEmpty()) {
                        mDetailListLiveData.postValue(mutableListOf())
                    } else {
                        mDetailListLiveData.postValue(albumList)
                    }
                }
            }
        }
    }

    fun getDetailListLiveData(): LiveData<MutableList<ItemDetail>> = mDetailListLiveData

    fun getMessage(context: Context, type: Int): String {
        when (type) {
            Const.TYPE_IMAGES -> {
                return context.getString(R.string.msg_images_unlock)
            }
            Const.TYPE_VIDEOS -> {
                return context.getString(R.string.msg_videos_unlock)
            }
            Const.TYPE_AUDIOS -> {
                return context.getString(R.string.msg_audios_unlock)
            }
            Const.TYPE_FILES -> {
                return context.getString(R.string.msg_files_unlock)
            }
            else -> {
                return context.getString(R.string.msg_images_unlock)
            }
        }
    }

    fun getMessageDelete(context: Context, type: Int): String {
        when (type) {
            Const.TYPE_IMAGES -> {
                return context.getString(R.string.msg_images_delete)
            }
            Const.TYPE_VIDEOS -> {
                return context.getString(R.string.msg_videos_delete)
            }
            Const.TYPE_AUDIOS -> {
                return context.getString(R.string.msg_audios_delete)
            }
            Const.TYPE_FILES -> {
                return context.getString(R.string.msg_files_delete)
            }
            else -> {
                return context.getString(R.string.msg_images_delete)
            }
        }
    }

    fun getMessageOne(context: Context, type: Int): String {
        when (type) {
            Const.TYPE_IMAGES -> {
                return context.getString(R.string.msg_images_unlock1)
            }
            Const.TYPE_VIDEOS -> {
                return context.getString(R.string.msg_videos_unlock1)
            }
            Const.TYPE_AUDIOS -> {
                return context.getString(R.string.msg_audios_unlock1)
            }
            Const.TYPE_FILES -> {
                return context.getString(R.string.msg_files_unlock1)
            }
            else -> {
                return context.getString(R.string.msg_images_unlock1)
            }
        }
    }

    fun setReload(setReload: Boolean) {
        appLockerPreferences.setReload(setReload)
    }
}