package com.mtg.applock.service.stateprovider

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.text.TextUtils
import com.mtg.applock.model.ForegroundData
import com.mtg.applock.permissions.PermissionChecker
import io.reactivex.Flowable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppForegroundObservable @Inject constructor(val context: Context) {

    fun get(): Flowable<ForegroundData>? {
        return Flowable.interval(100, TimeUnit.MILLISECONDS).filter { PermissionChecker.checkUsageAccessPermission(context) }.map { getLauncherTopApp(context) }.filter { it.className.isNotEmpty() }
                .distinctUntilChanged { foregroundData1, foregroundData2 -> foregroundData1.packageName == foregroundData2.packageName }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("WrongConstant")
    fun getLauncherTopApp(context: Context): ForegroundData {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val endTime = System.currentTimeMillis()
            val beginTime = endTime - 10000
            var packageName = ""
            var className = ""
            val event = UsageEvents.Event()
            val usageStatsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                context.getSystemService(Service.USAGE_STATS_SERVICE) as UsageStatsManager
            } else {
                context.getSystemService("usagestats") as UsageStatsManager
            }
            val usageEvents: UsageEvents = usageStatsManager.queryEvents(beginTime, endTime)
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    className = event.className
                    packageName = event.packageName
                }
            }
            if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(className)) {
                return ForegroundData(className, packageName)
            }
        } else {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val appTasks = activityManager.getRunningTasks(1)
            if (null != appTasks && appTasks.isNotEmpty()) {
                val className = appTasks[0].topActivity?.className ?: ""
                val packageName = appTasks[0].topActivity?.packageName ?: ""
                return ForegroundData(className, packageName)
            }
        }
        return ForegroundData("", "")
    }
}