package com.MTG.AppLock.ui.activity.main.settings.email.emailvalidate

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.CommonUtils
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import javax.inject.Inject

class EmailValidateViewModel @Inject constructor(private val appLockerPreferences: AppLockerPreferences) : RxAwareViewModel() {
    private var emailValidateLiveData = MutableLiveData<Int>()
    fun checkEmail(email: String) {
        when {
            TextUtils.isEmpty(email) -> {
                emailValidateLiveData.postValue(EmailValidateActivity.FAILED_EMPTY)
            }
            !CommonUtils.emailValidator(email) -> {
                emailValidateLiveData.postValue(EmailValidateActivity.FAILED_FORMAT)
            }
            !TextUtils.isEmpty(appLockerPreferences.getEmail()) && TextUtils.equals(email, appLockerPreferences.getEmail()) -> {
                emailValidateLiveData.postValue(EmailValidateActivity.SUCCESS)
            }
            else -> {
                emailValidateLiveData.postValue(EmailValidateActivity.FAILED)
            }
        }
    }

    fun getEmailValidateLiveData(): LiveData<Int> {
        return emailValidateLiveData
    }

    fun setLockSettingApp(lockSettingApp: Int) {
        appLockerPreferences.setLockSettingApp(lockSettingApp)
    }

    fun setFinishAllActivity(isFinishAllActivity: Boolean) {
        appLockerPreferences.setFinishAllActivity(isFinishAllActivity)
    }
}