package com.mtg.applock.service

import android.app.PendingIntent
import android.content.*
import android.content.res.Configuration
import android.os.IBinder
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mtg.applock.BuildConfig
import com.mtg.applock.R
import com.mtg.applock.data.database.pattern.PatternDao
import com.mtg.applock.data.database.pattern.PatternDot
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.data.sqllite.ConstantDB
import com.mtg.applock.data.sqllite.model.ConfigurationModel
import com.mtg.applock.model.ForegroundData
import com.mtg.applock.permissions.PermissionChecker
import com.mtg.applock.service.notification.ServiceNotificationManager
import com.mtg.applock.service.receiver.HomeWatcher
import com.mtg.applock.service.stateprovider.AppBackgroundObservable
import com.mtg.applock.service.stateprovider.AppForegroundObservable
import com.mtg.applock.ui.activity.asklocknewapplication.AskLockNewApplicationView
import com.mtg.applock.ui.activity.asklocknewapplication.AskLockNewApplicationViewLayoutParams
import com.mtg.applock.ui.activity.first.FirstActivity
import com.mtg.applock.ui.activity.password.overlay.activity.settings.OverlayValidationForSettingsActivity
import com.mtg.applock.ui.activity.password.overlay.view.OverlayViewLayoutParams
import com.mtg.applock.ui.activity.password.overlay.view.PatternOverlayView
import com.mtg.applock.ui.activity.password.overlay.view.failedmorepassword.FailedMorePasswordView
import com.mtg.applock.ui.activity.password.overlay.view.failedmorepassword.FailedMorePasswordViewLayoutParams
import com.mtg.applock.ui.activity.splash.SplashActivity
import com.mtg.applock.ui.base.BusMessage
import com.mtg.applock.util.CommonUtils
import com.mtg.applock.util.Const
import com.mtg.applock.util.EventBusUtils
import com.mtg.applock.util.extensions.convertToPatternDot
import com.mtg.applock.util.extensions.plusAssign
import com.mtg.applock.util.extensions.vibrate
import com.mtg.applock.util.file.DirectoryType
import com.mtg.applock.util.file.FileExtension
import com.mtg.applock.util.file.FileManager
import com.mtg.applock.util.file.FileOperationRequest
import com.mtg.applock.util.preferences.AppLockerPreferences
import com.mtg.applock.util.uninstall.UninstallUtils
import com.mtg.patternlockview.PatternLockView
import com.mtg.fingerprint.FingerprintIdentify
import com.mtg.fingerprint.base.BaseFingerprint
import com.mtg.fingerprint.manager.FingerprintManagerCompat
import com.mtg.pinlock.PinLockViewV2
import dagger.android.DaggerService
import es.MTG.toasty.Toasty
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import javax.inject.Inject

class AppLockerService : DaggerService(), PinLockViewV2.OnLockScreenLoginCompactListener, PatternOverlayView.OnShowForgotPasswordListener, FailedMorePasswordView.OnFailedMorePasswordListener, AskLockNewApplicationView.OnAskLockNewApplicationListener, PatternOverlayView.OnFingerprintListener, PinLockViewV2.OnFingerprintClick {
    @Inject
    lateinit var mServiceNotificationManager: ServiceNotificationManager

    @Inject
    lateinit var mAppForegroundObservable: AppForegroundObservable

    @Inject
    lateinit var mAppBackgroundObservable: AppBackgroundObservable

    @Inject
    lateinit var mPatternDao: PatternDao

    @Inject
    lateinit var mAppLockerPreferences: AppLockerPreferences

    @Inject
    lateinit var mAppLockHelper: AppLockHelper

    @Inject
    lateinit var mFileManager: FileManager
    private val mValidatedPatternObservable = PublishSubject.create<List<PatternDot>>()
    private val mAllDisposables: CompositeDisposable = CompositeDisposable()
    private var mForegroundAppDisposable: Disposable? = null
    private var mBackgroundAppDisposable: Disposable? = null
    private lateinit var mWindowManager: WindowManager
    private lateinit var mOverlayParams: WindowManager.LayoutParams
    private lateinit var mFailedMorePasswordViewLayoutParams: WindowManager.LayoutParams
    private lateinit var mAskLockNewApplicationViewLayoutParams: WindowManager.LayoutParams
    private lateinit var mOverlayView: PatternOverlayView
    private lateinit var mAskLockNewApplicationView: AskLockNewApplicationView
    private lateinit var mFailedMorePasswordView: FailedMorePasswordView
    private var mIsOverlayShowing = false
    private var mIsFailedMorePasswordShowing = false
    private var mIsAskLockNewApplicationShowing = false
    private lateinit var mHomeWatcher: HomeWatcher
    private var mNumberFailed = 0
    private var mNumberFailedFingerprint = 0
    private var mFingerprintIdentify: FingerprintIdentify? = null
    private var mLastForegroundAppPackage = ""
    private var mScreenOnOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    mAppLockerPreferences.setLockSettingApp(AppLockerPreferences.LOCK_APP_ALWAYS)
                    observeForegroundApplication()
                    observeBackgroundApplication()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    closeAllActivity()
                    if (checkLock()) {
                        goHome()
                    }
                    stopForegroundApplicationObserver()
                }
            }
        }
    }
    private var mInstallReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!mAppLockerPreferences.isAskLockingNewApplication()) return
            val extras = intent?.extras
            var replace = false
            extras?.let {
                replace = it.containsKey(Intent.EXTRA_REPLACING) && extras.getBoolean(Intent.EXTRA_REPLACING)
            }
            if (replace) return
            when (intent?.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    // show
                    intent.dataString?.let { packageNameFull ->
                        val packageName = packageNameFull.replaceFirst("package:", "")
                        if (TextUtils.equals(packageName, BuildConfig.APPLICATION_ID)) return
                        if (mAppLockHelper.isApplicationLocked(packageName)) return
                        if (PermissionChecker.checkOverlayPermission(this@AppLockerService).not()) return
                        if (PermissionChecker.checkUsageAccessPermission(this@AppLockerService).not()) return
                        if (UninstallUtils.isAppInSystemPartition(this@AppLockerService, packageName)) return
                        if (UninstallUtils.isSignedBySystem(this@AppLockerService, packageName)) return
                        showAskLockNewApplication(packageName)
                    }
                }
            }
        }
    }

    private var mLocaleChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_LOCALE_CHANGED -> {
                  //  adjustFontScale(resources.configuration)
                    initializeAskLockNewApplication()
                    initializeFailedMorePassword()
                }
            }
        }
    }
    private lateinit var mTime: String

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
      //  adjustFontScale(resources.configuration)
        EventBus.getDefault().register(this)
        mHomeWatcher = HomeWatcher(this)
        mHomeWatcher.setOnHomePressedListener(object : HomeWatcher.OnHomePressedListener {
            override fun onHomePressed() {
                closeAllActivity()
                observeForegroundApplication()
                observeBackgroundApplication()
                hideOverlay()
                mAppLockerPreferences.setLockSettingApp(AppLockerPreferences.LOCK_APP_ALWAYS)
                hideFailedMorePassword()
                mAppLockerPreferences.setReload(true)
                startAppLockService()
                hideAskLockNewApplication()
            }

            override fun onRecentAppPressed() {
                closeAllActivity()
                observeForegroundApplication()
                observeBackgroundApplication()
                hideOverlay()
                mAppLockerPreferences.setLockSettingApp(AppLockerPreferences.LOCK_APP_ALWAYS)
                hideFailedMorePassword()
                mAppLockerPreferences.setReload(true)
                startAppLockService()
                hideAskLockNewApplication()
            }
        })
        mHomeWatcher.startWatch()
        initializeFailedMorePassword()

        initializeAppLockerNotification()

        initializeOverlayView()
        initializeAskLockNewApplication()

        registerScreenReceiver()

        registerInstallReceiver()

        registerLocaleChangedReceiver()

        observeOverlayView()

        observeForegroundApplication()
        observeBackgroundApplication()
    }

    private fun initializeFailedMorePassword() {
        mFailedMorePasswordViewLayoutParams = FailedMorePasswordViewLayoutParams.get()
        mFailedMorePasswordView = FailedMorePasswordView(this)
        mFailedMorePasswordView.setOnFailedMorePasswordListener(this)
    }

    override fun onDestroy() {
        ContextCompat.startForegroundService(this, Intent(this, AppLockerService::class.java))
        unregisterScreenReceiver()
        unregisterInstallReceiver()
        unregisterLocaleChangeReceiver()
        if (mAllDisposables.isDisposed.not()) {
            mAllDisposables.dispose()
        }
        mHomeWatcher.stopWatch()
        mFingerprintIdentify?.cancelIdentify()
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    private fun registerInstallReceiver() {
        val installFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addDataScheme("package")
        }
        registerReceiver(mInstallReceiver, installFilter)
    }

    private fun unregisterInstallReceiver() {
        unregisterReceiver(mInstallReceiver)
    }

    private fun registerLocaleChangedReceiver() {
        val localeChangeFilter = IntentFilter().apply {
            addAction(Intent.ACTION_LOCALE_CHANGED)
        }
        registerReceiver(mLocaleChangeReceiver, localeChangeFilter)
    }

    private fun unregisterLocaleChangeReceiver() {
        unregisterReceiver(mLocaleChangeReceiver)
    }

    private fun registerScreenReceiver() {
        val screenFilter = IntentFilter()
        screenFilter.addAction(Intent.ACTION_SCREEN_ON)
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(mScreenOnOffReceiver, screenFilter)
    }

    private fun unregisterScreenReceiver() {
        unregisterReceiver(mScreenOnOffReceiver)
    }

    private fun observeOverlayView() {
        mAllDisposables += Flowable.combineLatest(mPatternDao.getPattern().map { it.patternMetadata.pattern }, mValidatedPatternObservable.toFlowable(BackpressureStrategy.BUFFER), PatternValidatorFunction()).subscribe(this@AppLockerService::onPatternValidated)
    }

    private fun initializeOverlayView() {
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mOverlayParams = OverlayViewLayoutParams.get()
        mOverlayView = PatternOverlayView(this).apply {
            observePattern(this@AppLockerService::onDrawPattern)
        }
        mOverlayView.setOnLockScreenLoginCompactListener(this)
        mOverlayView.setOnShowForgotPasswordListener(this)
        mOverlayView.setOnFingerprintListener(this)
        mOverlayView.setOnFingerprintClick(this)
    }

    private fun initializeAskLockNewApplication() {
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mAskLockNewApplicationView = AskLockNewApplicationView(this)
        mAskLockNewApplicationView.setOnAskLockNewApplicationListener(this)
        mAskLockNewApplicationViewLayoutParams = AskLockNewApplicationViewLayoutParams.get()
    }

    private fun observeForegroundApplication() {
        initFingerprint()
        //
        if (mForegroundAppDisposable != null && mForegroundAppDisposable?.isDisposed?.not() == true) {
            return
        }
        mForegroundAppDisposable = mAppForegroundObservable.get()?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe({ foregroundData -> onAppForeground(foregroundData) }, { error -> })
        mForegroundAppDisposable?.let { mAllDisposables.add(it) }
    }

    private fun observeBackgroundApplication() {
        if (mBackgroundAppDisposable != null && mBackgroundAppDisposable?.isDisposed?.not() == true) {
            return
        }
        mBackgroundAppDisposable = mAppBackgroundObservable.get()?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe({ foregroundData -> onAppBackground(foregroundData) }, { error -> })
        mBackgroundAppDisposable?.let { mAllDisposables.add(it) }
    }

    private fun initFingerprint() {
        if (mAppLockerPreferences.isFingerPrintEnabled()) {
            mFingerprintIdentify = FingerprintIdentify(this)
            mFingerprintIdentify?.setSupportAndroidL(true)
            mFingerprintIdentify?.init()
            mOverlayView.showFingerprint(enableFingerprint())
        } else {
            mOverlayView.showFingerprint(false)
        }
    }

    private fun stopForegroundApplicationObserver() {
        if (mForegroundAppDisposable != null && mForegroundAppDisposable?.isDisposed?.not() == true) {
            mForegroundAppDisposable?.dispose()
        }
    }

    private fun checkLock(): Boolean {
        // return true -> goToHome
        // return false -> nothing
        return when (mLastForegroundAppPackage) {
            BuildConfig.APPLICATION_ID -> true
            Const.SETTINGS_PACKAGE -> true
//            Const.SMART_LOG_PACKAGE -> true
//            Const.CH_PLAY_PACKAGE -> true
            else -> {
                val configurationModel = mAppLockHelper.getConfiguration(ConstantDB.CONFIGURATION_ID_DEFAULT)
                if (CommonUtils.enableSettings(this)) {
                    configurationModel?.let {
                        if (!it.getPackageAppLockList().contains(Const.CH_PLAY_PACKAGE)) {
                            it.addPackageAppLock(Const.CH_PLAY_PACKAGE)
                        }
                    }
                }
                val lockedAppList = configurationModel?.getPackageAppLockList()
                lockedAppList?.let {
                    if (it.contains(mLastForegroundAppPackage)) {
                        return true
                    }
                }
                // configuration active list
                val configurationActiveList = mAppLockHelper.getConfigurationActiveList()
                // lock with time
                val configurationList = mutableListOf<ConfigurationModel>()
                configurationActiveList.forEach {
                    it.getPackageAppLockList().forEach { packageName ->
                        if (TextUtils.equals(packageName, mLastForegroundAppPackage)) {
                            configurationList.add(it)
                        }
                    }
                }
                if (configurationActiveList.isNullOrEmpty()) {
                    return false
                } else {
                    configurationList.forEach {
                        if (it.isCurrentTimeAboutStartEnd()) {
                            return true
                        }
                    }
                    false
                }
            }
        }
    }

    private fun onAppBackground(foregroundData: ForegroundData) {
        //        val backgroundAppPackage = foregroundData.packageName
        //        val className = foregroundData.className
        //        Log.d("TAG1", "backgroundAppPackage = $backgroundAppPackage")
        //        Log.d("TAG1", "className = $className")
    }

    private fun onAppForeground(foregroundData: ForegroundData) {
        val foregroundAppPackage = foregroundData.packageName
        val className = foregroundData.className
//        Log.d("TAG1", "foregroundAppPackage = $foregroundAppPackage")
//        Log.d("TAG1", "className = $className")
        mLastForegroundAppPackage = foregroundAppPackage
        if (CommonUtils.enableSettings(this)) {
            if (TextUtils.equals(className, Const.CLASS_NAME_UNINSTALLER)) {
                if (mAppLockerPreferences.isShowPasswordUninstall()) {
                    showOverlay(foregroundAppPackage)
                } else {
                    mAppLockerPreferences.setShowPasswordUninstall(true)
                }
                return
            }
        }
        if (TextUtils.equals(foregroundAppPackage, BuildConfig.APPLICATION_ID)) {
            if (!mAppLockerPreferences.isReload()) return
            when {
                TextUtils.equals(className, SplashActivity::class.java.name) -> {
                    // nothing
                }
                TextUtils.equals(className, FirstActivity::class.java.name) -> {
                    // nothing
                }
                else -> {
                    closeAllActivity()
                }
            }
        } else {
            val configurationModel = mAppLockHelper.getConfiguration(ConstantDB.CONFIGURATION_ID_DEFAULT)
            if (CommonUtils.enableSettings(this)) {
                configurationModel?.let {
                    if (!it.getPackageAppLockList().contains(Const.SETTINGS_PACKAGE)) {
                        it.addPackageAppLock(Const.SETTINGS_PACKAGE)
                    }
                    if (!it.getPackageAppLockList().contains(Const.CH_PLAY_PACKAGE)) {
                        it.addPackageAppLock(Const.CH_PLAY_PACKAGE)
                    }
                }
            }
            if (TextUtils.equals(foregroundAppPackage, Const.SMART_LOG_PACKAGE)) {
                mAppLockerPreferences.setReload(false)
                hideOverlay()
                return
            }
            val lockedAppList = configurationModel?.getPackageAppLockList()
            lockedAppList?.let {
                if (it.contains(foregroundAppPackage)) {
                    if (TextUtils.equals(foregroundAppPackage, Const.SETTINGS_PACKAGE)) {
                        when (mAppLockerPreferences.getLockSettingApp()) {
                            AppLockerPreferences.LOCK_APP_NONE -> {
                                hideOverlay()
                            }
                            AppLockerPreferences.LOCK_APP_ALWAYS -> {
                                val intent = OverlayValidationForSettingsActivity.newIntent(this)
                                intent.putExtra(Const.EXTRA_PACKAGE_NAME, foregroundAppPackage)
                                intent.putExtra(Const.EXTRA_CLASS_NAME, className)
                                val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                try {
                                    pendingIntent.send()
                                } catch (e: PendingIntent.CanceledException) {
                                    e.printStackTrace()
                                }
                                hideOverlay(false)
                                closeAllActivity()
                            }
                            AppLockerPreferences.LOCK_APP_WAIT_FINGERPRINT -> {
                                // nothing
                            }
                            else -> {
                                hideOverlay()
                                mAppLockerPreferences.setLockSettingApp(AppLockerPreferences.LOCK_APP_ALWAYS)
                            }
                        }
                    } else {
                        showOverlay(foregroundAppPackage)
                    }
                } else {
                    checkAppInGroupLock(foregroundAppPackage, className)
                }
            } ?: checkAppInGroupLock(foregroundAppPackage, className)
        }
    }

    private fun closeAllActivity() {
        mAppLockerPreferences.setFinishAllActivity(true)
    }

    private fun checkAppInGroupLock(foregroundAppPackage: String, className: String) {
        // configuration active list
        val configurationActiveList = mAppLockHelper.getConfigurationActiveList()
        // lock with time
        val configurationList = mutableListOf<ConfigurationModel>()
        configurationActiveList.forEach {
            it.getPackageAppLockList().forEach { packageName ->
                if (TextUtils.equals(packageName, foregroundAppPackage)) {
                    configurationList.add(it)
                }
            }
        }
        if (configurationList.isNullOrEmpty()) {
            hideOverlay()
        } else {
            var show = false
            configurationList.forEach {
                if (it.isCurrentTimeAboutStartEnd()) {
                    show = true
                }
            }
            if (show) {
                showOverlay(foregroundAppPackage)
            } else {
                hideOverlay()
            }
        }
    }

    private fun onDrawPattern(pattern: List<PatternLockView.Dot>) {
        mValidatedPatternObservable.onNext(pattern.convertToPatternDot())
    }

    private fun onPatternValidated(isDrawedPatternCorrect: Boolean) {
        if (isDrawedPatternCorrect) {
            mOverlayView.notifyDrawnCorrect()
            hideOverlay()
            mFingerprintIdentify?.cancelIdentify()
        } else {
            mOverlayView.notifyDrawnWrong()
            addNumberFailed()
            intruderPhoto()
            vibrate(this, mAppLockerPreferences.isVibrate())
        }
    }

    private fun initializeAppLockerNotification() {
        val notification = mServiceNotificationManager.createNotification()
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID_APP_LOCKER_SERVICE, notification)
        startForeground(NOTIFICATION_ID_APP_LOCKER_SERVICE, notification)
    }

    private fun showOverlay(lockedAppPackageName: String) {
        if (mIsOverlayShowing.not()) {
            mNumberFailed = 0
            mNumberFailedFingerprint = 0
            mIsOverlayShowing = true
            mOverlayView.setAppPackageName(lockedAppPackageName)
            mOverlayView.resetTime()
            mOverlayView.setCodeValidation()
            mOverlayView.setBlockView(false)
            mOverlayView.checkUiOverlay()
            mWindowManager.addView(mOverlayView, mOverlayParams)
            onFingerprint()
        }
    }

    private fun hideOverlay() {
        hideOverlay(true)
    }

    private fun hideOverlay(isFull: Boolean) {
        if (mIsOverlayShowing) {
            mNumberFailed = 0
            mNumberFailedFingerprint = 0
            mIsOverlayShowing = false
            mWindowManager.removeViewImmediate(mOverlayView)
            if (isFull) {
                if (mAppLockerPreferences.getLockSettingApp() == AppLockerPreferences.LOCK_APP_WAIT) {
                    mAppLockerPreferences.setLockSettingApp(AppLockerPreferences.LOCK_APP_NONE)
                } else {
                    mAppLockerPreferences.setLockSettingApp(AppLockerPreferences.LOCK_APP_ALWAYS)
                }
            }
        }
    }

    private fun showFailedMorePassword() {
        if (mIsFailedMorePasswordShowing.not()) {
            mIsFailedMorePasswordShowing = true
            mOverlayView.setBlockView(true)
            mWindowManager.addView(mFailedMorePasswordView, mFailedMorePasswordViewLayoutParams)
        }
        mAppLockerPreferences.setShowForgotPasswordDialog(true)
    }

    private fun hideFailedMorePassword() {
        if (mIsFailedMorePasswordShowing) {
            mIsFailedMorePasswordShowing = false
            mWindowManager.removeViewImmediate(mFailedMorePasswordView)
        }
    }

    override fun onSuccess() {
        mAppLockerPreferences.setLockSettingApp(AppLockerPreferences.LOCK_APP_NONE)
        hideOverlay()
        mFingerprintIdentify?.cancelIdentify()
    }

    override fun onFailed(isFingerprint: Boolean) {
        if (isFingerprint) {
            addNumberFingerprintFailed()
            intruderPhotoFingerprint()
        } else {
            addNumberFailed()
            intruderPhoto()
        }
        vibrate(this, mAppLockerPreferences.isVibrate())
    }

    override fun onClose() {
        goHome()
        hideOverlay()
        mAppLockerPreferences.setReload(true)
        startAppLockService()
    }

    private fun startAppLockService() {
        if (!CommonUtils.isMyServiceRunning(this, AppLockerService::class.java)) {
            ContextCompat.startForegroundService(this, Intent(this, AppLockerService::class.java))
        }
    }

    override fun showForgotPasswordDialog() {
        goHome()
        hideOverlay()
    }

    override fun showFailedMorePasswordDialog() {
        goHome()
        hideOverlay()
    }

    override fun onFingerprint() {
        val enableFingerprint = enableFingerprint()
        mOverlayView.showFingerprint(enableFingerprint)
        if (!enableFingerprint) return
        mFingerprintIdentify?.startIdentify(3, object : BaseFingerprint.IdentifyListener {
            override fun onSucceed() {
                hideOverlay()
            }

            override fun onNotMatch(availableTimes: Int) {
                addNumberFingerprintFailed()
                intruderPhotoFingerprint()
                Toasty.showToast(this@AppLockerService, R.string.msg_fingerprint_no_match, Toasty.ERROR)
                vibrate(this@AppLockerService, mAppLockerPreferences.isVibrate())
            }

            override fun onFailed(isDeviceLocked: Boolean) {
                if (isDeviceLocked) {
                    // lock
                    mOverlayView.showFingerprint(false)
                    Toasty.showToast(this@AppLockerService, R.string.msg_failed_more_fingerprint, Toasty.ERROR)
                } else {
                    addNumberFingerprintFailed()
                    intruderPhotoFingerprint()
                    Toasty.showToast(this@AppLockerService, R.string.msg_fingerprint_no_match, Toasty.ERROR)
                    vibrate(this@AppLockerService, mAppLockerPreferences.isVibrate())
                }
            }

            override fun onLockFingerprint() {
                mOverlayView.showFingerprint(false)
                Toasty.showToast(this@AppLockerService, R.string.msg_warning_fingerprint_lock, Toasty.ERROR)
            }

            override fun onStartFailedByDeviceLocked() {
                Toasty.showToast(this@AppLockerService, R.string.msg_failed_more_fingerprint, Toasty.ERROR)
                mOverlayView.showFingerprint(false)
                mFingerprintIdentify?.cancelIdentify()
            }
        })
    }

    override fun onFingerprintClick() {
        onFingerprint()
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        startActivity(homeIntent)
    }

    private fun addNumberFailed() {
        mNumberFailed += 1
        if (mNumberFailed == NUMBER_FAILED_MAX) {
            showFailedMorePassword()
        }
    }

    private fun addNumberFingerprintFailed() {
        mNumberFailedFingerprint += 1
    }

    private fun isTakePhoto(): Boolean {
        return mNumberFailed > 0 && mNumberFailed % mAppLockerPreferences.getNumberFailed() == 0 && mAppLockerPreferences.getIntrudersCatcherEnabled()
    }

    private fun isTakePhotoFingerprint(): Boolean {
        return mNumberFailedFingerprint > 0 && mNumberFailedFingerprint % mAppLockerPreferences.getNumberFailed() == 0 && mAppLockerPreferences.getIntrudersCatcherEnabled()
    }

    private fun intruderPhoto() {
        if (isTakePhoto()) {
            setShowIntruderDialog()
            // take picture
            mTime = System.currentTimeMillis().toString()
            mOverlayView.takePicture(getIntruderPictureImageFile(mTime), mTime)
        }
    }

    private fun getIntruderPictureImageFile(time: String): File {
        val fileOperationRequest = FileOperationRequest(".IMG_$time", FileExtension.JPEG, DirectoryType.EXTERNAL)
        return mFileManager.createFile(fileOperationRequest, FileManager.SubFolder.INTRUDERS)
    }

    private fun intruderPhotoFingerprint() {
        if (isTakePhotoFingerprint()) {
            setShowIntruderDialog()
            // take picture
            mTime = System.currentTimeMillis().toString()
            mOverlayView.takePicture(getIntruderPictureImageFile(mTime), mTime)
        }
    }

    private fun setShowIntruderDialog() {
        mAppLockerPreferences.setShowIntruderDialog(true)
    }

    override fun onCloseFailedMorePasswordView() {
        goHome()
        hideOverlay()
        hideFailedMorePassword()
    }

    private fun showAskLockNewApplication(packageName: String) {
        if (mIsAskLockNewApplicationShowing.not()) {
            mIsAskLockNewApplicationShowing = true
            mAskLockNewApplicationView.setAppPackageName(packageName)
            mWindowManager.addView(mAskLockNewApplicationView, mAskLockNewApplicationViewLayoutParams)
        } else {
            mAskLockNewApplicationView.setAppPackageName(packageName)
        }
    }

    private fun hideAskLockNewApplication() {
        if (mIsAskLockNewApplicationShowing) {
            mIsAskLockNewApplicationShowing = false
            mWindowManager.removeView(mAskLockNewApplicationView)
        }
    }

    override fun onCloseAskLockNewApplication() {
        hideAskLockNewApplication()
    }

    override fun onLockAskLockNewApplication(packageName: String) {
        hideAskLockNewApplication()
        val configurationModel = mAppLockHelper.getConfiguration(ConstantDB.CONFIGURATION_ID_DEFAULT)
        configurationModel?.let {
            if (TextUtils.isEmpty(it.packageAppLock)) {
                it.addPackageAppLock(packageName)
                it.addPackageAppLock(Const.CH_PLAY_PACKAGE)
                it.addPackageAppLock(Const.SETTINGS_PACKAGE)
            } else {
                it.addPackageAppLock(packageName)
            }
            mAppLockHelper.updateConfiguration(it)
        }
    }

    override fun onOpenAppAskLockNewApplication(packageName: String) {
        hideAskLockNewApplication()
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(launchIntent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEvent(event: BusMessage) {
        if (TextUtils.isEmpty(event.message)) return
        when (event.message) {
            EventBusUtils.EVENT_UPDATE_FINGERPRINT -> {
                initFingerprint()
            }
        }
        EventBus.getDefault().postSticky(BusMessage(""))
    }

    private fun enableFingerprint(): Boolean {
        val manager: FingerprintManagerCompat = FingerprintManagerCompat.from(this)
        return mAppLockerPreferences.isFingerPrintEnabled() && manager.isHardwareDetected && manager.hasEnrolledFingerprints()
    }

    //in base activity add this code.
    private fun adjustFontScale(configuration: Configuration) {
        configuration.fontScale = 1.0.toFloat()
        val metrics: DisplayMetrics = resources.displayMetrics
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        metrics.scaledDensity = configuration.fontScale * metrics.density
        configuration.densityDpi = resources.displayMetrics.xdpi.toInt()
        baseContext.resources.updateConfiguration(configuration, metrics)
    }

    companion object {
        private const val NOTIFICATION_ID_APP_LOCKER_SERVICE = 1
        private const val NUMBER_FAILED_MAX = 3
    }
}