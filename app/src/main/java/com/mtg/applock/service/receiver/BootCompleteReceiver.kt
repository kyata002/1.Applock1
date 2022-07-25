package com.mtg.applock.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.mtg.applock.service.AppLockerService

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (TextUtils.equals(Intent.ACTION_BOOT_COMPLETED, intent?.action)) {
            context?.let { ContextCompat.startForegroundService(it, Intent(it, AppLockerService::class.java)) }
        }
    }
}