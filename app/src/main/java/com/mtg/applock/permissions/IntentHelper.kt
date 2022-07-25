package com.mtg.applock.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.mtg.applock.BuildConfig

object IntentHelper {
    fun usageAccessIntent(): Intent {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        return intent
    }

    fun getRateAppIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"))
    }
}