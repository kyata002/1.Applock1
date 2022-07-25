package com.mtg.applock.ui.activity.main.settings.superpassword.validate

import android.content.Context
import com.mtg.applock.R
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.CommonUtils
import com.mtg.applock.util.preferences.AppLockerPreferences
import es.MTG.toasty.Toasty
import javax.inject.Inject

class SuperPasswordValidateViewModel @Inject constructor(private val appLockerPreferences: AppLockerPreferences) : RxAwareViewModel() {
    fun checkSuperPassword(context: Context, password: String): Boolean {
        return when (CommonUtils.passwordValidator(appLockerPreferences.getSuperPassword(), password)) {
//            CommonUtils.PasswordValidate.PASSWORD_EMPTY -> {
//                Toasty.showToast(context, R.string.msg_validate_password_0, Toasty.WARNING)
//                false
//            }
//            CommonUtils.PasswordValidate.PASSWORD_NO_NUMBER -> {
//                Toasty.showToast(context, R.string.msg_validate_password_1, Toasty.WARNING)
//                false
//            }
//            CommonUtils.PasswordValidate.PASSWORD_NO_CHARACTER -> {
//                Toasty.showToast(context, R.string.msg_validate_password_2, Toasty.WARNING)
//                false
//            }
//            CommonUtils.PasswordValidate.PASSWORD_LENGTH_SHORT -> {
//                Toasty.showToast(context, R.string.msg_validate_password_3, Toasty.WARNING)
//                false
//            }
//            CommonUtils.PasswordValidate.PASSWORD_LENGTH_LONG -> {
//                Toasty.showToast(context, R.string.msg_validate_password_4, Toasty.WARNING)
//                false
//            }
//            CommonUtils.PasswordValidate.PASSWORD_HAS_SPACES -> {
//                Toasty.showToast(context, R.string.msg_validate_password_5, Toasty.WARNING)
//                false
//            }
//            CommonUtils.PasswordValidate.PASSWORD_CONFIRM_EMPTY -> {
//                Toasty.showToast(context, R.string.msg_validate_password_6, Toasty.WARNING)
//                false
//            }
//            CommonUtils.PasswordValidate.PASSWORD_CONFIRM_NO_NUMBER -> {
//                Toasty.showToast(context, R.string.msg_validate_password_7, Toasty.WARNING)
//                false
//            }
//            CommonUtils.PasswordValidate.PASSWORD_CONFIRM_NO_CHARACTER -> {
//                Toasty.showToast(context, R.string.msg_validate_password_8, Toasty.WARNING)
//                false
//            }
//            CommonUtils.PasswordValidate.PASSWORD_CONFIRM_LENGTH_SHORT -> {
//                Toasty.showToast(context, R.string.msg_validate_password_9, Toasty.WARNING)
//                false
//            }
//            CommonUtils.PasswordValidate.PASSWORD_CONFIRM_LENGTH_LONG -> {
//                Toasty.showToast(context, R.string.msg_validate_password_10, Toasty.WARNING)
//                false
//            }
//            CommonUtils.PasswordValidate.PASSWORD_CONFIRM_HAS_SPACES -> {
//                Toasty.showToast(context, R.string.msg_validate_password_11, Toasty.WARNING)
//                false
//            }
            CommonUtils.PasswordValidate.PASSWORD_VALIDATE_SUCCESS -> {
                true
            }
//            CommonUtils.PasswordValidate.PASSWORD_CONFIRM_VALIDATE -> {
//                Toasty.showToast(context, R.string.msg_supper_password_incorrect, Toasty.WARNING)
//                false
//            }
            else -> { // PASSWORD_CONFIRM_VALIDATE
                Toasty.showToast(context, R.string.msg_supper_password_incorrect, Toasty.WARNING)
                false
            }
        }
    }

    fun setLockSettingApp(lockSettingApp: Int) {
        appLockerPreferences.setLockSettingApp(lockSettingApp)
    }

    fun setFinishAllActivity(isFinishAllActivity: Boolean) {
        appLockerPreferences.setFinishAllActivity(isFinishAllActivity)
    }
}