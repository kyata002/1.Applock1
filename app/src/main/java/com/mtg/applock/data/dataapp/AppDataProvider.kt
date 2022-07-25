package com.mtg.applock.data.dataapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.text.TextUtils
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.data.sqllite.ConstantDB
import com.mtg.applock.util.preferences.AppLockerPreferences
import io.reactivex.Single
import java.text.Collator
import java.util.*
import javax.inject.Inject

class AppDataProvider @Inject constructor(
        val context: Context,
        val appLockHelper: AppLockHelper,
        val appLockerPreferences: AppLockerPreferences
) {
    fun fetchInstalledAppList(): Single<List<AppData>> {
        return Single.create {
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfoList: List<ResolveInfo> = context.packageManager.queryIntentActivities(mainIntent, 0)
            val appDataList: ArrayList<AppData> = arrayListOf()
            resolveInfoList.forEach { resolveInfo ->
                with(resolveInfo) {
                    if (activityInfo.packageName != context.packageName) {
                        val mainActivityName = activityInfo.name.substring(activityInfo.name.lastIndexOf(".") + 1)
                        val appData = AppData(
                                appName = getAppNameFromPackageName(context, activityInfo.packageName),
                                packageName = "${activityInfo.packageName}/$mainActivityName",
                                appIconDrawable = loadLogo(this, activityInfo.packageName))
                        appDataList.add(appData)
                    }
                }
            }
            val configurationModel = appLockHelper.getConfiguration(ConstantDB.CONFIGURATION_ID_DEFAULT)
            val lockedAppList = configurationModel?.getPackageAppLockList()
            val orderedList = lockedAppList?.let { lockedApp -> orderAppsByLockStatus(appDataList, lockedApp) }
            orderedList?.let { ordered -> it.onSuccess(ordered) }
        }
    }

    private fun orderAppsByLockStatus(allApps: List<AppData>, packageAppLockList: List<String>): List<AppData> {
        val resultList = arrayListOf<AppData>()
        packageAppLockList.forEach { packageAppLock ->
            allApps.forEach { appData ->
                if (TextUtils.equals(packageAppLock, appData.parsePackageName())) {
                    resultList.add(appData)
                }
            }
        }
        val alphabeticOrderList: ArrayList<AppData> = arrayListOf()
        allApps.forEach { appData ->
            if (resultList.contains(appData).not()) {
                alphabeticOrderList.add(appData)
            }
        }
        val appNameList = mutableListOf<String>()
        alphabeticOrderList.forEach {
            appNameList.add(it.appName)
        }
        //
        Collections.sort(appNameList, Collator.getInstance())
        //
        appNameList.forEach { name ->
            alphabeticOrderList.forEach { alphabeticOrder ->
                if (TextUtils.equals(name, alphabeticOrder.appName)) {
                    resultList.add(alphabeticOrder)
                }
            }
        }
        return resultList
    }

    private fun getAppNameFromPackageName(context: Context, packageName: String): String {
        return try {
            val packageManager: PackageManager = context.packageManager
            val info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            packageManager.getApplicationLabel(info) as String
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    private fun loadLogo(resolveInfo: ResolveInfo, appPackageName: String?): Drawable {
        return appPackageName?.let {
            try {
                context.packageManager.getApplicationIcon(it)
            } catch (e: PackageManager.NameNotFoundException) {
                resolveInfo.loadIcon(context.packageManager)
            }
        } ?: resolveInfo.loadIcon(context.packageManager)
    }
}