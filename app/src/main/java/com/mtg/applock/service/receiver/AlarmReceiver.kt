package com.mtg.applock.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mtg.applock.util.ApplicationListUtils
import com.mtg.applock.util.alarms.AlarmsUtils

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