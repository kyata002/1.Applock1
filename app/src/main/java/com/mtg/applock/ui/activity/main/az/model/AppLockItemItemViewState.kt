package com.mtg.applock.ui.activity.main.az.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.mtg.applock.R
import com.mtg.applock.data.dataapp.AppData

data class AppLockItemItemViewState(val appData: AppData, var isLocked: Boolean = false, var hasRemove: Boolean = false) : AppLockItemBaseViewState() {
    fun appName() = appData.appName

    fun getLockIcon(context: Context, hasPermission: Boolean): Drawable? {
        return if (isLocked) {
            if (hasPermission) {
                ContextCompat.getDrawable(context, R.drawable.ic_lock_active)
            } else {
                ContextCompat.getDrawable(context, R.drawable.ic_lock_grey_active)
            }
        } else {
            ContextCompat.getDrawable(context, R.drawable.ic_lock_inactive)
        }
    }

    fun getAppIcon(): Drawable = appData.appIconDrawable
}