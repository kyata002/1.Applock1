package com.MTG.AppLock.ui.activity.password.overlay.activity.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.MTG.AppLock.ui.activity.password.overlay.activity.OverlayValidationActivity
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.preferences.AppLockerPreferences

class OverlayValidationForSettingsActivity : OverlayValidationActivity() {
    override fun goMainActivity() {
        viewModel.setLockSettingApp(AppLockerPreferences.LOCK_APP_WAIT)
//        Log.d("TAG1", "OverlayValidationForSettingsActivity.mClassName = $mClassName")
        when {
            mClassName.contains("DeviceAdminSettingsActivity") -> {
                val intent = Intent()
                intent.component = ComponentName("com.android.settings", "com.android.settings.Settings\$DeviceAdminSettingsActivity")
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(intent)
                finish()
            }
            else -> {
                finish()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mClassName = intent?.getStringExtra(Const.EXTRA_CLASS_NAME) ?: ""
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, OverlayValidationForSettingsActivity::class.java)
        }
    }
}