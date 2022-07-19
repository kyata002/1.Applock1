package com.MTG.AppLock.ui.activity.main.az.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemHeaderViewState
import kotlinx.android.synthetic.main.item_locked_list_header.view.*

class HeaderViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun bind(headerViewState: AppLockItemHeaderViewState) {
        view.textViewHeader.setText(headerViewState.headerTextResource)
    }

    companion object {
        fun create(parent: ViewGroup): HeaderViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_locked_list_header, parent, false)
            return HeaderViewHolder(view)
        }
    }
}