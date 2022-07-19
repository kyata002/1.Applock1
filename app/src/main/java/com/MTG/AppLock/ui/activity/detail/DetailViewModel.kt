package com.MTG.AppLock.ui.activity.detail

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.MTG.AppLock.R
import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.Const
import java.io.File
import javax.inject.Inject

class DetailViewModel @Inject constructor(val appLockHelper: AppLockHelper) : RxAwareViewModel() {
    private val mTextDataLiveData = MutableLiveData<String>()
    fun getMessage(context: Context, type: Int): String {
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

    fun loadData(path: String) {
        val inputStream = File(path).inputStream()
        val builder = StringBuffer()
        var line = 0
        inputStream.bufferedReader().forEachLine {
            line++
            builder.append(it).append("\n")
            if (line % 8 == 0) {
                try {
                    Thread.sleep(1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mTextDataLiveData.postValue(builder.toString())
                builder.setLength(0)
            }
        }
        mTextDataLiveData.postValue(builder.toString())
    }

    fun getTextDataLiveData(): LiveData<String> = mTextDataLiveData
}
