package com.mtg.applock.ui.activity.main.configuration.detail.viewholder

import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mtg.applock.R
import com.mtg.applock.ui.activity.main.az.model.AppLockItemItemViewState
import com.mtg.applock.util.extensions.gone
import com.mtg.applock.util.extensions.visible
import com.mtg.applock.util.text.VNCharacterUtils
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_app_lock_list_detail_configuration.view.*
import java.util.*

class ItemViewDetailConfigurationHolder(private val view: View, private val appItemClicked: ((position: Int, AppLockItemItemViewState) -> Unit)?) : RecyclerView.ViewHolder(view) {
    fun bind(appLockItemViewState: AppLockItemItemViewState, position: Int, count: Int, searchData: String) {
        highLightText(view.tvName, appLockItemViewState.appName(), searchData)
        view.imageSelected.setOnClickListener {
            appItemClicked?.invoke(position, appLockItemViewState)
        }
        view.imageSelected.isSelected = appLockItemViewState.isLocked
        Glide.with(view.context).load(appLockItemViewState.getAppIcon()).into(view.imageViewAppIcon)
        when (position) {
            0 -> {
                view.viewLine.visible()
                view.cvRoot.setBackgroundResource(R.drawable.background_item_app_configuration_top)
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

    private fun highLightText(textView: TextView?, target: String, highlightText: String) {
        if (textView == null || TextUtils.isEmpty(target)) return
        if (TextUtils.isEmpty(highlightText)) {
            textView.text = target
            return
        }
        val spannable: Spannable = SpannableString(target)
        val start: Int = VNCharacterUtils.removeAccent(target.toLowerCase(Locale.getDefault())).indexOf(VNCharacterUtils.removeAccent(highlightText.toLowerCase(Locale.getDefault())))
        if (start != -1) {
            spannable.setSpan(ForegroundColorSpan(HIGHLIGHT_COLOR), start, start + highlightText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            textView.text = spannable
        } else {
            textView.text = target
        }
    }

    companion object {
        private val HIGHLIGHT_COLOR: Int = android.graphics.Color.parseColor("#FFF44336")
        fun create(parent: ViewGroup, appItemClicked: ((position: Int, AppLockItemItemViewState) -> Unit)?): ItemViewDetailConfigurationHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_lock_list_detail_configuration, parent, false)
            return ItemViewDetailConfigurationHolder(view, appItemClicked)
        }
    }
}