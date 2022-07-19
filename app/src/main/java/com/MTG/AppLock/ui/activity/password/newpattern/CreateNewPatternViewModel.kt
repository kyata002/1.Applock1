package com.MTG.AppLock.ui.activity.password.newpattern

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.MTG.AppLock.data.database.pattern.PatternDao
import com.MTG.AppLock.data.database.pattern.PatternDotMetadata
import com.MTG.AppLock.data.database.pattern.PatternEntity
import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.model.ThemeModel
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.ThemeUtils
import com.MTG.AppLock.util.extensions.background
import com.MTG.AppLock.util.extensions.convertToPatternDot
import com.MTG.AppLock.util.password.PatternChecker
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import com.MTG.patternlockview.PatternLockView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import javax.inject.Inject

class CreateNewPatternViewModel @Inject constructor(private val patternDao: PatternDao, val appLockerPreferences: AppLockerPreferences, private val appLockHelper: AppLockHelper) : RxAwareViewModel() {
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

    enum class PatternEvent {
        INITIALIZE, FIRST_COMPLETED, SECOND_COMPLETED, ERROR, ERROR_SHORT_FIRST, ERROR_SHORT_SECOND
    }

    private val patternEventLiveData = MutableLiveData<CreateNewPatternViewState>().apply {
        value = CreateNewPatternViewState(PatternEvent.INITIALIZE)
    }
    private var firstDrawPattern: ArrayList<PatternLockView.Dot> = arrayListOf()
    private var redrawPattern: ArrayList<PatternLockView.Dot> = arrayListOf()
    fun getPatternEventLiveData(): LiveData<CreateNewPatternViewState> = patternEventLiveData

    fun setFirstDrawPattern(pattern: List<PatternLockView.Dot>?) {
        pattern?.let {
            this.firstDrawPattern.clear()
            this.firstDrawPattern.addAll(it)
            if (firstDrawPattern.size < 4) {
                firstDrawPattern.clear()
                patternEventLiveData.postValue(CreateNewPatternViewState(PatternEvent.ERROR_SHORT_FIRST))
                return
            }
            patternEventLiveData.postValue(CreateNewPatternViewState(PatternEvent.FIRST_COMPLETED))
        }
    }

    fun setRedrawnPattern(pattern: List<PatternLockView.Dot>?) {
        pattern?.let {
            this.redrawPattern.clear()
            this.redrawPattern.addAll(it)
            if (firstDrawPattern.size < 4 || redrawPattern.size < 4) {
                redrawPattern.clear()
                patternEventLiveData.postValue(CreateNewPatternViewState(PatternEvent.ERROR_SHORT_SECOND))
                return
            }
            if (PatternChecker.checkPatternsEqual(firstDrawPattern.convertToPatternDot(), redrawPattern.convertToPatternDot())) {
                saveNewCreatedPattern(firstDrawPattern)
                patternEventLiveData.postValue(CreateNewPatternViewState(PatternEvent.SECOND_COMPLETED))
            } else {
                redrawPattern.clear()
                patternEventLiveData.postValue(CreateNewPatternViewState(PatternEvent.ERROR))
            }
        }
    }

    fun isFirstPattern(): Boolean = firstDrawPattern.isEmpty()

    private fun saveNewCreatedPattern(pattern: List<PatternLockView.Dot>) {
        background.submit {
            val patternMetadata = PatternDotMetadata(pattern.convertToPatternDot())
            val patternEntity = PatternEntity(patternMetadata)
            patternDao.createPattern(patternEntity)
        }
    }

    fun setPinLock(pin: String) {
        appLockerPreferences.setPinLock(pin)
    }

    fun isPatternLock(): Boolean {
        return appLockHelper.getThemeDefault()?.typeTheme == ThemeUtils.TYPE_PATTERN
    }

    fun reset() {
        firstDrawPattern.clear()
        redrawPattern.clear()
        patternEventLiveData.postValue(CreateNewPatternViewState(PatternEvent.INITIALIZE))
    }

    fun loadSelectedBitmap(context: Context, selectedUrl: String) {
        Glide.with(context).asBitmap().load(selectedUrl).apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).override(400)).addListener(object : RequestListener<Bitmap?> {
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
        Glide.with(context).asBitmap().load(unSelectedUrl).apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).override(400)).addListener(object : RequestListener<Bitmap?> {
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
        var success = 0
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
        list.forEachIndexed { index, path ->
            Glide.with(context).asBitmap().load(path).apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).override(400)).addListener(object : RequestListener<Bitmap?> {
                override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                    mDownloadButtonBitmapListLiveData.postValue(mutableListOf())
                    return false
                }

                override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    success++
                    bitmapList[index] = resource
                    if (success == list.size) {
                        mDownloadButtonBitmapListLiveData.postValue(bitmapList)
                    }
                    return false
                }
            }).submit()
        }
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

    fun setReload(isReload: Boolean) {
        appLockerPreferences.setReload(isReload)
    }

    fun updateThemeDefault(themeModel: ThemeModel) {
        appLockHelper.updateThemeDefault(themeModel)
    }
}