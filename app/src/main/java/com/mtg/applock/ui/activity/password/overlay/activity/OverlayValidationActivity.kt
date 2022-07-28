package com.mtg.applock.ui.activity.password.overlay.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Observer
import com.mtg.applock.BuildConfig
import com.mtg.applock.R
import com.mtg.applock.model.DialogType
import com.mtg.applock.ui.activity.main.MainActivity
import com.mtg.applock.ui.activity.main.settings.email.emailvalidate.EmailValidateActivity
import com.mtg.applock.ui.activity.main.settings.superpassword.validate.SuperPasswordValidateActivity
import com.mtg.applock.ui.activity.password.newpattern.SimplePatternListener
import com.mtg.applock.ui.base.IntruderHiddenCameraActivity
import com.mtg.applock.util.ApplicationListUtils
import com.mtg.applock.util.Const
import com.mtg.applock.util.ThemeUtils
import com.mtg.applock.util.extensions.*
import com.mtg.applock.util.file.FilePathHelper
import com.mtg.applock.util.preferences.AppLockerPreferences
import com.mtg.patternlockview.PatternLockView
import com.androidhiddencamera.CameraConfig
import com.androidhiddencamera.CameraError
import com.androidhiddencamera.config.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestOptions
import com.common.control.interfaces.AdCallback
import com.common.control.manager.AdmobManager
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.mtg.fingerprint.FingerprintIdentify
import com.mtg.fingerprint.base.BaseFingerprint
import com.mtg.pinlock.PinLockConfiguration
import com.mtg.pinlock.PinLockViewV2
import com.mtg.pinlock.control.OnLockScreenLoginListener
import com.mtg.pinlock.extension.decodeBase64
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.activity_overlay_validation.*
import kotlinx.android.synthetic.main.dialog_failed.view.*
import kotlinx.android.synthetic.main.dialog_forgot_password.view.*
import kotlinx.android.synthetic.main.layout_view_pattern_overlay.view.*
import java.io.File

open class OverlayValidationActivity : IntruderHiddenCameraActivity<OverlayValidationViewModel>() {
    private lateinit var mCameraConfig: CameraConfig
    private lateinit var mPackageName: String
    private var mForgotPasswordDialog: AlertDialog? = null
    private var mFailedMorePasswordDialog: AlertDialog? = null
    private lateinit var mFingerprintIdentify: FingerprintIdentify
    private var mIsChangePassword: Boolean = false
    private var mBannerAd: AdView? = null
    private var interAds: InterstitialAd? = null
    private var mTakePicture: Boolean = false
    private var mHandler: Handler? = null
    private var mHandlerExtend: Handler? = null
    private var mCameraError: Boolean = false
    private var mTime: Long = 0L
    var mClassName: String = ""

    override fun getViewModel(): Class<OverlayValidationViewModel> =
        OverlayValidationViewModel::class.java

    override fun getLayoutId(): Int {
        return R.layout.activity_overlay_validation
    }

    override fun initViews() {
        loadNav()
        mIsChangePassword = intent.getBooleanExtra(Const.EXTRA_CHANGE_PASSWORD, false)
        mFingerprintIdentify = FingerprintIdentify(this)
        mFingerprintIdentify.setSupportAndroidL(true)
        mFingerprintIdentify.init()
        mPackageName = intent.getStringExtra(Const.EXTRA_PACKAGE_NAME) ?: ""
        mClassName = intent.getStringExtra(Const.EXTRA_CLASS_NAME) ?: ""
        updateIcon(FilePathHelper.getIconIcon(this, mPackageName))
        //
        startCamera()
        //
        viewModel.getViewStateObservable().observe(this, {
            it.showNotification(this)
            if (it.isDrawnCorrect == true || it.fingerPrintResultData?.isSuccess() == true) {
                goMainActivity()
            }
            if (it.isDrawnCorrect == false || it.fingerPrintResultData?.isNotSuccess() == true) {
                vibrate(this, viewModel.isVibrate())
                if (!mIsChangePassword) {
                    viewModel.addNumberFailed(mPackageName)
                    if (it.isIntrudersCatcherMode) {
                        intruderPhoto()
                    }
                }
            }
        })
        viewModel.getThemeLiveData().observe(this) {
            it?.let { theme ->
                when (theme.typeTheme) {
                    ThemeUtils.TYPE_PIN -> {
                        clPatternView.gone()
                        pinLock.visible()
                        if (mIsChangePassword) {
                            avatarLock.gone()
                            tvTitle.gone()
                            tvTitlePin.visible()
                            Toasty.showToast(
                                this@OverlayValidationActivity,
                                R.string.msg_pin_lock_current_password,
                                Toasty.NORMAL
                            )
                        } else {
                            tvTitle.gone()
                            tvTitlePin.gone()
                        }
                    }
                    else -> {
                        clPatternView.visible()
                        pinLock.gone()
                        if (mIsChangePassword) {
                            avatarLock.gone()
                            tvTitle.visible()
                            tvTitlePin.gone()
                            Toasty.showToast(
                                this@OverlayValidationActivity,
                                R.string.pattern_draw_your_unlock_current_pattern,
                                Toasty.NORMAL
                            )
                        } else {
                            tvTitle.gone()
                            tvTitlePin.gone()
                        }
                    }
                }
//                if (theme.backgroundResId != 0) {
////                    imageBackground.setImageResource(theme.backgroundResId)
//                    pinLock.setImageResourcePinLock(theme.backgroundResId)
//                } else {
//                    val drawable = Drawable.createFromPath(theme.backgroundDownload)
//                    if (drawable == null) {
////                        imageBackground.setImageResource(R.drawable.background_theme_default)
//                        pinLock.setImageResourcePinLock(R.drawable.background_theme_default)
//                    } else {
//                        //                        imageBackground.setImageDrawable(drawable)
//                        pinLock.setImageDrawablePinLock(drawable)
//                        Glide.with(this).load(drawable).apply(
//                            RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE)
//                                .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
//                        )
////                            .into(imageBackground)
//                    }
//                }
                if (theme.selectedResId != 0) {
                    patternLockView.setBitmapSelected(
                        AppCompatResources.getDrawable(
                            this,
                            theme.selectedResId
                        )?.toBitmap()
                    )
                } else {
                    patternLockView.setBitmapSelected(BitmapFactory.decodeFile(theme.selectedDownload))
                }
                if (theme.unselectedResId != 0) {
                    patternLockView.setBitmapUnSelected(
                        AppCompatResources.getDrawable(
                            this,
                            theme.unselectedResId
                        )?.toBitmap()
                    )
                } else {
                    patternLockView.setBitmapUnSelected(BitmapFactory.decodeFile(theme.unselectedDownload))
                }
                //
                val bitmapSelectedList = mutableListOf<Bitmap>()
                if (theme.selectedResIdList.isNotEmpty()) {
                    theme.selectedResIdList.forEach { bitmapSelected ->
                        AppCompatResources.getDrawable(this, bitmapSelected)?.toBitmap()
                            ?.let { it1 -> bitmapSelectedList.add(it1) }
                    }
                }
                patternLockView.setBitmapSelectedList(bitmapSelectedList)
                val bitmapUnselectedList = mutableListOf<Bitmap>()
                if (theme.unselectedResIdList.isNotEmpty()) {
                    theme.unselectedResIdList.forEach { bitmapUnselected ->
                        AppCompatResources.getDrawable(this, bitmapUnselected)?.toBitmap()
                            ?.let { it1 -> bitmapUnselectedList.add(it1) }
                    }
                }
                patternLockView.setBitmapUnSelectedList(bitmapUnselectedList)
                //
                if (theme.lineColorResId != 0) {
                    patternLockView.correctStateColor =
                        ContextCompat.getColor(this, theme.lineColorResId)
                    patternLockView.normalStateColor =
                        ContextCompat.getColor(this, theme.lineColorResId)
                } else {
                    try {
                        if (!TextUtils.isEmpty(theme.lineColor) && theme.lineColor.startsWith("#")) {
                            patternLockView.correctStateColor = Color.parseColor(theme.lineColor)
                            patternLockView.normalStateColor = Color.parseColor(theme.lineColor)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                val builder =
                    PinLockConfiguration.Builder().setMode(PinLockConfiguration.Config.MODE_AUTH)
                        .setDeletePadding(theme.isDeletePadding)
                        .setNumberPadding(theme.isNumberPadding)
                        .setDeletePadding(theme.deletePadding).setResPinList(theme.buttonResList)
                        .setPinUrlList(theme.buttonDownloadList())
                        .setResColorButton(theme.colorButtonResId)
                        .setResSelectorCheckbox(theme.selectorCheckboxColorResId)
                        .setColorCheckbox(theme.selectorCheckboxColor)
                        .setResColorMessage(theme.textMsgColorResId)
                        .setColorMessage(theme.textMsgColor)
                        .setNextTitle(getString(R.string.text_create))
                viewModel.getPinLock()?.let { pinLock ->
                    builder.setCodeLength(pinLock.decodeBase64().length)
                }
                pinLock.applyConfiguration(builder.build())
            }
        }
        patternLockView.addPatternLockListener(object : SimplePatternListener() {
            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                super.onComplete(pattern)
                if (System.currentTimeMillis() - mTime < Const.TIME_DELAY) {
                    return
                }
                mTime = System.currentTimeMillis()
                pattern?.let { viewModel.onPatternDrawn(it.convertToPatternDot()) }
                patternLockView.clearPattern()
                patternLockView.isInputEnabled = false
                mHandler = Handler()
                mHandler?.postDelayed({
                    if (mTakePicture.not()) {
                        patternLockView.isInputEnabled = true
                    } else {
                        if (mCameraError) {
                            patternLockView.isInputEnabled = true
                        } else {
                            mHandlerExtend = Handler()
                            mHandlerExtend?.postDelayed({
                                patternLockView.isInputEnabled = true
                                mTakePicture = false
                            }, Const.TIME_DELAY_EXTEND)
                        }
                    }
                }, Const.TIME_DELAY)
            }

            override fun onTouch(inputEnabled: Boolean) {
                super.onTouch(inputEnabled)
                Toasty.hideToast()
            }
        })
        patternLockView.isInStealthMode = !viewModel.isShowPathLine()
        pinLock.setCodeValidation(viewModel.getPinLock())
        pinLock.setOnLockScreenLoginListener(object : OnLockScreenLoginListener {
            override fun onCodeInputSuccessful() {
                goMainActivity()
            }

            override fun onPinLoginFailed() {
                if (!mIsChangePassword) {
                    viewModel.addNumberFailed(mPackageName)
                    intruderPhoto()
                }
                vibrate(this@OverlayValidationActivity, viewModel.isVibrate())
                Toasty.showToast(
                    this@OverlayValidationActivity,
                    R.string.msg_pin_lock_error_login,
                    Toasty.ERROR
                )
                pinLock.setInputEnabled(false)
                if (System.currentTimeMillis() - mTime < Const.TIME_DELAY) {
                    return
                }
                mTime = System.currentTimeMillis()
                mHandler = Handler()
                mHandler?.postDelayed({
                    if (mTakePicture.not()) {
                        pinLock.setInputEnabled(true)
                    } else {
                        if (mCameraError) {
                            pinLock.setInputEnabled(true)
                        } else {
                            mHandlerExtend = Handler()
                            mHandlerExtend?.postDelayed({
                                pinLock.setInputEnabled(true)
                                mTakePicture = false
                            }, Const.TIME_DELAY_EXTEND)
                        }
                    }
                }, Const.TIME_DELAY)
            }

            override fun onFingerprintSuccessful() {
                goMainActivity()
            }

            override fun onFingerprintLoginFailed() {
                viewModel.addNumberFingerprintFailed()
                vibrate(this@OverlayValidationActivity, viewModel.isVibrate())
                intruderPhotoFingerprint()
            }
        })
        //
        viewModel.getShowForgotPasswordLiveData().observe(this, Observer {
            when (it) {
                DialogType.FORGOT_PASSWORD -> {
                    // show dialog forget password
                    if (viewModel.hasSuperPassword()) {
                        showForgotPasswordDialog()
                    } else if (viewModel.hasEmail()) {
                        showForgotPasswordDialog()
                    }
                }
                DialogType.FAILED_MORE_PASSWORD -> {
                    // show dialog failed more password
                    showFailedMorePasswordDialog()
                }
            }
        })
        fingerPattern.setOnClickListener {
            startFingerPrint()
        }
        if (viewModel.isFingerPrintEnabled() && mFingerprintIdentify.isHardwareEnable && mFingerprintIdentify.isFingerprintEnable) {
            startFingerPrint()
            pinLock.showFingerprint(true)
            pinLock.setOnFingerprintClick(object : PinLockViewV2.OnFingerprintClick {
                override fun onFingerprintClick() {
                    startFingerPrint()
                }
            })
        } else {
            fingerPattern.gone()
            pinLock.showFingerprint(false)
        }
        if (viewModel.isShowForgotPasswordDialog()) {
            showForgotPasswordDialog()
            viewModel.setShowForgotPasswordDialog(false)
        }
        if (!mIsChangePassword) {
            if (Resources.getSystem().displayMetrics.density > 1.5) {
                pinLock.setGoneNext(false)
            }
        } else {
            fingerPattern.gone()
            pinLock.showFingerprint(false)
            pinLock.showImageIcon(false)
            pinLock.setGoneNext(true)
            flAds.gone()
        }
    }

    open fun goMainActivity() {
        viewModel.setFinishAllActivity(false)
        if (mIsChangePassword) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            val intent = MainActivity.newIntent(this)
            startActivity(intent)
            finish()
            showInter()
        }
    }

    private fun startFingerPrint() {
        mFingerprintIdentify.startIdentify(3, object : BaseFingerprint.IdentifyListener {
            override fun onSucceed() {
                goMainActivity()
            }

            override fun onNotMatch(availableTimes: Int) {
                viewModel.addNumberFingerprintFailed()
                vibrate(this@OverlayValidationActivity, viewModel.isVibrate())
                intruderPhotoFingerprint()
                Toasty.showToast(
                    this@OverlayValidationActivity,
                    R.string.msg_fingerprint_no_match,
                    Toasty.ERROR
                )
            }

            override fun onFailed(isDeviceLocked: Boolean) {
                if (isDeviceLocked) {
                    // lock
                    fingerPattern.gone()
                    pinLock.showFingerprint(false)
                    Toasty.showToast(
                        this@OverlayValidationActivity,
                        R.string.msg_failed_more_fingerprint,
                        Toasty.ERROR
                    )
                } else {
                    viewModel.addNumberFingerprintFailed()
                    vibrate(this@OverlayValidationActivity, viewModel.isVibrate())
                    intruderPhotoFingerprint()
                    Toasty.showToast(
                        this@OverlayValidationActivity,
                        R.string.msg_fingerprint_no_match,
                        Toasty.ERROR
                    )
                }
            }

            override fun onLockFingerprint() {
                fingerPattern.gone()
                pinLock.showFingerprint(false)
                Toasty.showToast(
                    this@OverlayValidationActivity,
                    R.string.msg_warning_fingerprint_lock,
                    Toasty.ERROR
                )
            }

            override fun onStartFailedByDeviceLocked() {
                Toasty.showToast(
                    this@OverlayValidationActivity,
                    R.string.msg_failed_more_fingerprint,
                    Toasty.ERROR
                )
                fingerPattern.gone()
                pinLock.showFingerprint(false)
            }
        })
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null, false)
        builder.setView(view)
        view.btnNoForgotPassword.setOnClickListener { mForgotPasswordDialog?.dismiss() }
        view.btnOkForgotPassword.setOnClickListener {
            mForgotPasswordDialog?.dismiss()
            if (viewModel.hasSuperPassword()) {
                startActivity(SuperPasswordValidateActivity.newIntent(this))
                finish()
            } else if (viewModel.hasEmail()) {
                startActivity(EmailValidateActivity.newIntent(this))
                finish()
            }
        }
        mForgotPasswordDialog?.dismiss()
        mForgotPasswordDialog = builder.create()
        mForgotPasswordDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mForgotPasswordDialog?.setCancelable(false)
        mForgotPasswordDialog?.show()
        dialogLayout(mForgotPasswordDialog)
    }

    private fun showFailedMorePasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_failed, null, false)
        builder.setView(view)
        view.btnOkFailedMorePassword.setOnClickListener {
            mFailedMorePasswordDialog?.dismiss()
            if (TextUtils.isEmpty(mPackageName) || TextUtils.equals(
                    mPackageName,
                    Const.SETTINGS_PACKAGE
                ) || TextUtils.equals(mPackageName, BuildConfig.APPLICATION_ID)
            ) {
                onBackPressed()
            }
        }
        mFailedMorePasswordDialog?.dismiss()
        mFailedMorePasswordDialog = builder.create()
        mFailedMorePasswordDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mFailedMorePasswordDialog?.setCancelable(false)
        mFailedMorePasswordDialog?.show()
        dialogLayout(mFailedMorePasswordDialog)
    }

    private fun intruderPhoto() {
        if (viewModel.isTakePhoto() && mTakePicture.not()) {
            viewModel.setShowIntruderDialog(true)
            mTakePicture = true
            Handler(Looper.getMainLooper()).post {
                try {
                    takePicture(viewModel.getIntruderPictureImageFile(), viewModel.getTime())
                } catch (e: Exception) {
                    e.printStackTrace()
                    onCameraError(CameraError.ERROR_IMAGE_WRITE_FAILED)
                }
            }
        }
    }

    private fun intruderPhotoFingerprint() {
        if (viewModel.isTakePhotoFingerprint() && mTakePicture.not()) {
            viewModel.setShowIntruderDialog(true)
            mTakePicture = true
            Handler(Looper.getMainLooper()).post {
                try {
                    takePicture(viewModel.getIntruderPictureImageFile(), viewModel.getTime())
                } catch (e: Exception) {
                    e.printStackTrace()
                    onCameraError(CameraError.ERROR_IMAGE_WRITE_FAILED)
                }
            }
        }
    }

    private fun startCamera() {
        // setup camera config
        mCameraConfig =
            CameraConfig().getBuilder(this).setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                .setCameraResolution(CameraResolution.HIGH_RESOLUTION)
                .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                .setImageRotation(CameraRotation.ROTATION_270).setCameraFocus(CameraFocus.AUTO)
                .build()
        //Check for the camera permission for the runtime
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //Start camera preview
            startCamera(mCameraConfig)
        }
    }

    override fun onBackPressed() {
        if (mIsChangePassword) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        } else {
            ApplicationListUtils.instance?.destroy()
            viewModel.setLockSettingApp(AppLockerPreferences.LOCK_APP_ALWAYS)
            val intent = Intent("android.intent.action.MAIN")
            intent.addCategory("android.intent.category.HOME")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        mBannerAd?.pause()
    }

    override fun onResume() {
        super.onResume()
        loadInter()
        mBannerAd?.resume()
    }
    private fun loadNav(){
        AdmobManager.getInstance().loadNative(this, BuildConfig.native_open_app, frAds,R.layout.custom_native_media)
    }

    private fun showInter() {
        AdmobManager.getInstance().showInterstitial(this, interAds, object : AdCallback() {
//            override fun onAdClosed() {
//                super.onAdClosed()
//                onBackPressed()
//            }
        })
    }

    private fun loadInter() {
        AdmobManager.getInstance()
            .loadInterAds(this, com.mtg.applock.BuildConfig.inter_open_app, object :
                AdCallback() {
                override fun onResultInterstitialAd(interstitialAd: InterstitialAd?) {
                    super.onResultInterstitialAd(interstitialAd)
                    interAds = interstitialAd
                }
            })
    }

    override fun onDestroy() {
        mForgotPasswordDialog?.dismiss()
        mFailedMorePasswordDialog?.dismiss()
        mFingerprintIdentify.cancelIdentify()
        mBannerAd?.destroy()
        mHandler?.removeCallbacksAndMessages(null)
        mHandlerExtend?.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun updateIcon(drawable: Drawable?) {
        pinLock.setIcon(drawable)
        avatarLock.setImageDrawable(drawable)
    }

    override fun onImageCapture(imageFile: File, time: String) {
        viewModel.saveIntruder(mPackageName, imageFile.absolutePath, time)
        mTakePicture = false
        pinLock.setInputEnabled(true)
        patternLockView.isInputEnabled = true
    }

    override fun onCameraError(errorCode: Int) {
        super.onCameraError(errorCode)
        mTakePicture = false
        pinLock.setInputEnabled(true)
        patternLockView.isInputEnabled = true
        when (errorCode) {
            CameraError.ERROR_CAMERA_OPEN_FAILED -> {
                mCameraError = true
            }
            CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE -> {
                mCameraError = true
            }
            CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION -> {
                mCameraError = true
            }
            CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA -> {
                mCameraError = true
            }
            else -> {
                mCameraError = false
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, OverlayValidationActivity::class.java)
        }
    }
}