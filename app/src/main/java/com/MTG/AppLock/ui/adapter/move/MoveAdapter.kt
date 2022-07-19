package com.MTG.AppLock.ui.adapter.move

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.MTG.AppLock.R
import com.MTG.AppLock.model.ItemDetail
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.extensions.gone
import com.MTG.AppLock.util.extensions.invisible
import com.MTG.AppLock.util.extensions.visible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.MTG.library.customview.SquareLayout
import kotlinx.android.synthetic.main.item_move.view.*

class MoveAdapter(private val mContext: Context, private val mItemDetailList: List<ItemDetail>) : RecyclerView.Adapter<MoveAdapter.MoveHolder>() {
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoveHolder {
        val view = mLayoutInflater.inflate(R.layout.item_move, parent, false)
        return MoveHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MoveHolder, position: Int) {
        val itemDetail = mItemDetailList[position]
        Glide.with(mContext).asBitmap().override(400).load(itemDetail.path).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                holder.imageMove.setImageBitmap(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }

        })
        holder.imageLogoAudioFile.setImageResource(itemDetail.resIdThumbnail)
        if (itemDetail.isSelected) {
            holder.imageSelected.visible()
            holder.imageSelectedAudioFile.visible()
        } else {
            holder.imageSelected.invisible()
            holder.imageSelectedAudioFile.invisible()
        }
        //
        holder.textNameAudioFile.text = itemDetail.name
        holder.textDuration.text = itemDetail.tvSize
        when (itemDetail.type) {
            Const.TYPE_IMAGES, Const.TYPE_VIDEOS -> {
                holder.slImageVideo.visible()
                holder.llAudioFile.gone()
            }
            Const.TYPE_AUDIOS, Const.TYPE_FILES -> {
                holder.slImageVideo.gone()
                holder.llAudioFile.visible()
            }
        }
    }

    override fun getItemCount(): Int {
        return mItemDetailList.size
    }

    inner class MoveHolder(view: View) : RecyclerView.ViewHolder(view) {
        val slImageVideo: SquareLayout = view.slImageVideo
        val llAudioFile: LinearLayout = view.llAudioFile
        val imageMove: ImageView = view.imageMove
        val imageSelected: ImageView = view.imageSelected
        val imageSelectedAudioFile: ImageView = view.imageSelectedAudioFile
        val textNameAudioFile: TextView = view.textNameAudioFile
        val textDuration: TextView = view.textDuration
        val imageLogoAudioFile: ImageView = view.imageLogoAudioFile
    }
}
