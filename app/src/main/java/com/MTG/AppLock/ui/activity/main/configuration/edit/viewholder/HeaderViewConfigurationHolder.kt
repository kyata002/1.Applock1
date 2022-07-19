package com.MTG.AppLock.ui.activity.main.configuration.edit.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemHeaderViewState
import com.MTG.AppLock.util.extensions.gone
import kotlinx.android.synthetic.main.item_locked_list_header.view.*

class HeaderViewConfigurationHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun bind(headerViewState: AppLockItemHeaderViewState) {
        view.textViewHeader.setText(headerViewState.headerTextResource)
        view.gone()
        view.layoutParams = RecyclerView.LayoutParams(0, 0)
    }

    companion object {
        fun create(parent: ViewGroup): HeaderViewConfigurationHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_locked_list_header, parent, false)
            return HeaderViewConfigurationHolder(view)
        }
    }
}