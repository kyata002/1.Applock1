package com.mtg.applock.ui.activity.main.configuration.edit.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mtg.applock.ui.activity.main.az.model.AppLockItemBaseViewState
import com.mtg.applock.ui.activity.main.az.model.AppLockItemHeaderViewState
import com.mtg.applock.ui.activity.main.az.model.AppLockItemItemViewState
import com.mtg.applock.ui.activity.main.configuration.edit.viewholder.HeaderViewConfigurationHolder
import com.mtg.applock.ui.activity.main.configuration.edit.viewholder.ItemViewEditConfigurationHolder


class ConfigurationEditListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var appItemClicked: ((position: Int, AppLockItemItemViewState) -> Unit)? = null
    private val itemViewStateList = mutableListOf<AppLockItemBaseViewState>()

    @SuppressLint("CheckResult")
    fun setAppDataList(itemViewStateList: List<AppLockItemBaseViewState>) {
        this.itemViewStateList.clear()
        this.itemViewStateList.addAll(itemViewStateList)
        notifyDataSetChanged()
    }

    fun removeData(itemItemViewState: AppLockItemItemViewState) {
        this.itemViewStateList.remove(itemItemViewState)
        notifyDataSetChanged()
    }

    fun getCount(): Int {
        return itemViewStateList.size
    }

    override fun getItemCount(): Int = itemViewStateList.size

    override fun getItemViewType(position: Int): Int {
        return when (itemViewStateList[position]) {
            is AppLockItemHeaderViewState -> TYPE_HEADER
            is AppLockItemItemViewState -> TYPE_APP_ITEM
            else -> throw IllegalArgumentException("No type found")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_APP_ITEM -> ItemViewEditConfigurationHolder.create(parent, appItemClicked)
            TYPE_HEADER -> HeaderViewConfigurationHolder.create(parent)
            else -> throw IllegalStateException("No type found")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < 0) return
        when (holder) {
            is ItemViewEditConfigurationHolder -> holder.bind(itemViewStateList[position] as AppLockItemItemViewState, position, itemCount)
            is HeaderViewConfigurationHolder -> holder.bind(itemViewStateList[position] as AppLockItemHeaderViewState)
        }
    }


    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_APP_ITEM = 1
    }
}