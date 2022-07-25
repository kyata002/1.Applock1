package com.mtg.applock.util.uninstall

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

object UninstallUtils {
    /**
     * Check if an App is signed by system or not.
     */
    fun isSignedBySystem(context: Context, packageName: String?): Boolean {
        return try {
            packageName?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val piApp = context.packageManager.getPackageInfo(it, PackageManager.GET_SIGNING_CERTIFICATES)
                    val piSys = context.packageManager.getPackageInfo("android", PackageManager.GET_SIGNING_CERTIFICATES)
                    piApp?.signingInfo != null
                            && piSys?.signingInfo != null
                            && piApp.signingInfo.signingCertificateHistory != null
                            && piSys.signingInfo.signingCertificateHistory != null
                            && piSys.signingInfo.signingCertificateHistory[0] == piApp.signingInfo.signingCertificateHistory[0]
                } else {
                    val piApp = context.packageManager.getPackageInfo(it, PackageManager.GET_SIGNATURES)
                    val piSys = context.packageManager.getPackageInfo("android", PackageManager.GET_SIGNATURES)
                    piApp?.signatures != null && piSys.signatures[0] == piApp.signatures[0]
                }
            } ?: false
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Check if an App is under /system or has been installed as an update to a built-in system application.
     */
    fun isAppInSystemPartition(context: Context, packageName: String?): Boolean {
        return try {
            packageName?.let {
                val ai = context.packageManager.getApplicationInfo(it, 0)
                ai.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            } ?: false
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }
}