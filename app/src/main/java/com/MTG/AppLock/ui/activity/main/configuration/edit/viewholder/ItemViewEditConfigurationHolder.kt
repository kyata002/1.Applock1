package com.MTG.AppLock.ui.activity.main.configuration.edit.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemItemViewState
import com.MTG.AppLock.util.extensions.gone
import com.MTG.AppLock.util.extensions.visible
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_app_lock_list_configuration.view.*

class ItemViewEditConfigurationHolder(private val view: View, private val appItemClicked: ((position: Int, AppLockItemItemViewState) -> Unit)?) : RecyclerView.ViewHolder(view) {
    fun bind(appLockItemViewState: AppLockItemItemViewState, position: Int, count: Int) {
        view.tvName.text = appLockItemViewState.appName()
        view.imageDelete.setOnClickListener {
            appItemClicked?.invoke(position, appLockItemViewState)
        }
        Glide.with(view.context).load(appLockItemViewState.getAppIcon()).into(view.imageViewAppIcon)
        when (position) {
            0 -> {
                if (count == 1) {
                    view.viewLine.gone()
                    view.cvRoot.setBackgroundResource(R.drawable.background_item_app_configuration_full)
                } else {
                    view.viewLine.visible()
                    view.cvRoot.setBackgroundResource(R.drawable.background_item_app_configuration_top)
                }
            }
            1 -> {
                if (count == 2) {
                    view.viewLine.gone()
                    view.cvRoot.setBackgroundResource(R.drawable.background_item_app_configuration_bottom)
                } else {
                    view.viewLine.visible()
                    view.cvRoot.setBackgroundResource(android.R.color.white)
                }
            }
            count - 1 -> {
                view.viewLine.gone()
                view.cvRoot.setBackgroundResource(R.drawable.background_item_app_configuration_bottom)
            }
            else -> {
                view.viewLine.visible()
                view.cvRoot.setBackgroundResource(android.R.color.white)
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup, appItemClicked: ((position: Int, AppLockItemItemViewState) -> Unit)?): ItemViewEditConfigurationHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_lock_list_configuration, parent, false)
            return ItemViewEditConfigurationHolder(view, appItemClicked)
        }
    }
}