package com.mtg.applock.ui.activity.first

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mtg.applock.R
import com.mtg.applock.data.database.pattern.PatternDao
import com.mtg.applock.data.database.pattern.PatternDotMetadata
import com.mtg.applock.data.database.pattern.PatternEntity
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.model.ThemeModel
import com.mtg.applock.ui.activity.password.newpattern.CreateNewPatternViewModel
import com.mtg.applock.ui.activity.password.newpattern.CreateNewPatternViewState
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.CommonUtils
import com.mtg.applock.util.ThemeUtils
import com.mtg.applock.util.extensions.background
import com.mtg.applock.util.extensions.convertToPatternDot
import com.mtg.applock.util.password.PatternChecker
import com.mtg.applock.util.preferences.AppLockerPreferences
import com.mtg.patternlockview.PatternLockView
import es.MTG.toasty.Toasty
import javax.inject.Inject

class FistAllViewModel @Inject constructor(private val patternDao: PatternDao, val appLockerPreferences: AppLockerPreferences, private val appLockHelper: AppLockHelper) : RxAwareViewModel() {
    private var mFirstDrawPattern: ArrayList<PatternLockView.Dot> = arrayListOf()
    private var mRedrawPattern: ArrayList<PatternLockView.Dot> = arrayListOf()
    private var mPinCode: String = ""

    private val patternEventLiveData = MutableLiveData<CreateNewPatternViewState>().apply {
        value = CreateNewPatternViewState(CreateNewPatternViewModel.PatternEvent.INITIALIZE)
    }

    fun getPatternEventLiveData(): LiveData<CreateNewPatternViewState> = patternEventLiveData

    fun setFirstDrawPattern(pattern: List<PatternLockView.Dot>?) {
        pattern?.let {
            this.mFirstDrawPattern.clear()
            this.mFirstDrawPattern.addAll(it)
            if (mFirstDrawPattern.size < 4) {
                mFirstDrawPattern.clear()
                patternEventLiveData.postValue(CreateNewPatternViewState(CreateNewPatternViewModel.PatternEvent.ERROR_SHORT_FIRST))
                return
            }
            patternEventLiveData.postValue(CreateNewPatternViewState(CreateNewPatternViewModel.PatternEvent.FIRST_COMPLETED))
        }
    }

    fun setRedrawnPattern(pattern: List<PatternLockView.Dot>?) {
        pattern?.let {
            this.mRedrawPattern.clear()
            this.mRedrawPattern.addAll(it)
            if (mFirstDrawPattern.size < 4 || mRedrawPattern.size < 4) {
                mRedrawPattern.clear()
                patternEventLiveData.postValue(CreateNewPatternViewState(CreateNewPatternViewModel.PatternEvent.ERROR_SHORT_SECOND))
                return
            }
            if (PatternChecker.checkPatternsEqual(mFirstDrawPattern.convertToPatternDot(), mRedrawPattern.convertToPatternDot())) {
                patternEventLiveData.postValue(CreateNewPatternViewState(CreateNewPatternViewModel.PatternEvent.SECOND_COMPLETED))
            } else {
                mRedrawPattern.clear()
                patternEventLiveData.postValue(CreateNewPatternViewState(CreateNewPatternViewModel.PatternEvent.ERROR))
            }
        }
    }

    fun isFirstPattern(): Boolean = mFirstDrawPattern.isEmpty()

    private fun saveNewCreatedPattern() {
        background.submit {
            val patternMetadata = PatternDotMetadata(mFirstDrawPattern.convertToPatternDot())
            val patternEntity = PatternEntity(patternMetadata)
            patternDao.createPattern(patternEntity)
            appLockHelper.updateThemeDefault(ThemeModel(typeTheme = ThemeUtils.TYPE_PATTERN))
        }
    }

    fun setPinLock(pinCode: String) {
        mPinCode = pinCode
    }

    fun completed(context: Context, superPassword: String, confirmSuperPassword: String, isPattern: Boolean): Boolean {
        return if (passwordValidator(context, superPassword, confirmSuperPassword)) {
            appLockerPreferences.setSuperPassword(superPassword)
            if (isPattern) {
                saveNewCreatedPattern()
            } else {
                background.submit {
                    appLockerPreferences.setPinLock(mPinCode)
                    appLockHelper.updateThemeDefault(ThemeModel(typeTheme = ThemeUtils.TYPE_PIN, isDeletePadding = true, deletePadding = 30))
                }
            }
            true
        } else {
            Toasty.showToast(context, R.string.text_invalid_email_address, Toasty.ERROR)
            false
        }
    }

    fun passwordValidator(context: Context, superPassword: String, confirmSuperPassword: String): Boolean {
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

    fun reset() {
        mFirstDrawPattern.clear()
    }

    fun setLockSettingApp(lockSettingApp: Int) {
        appLockerPreferences.setLockSettingApp(lockSettingApp)
    }

    fun setFinishAllActivity(isFinishAllActivity: Boolean) {
        appLockerPreferences.setFinishAllActivity(isFinishAllActivity)
    }

    fun setFirstCreatePassword(isFirstCreatePassword: Boolean) {
        appLockerPreferences.setFirstCreatePassword(isFirstCreatePassword)
    }
}