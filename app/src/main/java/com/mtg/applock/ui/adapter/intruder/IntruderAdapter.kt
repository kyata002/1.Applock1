package com.mtg.applock.ui.adapter.intruder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mtg.applock.R
import com.mtg.applock.ui.activity.intruders.IntrudersPhotoItemViewState
import com.mtg.applock.util.extensions.gone
import com.mtg.applock.util.extensions.visible
import com.mtg.applock.util.file.FilePathHelper
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_intruders_photo.view.*

class IntruderAdapter(private val mContext: Context, private val mIntrudersPhotoItemViewStateList: List<IntrudersPhotoItemViewState>, private val mOpenIntrudersPhotosListener: OpenIntrudersPhotosListener) : RecyclerView.Adapter<IntruderAdapter.IntruderHolder>() {
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntruderHolder {
        val view = mLayoutInflater.inflate(R.layout.item_intruders_photo, parent, false)
        return IntruderHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: IntruderHolder, position: Int) {
        val intruderPhotoItemViewState = mIntrudersPhotoItemViewStateList[position]
        Glide.with(mContext).load(intruderPhotoItemViewState.filePath).placeholder(R.drawable.placeholder).into(holder.imageIntruder)
        Glide.with(mContext).load(FilePathHelper.getIconIcon(mContext, intruderPhotoItemViewState.packageApp)).into(holder.imageLogoApp)
        holder.tvDay.text = intruderPhotoItemViewState.day
        holder.tvTime.text = intruderPhotoItemViewState.time
        //
        holder.imageIntruder.setOnClickListener {
            mOpenIntrudersPhotosListener.openIntrudersPhotosListener(intruderPhotoItemViewState)
        }
        holder.imageMore.setOnClickListener {
            mOpenIntrudersPhotosListener.openMore(it, intruderPhotoItemViewState)
        }
        if (position == itemCount - 1) {
            holder.viewLine.gone()
        } else {
            holder.viewLine.visible()
        }
    }

    override fun getItemCount(): Int {
        return mIntrudersPhotoItemViewStateList.size
    }

    inner class IntruderHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageIntruder: ImageView = view.imageIntruder
        val imageLogoApp: ImageView = view.imageLogoApp
        val imageMore: ImageView = view.imageMore
        val tvDay: TextView = view.tvDay
        val tvTime: TextView = view.tvTime
        val viewLine: View = view.viewLine
    }

    interface OpenIntrudersPhotosListener {
        fun openIntrudersPhotosListener(mIntrudersPhotoItemViewState: IntrudersPhotoItemViewState)
        fun openMore(view: View?, intrudersPhotoItemViewState: IntrudersPhotoItemViewState)
    }
}