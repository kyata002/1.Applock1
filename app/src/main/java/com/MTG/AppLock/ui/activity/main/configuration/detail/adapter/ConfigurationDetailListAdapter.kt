package com.MTG.AppLock.ui.activity.main.configuration.detail.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemBaseViewState
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemHeaderViewState
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemItemViewState
import com.MTG.AppLock.ui.activity.main.configuration.detail.viewholder.ItemViewDetailConfigurationHolder
import com.MTG.AppLock.ui.activity.main.configuration.edit.viewholder.HeaderViewConfigurationHolder
import com.MTG.AppLock.ui.adapter.all.AppLockListDiffUtil

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ConfigurationDetailListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var appItemClicked: ((position: Int, AppLockItemItemViewState) -> Unit)? = null
    private var searchData: String = ""
    private val itemViewStateList = mutableListOf<AppLockItemBaseViewState>()

    @SuppressLint("CheckResult")
    fun setAppDataList(itemViewStateList: List<AppLockItemBaseViewState>) {
        Single.create<DiffUtil.DiffResult> {
            val diffResult = DiffUtil.calculateDiff(AppLockListDiffUtil(this.itemViewStateList, itemViewStateList))
            it.onSuccess(diffResult)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            this.itemViewStateList.clear()
            this.itemViewStateList.addAll(itemViewStateList)
            it.dispatchUpdatesTo(this)
        }, {
            this.itemViewStateList.clear()
            this.itemViewStateList.addAll(itemViewStateList)
            notifyDataSetChanged()
        })
    }

    fun setSearchData(searchData: String) {
        this.searchData = searchData
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
            TYPE_APP_ITEM -> ItemViewDetailConfigurationHolder.create(parent, appItemClicked)
            TYPE_HEADER -> HeaderViewConfigurationHolder.create(parent)
            else -> throw IllegalStateException("No type found")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < 0) return
        when (holder) {
            is ItemViewDetailConfigurationHolder -> holder.bind(itemViewStateList[position] as AppLockItemItemViewState, position, itemCount, searchData)
            is HeaderViewConfigurationHolder -> holder.bind(itemViewStateList[position] as AppLockItemHeaderViewState)

        }
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_APP_ITEM = 1
        private const val TYPE_ADS = 2
    }
}