package com.mtg.applock.util

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import com.mtg.applock.AppLockerApplication
import com.mtg.applock.R
import com.mtg.applock.permissions.PermissionChecker
import com.mtg.applock.ui.activity.main.settings.AdminReceiver
import es.MTG.toasty.Toasty
import java.io.File

object CommonUtils {
    fun isMyServiceRunning(context: Context?, serviceClass: Class<*>): Boolean {
        val manager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        manager?.let {
            for (service in it.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
        }
        return false
    }

    @Suppress("DEPRECATION")
    fun isCanSaveFile(context: Context?): Boolean {
        return isCanSaveFile(context, R.string.msg_not_enough_memory)
    }

    @Suppress("DEPRECATION")
    fun isCanSaveFile(context: Context?, resIdMessage: Int): Boolean {
        if (context == null) {
            return false
        }
        var freeSpace=0L;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            freeSpace= AppLockerApplication.appContext.getExternalFilesDir(null)?.freeSpace!!
        else
            freeSpace = Environment.getExternalStorageDirectory().freeSpace
        if (freeSpace > 100 * 1024 * 1024) { // 100 Mb
            return true
        }
        if (resIdMessage != 0) {
            Toasty.showToast(context, resIdMessage, Toasty.WARNING)
        }
        return false
    }

    fun isCanUseFile(context: Context?, path: String?): Boolean {
        if (TextUtils.isEmpty(path)) {
            context?.let { Toasty.showToast(it, R.string.msg_file_has_problem, Toasty.WARNING) }
            return false
        }
        val file = File(path)
        if (file.exists()) {
            return true
        }
        if (context != null) {
            Toasty.showToast(context, R.string.msg_file_not_found, Toasty.WARNING)
        }
        return false
    }

    fun enableSettings(context: Context?): Boolean {
        return context?.let {
            val devicePolicyManager = it.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val componentName = ComponentName(it, AdminReceiver::class.java)
            return devicePolicyManager.isAdminActive(componentName) && PermissionChecker.checkFullPermission(it)
        } ?: false
    }

    fun isAdminActive(context: Context): Boolean {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(context, AdminReceiver::class.java)
        return devicePolicyManager.isAdminActive(componentName)
    }

    /**
     * validate your email address format
     */
    fun emailValidator(email: String): Boolean {
        return if (TextUtils.isEmpty(email)) {
            false
        } else {
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }

    /**
     * validate your password
     * must have at least one number
     * must have at least one character
     * must be between 6 and 32 characters in length.
     * must not contain spaces
     * Special (Non-Alphanumeric) character -> OK
     */
    fun passwordValidator(password: String, confirmPassword: String): Int {
        return if (TextUtils.isEmpty(password)) {
            PasswordValidate.PASSWORD_EMPTY
        } else {
            var hasNumber = false
            var hasCharacter = false
            var hasSpace = false
            password.forEach {
                when {
                    it.isDigit() -> {
                        hasNumber = true
                    }
                    it.isLetter() -> {
                        hasCharacter = true
                    }
                    it.isWhitespace() -> {
                        hasSpace = true
                    }
                }
            }
            if (!hasNumber) {
                return PasswordValidate.PASSWORD_NO_NUMBER
            }
            if (!hasCharacter) {
                return PasswordValidate.PASSWORD_NO_CHARACTER
            }
            if (password.length < 6) {
                return PasswordValidate.PASSWORD_LENGTH_SHORT
            } else if (password.length > 32) {
                return PasswordValidate.PASSWORD_LENGTH_LONG
            }
            if (hasSpace) {
                return PasswordValidate.PASSWORD_HAS_SPACES
            }
            return if (TextUtils.isEmpty(confirmPassword)) {
                PasswordValidate.PASSWORD_CONFIRM_EMPTY
            } else {
                var hasConfirmNumber = false
                var hasConfirmCharacter = false
                var hasConfirmSpace = false
                confirmPassword.forEach {
                    when {
                        it.isDigit() -> {
                            hasConfirmNumber = true
                        }
                        it.isLetter() -> {
                            hasConfirmCharacter = true
                        }
                        it.isWhitespace() -> {
                            hasConfirmSpace = true
                        }
                    }
                }
                if (!hasConfirmNumber) {
                    return PasswordValidate.PASSWORD_CONFIRM_NO_NUMBER
                }
                if (!hasConfirmCharacter) {
                    return PasswordValidate.PASSWORD_CONFIRM_NO_CHARACTER
                }
                if (confirmPassword.length < 6) {
                    return PasswordValidate.PASSWORD_CONFIRM_LENGTH_SHORT
                } else if (confirmPassword.length > 32) {
                    return PasswordValidate.PASSWORD_CONFIRM_LENGTH_LONG
                }
                if (hasConfirmSpace) {
                    return PasswordValidate.PASSWORD_CONFIRM_HAS_SPACES
                }
                return if (TextUtils.equals(password, confirmPassword)) {
                    PasswordValidate.PASSWORD_VALIDATE_SUCCESS
                } else {
                    PasswordValidate.PASSWORD_CONFIRM_VALIDATE
                }
            }
        }
    }

    object PasswordValidate {
        const val PASSWORD_EMPTY = 0
        const val PASSWORD_NO_NUMBER = 1
        const val PASSWORD_NO_CHARACTER = 2
        const val PASSWORD_LENGTH_SHORT = 3
        const val PASSWORD_LENGTH_LONG = 4
        const val PASSWORD_HAS_SPACES = 5

        //
        const val PASSWORD_CONFIRM_EMPTY = 6
        const val PASSWORD_CONFIRM_NO_NUMBER = 7
        const val PASSWORD_CONFIRM_NO_CHARACTER = 8
        const val PASSWORD_CONFIRM_LENGTH_SHORT = 9
        const val PASSWORD_CONFIRM_LENGTH_LONG = 10
        const val PASSWORD_CONFIRM_HAS_SPACES = 11

        //
        const val PASSWORD_CONFIRM_VALIDATE = 12
        const val PASSWORD_VALIDATE_SUCCESS = 13
    }
}