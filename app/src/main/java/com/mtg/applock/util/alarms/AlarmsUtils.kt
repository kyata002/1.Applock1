package com.mtg.applock.util.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mtg.applock.data.sqllite.model.ConfigurationModel
import com.mtg.applock.service.receiver.AlarmReceiver
import java.util.*

object AlarmsUtils {
    const val ALARM_RECEIVER_ACTION = "ALARM_RECEIVER_ACTION"
    private const val ID_END_DELTA = 100000
    private fun getAlarmIntent(context: Context): Intent {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = ALARM_RECEIVER_ACTION
        return intent
    }

    fun startAllAlarms(context: Context, configurationActiveList: MutableList<ConfigurationModel>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE)
        if (alarmManager !is AlarmManager) return
        configurationActiveList.forEach {
            if (it.isDefaultSetting) {
                // cancel alarm if it actived
                updateAlarms(context, it, false)
            } else {
                updateAlarms(context, it, true)
            }
        }
    }

    fun finishAllAlarms(context: Context, configurationActiveList: MutableList<ConfigurationModel>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE)
        if (alarmManager !is AlarmManager) return
        configurationActiveList.forEach {
            // start
            val senderStart = PendingIntent.getBroadcast(context, it.id, getAlarmIntent(context), PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManager.cancel(senderStart)
            // end
            val senderEnd = PendingIntent.getBroadcast(context, ID_END_DELTA + it.id, getAlarmIntent(context), PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManager.cancel(senderEnd)
        }
    }

    private fun updateAlarms(context: Context, configurationModel: ConfigurationModel, active: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE)
        if (alarmManager !is AlarmManager) return
        configurationModel.let {
            if (active) {
                // start
                val senderStart = PendingIntent.getBroadcast(context, it.id, getAlarmIntent(context), PendingIntent.FLAG_UPDATE_CURRENT)
                val calendar = Calendar.getInstance()
                calendar.firstDayOfWeek = Calendar.SUNDAY
                val day = calendar.get(Calendar.DAY_OF_WEEK) - 1
                //
                calendar.set(Calendar.HOUR_OF_DAY, it.hourStart)
                calendar.set(Calendar.MINUTE, it.minuteStart)
                calendar.set(Calendar.SECOND, 0)
                var dayNearest = day
                var isRunning = true
                while (isRunning) {
                    if (it.every.contains(dayNearest.toString())) {
                        isRunning = false
                    } else {
                        if (dayNearest < 6) {
                            dayNearest += 1
                        } else {
                            dayNearest = 0
                        }
                    }
                }
                if (dayNearest == -1) return@let
                when {
                    dayNearest >= day -> {
                        calendar.set(Calendar.DAY_OF_WEEK, dayNearest + 1)
                    }
                    dayNearest < day -> {
                        calendar.set(Calendar.DAY_OF_WEEK, dayNearest + 1)
                        calendar.add(Calendar.DAY_OF_WEEK, 7)
                    }
                }
                val timeStart = calendar.timeInMillis
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeStart, senderStart)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeStart, senderStart)
                }
                // end
                val senderEnd = PendingIntent.getBroadcast(context, ID_END_DELTA + it.id, getAlarmIntent(context), PendingIntent.FLAG_UPDATE_CURRENT)
                calendar.set(Calendar.HOUR_OF_DAY, it.hourEnd)
                calendar.set(Calendar.MINUTE, it.minuteEnd)
                calendar.add(Calendar.MINUTE, 1)
                calendar.set(Calendar.SECOND, 0)
                val timeEnd = calendar.timeInMillis
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeEnd, senderEnd)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeEnd, senderEnd)
                }
            } else {
                // start
                val senderStart = PendingIntent.getBroadcast(context, it.id, getAlarmIntent(context), PendingIntent.FLAG_UPDATE_CURRENT)
                alarmManager.cancel(senderStart)
                // end
                val senderEnd = PendingIntent.getBroadcast(context, ID_END_DELTA + it.id, getAlarmIntent(context), PendingIntent.FLAG_UPDATE_CURRENT)
                alarmManager.cancel(senderEnd)
            }
        }
    }
}