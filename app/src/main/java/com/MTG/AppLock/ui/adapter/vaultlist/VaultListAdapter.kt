package com.MTG.AppLock.ui.adapter.vaultlist

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.MTG.AppLock.R
import com.MTG.AppLock.model.Album
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.extensions.gone
import com.MTG.AppLock.util.extensions.visible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.item_album.view.*
import java.util.*

class VaultListAdapter(private val mContext: Context, private val mAlbumList: List<Album>, private val mOnSelectedAlbumListener: OnSelectedAlbumListener) : RecyclerView.Adapter<VaultListAdapter.AlbumHolder>() {
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private var mIsShow: Boolean = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumHolder {
        val view = mLayoutInflater.inflate(R.layout.item_album, parent, false)
        return AlbumHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VaultListAdapter.AlbumHolder, position: Int) {
        val album = mAlbumList[position]
        Glide.with(mContext).load(album.pathThumbnail).override(400).placeholder(R.drawable.placeholder).transition(
            DrawableTransitionOptions.withCrossFade()).into(holder.imageAlbum)
        holder.imageAlbumAudio.setImageResource(album.resIdThumbnail)
        when (album.type) {
            Const.TYPE_AUDIOS, Const.TYPE_FILES -> {
                holder.imageAlbum.gone()
                holder.imageAlbumAudio.visible()
            }
            else -> {
                holder.imageAlbum.visible()
                holder.imageAlbumAudio.gone()
            }
        }
        //
        holder.tvName.text = String.format(Locale.getDefault(), "%s", album.name)
        holder.tvCount.text= String.format(Locale.getDefault(), "%d", album.number)
        //
        holder.llRootImageVideo.isSelected = album.isSelected
        holder.llRootImageVideo.setOnClickListener {
            album.isSelected = !album.isSelected
            mOnSelectedAlbumListener.onSelectedAlbum(album)
            notifyItemChanged(holder.adapterPosition)
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
        for (album in mAlbumList) {
            if (album.isSelected) {
                number++
            }
        }
        return number
    }

    override fun getItemCount(): Int {
        return mAlbumList.size
    }

    fun setSelectedAll(selected: Boolean) {
        for (album in mAlbumList) {
            album.isSelected = selected
        }
        notifyDataSetChanged()
    }

    inner class AlbumHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageAlbum: ImageView = view.imageAlbum
        val imageAlbumAudio: ImageView = view.imageAlbumAudio
        val llRootImageVideo: CardView = view.cvRoot
        val tvName: TextView = view.tvName
        val tvCount:TextView = view.tvCount
    }

    interface OnSelectedAlbumListener {
        fun onSelectedAlbum(album: Album)
    }
}
