package com.MTG.AppLock.permissions

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.WindowManager

object PermissionChecker {
    fun checkUsageAccessPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return true
        }

        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            var mode = 0

            mode = appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED

        } catch (e: NameNotFoundException) {
            false
        }

    }

    fun checkOverlayPermission(context: Context): Boolean {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> return true
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> return Settings.canDrawOverlays(context)
            else -> {
                if (Settings.canDrawOverlays(context)) return true
                try {
                    val mgr = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    val viewToAdd = View(context)
                    val params = WindowManager.LayoutParams(
                            0, 0, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT
                    )
                    viewToAdd.layoutParams = params
                    mgr.addView(viewToAdd, params)
                    mgr.removeView(viewToAdd)
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return false
            }
        }
    }

    fun checkFullPermission(context: Context): Boolean {
        return checkOverlayPermission(context) && checkUsageAccessPermission(context)
    }
}