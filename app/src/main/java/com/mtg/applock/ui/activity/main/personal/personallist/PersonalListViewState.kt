package com.mtg.applock.ui.activity.main.personal.personallist

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import com.mtg.applock.R
import com.mtg.applock.model.Album
import com.mtg.applock.util.Const

data class PersonalListViewState(val type: Int, val albumList: MutableList<Album>) {
    fun getEmptyPageVisibility(): Int {
        return if (albumList.isEmpty()) View.VISIBLE else View.INVISIBLE
    }

    fun getEmptyRecyclerVisibility(): Int {
        return if (albumList.isEmpty()) View.INVISIBLE else View.VISIBLE
    }

    companion object {
        fun empty(type: Int) = PersonalListViewState(type = type, albumList = mutableListOf())
    }

    fun getEmptyPageDrawable(context: Context, type: Int): Drawable? {
        return when (type) {
            Const.TYPE_VIDEOS -> ContextCompat.getDrawable(context, R.drawable.ic_no_video)
            Const.TYPE_AUDIOS -> ContextCompat.getDrawable(context, R.drawable.ic_no_audio)
            Const.TYPE_FILES -> ContextCompat.getDrawable(context, R.drawable.ic_no_file)
            else -> ContextCompat.getDrawable(context, R.drawable.ic_no_image)
        }
    }

    fun getEmptyPageTitle(context: Context, type: Int): String {
        return when (type) {
            Const.TYPE_VIDEOS -> context.getString(R.string.title_video_empty_page)
            Const.TYPE_AUDIOS -> context.getString(R.string.title_audio_empty_page)
            Const.TYPE_FILES -> context.getString(R.string.title_file_empty_page)
            else -> context.getString(R.string.title_photo_empty_page)
        }
    }

    fun getEmptyPageDescription(context: Context, type: Int): String {
        return when (type) {
            Const.TYPE_VIDEOS -> context.getString(R.string.text_press_to_add_video_into_vault)
            Const.TYPE_AUDIOS -> context.getString(R.string.text_press_to_add_audio_into_vault)
            Const.TYPE_FILES -> context.getString(R.string.text_press_to_add_file_into_vault)
            else -> context.getString(R.string.text_press_to_add_photo_into_vault)
        }
    }
}