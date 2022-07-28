package com.mtg.applock.ui.adapter.config

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.common.control.manager.AdmobManager
import com.mtg.applock.BuildConfig
import com.mtg.applock.R
import kotlinx.android.synthetic.main.item_native_ads.view.*

class AdsHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    init {
        AdmobManager.getInstance().loadNative(itemView.context,BuildConfig.native_grouplock,itemView.fakeAdsNative)
    }
}