package com.MTG.AppLock.ui.activity.main.az.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.MTG.AppLock.R
import com.MTG.AppLock.data.DataApp.AppData

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