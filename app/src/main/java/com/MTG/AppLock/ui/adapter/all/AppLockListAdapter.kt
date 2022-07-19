package com.MTG.AppLock.ui.adapter.all

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.MTG.AppLock.control.OnAppSelectedListener
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemBaseViewState
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemHeaderViewState
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemItemViewState
import com.MTG.AppLock.ui.activity.main.az.viewholder.AppLockItemViewHolder
import com.MTG.AppLock.ui.activity.main.az.viewholder.HeaderViewHolder
import com.MTG.AppLock.util.Const
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AppLockListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mOnAppSelectedListener: OnAppSelectedListener? = null
    private var mSearchData: String = ""
    private val mItemViewStateList = mutableListOf<AppLockItemBaseViewState>()
    private var mEnableSettings = false
    private var mHideRemove = false
    private var mHasPermission: Boolean = false

    @SuppressLint("CheckResult")
    fun setAppDataList(itemViewStateList: List<AppLockItemBaseViewState>, isRemove: Boolean) {
        if (isRemove) {
            Single.create<DiffUtil.DiffResult> {
                val diffResult = DiffUtil.calculateDiff(AppLockListDiffUtil(mItemViewStateList, itemViewStateList))
                it.onSuccess(diffResult)
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                mItemViewStateList.clear()
                mItemViewStateList.addAll(itemViewStateList)
                it.dispatchUpdatesTo(this)
            }, {
                mItemViewStateList.clear()
                mItemViewStateList.addAll(itemViewStateList)
                notifyDataSetChanged()
            })
        } else {
            mItemViewStateList.clear()
            mItemViewStateList.addAll(itemViewStateList)
            notifyDataSetChanged()
        }
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
            is AppLockItemViewHolder -> holder.bind(mItemViewStateList[position] as AppLockItemItemViewState, mSearchData, position, itemCount, mEnableSettings, false, mHideRemove, mHasPermission)
            is HeaderViewHolder -> holder.bind(mItemViewStateList[position] as AppLockItemHeaderViewState)
        }
    }


    fun setSearchData(searchData: String) {
        mSearchData = searchData
    }

    fun setEnableSettings(enableSettings: Boolean) {
        mEnableSettings = enableSettings
    }

    fun setHideRemove(hideRemove: Boolean) {
        mHideRemove = hideRemove
    }

    fun setHasPermission(hasPermission: Boolean) {
        mHasPermission = hasPermission
        notifyDataSetChanged()
    }

    fun isSettingsLocked(): Boolean {
        mItemViewStateList.forEach {
            if (it is AppLockItemItemViewState) {
                if (TextUtils.equals(it.appData.parsePackageName(), Const.SETTINGS_PACKAGE)) {
                    if (it.isLocked) {
                        return true
                    }
                }
            }
        }
        return false
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_APP_ITEM = 1
    }
}