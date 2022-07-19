package com.MTG.AppLock.ui.activity.main.settings.theme.selected

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.MTG.AppLock.model.Album
import com.MTG.AppLock.model.ItemDetail
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.LoadDataUtils
import java.io.File
import javax.inject.Inject

class SelectedImageViewModel @Inject constructor() : RxAwareViewModel() {
    private val mAlbumListLiveData = MutableLiveData<MutableList<Album>>()
    private val mDetailListLiveData = MutableLiveData<MutableList<ItemDetail>>()
    private val mDetailWithAlbumListLiveData = MutableLiveData<MutableList<ItemDetail>>()

    fun loadAlbumWithType(context: Context) {
        val albumList = mutableListOf<Album>()
        albumList.addAll(LoadDataUtils.Image.findImageAlbumsSub(context = context))
        if (albumList.isNullOrEmpty()) {
            mAlbumListLiveData.postValue(mutableListOf())
        } else {
            mAlbumListLiveData.postValue(albumList)
        }
    }

    fun loadDetailWithType(context: Context) {
        val itemDetailList = mutableListOf<ItemDetail>()
        itemDetailList.addAll(LoadDataUtils.Image.loadFullImageSub(context = context))
        if (itemDetailList.isNullOrEmpty()) {
            mDetailListLiveData.postValue(mutableListOf())
        } else {
            mDetailListLiveData.postValue(itemDetailList)
        }
    }

    fun loadDetailWithAlbum(albumPath: String) {
        val detailList: MutableList<ItemDetail>? = mDetailListLiveData.value
        val detailWithAlbumList = mutableListOf<ItemDetail>()
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
        if (detailWithAlbumList.isNullOrEmpty()) {
            mDetailWithAlbumListLiveData.postValue(mutableListOf())
        } else {
            mDetailWithAlbumListLiveData.postValue(detailWithAlbumList)
        }
    }

    fun getAlbumListLiveData(): LiveData<MutableList<Album>> = mAlbumListLiveData
    fun getDetailWithAlbumListLiveData(): LiveData<MutableList<ItemDetail>> = mDetailWithAlbumListLiveData
}
