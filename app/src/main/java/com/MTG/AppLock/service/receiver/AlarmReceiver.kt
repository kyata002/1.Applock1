package com.MTG.AppLock.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.MTG.AppLock.util.ApplicationListUtils
import com.MTG.AppLock.util.alarms.AlarmsUtils

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            AlarmsUtils.ALARM_RECEIVER_ACTION -> {
                if (context != null) {
                    ApplicationListUtils.instance?.reload(context)
                }
            }
        }
    }
}