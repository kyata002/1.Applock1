package com.mtg.applock.ui.adapter.all

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mtg.applock.control.OnAppSelectedListener
import com.mtg.applock.ui.activity.main.az.model.AppLockItemBaseViewState
import com.mtg.applock.ui.activity.main.az.model.AppLockItemHeaderViewState
import com.mtg.applock.ui.activity.main.az.model.AppLockItemItemViewState
import com.mtg.applock.ui.activity.main.az.viewholder.AppLockItemViewHolder
import com.mtg.applock.ui.activity.main.az.viewholder.HeaderViewHolder


class AppLockListForLockedAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mOnAppSelectedListener: OnAppSelectedListener? = null
    private var mSearchData: String = ""
    private val mItemViewStateList = mutableListOf<AppLockItemBaseViewState>()
    private var mEnableSettings = false
    private var mHasPermission: Boolean = false

    @SuppressLint("CheckResult")
    fun setAppDataList(itemViewStateList: List<AppLockItemBaseViewState>) {
        mItemViewStateList.clear()
        mItemViewStateList.addAll(itemViewStateList)
        notifyDataSetChanged()
    }

    fun setOnAppSelectedListener(onAppSelectedListener: OnAppSelectedListener?) {
        mOnAppSelectedListener = onAppSelectedListener
    }

    override fun getItemCount(): Int = mItemViewStateList.size

    override fun getItemViewType(position: Int): Int {
        return when (mItemViewStateList[position]) {
            is AppLockItemHeaderViewState -> TYPE_HEADER
            is AppLockItemItemViewState -> TYPE_APP_ITEM
            else -> throw IllegalArgumentException("No type found")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_APP_ITEM -> AppLockItemViewHolder.create(parent, mOnAppSelectedListener)
            TYPE_HEADER -> HeaderViewHolder.create(parent)

            else -> throw IllegalStateException("No type found")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < 0) return
        when (holder) {
            is AppLockItemViewHolder -> holder.bind(mItemViewStateList[position] as AppLockItemItemViewState, mSearchData, position, itemCount, mEnableSettings, isOnlyLocked = true, hideRemove = true, hasPermission = mHasPermission)
            is HeaderViewHolder -> holder.bind(mItemViewStateList[position] as AppLockItemHeaderViewState)

        }
    }

    fun setSearchData(searchData: String) {
        mSearchData = searchData
    }

    fun setEnableSettings(enableSettings: Boolean) {
        mEnableSettings = enableSettings
    }

    fun setHasPermission(hasPermission: Boolean) {
        mHasPermission = hasPermission
        notifyDataSetChanged()
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_APP_ITEM = 1
        private const val TYPE_ADS = 2
    }
}