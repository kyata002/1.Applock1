package com.MTG.AppLock.ui.adapter.theme

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.MTG.AppLock.R
import com.MTG.AppLock.model.ThemeModel
import com.bumptech.glide.Glide
import com.MTG.library.customview.CustomImageView
import kotlinx.android.synthetic.main.item_theme.view.*

class ThemeAdapter(private val mContext: Context, private val mOnSelectedThemeListener: OnSelectedThemeListener) : RecyclerView.Adapter<ThemeAdapter.ThemeHolder>() {
    private var mThemeList: MutableList<ThemeModel> = mutableListOf()
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)

    fun clear() {
        mThemeList.clear()
        notifyDataSetChanged()
    }

    fun addData(themeList: MutableList<ThemeModel>) {
        val newList = mutableListOf<ThemeModel>()
        themeList.forEach { themeModel ->
            if (!TextUtils.isEmpty(themeModel.backgroundUrl) && !TextUtils.isEmpty(themeModel.thumbnailUrl)) {
                mThemeList.forEach { wallpaper ->
                    if (!TextUtils.isEmpty(wallpaper.backgroundUrl) && !TextUtils.isEmpty(wallpaper.thumbnailUrl)) {
                        if (TextUtils.equals(themeModel.backgroundUrl, wallpaper.backgroundUrl) && TextUtils.equals(themeModel.thumbnailUrl, wallpaper.thumbnailUrl)) {
                            themeModel.exist = true
                        }
                    }
                }
            }
        }
        themeList.forEach { themeModel ->
            if (!themeModel.exist) {
                newList.add(themeModel)
            }
        }
        mThemeList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeHolder {
        val view = mLayoutInflater.inflate(R.layout.item_theme, parent, false)
        return ThemeHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ThemeHolder, position: Int) {
        val themeModel = mThemeList[position]
        if (themeModel.thumbnailResId == 0) {
            Glide.with(mContext).load(if (TextUtils.isEmpty(themeModel.thumbnailUrl)) themeModel.backgroundUrl else themeModel.thumbnailUrl).placeholder(R.drawable.placeholder).into(holder.imageTheme)
        } else {
            Glide.with(mContext).load(themeModel.thumbnailResId).placeholder(R.drawable.placeholder).into(holder.imageTheme)
        }

        holder.imageTheme.setOnClickListener {
            mOnSelectedThemeListener.onSelectedTheme(themeModel)
        }
    }

    override fun getItemCount(): Int {
        return mThemeList.size
    }

    inner class ThemeHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageTheme: CustomImageView = view.imageTheme
    }

    interface OnSelectedThemeListener {
        fun onSelectedTheme(themeModel: ThemeModel)
    }
}
