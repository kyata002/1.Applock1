package com.MTG.AppLock.ui.activity.main.settings.apply

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.model.ThemeModel
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.ThemeUtils
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import javax.inject.Inject

class ApplyThemeViewModel @Inject constructor(private val appLockerPreferences: AppLockerPreferences, private val appLockHelper: AppLockHelper) : RxAwareViewModel() {
    private val mSelectedBitmapLiveData = MutableLiveData<Bitmap?>()
    private val mUnSelectedBitmapLiveData = MutableLiveData<Bitmap?>()

    private val mDownloadBackgroundBitmapLiveData = MutableLiveData<Bitmap?>()
    private val mDownloadThumbnailBitmapLiveData = MutableLiveData<Bitmap?>()
    private val mDownloadSelectedBitmapLiveData = MutableLiveData<Bitmap?>()
    private val mDownloadUnSelectedBitmapLiveData = MutableLiveData<Bitmap?>()
    private val mDownloadButtonBitmapListLiveData = MutableLiveData<MutableList<Bitmap?>>()

    fun getSelectedBitmapLiveData(): LiveData<Bitmap?> = mSelectedBitmapLiveData
    fun getUnSelectedBitmapLiveData(): LiveData<Bitmap?> = mUnSelectedBitmapLiveData

    fun getDownloadBackgroundBitmapLiveData(): LiveData<Bitmap?> = mDownloadBackgroundBitmapLiveData
    fun getDownloadThumbnailBitmapLiveData(): LiveData<Bitmap?> = mDownloadThumbnailBitmapLiveData
    fun getDownloadSelectedBitmapLiveData(): LiveData<Bitmap?> = mDownloadSelectedBitmapLiveData
    fun getDownloadUnSelectedBitmapLiveData(): LiveData<Bitmap?> = mDownloadUnSelectedBitmapLiveData
    fun getDownloadButtonBitmapListLiveData(): LiveData<MutableList<Bitmap?>> = mDownloadButtonBitmapListLiveData

    fun loadSelectedBitmap(context: Context, selectedUrl: String) {
        Glide.with(context).asBitmap().load(selectedUrl).apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)).addListener(object : RequestListener<Bitmap?> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                mSelectedBitmapLiveData.postValue(null)
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                mSelectedBitmapLiveData.postValue(resource)
                return false
            }
        }).submit()
    }

    fun loadUnSelectedBitmap(context: Context, unSelectedUrl: String) {
        Glide.with(context).asBitmap().load(unSelectedUrl).apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)).addListener(object : RequestListener<Bitmap?> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                mUnSelectedBitmapLiveData.postValue(null)
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                mUnSelectedBitmapLiveData.postValue(resource)
                return false
            }
        }).submit()
    }

    // download
    fun downloadPattern(context: Context, backgroundUrl: String, thumbnailUrl: String, selectedUrl: String, unSelectedUrl: String) {
        Glide.with(context).asBitmap().load(backgroundUrl).apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)).addListener(object : RequestListener<Bitmap?> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                mDownloadBackgroundBitmapLiveData.postValue(null)
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                mDownloadBackgroundBitmapLiveData.postValue(resource)
                return false
            }
        }).submit()
        //
        Glide.with(context).asBitmap().load(thumbnailUrl).apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)).addListener(object : RequestListener<Bitmap?> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                mDownloadThumbnailBitmapLiveData.postValue(null)
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                mDownloadThumbnailBitmapLiveData.postValue(resource)
                return false
            }
        }).submit()
        //
        Glide.with(context).asBitmap().load(selectedUrl).apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).override(400)).addListener(object : RequestListener<Bitmap?> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                mDownloadSelectedBitmapLiveData.postValue(null)
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                mDownloadSelectedBitmapLiveData.postValue(resource)
                return false
            }
        }).submit()
        //
        Glide.with(context).asBitmap().load(unSelectedUrl).apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).override(400)).addListener(object : RequestListener<Bitmap?> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                mDownloadUnSelectedBitmapLiveData.postValue(null)
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                mDownloadUnSelectedBitmapLiveData.postValue(resource)
                return false
            }
        }).submit()
    }

    fun downloadPin(context: Context, backgroundUrl: String, thumbnailUrl: String, buttonUrl: String) {
        Glide.with(context).asBitmap().load(backgroundUrl).apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)).addListener(object : RequestListener<Bitmap?> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                mDownloadBackgroundBitmapLiveData.postValue(null)
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                mDownloadBackgroundBitmapLiveData.postValue(resource)
                return false
            }
        }).submit()
        //
        Glide.with(context).asBitmap().load(thumbnailUrl).apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)).addListener(object : RequestListener<Bitmap?> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                mDownloadThumbnailBitmapLiveData.postValue(null)
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                mDownloadThumbnailBitmapLiveData.postValue(resource)
                return false
            }
        }).submit()
        //
        val list = mutableListOf<String>()
        buttonUrl.split(",").forEach {
            if (!TextUtils.isEmpty(it)) {
                list.add(it)
            }
        }
        val bitmapList = mutableListOf<Bitmap?>()
        list.forEach { _ ->
            bitmapList.add(null)
        }
        if (list.isNotEmpty()) {
            loadBitmapButton(context, 0, list, bitmapList)
        } else {
            mDownloadButtonBitmapListLiveData.postValue(mutableListOf())
        }
    }

    private fun loadBitmapButton(context: Context, index: Int, list: MutableList<String>, bitmapList: MutableList<Bitmap?>) {
        Glide.with(context).asBitmap().load(list[index]).apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).override(400)).addListener(object : RequestListener<Bitmap?> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                mDownloadButtonBitmapListLiveData.postValue(mutableListOf())
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                bitmapList[index] = resource
                if (index == list.size - 1) {
                    mDownloadButtonBitmapListLiveData.postValue(bitmapList)
                } else {
                    loadBitmapButton(context, index + 1, list, bitmapList)
                }
                return false
            }
        }).submit()
    }

    fun downloadWallpaper(context: Context, backgroundUrl: String, thumbnailUrl: String) {
        Glide.with(context).asBitmap().load(backgroundUrl).apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)).addListener(object : RequestListener<Bitmap?> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                mDownloadBackgroundBitmapLiveData.postValue(null)
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                mDownloadBackgroundBitmapLiveData.postValue(resource)
                return false
            }
        }).submit()
        //
        Glide.with(context).asBitmap().load(thumbnailUrl).apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)).addListener(object : RequestListener<Bitmap?> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                mDownloadThumbnailBitmapLiveData.postValue(null)
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                mDownloadThumbnailBitmapLiveData.postValue(resource)
                return false
            }
        }).submit()
    }

    fun isPatternLock(): Boolean {
        return appLockHelper.getThemeDefault()?.typeTheme == ThemeUtils.TYPE_PATTERN
    }

    fun getTheme(imageUrl: String, thumbnailUrl: String): ThemeModel? {
        return appLockHelper.getThemeOnline(imageUrl, thumbnailUrl)
    }

    fun getThemeLastId(): Int {
        return appLockHelper.getThemeLastId()
    }

    fun updateTheme(themeModel: ThemeModel) {
        appLockHelper.updateTheme(themeModel)
    }

    fun updateThemeDefault(themeModel: ThemeModel) {
        appLockHelper.updateThemeDefault(themeModel)
    }

    fun updateThemeDefault(backgroundResId: Int, backgroundUrl: String, backgroundDownload: String) {
        appLockHelper.updateThemeDefault(backgroundResId, backgroundUrl, backgroundDownload)
    }

    fun setReload(isReload: Boolean) {
        appLockerPreferences.setReload(isReload)
    }
}