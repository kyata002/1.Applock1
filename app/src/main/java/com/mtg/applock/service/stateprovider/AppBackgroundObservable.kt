package com.mtg.applock.service.stateprovider

import android.annotation.SuppressLint
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.text.TextUtils
import com.mtg.applock.model.ForegroundData
import io.reactivex.Flowable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppBackgroundObservable @Inject constructor(val context: Context) {
    @SuppressLint("WrongConstant")
    fun get(): Flowable<ForegroundData>? {
        return Flowable.interval(100, TimeUnit.MILLISECONDS).map {
            var packageName = ""
            var className = ""
            val mUsageStatsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                context.getSystemService(Service.USAGE_STATS_SERVICE) as UsageStatsManager
            } else {
                context.getSystemService("usagestats") as UsageStatsManager
            }
            val time = System.currentTimeMillis()
            val usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 3600, time)
            val event = UsageEvents.Event()
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                    packageName = event.packageName
                    className = event.className
                }
            }
            if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(className)) {
                ForegroundData(className, packageName)
            } else {
                ForegroundData("", "")
            }
        }.distinctUntilChanged { foregroundData1, foregroundData2 -> foregroundData1.packageName == foregroundData2.packageName }
    }
}