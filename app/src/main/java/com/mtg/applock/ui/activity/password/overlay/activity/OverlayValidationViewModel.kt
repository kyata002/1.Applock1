package com.mtg.applock.ui.activity.password.overlay.activity

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.mtg.applock.BuildConfig
import com.mtg.applock.data.database.pattern.PatternDao
import com.mtg.applock.data.database.pattern.PatternDot
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.data.sqllite.model.IntruderPhoto
import com.mtg.applock.model.DialogType
import com.mtg.applock.model.ThemeModel
import com.mtg.applock.service.PatternValidatorFunction
import com.mtg.applock.ui.activity.password.overlay.OverlayValidateType
import com.mtg.applock.ui.activity.password.overlay.OverlayViewState
import com.mtg.applock.ui.base.viewmodel.RxAwareAndroidViewModel
import com.mtg.applock.util.ThemeUtils
import com.mtg.applock.util.extensions.plusAssign
import com.mtg.applock.util.file.DirectoryType
import com.mtg.applock.util.file.FileExtension
import com.mtg.applock.util.file.FileManager
import com.mtg.applock.util.file.FileOperationRequest
import com.mtg.applock.util.preferences.AppLockerPreferences
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.File
import javax.inject.Inject

class OverlayValidationViewModel @Inject constructor(val app: Application, private val patternDao: PatternDao, val appLockerPreferences: AppLockerPreferences, private val fileManager: FileManager, private val appLockHelper: AppLockHelper) : RxAwareAndroidViewModel(app) {
    private val patternValidationViewStateLiveData = MediatorLiveData<OverlayViewState>().apply {
        this.postValue(OverlayViewState(isFingerPrintMode = appLockerPreferences.isFingerPrintEnabled()))
    }
    private val mThemeLiveData = MutableLiveData<ThemeModel>()
    private val patternDrawnSubject = PublishSubject.create<List<PatternDot>>()
    private val showForgotPasswordLiveData = MutableLiveData<DialogType>()
    private var mNumberFailed = 0
    private var mNumberFailedFingerprint = 0
    private var mTime = ""

    init {
        if (appLockerPreferences.isUpdate()) {
            deleteTheme()
            appLockerPreferences.setUpdate(false)
        }
        val existingPatternObservable = patternDao.getPattern().map { it.patternMetadata.pattern }
        disposables += Flowable.combineLatest(existingPatternObservable, patternDrawnSubject.toFlowable(BackpressureStrategy.BUFFER), PatternValidatorFunction()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ isValidated ->
            patternValidationViewStateLiveData.postValue(OverlayViewState(overlayValidateType = OverlayValidateType.TYPE_PATTERN, isDrawnCorrect = isValidated, isIntrudersCatcherMode = appLockerPreferences.getIntrudersCatcherEnabled(), isFingerPrintMode = appLockerPreferences.isFingerPrintEnabled()))
        }, { })
        val themeModel = appLockHelper.getThemeDefault()
        mThemeLiveData.postValue(themeModel)
    }

    fun getViewStateObservable(): LiveData<OverlayViewState> = patternValidationViewStateLiveData

    fun getThemeLiveData(): LiveData<ThemeModel> = mThemeLiveData

    fun getShowForgotPasswordLiveData(): LiveData<DialogType> = showForgotPasswordLiveData

    fun onPatternDrawn(pattern: List<PatternDot>) {
        patternDrawnSubject.onNext(pattern)
    }

    fun getIntruderPictureImageFile(): File {
        mTime = System.currentTimeMillis().toString()
        val fileOperationRequest = FileOperationRequest(".IMG_$mTime", FileExtension.JPEG, DirectoryType.EXTERNAL)
        return fileManager.createFile(fileOperationRequest, FileManager.SubFolder.INTRUDERS)
    }

    fun addNumberFailed(packageName: String) {
        mNumberFailed += 1
        if (mNumberFailed == 3) {
            if (TextUtils.isEmpty(packageName) || TextUtils.equals(packageName, BuildConfig.APPLICATION_ID)) {
                showForgotPasswordLiveData.postValue(DialogType.FORGOT_PASSWORD)
            }
        } else if (mNumberFailed >= 6) {
            showForgotPasswordLiveData.postValue(DialogType.FAILED_MORE_PASSWORD)
        } else {
            showForgotPasswordLiveData.postValue(DialogType.NONE)
        }
    }

    fun addNumberFingerprintFailed() {
        mNumberFailedFingerprint += 1
    }

    fun getTime(): String {
        return mTime
    }

    fun isTakePhoto(): Boolean {
        return mNumberFailed > 0 && mNumberFailed % appLockerPreferences.getNumberFailed() == 0 && appLockerPreferences.getIntrudersCatcherEnabled()
    }

    fun isTakePhotoFingerprint(): Boolean {
        return mNumberFailedFingerprint > 0 && mNumberFailedFingerprint % appLockerPreferences.getNumberFailed() == 0 && appLockerPreferences.getIntrudersCatcherEnabled()
    }

    fun getPinLock(): String? {
        return appLockerPreferences.getPinLock()
    }

    fun setShowIntruderDialog(isShowIntruderDialog: Boolean) {
        appLockerPreferences.setShowIntruderDialog(isShowIntruderDialog)
    }

    fun saveIntruder(packageName: String, path: String, time: String) {
        appLockHelper.addIntruder(IntruderPhoto(0, packageName, path, time))
    }

    fun isFingerPrintEnabled(): Boolean {
        return appLockerPreferences.isFingerPrintEnabled()
    }

    fun setLockSettingApp(lockSettingApp: Int) {
        appLockerPreferences.setLockSettingApp(lockSettingApp)
    }

    fun isShowForgotPasswordDialog(): Boolean {
        return appLockerPreferences.isShowForgotPasswordDialog()
    }

    fun setShowForgotPasswordDialog(showDialog: Boolean) {
        appLockerPreferences.setShowForgotPasswordDialog(showDialog)
    }

    fun isShowPathLine(): Boolean {
        return appLockerPreferences.isShowPathLine()
    }

    fun isVibrate(): Boolean {
        return appLockerPreferences.isVibrate()
    }

    fun setFinishAllActivity(isFinishAllActivity: Boolean) {
        appLockerPreferences.setFinishAllActivity(isFinishAllActivity)
    }

    fun hasSuperPassword(): Boolean {
        return !TextUtils.isEmpty(appLockerPreferences.getSuperPassword())
    }

    fun hasEmail(): Boolean {
        return !TextUtils.isEmpty(appLockerPreferences.getEmail())
    }

    private fun deleteTheme() {
        val themeDefault = appLockHelper.getThemeDefault()
        val typeTheme = themeDefault?.typeTheme
        themeDefault?.let {
            appLockHelper.deleteTheme(it)
        }
        appLockHelper.getThemeList(ThemeUtils.TYPE_PIN).forEach {
            appLockHelper.deleteTheme(it)
        }
        appLockHelper.getThemeList(ThemeUtils.TYPE_PATTERN).forEach {
            appLockHelper.deleteTheme(it)
        }
        appLockHelper.getThemeList(ThemeUtils.TYPE_WALLPAPER).forEach {
            appLockHelper.deleteTheme(it)
        }
        typeTheme?.let {
            if (typeTheme == ThemeUtils.TYPE_PATTERN) {
                appLockHelper.addTheme(ThemeModel(typeTheme = ThemeUtils.TYPE_PATTERN))
            } else {
                appLockHelper.addTheme(ThemeModel(typeTheme = ThemeUtils.TYPE_PIN, isDeletePadding = true, deletePadding = 30))
            }
        }
        ThemeUtils.initThemeDefault(appLockHelper, false)
    }
}