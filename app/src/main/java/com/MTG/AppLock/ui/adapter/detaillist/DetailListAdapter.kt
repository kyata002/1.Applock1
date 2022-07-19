package com.MTG.AppLock.ui.adapter.detaillist

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.MTG.AppLock.R
import com.MTG.AppLock.model.ItemDetail
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.extensions.gone
import com.MTG.AppLock.util.extensions.visible
import com.bumptech.glide.Glide
import com.MTG.library.customview.SquareLayout
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailListAdapter(private val mContext: Context, private val mItemDetailList: List<ItemDetail>, private val mOnSelectedDetailListener: OnSelectedDetailListener) : RecyclerView.Adapter<DetailListAdapter.DetailHolder>() {
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private var mIsShow: Boolean = false
    private var scaleUpAnim: Animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_up)
    private var scaleDownAnim: Animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_down)
    private var spadding = mContext.resources.getDimensionPixelSize(R.dimen.selection_padding)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailHolder {
        val view = mLayoutInflater.inflate(R.layout.item_detail, parent, false)
        return DetailHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DetailHolder, position: Int) {
        val itemDetail = mItemDetailList[position]
        Glide.with(mContext).load(itemDetail.path).override(400).placeholder(R.drawable.placeholder).into(holder.imageDetailImageVideo)
        holder.imageLogoAudioFile.setImageResource(itemDetail.resIdThumbnail)
        //
        holder.tvSizeVideo.text = itemDetail.tvSize
        //
        holder.imageSelectedImageVideo.isSelected = itemDetail.isSelected
        holder.imageSelectedAudioFile.isSelected = itemDetail.isSelected
        holder.slRootImageVideo.isSelected = itemDetail.isSelected
        if (mIsShow) {
            holder.imageSelectedImageVideo.visible()
            holder.imageSelectedAudioFile.visible()
            holder.imageMoreAudioFile.gone()
            if(itemDetail.isSelected){
                holder.imageDetailImageVideo.setPadding(spadding,spadding,spadding,spadding)
            }
            else{
                holder.imageDetailImageVideo.setPadding(0,0,0,0)
            }
        } else {
            holder.imageSelectedImageVideo.gone()
            holder.imageSelectedAudioFile.gone()
            holder.imageMoreAudioFile.visible()
        }
        when (itemDetail.type) {
            Const.TYPE_IMAGES -> {
                holder.slRootImageVideo.visible()
                holder.llVideo.gone()
                holder.llAudioFile.gone()
            }
            Const.TYPE_VIDEOS -> {
                holder.slRootImageVideo.visible()
                holder.llVideo.visible()
                holder.llAudioFile.gone()
            }
            Const.TYPE_AUDIOS, Const.TYPE_FILES -> {
                holder.slRootImageVideo.gone()
                holder.llVideo.gone()
                holder.llAudioFile.visible()
            }
        }
        holder.textNameAudioFile.text = itemDetail.name
        holder.textDuration.text = itemDetail.tvSize

        holder.slRootImageVideo.setOnClickListener {

            itemDetail.isSelected = !itemDetail.isSelected
            mOnSelectedDetailListener.onSelectedDetail(itemDetail, position)
            notifyItemChanged(position)

        }
        holder.llAudioFile.setOnClickListener {
            itemDetail.isSelected = !itemDetail.isSelected
            mOnSelectedDetailListener.onSelectedDetail(itemDetail, position)
            notifyItemChanged(position)
        }
        holder.imageMoreAudioFile.setOnClickListener {
            mOnSelectedDetailListener.onMoreDetail(it, itemDetail, position)
        }
    }

    fun setShow(show: Boolean) {
        mIsShow = show
        notifyDataSetChanged()
    }

    fun isShow(): Boolean {
        return mIsShow
    }

    fun getSelectedNumber(): Int {
        var number = 0
        for (itemDetail in mItemDetailList) {
            if (itemDetail.isSelected) {
                number++
            }
        }
        return number
    }

    override fun getItemCount(): Int {
        return mItemDetailList.size
    }

    fun setSelectedAll(selected: Boolean) {
        for (itemDetail in mItemDetailList) {
            itemDetail.isSelected = selected
        }
        notifyDataSetChanged()
    }

    inner class DetailHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageDetailImageVideo: ImageView = view.imageDetailImageVideo
        val imageSelectedImageVideo: ImageView = view.imageSelectedImageVideo
        val slRootImageVideo: SquareLayout = view.slRootImageVideo
        val llVideo: LinearLayout = view.llVideo
        val tvSizeVideo: TextView = view.tvSizeVideo
        val llAudioFile: LinearLayout = view.llAudioFile
        val imageLogoAudioFile: ImageView = view.imageLogoAudioFile
        val textNameAudioFile: TextView = view.textNameAudioFile
        val textDuration: TextView = view.textDuration
        val imageSelectedAudioFile: ImageView = view.imageSelectedAudioFile
        val imageMoreAudioFile: ImageView = view.imageMoreAudioFile

    }

    interface OnSelectedDetailListener {
        fun onSelectedDetail(itemDetail: ItemDetail, position: Int)
        fun onMoreDetail(view: View, itemDetail: ItemDetail, position: Int)
    }
}
