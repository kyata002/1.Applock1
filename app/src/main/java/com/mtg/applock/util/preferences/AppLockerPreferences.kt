package com.mtg.applock.util.preferences

import android.content.Context
import android.text.TextUtils
import com.mtg.applock.BuildConfig
import com.mtg.applock.security.AESHelper
import javax.inject.Inject

class AppLockerPreferences @Inject constructor(val context: Context) {
    private val sharedPref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun setFingerPrintEnable(fingerPrintEnabled: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_IS_FINGERPRINT_ENABLE, fingerPrintEnabled)
            apply()
        }
    }

    fun isFingerPrintEnabled(): Boolean = sharedPref.getBoolean(KEY_IS_FINGERPRINT_ENABLE, false)

    fun setIntrudersCatcherEnable(intrudersCatcherEnabled: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_IS_INTRUDERS_CATCHER_ENABLE, intrudersCatcherEnabled)
            apply()
        }
    }

    fun getIntrudersCatcherEnabled(): Boolean = sharedPref.getBoolean(KEY_IS_INTRUDERS_CATCHER_ENABLE, false)

    fun setPinLock(pin: String) {
        with(sharedPref.edit()) {
            putString(KEY_PIN, pin)
            apply()
        }
    }

    fun getPinLock(): String {
        return sharedPref.getString(KEY_PIN, "") ?: ""
    }

    fun getNumberFailed(): Int {
        return 3
    }

    fun setShowIntruderDialog(showDialog: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_SHOW_INTRUDER_DIALOG, showDialog)
            apply()
        }
    }

    fun isShowIntruderDialog(): Boolean {
        return sharedPref.getBoolean(KEY_SHOW_INTRUDER_DIALOG, false)
    }

    fun setShowForgotPasswordDialog(showDialog: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_SHOW_FORGOT_PASSWORD_DIALOG, showDialog)
            apply()
        }
    }

    fun isShowForgotPasswordDialog(): Boolean {
        return sharedPref.getBoolean(KEY_SHOW_FORGOT_PASSWORD_DIALOG, false)
    }

    fun setSuperPassword(superPassword: String) {
        with(sharedPref.edit()) {
            putString(KEY_SUPER_PASSWORD, AESHelper.encrypt(superPassword))
            apply()
        }
    }

    fun getSuperPassword(): String {
        val superPassword = sharedPref.getString(KEY_SUPER_PASSWORD, "") ?: ""
        return if (TextUtils.isEmpty(superPassword)) {
            superPassword
        } else {
            AESHelper.decrypt(superPassword)
        }
    }


    fun setFirst(isFirst: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_IS_FIRST, isFirst)
            apply()
        }
    }

    fun isFirst(): Boolean {
        return sharedPref.getBoolean(KEY_IS_FIRST, true)
    }

    fun setFirstCreatePassword(isFirstCreatePassword: Boolean): AppLockerPreferences {
        with(sharedPref.edit()) {
            putBoolean(KEY_IS_FIRST_CREATE_PASSWORD, isFirstCreatePassword)
            apply()
        }
        return this
    }

    fun isFirstCreatePassword(): Boolean {
        return sharedPref.getBoolean(KEY_IS_FIRST_CREATE_PASSWORD, true)
    }

    fun setReload(isReload: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_IS_RELOAD, isReload)
            apply()
        }
    }

    fun isReload(): Boolean {
        return sharedPref.getBoolean(KEY_IS_RELOAD, false)
    }

    fun setLockSettingApp(lockSettingApp: Int) {
        with(sharedPref.edit()) {
            putInt(KEY_LOCK_SETTING_APP, lockSettingApp)
            apply()
        }
    }

    fun getLockSettingApp(): Int {
        return sharedPref.getInt(KEY_LOCK_SETTING_APP, LOCK_APP_ALWAYS)
    }

    fun setFinish(isFinish: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_IS_FINISH, isFinish)
            apply()
        }
    }

    fun isFinish(): Boolean {
        return sharedPref.getBoolean(KEY_IS_FINISH, false)
    }

    fun setRating(isRating: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_IS_RATING, isRating)
            apply()
        }
    }

    fun isRating(): Boolean = sharedPref.getBoolean(KEY_IS_RATING, false)

    fun setTimeRateFirst(timeRateFirst: Long) {
        with(sharedPref.edit()) {
            putLong(TIME_RATE_FIRST, timeRateFirst)
            apply()
        }
    }

    private fun getTimeRateFirst(): Long = sharedPref.getLong(TIME_RATE_FIRST, 0)

    fun checkShowRateAfterLater(): Boolean {
        val timeLater = getTimeRateFirst()
        return System.currentTimeMillis() - timeLater > 30 * 60 * 1000
    }

    fun setAskLockingNewApplication(askLockingNewApplication: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_ASK_LOOKING_NEW_APPLICATION, askLockingNewApplication)
            apply()
        }
    }

    fun isAskLockingNewApplication(): Boolean = sharedPref.getBoolean(KEY_ASK_LOOKING_NEW_APPLICATION, true)

    fun setVibrate(isVibrate: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_VIBRATE, isVibrate)
            apply()
        }
    }

    fun isVibrate(): Boolean = sharedPref.getBoolean(KEY_VIBRATE, false)

    fun setShowPathLine(showPathLine: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_SHOW_PATH_LINE, showPathLine)
            apply()
        }
    }

    fun isShowPathLine(): Boolean = sharedPref.getBoolean(KEY_SHOW_PATH_LINE, false)

    fun setShowPasswordUninstall(isShowPasswordUninstall: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_IS_SHOW_PASSWORD_UNINSTALL, isShowPasswordUninstall)
            apply()
        }
    }

    fun isShowPasswordUninstall(): Boolean = sharedPref.getBoolean(KEY_IS_SHOW_PASSWORD_UNINSTALL, true)

    fun setFinishAllActivity(isFinishAllActivity: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_FINISH_ALL_ACTIVITY, isFinishAllActivity)
            apply()
        }
    }

    fun isFinishAllActivity(): Boolean = sharedPref.getBoolean(KEY_FINISH_ALL_ACTIVITY, false)

    fun isUpdate(): Boolean = sharedPref.getBoolean(KEY_IS_UPDATE, false)

    fun setUpdate(isUpdate: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_IS_UPDATE, isUpdate)
            apply()
        }
    }

    fun setEmail(email: String) {
        with(sharedPref.edit()) {
            putString(KEY_EMAIL, AESHelper.encrypt(email))
            apply()
        }
    }

    fun getEmail(): String {
        val email = sharedPref.getString(KEY_EMAIL, "") ?: ""
        return if (TextUtils.isEmpty(email)) {
            email
        } else {
            AESHelper.decrypt(email)
        }
    }

    companion object {
        private const val PREFERENCES_NAME = BuildConfig.APPLICATION_ID
        private const val KEY_IS_FINGERPRINT_ENABLE = "KEY_IS_FINGERPRINT_ENABLE"
        private const val KEY_IS_INTRUDERS_CATCHER_ENABLE = "KEY_IS_INTRUDERS_CATCHER_ENABLE"
        private const val KEY_PIN = "KEY_PIN" // mã số
        private const val KEY_SHOW_INTRUDER_DIALOG = "KEY_SHOW_INTRUDER_DIALOG"
        private const val KEY_SHOW_FORGOT_PASSWORD_DIALOG = "KEY_SHOW_FORGOT_PASSWORD_DIALOG"
        private const val KEY_SUPER_PASSWORD = "KEY_SUPER_PASSWORD"
        private const val KEY_EMAIL = "KEY_EMAIL"
        private const val KEY_IS_FIRST = "KEY_IS_FIRST"
        private const val KEY_IS_FIRST_CREATE_PASSWORD = "KEY_IS_FIRST_CREATE_PASSWORD"
        private const val KEY_IS_SHOW_PASSWORD_UNINSTALL = "KEY_IS_SHOW_PASSWORD_UNINSTALL"

        private const val KEY_IS_RELOAD = "KEY_IS_RELOAD"
        private const val KEY_LOCK_SETTING_APP = "KEY_LOCK_SETTING_APP"
        private const val KEY_IS_FINISH = "KEY_IS_FINISH"

        private const val KEY_IS_RATING = "IS_RATING"
        private const val TIME_RATE_FIRST = "TIME_RATE_FIRST"

        private const val KEY_ASK_LOOKING_NEW_APPLICATION = "KEY_ASK_LOOKING_NEW_APPLICATION"
        private const val KEY_VIBRATE = "KEY_VIBRATE"
        private const val KEY_SHOW_PATH_LINE = "KEY_SHOW_PATH_LINE"

        private const val KEY_FINISH_ALL_ACTIVITY = "KEY_FINISH_ALL_ACTIVITY"
        private const val KEY_IS_UPDATE = "KEY_IS_UPDATE"

        const val LOCK_APP_ALWAYS = 0
        const val LOCK_APP_WAIT = 1
        const val LOCK_APP_NONE = 2
        const val LOCK_APP_WAIT_FINGERPRINT = 3
    }
}