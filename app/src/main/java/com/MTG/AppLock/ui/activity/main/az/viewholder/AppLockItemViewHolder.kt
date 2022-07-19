package com.MTG.AppLock.ui.activity.main.az.viewholder

import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.MTG.AppLock.R
import com.MTG.AppLock.control.OnAppSelectedListener
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemItemViewState
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.extensions.gone
import com.MTG.AppLock.util.extensions.invisible
import com.MTG.AppLock.util.extensions.visible
import com.MTG.AppLock.util.text.VNCharacterUtils
import com.bumptech.glide.Glide
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.item_app_lock_list.view.*
import java.util.*

class AppLockItemViewHolder(private val view: View, private val onAppSelectedListener: OnAppSelectedListener?) : RecyclerView.ViewHolder(view) {
    fun bind(appLockItemViewState: AppLockItemItemViewState, searchData: String, position: Int, count: Int, enableSettings: Boolean, isOnlyLocked: Boolean, hideRemove: Boolean, hasPermission: Boolean) {
        highLightText(view.tvName, appLockItemViewState.appName(), searchData)
        view.imageViewLock.setOnClickListener {
            if (enableSettings && (TextUtils.equals(appLockItemViewState.appData.parsePackageName(), Const.SETTINGS_PACKAGE) || TextUtils.equals(appLockItemViewState.appData.parsePackageName(), Const.CH_PLAY_PACKAGE))) {
                Toasty.showToast(view.imageViewLock.context, R.string.msg_apply_prevent_application_uninstall, Toasty.WARNING)
            } else {
                onAppSelectedListener?.onLockApp(position, appLockItemViewState)
            }
        }
        view.imageDelete.setOnClickListener {
            onAppSelectedListener?.onDeleteApp(position, appLockItemViewState)
        }
        if (TextUtils.equals(appLockItemViewState.appData.parsePackageName(), Const.SETTINGS_PACKAGE) || TextUtils.equals(appLockItemViewState.appData.parsePackageName(), Const.CH_PLAY_PACKAGE)) {
            view.tvMessage.visible()
        } else {
            view.tvMessage.gone()
        }
        Glide.with(view.context).load(appLockItemViewState.getAppIcon()).into(view.imageViewAppIcon)
        appLockItemViewState.getLockIcon(view.context, hasPermission)?.let { view.imageViewLock.setImageDrawable(it) }
        when (position) {
            0 -> {
                if (isOnlyLocked && count == 1) {
                    view.viewLine.gone()
                    view.cvRoot.setBackgroundResource(R.drawable.background_item_app_configuration_full)
                } else {
                    view.viewLine.visible()
                    view.cvRoot.setBackgroundResource(R.drawable.background_item_app_configuration_top)
                }
            }
            1 -> {
                if (isOnlyLocked) {
                    if (count == 2) {
                        view.viewLine.gone()
                        view.cvRoot.setBackgroundResource(R.drawable.background_item_app_configuration_bottom)
                    } else {
                        view.viewLine.visible()
                        view.cvRoot.setBackgroundResource(android.R.color.white)
                    }
                } else {
                    view.viewLine.visible()
                    view.cvRoot.setBackgroundResource(R.drawable.background_item_app_configuration_top)
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
        if (hideRemove) {
            view.imageDelete.invisible()
        } else {
            if (isOnlyLocked) {
                view.imageDelete.invisible()
            } else {
                if (appLockItemViewState.hasRemove) {
                    view.imageDelete.visible()
                } else {
                    view.imageDelete.invisible()
                }
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
        fun create(parent: ViewGroup, appSelectedListener: OnAppSelectedListener?): AppLockItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_lock_list, parent, false)
            return AppLockItemViewHolder(view, appSelectedListener)
        }
    }
}