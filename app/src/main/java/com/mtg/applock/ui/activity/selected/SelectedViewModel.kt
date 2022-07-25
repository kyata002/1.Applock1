package com.mtg.applock.ui.activity.selected

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mtg.applock.model.Album
import com.mtg.applock.model.ItemDetail
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.Const
import com.mtg.applock.util.LoadDataUtils
import com.mtg.applock.util.file.FilePathHelper
import com.mtg.applock.util.file.MediaHelper
import java.io.File
import javax.inject.Inject

class SelectedViewModel @Inject constructor() : RxAwareViewModel() {
    private val mAlbumListLiveData = MutableLiveData<MutableList<Album>>()
    private val mDetailListLiveData = MutableLiveData<MutableList<ItemDetail>>()
    private val mDetailWithAlbumListLiveData = MutableLiveData<MutableList<ItemDetail>>()

    fun getAlbumListLiveData(): LiveData<MutableList<Album>> = mAlbumListLiveData
    fun getDetailWithAlbumListLiveData(): LiveData<MutableList<ItemDetail>> = mDetailWithAlbumListLiveData

    fun loadAlbumWithType(context: Context, type: Int) {
        val albumList = mutableListOf<Album>()
        when (type) {
            Const.TYPE_IMAGES -> {
                albumList.addAll(LoadDataUtils.Image.findImageAlbums(context))
            }
            Const.TYPE_VIDEOS -> {
                albumList.addAll(LoadDataUtils.Video.findVideoAlbums(context))
            }
            Const.TYPE_AUDIOS -> {
                albumList.addAll(LoadDataUtils.Audio.findAudioAlbums(context))
            }
            Const.TYPE_FILES -> {
                albumList.addAll(LoadDataUtils.File.loadFileAlbums())
            }
        }
        if (albumList.isNullOrEmpty()) {
            mAlbumListLiveData.postValue(mutableListOf())
        } else {
            mAlbumListLiveData.postValue(albumList)
        }
    }

    fun loadDetailWithType(context: Context, type: Int) {
        val itemDetailList = mutableListOf<ItemDetail>()
        when (type) {
            Const.TYPE_IMAGES -> {
                itemDetailList.addAll(LoadDataUtils.Image.loadFullImage(context = context))
            }
            Const.TYPE_VIDEOS -> {
                itemDetailList.addAll(LoadDataUtils.Video.loadFullVideo(context = context))
            }
            Const.TYPE_AUDIOS -> {
                itemDetailList.addAll(LoadDataUtils.Audio.loadFullAudio(context = context))
            }
            Const.TYPE_FILES -> {
                itemDetailList.addAll(LoadDataUtils.File.loadFullFile())
            }
        }
        if (itemDetailList.isNullOrEmpty()) {
            mDetailListLiveData.postValue(mutableListOf())
        } else {
            mDetailListLiveData.postValue(itemDetailList)
        }
    }

    fun loadDetailWithAlbum(albumPath: String, type: Int, extension: String) {
        val detailList: MutableList<ItemDetail>? = mDetailListLiveData.value
        val detailWithAlbumList = mutableListOf<ItemDetail>()
        when (type) {
            Const.TYPE_FILES -> {
                detailList?.forEach {
                    try {
                        if (TextUtils.equals(extension, MediaHelper.EXTENSION_OTHER_SUPPORT_FORMAT)) {
                            if (MediaHelper.FILES_SUPPORT_FORMAT_OTHER.contains(FilePathHelper.getExtension(it.path))) {
                                detailWithAlbumList.add(it)
                            }
                        } else {
                            if (TextUtils.equals(FilePathHelper.getExtension(it.path), extension)) {
                                detailWithAlbumList.add(it)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            else -> {
                detailList?.forEach {
                    try {
                        val path = it.path
                        val parentFile = File(path).parentFile
                        if (parentFile != null && parentFile.absolutePath.equals(albumPath, ignoreCase = true)) {
                            detailWithAlbumList.add(it)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        if (detailWithAlbumList.isNullOrEmpty()) {
            mDetailWithAlbumListLiveData.postValue(mutableListOf())
        } else {
            mDetailWithAlbumListLiveData.postValue(detailWithAlbumList)
        }
    }
}
