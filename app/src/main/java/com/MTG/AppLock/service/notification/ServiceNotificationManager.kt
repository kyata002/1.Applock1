package com.MTG.AppLock.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.MTG.AppLock.R
import javax.inject.Inject

class ServiceNotificationManager @Inject constructor(val context: Context) {
    fun createNotification(): Notification {
        createAppLockerServiceChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_APP_LOCKER_SERVICE)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_note_icon))
                .setContentTitle(context.getString(R.string.notification_protecting_title))
                .setContentText(context.getString(R.string.notification_protecting_description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_APP_LOCKER_SERVICE, notification)
        return notification
    }

    private fun createAppLockerServiceChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID_APP_LOCKER_SERVICE, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID_APP_LOCKER_SERVICE = "CHANNEL_ID_APP_LOCKER_SERVICE"
        private const val NOTIFICATION_ID_APP_LOCKER_SERVICE = 1
    }
}