package com.MTG.AppLock.ui.activity.main.settings.superpassword

import android.content.Context
import android.text.TextUtils
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.CommonUtils
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import es.MTG.toasty.Toasty
import javax.inject.Inject

class SuperPasswordViewModel @Inject constructor(private val appLockerPreferences: AppLockerPreferences) : RxAwareViewModel() {
    fun noSuperPassword(): Boolean {
        return TextUtils.isEmpty(appLockerPreferences.getSuperPassword())
    }

    fun saveSuperPassword(context: Context, oldPassword: String, superPassword: String, confirmSuperPassword: String): Boolean {
        return if (noSuperPassword()) {
            passwordValidator(context, superPassword, confirmSuperPassword)
        } else {
            if (!TextUtils.equals(oldPassword, appLockerPreferences.getSuperPassword())) {
                Toasty.showToast(context, R.string.msg_old_supper_password_incorrect, Toasty.WARNING)
                false
            } else {
                passwordValidator(context, superPassword, confirmSuperPassword)
            }
        }
    }

    private fun passwordValidator(context: Context, superPassword: String, confirmSuperPassword: String): Boolean {
        return when (CommonUtils.passwordValidator(superPassword, confirmSuperPassword)) {
            CommonUtils.PasswordValidate.PASSWORD_EMPTY -> {
                Toasty.showToast(context, R.string.msg_validate_password_0, Toasty.WARNING)
                false
            }
            CommonUtils.PasswordValidate.PASSWORD_NO_NUMBER -> {
                Toasty.showToast(context, R.string.msg_validate_password_1, Toasty.WARNING)
                false
            }
            CommonUtils.PasswordValidate.PASSWORD_NO_CHARACTER -> {
                Toasty.showToast(context, R.string.msg_validate_password_2, Toasty.WARNING)
                false
            }
            CommonUtils.PasswordValidate.PASSWORD_LENGTH_SHORT -> {
                Toasty.showToast(context, R.string.msg_validate_password_3, Toasty.WARNING)
                false
            }
            CommonUtils.PasswordValidate.PASSWORD_LENGTH_LONG -> {
                Toasty.showToast(context, R.string.msg_validate_password_4, Toasty.WARNING)
                false
            }
            CommonUtils.PasswordValidate.PASSWORD_HAS_SPACES -> {
                Toasty.showToast(context, R.string.msg_validate_password_5, Toasty.WARNING)
                false
            }
            CommonUtils.PasswordValidate.PASSWORD_CONFIRM_EMPTY -> {
                Toasty.showToast(context, R.string.msg_validate_password_6, Toasty.WARNING)
                false
            }
            CommonUtils.PasswordValidate.PASSWORD_CONFIRM_NO_NUMBER -> {
                Toasty.showToast(context, R.string.msg_validate_password_7, Toasty.WARNING)
                false
            }
            CommonUtils.PasswordValidate.PASSWORD_CONFIRM_NO_CHARACTER -> {
                Toasty.showToast(context, R.string.msg_validate_password_8, Toasty.WARNING)
                false
            }
            CommonUtils.PasswordValidate.PASSWORD_CONFIRM_LENGTH_SHORT -> {
                Toasty.showToast(context, R.string.msg_validate_password_9, Toasty.WARNING)
                false
            }
            CommonUtils.PasswordValidate.PASSWORD_CONFIRM_LENGTH_LONG -> {
                Toasty.showToast(context, R.string.msg_validate_password_10, Toasty.WARNING)
                false
            }
            CommonUtils.PasswordValidate.PASSWORD_CONFIRM_HAS_SPACES -> {
                Toasty.showToast(context, R.string.msg_validate_password_11, Toasty.WARNING)
                false
            }
            CommonUtils.PasswordValidate.PASSWORD_VALIDATE_SUCCESS -> {
                appLockerPreferences.setSuperPassword(superPassword)
                true
            }
            CommonUtils.PasswordValidate.PASSWORD_CONFIRM_VALIDATE -> {
                Toasty.showToast(context, R.string.msg_validate_password_12, Toasty.WARNING)
                false
            }
            else -> { // PASSWORD_CONFIRM_VALIDATE
                Toasty.showToast(context, R.string.msg_validate_password_12, Toasty.WARNING)
                false
            }
        }
    }
}