package com.MTG.AppLock.ui.activity.first

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.ui.activity.splash.SplashActivity
import com.MTG.AppLock.util.ConfigurationUtils
import com.MTG.AppLock.util.ThemeUtils
import com.MTG.AppLock.util.preferences.AppLockerPreferences

class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appLockerPreferences = AppLockerPreferences(this)
        appLockerPreferences.setFinishAllActivity(false)
        if (appLockerPreferences.isFirstCreatePassword()) {
            createConfigurationDefault(this)
            startActivity(FirstAllActivity.newIntent(this))
        } else {
            startActivity(SplashActivity.newIntent(this))
        }
        finish()
    }

    private fun createConfigurationDefault(context: Context) {
        // create and add configuration default
        val appLockHelper = AppLockHelper(context)
        if (AppLockerPreferences(context).isFirst()) {
            ConfigurationUtils.initConfigurationDefault(appLockHelper)
            ThemeUtils.initThemeDefault(appLockHelper, true)
            AppLockerPreferences(context).setFirst(false)
        }
    }
}