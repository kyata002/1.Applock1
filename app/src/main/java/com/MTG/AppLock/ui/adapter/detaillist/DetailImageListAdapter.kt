package com.MTG.AppLock.ui.adapter.detaillist

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.MTG.AppLock.R
import com.MTG.AppLock.model.ItemDetail
import com.bumptech.glide.Glide
import com.MTG.library.customview.SquareLayout
import kotlinx.android.synthetic.main.item_detail_image.view.*

class DetailImageListAdapter(private val mContext: Context, private val mItemDetailList: List<ItemDetail>, private val mOnSelectedDetailListener: OnSelectedDetailListener) : RecyclerView.Adapter<DetailImageListAdapter.DetailHolder>() {
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailHolder {
        val view = mLayoutInflater.inflate(R.layout.item_detail_image, parent, false)
        return DetailHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DetailHolder, position: Int) {
        val itemDetail = mItemDetailList[position]
        Glide.with(mContext).load(itemDetail.path).override(400).placeholder(R.drawable.placeholder).into(holder.imageDetailImageVideo)
        holder.imageSelectedImageVideo.isSelected = itemDetail.isSelected
        holder.slRootImageVideo.isSelected = itemDetail.isSelected
        holder.slRootImageVideo.setOnClickListener {
            itemDetail.isSelected = !itemDetail.isSelected
            mOnSelectedDetailListener.onSelectedDetail(itemDetail)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return mItemDetailList.size
    }

    inner class DetailHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageDetailImageVideo: ImageView = view.imageDetailImageVideo
        val imageSelectedImageVideo: ImageView = view.imageSelectedImageVideo
        val slRootImageVideo: SquareLayout = view.slRootImageVideo
    }

    interface OnSelectedDetailListener {
        fun onSelectedDetail(itemDetail: ItemDetail)
    }
}
