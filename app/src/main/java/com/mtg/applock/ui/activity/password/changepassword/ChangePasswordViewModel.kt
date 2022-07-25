package com.mtg.applock.ui.activity.password.changepassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mtg.applock.data.database.pattern.PatternDao
import com.mtg.applock.data.database.pattern.PatternDotMetadata
import com.mtg.applock.data.database.pattern.PatternEntity
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.ui.activity.password.newpattern.CreateNewPatternViewModel
import com.mtg.applock.ui.activity.password.newpattern.CreateNewPatternViewState
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.ThemeUtils
import com.mtg.applock.util.extensions.background
import com.mtg.applock.util.extensions.convertToPatternDot
import com.mtg.applock.util.password.PatternChecker
import com.mtg.applock.util.preferences.AppLockerPreferences
import com.mtg.patternlockview.PatternLockView
import javax.inject.Inject

class ChangePasswordViewModel @Inject constructor(private val patternDao: PatternDao, val appLockerPreferences: AppLockerPreferences, private val appLockHelper: AppLockHelper) : RxAwareViewModel() {
    private var firstDrawPattern: ArrayList<PatternLockView.Dot> = arrayListOf()
    private var redrawPattern: ArrayList<PatternLockView.Dot> = arrayListOf()
    private var mPinCode: String = ""
    private val typeThemeLiveData = MutableLiveData<Boolean>()

    fun getTypeThemeLiveData(): LiveData<Boolean> = typeThemeLiveData

    init {
        appLockHelper.getThemeDefault()?.let { theme ->
            val isPattern = theme.typeTheme == ThemeUtils.TYPE_PATTERN
            typeThemeLiveData.postValue(isPattern)
        }
    }

    private val patternEventLiveData = MutableLiveData<CreateNewPatternViewState>().apply {
        value = CreateNewPatternViewState(CreateNewPatternViewModel.PatternEvent.INITIALIZE)
    }

    fun getPatternEventLiveData(): LiveData<CreateNewPatternViewState> = patternEventLiveData

    fun setFirstDrawPattern(pattern: List<PatternLockView.Dot>?) {
        pattern?.let {
            this.firstDrawPattern.clear()
            this.firstDrawPattern.addAll(it)
            if (firstDrawPattern.size < 4) {
                firstDrawPattern.clear()
                patternEventLiveData.postValue(CreateNewPatternViewState(CreateNewPatternViewModel.PatternEvent.ERROR_SHORT_FIRST))
                return
            }
            patternEventLiveData.postValue(CreateNewPatternViewState(CreateNewPatternViewModel.PatternEvent.FIRST_COMPLETED))
        }
    }

    fun setRedrawnPattern(pattern: List<PatternLockView.Dot>?) {
        pattern?.let {
            this.redrawPattern.clear()
            this.redrawPattern.addAll(it)
            if (firstDrawPattern.size < 4 || redrawPattern.size < 4) {
                redrawPattern.clear()
                patternEventLiveData.postValue(CreateNewPatternViewState(CreateNewPatternViewModel.PatternEvent.ERROR_SHORT_SECOND))
                return
            }
            if (PatternChecker.checkPatternsEqual(firstDrawPattern.convertToPatternDot(), redrawPattern.convertToPatternDot())) {
                patternEventLiveData.postValue(CreateNewPatternViewState(CreateNewPatternViewModel.PatternEvent.SECOND_COMPLETED))
            } else {
                redrawPattern.clear()
                patternEventLiveData.postValue(CreateNewPatternViewState(CreateNewPatternViewModel.PatternEvent.ERROR))
            }
        }
    }

    fun isFirstPattern(): Boolean = firstDrawPattern.isEmpty()

    private fun saveNewCreatedPattern() {
        background.submit {
            val patternMetadata = PatternDotMetadata(firstDrawPattern.convertToPatternDot())
            val patternEntity = PatternEntity(patternMetadata)
            patternDao.createPattern(patternEntity)
        }
    }

    fun setPinLock(pinCode: String) {
        mPinCode = pinCode
    }

    fun getPinLock(): String? {
        return appLockerPreferences.getPinLock()
    }

    fun completed(isPattern: Boolean) {
        if (isPattern) {
            saveNewCreatedPattern()
        } else {
            background.submit {
                appLockerPreferences.setPinLock(mPinCode)
            }
        }
    }

    fun reset() {
        firstDrawPattern.clear()
    }
}