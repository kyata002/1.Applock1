package com.mtg.applock.ui.activity.password.overlay.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresPermission
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.data.sqllite.model.IntruderPhoto
import com.mtg.applock.ui.activity.password.newpattern.SimplePatternListener
import com.mtg.applock.util.Const
import com.mtg.applock.util.ThemeUtils
import com.mtg.applock.util.extensions.gone
import com.mtg.applock.util.extensions.visible
import com.mtg.applock.util.file.FilePathHelper
import com.mtg.applock.util.preferences.AppLockerPreferences
import com.mtg.patternlockview.PatternLockView
import com.androidhiddencamera.*
import com.androidhiddencamera.config.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestOptions
import com.common.control.manager.AdmobManager
import com.mtg.applock.BuildConfig
import com.mtg.pinlock.PinLockConfiguration
import com.mtg.pinlock.PinLockViewV2
import com.mtg.pinlock.control.OnLockScreenLoginListener
import com.mtg.pinlock.extension.decodeBase64
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.layout_view_pattern_overlay.view.*
import java.io.File
import com.mtg.applock.R

class PatternOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), CameraCallbacks {
    private val appLockerPreferences = AppLockerPreferences(context)
    private val appLockHelper = AppLockHelper(context)
    private var onPatternCompleted: ((List<PatternLockView.Dot>) -> Unit)? = null
    private var mOnLockScreenLoginCompactListenerV2: PinLockViewV2.OnLockScreenLoginCompactListener? =
        null
    private var mOnShowForgotPasswordListener: OnShowForgotPasswordListener? = null
    private var mOnFingerprintListener: OnFingerprintListener? = null
    private var mOnFingerprintClick: PinLockViewV2.OnFingerprintClick? = null
    private var mIsBlock = false
    private var mAppPackageName: String = ""
    private var mCameraPreview: CameraPreview? = null
    private var mCachedCameraConfig: CameraConfig? = null
    private var mCameraConfig: CameraConfig? = null
    private var mRootView: View =
        LayoutInflater.from(context).inflate(R.layout.layout_view_pattern_overlay, this, true)

    private var mTakePicture: Boolean = false
    private var mHandler: Handler? = null
    private var mHandlerExtend: Handler? = null
    private var mCameraError: Boolean = false
    private var mTime: Long = 0L

    init {
        mIsBlock = false
        //Add the camera preview surface to the root of the activity view.
        mCameraPreview = addPreView()
        initListener()
    }

    private fun initListener() {
        patternLockView.addPatternLockListener(object : SimplePatternListener() {
            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                super.onComplete(pattern)
                if (System.currentTimeMillis() - mTime < Const.TIME_DELAY) {
                    return
                }
                mTime = System.currentTimeMillis()
                //
                pattern?.let { onPatternCompleted?.invoke(it) }
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
        pinLock.setOnLockScreenLoginListener(object : OnLockScreenLoginListener {
            override fun onCodeInputSuccessful() {
                mOnLockScreenLoginCompactListenerV2?.onSuccess()
            }

            override fun onFingerprintLoginFailed() {
                mOnLockScreenLoginCompactListenerV2?.onFailed(true)
            }

            override fun onFingerprintSuccessful() {
                mOnLockScreenLoginCompactListenerV2?.onSuccess()
            }

            override fun onPinLoginFailed() {
                Toasty.showToast(context, R.string.msg_pin_lock_error_login, Toasty.ERROR)
                mOnLockScreenLoginCompactListenerV2?.onFailed(false)
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
        })
        fingerPattern.setOnClickListener {
            mOnFingerprintListener?.onFingerprint()
        }
        pinLock.setOnFingerprintClick(object : PinLockViewV2.OnFingerprintClick {
            override fun onFingerprintClick() {
                mOnFingerprintClick?.onFingerprintClick()
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateSelectedBackground()
        updatePinLockOrPattern()
        startCamera()
        if (Resources.getSystem().displayMetrics.density > 1.5) {
            pinLock.setGoneNext(false)
        } else {
            pinLock.setGoneNext(true)
        }
    }

    // https://stackoverflow.com/questions/30861075/back-pressed-events-with-system-alert-window
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                keyDispatcherState?.startTracking(event, this)
                return true
            } else if (event.action == KeyEvent.ACTION_UP) {
                keyDispatcherState?.handleUpEvent(event)
                if (event.isTracking && !event.isCanceled) {
                    mOnLockScreenLoginCompactListenerV2?.onClose()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchDisplayHint(hint: Int) {
        super.dispatchDisplayHint(hint)
        mCameraPreview?.stopPreviewAndFreeCamera()
    }

    fun observePattern(onPatternCompleted: (List<PatternLockView.Dot>) -> Unit) {
        this.onPatternCompleted = onPatternCompleted
    }

    fun notifyDrawnWrong() {
        Toasty.showToast(context, R.string.overlay_prompt_pattern_title_wrong, Toasty.ERROR)
        patternLockView.clearPattern()
    }

    fun notifyDrawnCorrect() {
        patternLockView.clearPattern()
    }

    fun setAppPackageName(appPackageName: String) {
        mAppPackageName = appPackageName
        val drawable = FilePathHelper.getIconIcon(context, appPackageName)
        avatarLock.setImageDrawable(drawable)
        pinLock.setIcon(drawable)
    }

    fun resetTime() {
        mTime = 0
    }

    fun setCodeValidation() {
        pinLock.setCodeValidation(appLockerPreferences.getPinLock())
    }

    fun setOnLockScreenLoginCompactListener(onLockScreenLoginCompactListenerV2: PinLockViewV2.OnLockScreenLoginCompactListener) {
        mOnLockScreenLoginCompactListenerV2 = onLockScreenLoginCompactListenerV2
    }

    fun setOnShowForgotPasswordListener(onShowForgotPasswordListener: OnShowForgotPasswordListener) {
        mOnShowForgotPasswordListener = onShowForgotPasswordListener
    }

    private fun updateSelectedBackground() {
        appLockHelper.getThemeDefault()?.let { theme ->
            when (theme.typeTheme) {
                ThemeUtils.TYPE_PATTERN -> {
                    clPatternView.visible()
                    pinLock.gone()
                }
                ThemeUtils.TYPE_PIN -> {
                    clPatternView.gone()
                    pinLock.visible()
                }
                else -> {
                    clPatternView.visible()
                    pinLock.gone()
                }
            }
            if (theme.backgroundResId != 0) {
                imageBackground.setImageResource(theme.backgroundResId)
                pinLock.setImageResourcePinLock(theme.backgroundResId)
            } else {
                val drawable = Drawable.createFromPath(theme.backgroundDownload)
                if (drawable == null) {
                    imageBackground.setImageResource(R.drawable.background_theme_default)
                    pinLock.setImageResourcePinLock(R.drawable.background_theme_default)
                } else {
//                    imageBackground.setImageDrawable(drawable)
                    pinLock.setImageDrawablePinLock(drawable)
                    Glide.with(this).load(drawable).apply(
                        RequestOptions()
                            .downsample(DownsampleStrategy.CENTER_INSIDE)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                    )
                        .into(imageBackground)
                }
            }
            if (theme.selectedResId != 0) {
                patternLockView.setBitmapSelected(
                    AppCompatResources.getDrawable(
                        context,
                        theme.selectedResId
                    )?.toBitmap()
                )
            } else {
                patternLockView.setBitmapSelected(BitmapFactory.decodeFile(theme.selectedDownload))
            }
            if (theme.unselectedResId != 0) {
                patternLockView.setBitmapUnSelected(
                    AppCompatResources.getDrawable(
                        context,
                        theme.unselectedResId
                    )?.toBitmap()
                )
            } else {
                patternLockView.setBitmapUnSelected(BitmapFactory.decodeFile(theme.unselectedDownload))
            }
            //
            val bitmapSelectedList = mutableListOf<Bitmap>()
            if (theme.selectedResIdList.isNotEmpty()) {
                theme.selectedResIdList.forEach {
                    AppCompatResources.getDrawable(context, it)?.toBitmap()
                        ?.let { it1 -> bitmapSelectedList.add(it1) }
                }
            }
            patternLockView.setBitmapSelectedList(bitmapSelectedList)
            val bitmapUnselectedList = mutableListOf<Bitmap>()
            if (theme.unselectedResIdList.isNotEmpty()) {
                theme.unselectedResIdList.forEach {
                    AppCompatResources.getDrawable(context, it)?.toBitmap()
                        ?.let { it1 -> bitmapUnselectedList.add(it1) }
                }
            }
            patternLockView.setBitmapUnSelectedList(bitmapUnselectedList)
            //
            if (theme.lineColorResId != 0) {
                patternLockView.correctStateColor =
                    ContextCompat.getColor(context, theme.lineColorResId)
                patternLockView.normalStateColor =
                    ContextCompat.getColor(context, theme.lineColorResId)
            } else {
                try {
                    patternLockView.correctStateColor = Color.parseColor(theme.lineColor)
                    patternLockView.normalStateColor = Color.parseColor(theme.lineColor)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val builder = PinLockConfiguration.Builder()
                .setMode(PinLockConfiguration.Config.MODE_AUTH)
                .setDeletePadding(theme.isDeletePadding)
                .setNumberPadding(theme.isNumberPadding)
                .setDeletePadding(theme.deletePadding)
                .setResPinList(theme.buttonResList)
                .setPinUrlList(theme.buttonDownloadList())
                .setResColorButton(theme.colorButtonResId)
                .setResSelectorCheckbox(theme.selectorCheckboxColorResId)
                .setColorCheckbox(theme.selectorCheckboxColor)
                .setResBackgroundId(theme.backgroundResId)
                .setBackgroundUrl(theme.backgroundDownload)
                .setResColorMessage(theme.textMsgColorResId).setColorMessage(theme.textMsgColor)
                .setNextTitle(context.getString(R.string.text_create))
            builder.setCodeLength(appLockerPreferences.getPinLock().decodeBase64().length)
            pinLock.applyConfiguration(builder.build())
        }
    }

    fun showFingerprint(show: Boolean) {
        fingerPattern.visibility = if (show) View.VISIBLE else View.GONE
        pinLock.showFingerprint(show)
    }

    private fun updatePinLockOrPattern() {
        patternLockView.clearPattern()
    }

    fun checkUiOverlay() {
        if (pinLock.isVisible) {
            pinLock.applyConfiguration()
            pinLock.hideDelete()
        } else if (patternLockView.isVisible) {
            patternLockView.isInStealthMode = !appLockerPreferences.isShowPathLine()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (mIsBlock) {
            true
        } else {
            super.onInterceptTouchEvent(ev)
        }
    }

    fun setBlockView(isBlockView: Boolean) {
        mIsBlock = isBlockView
    }

    fun setOnFingerprintListener(onFingerprintListener: OnFingerprintListener) {
        mOnFingerprintListener = onFingerprintListener
    }

    fun setOnFingerprintClick(onFingerprintClick: PinLockViewV2.OnFingerprintClick) {
        mOnFingerprintClick = onFingerprintClick
    }

    private fun startCamera() {
        try {
            // setup camera config
            mCameraConfig =
                CameraConfig().getBuilder(context).setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                    .setCameraResolution(CameraResolution.HIGH_RESOLUTION)
                    .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                    .setImageRotation(CameraRotation.ROTATION_270).setCameraFocus(CameraFocus.AUTO)
                    .build()
            //Check for the camera permission for the runtime
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //Start camera preview
                mCameraConfig?.let { startCamera(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Start the hidden camera. Make sure that you check for the runtime permissions before you start
     * the camera.
     *
     * @param cameraConfig camera configuration [CameraConfig]
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    protected open fun startCamera(cameraConfig: CameraConfig) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) { //check if the camera permission is available
            onCameraError(CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE)
        } else if (cameraConfig.facing == CameraFacing.FRONT_FACING_CAMERA && !HiddenCameraUtils.isFrontCameraAvailable(
                context
            )
        ) {   //Check if for the front camera
            onCameraError(CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA)
        } else {
            mCachedCameraConfig = cameraConfig
            mCameraPreview?.startCameraInternal(cameraConfig)
        }
    }

    /**
     * Call this method to capture the image using the camera you initialized. Don't forget to
     * initialize the camera using [.startCamera] before using this function.
     */
    fun takePicture(fileSave: File, time: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (mCameraPreview != null) {
            if (mCameraPreview?.isSafeToTakePictureInternal == true) {
                try {
                    mTakePicture = true
                    mCameraPreview?.takePictureInternal(fileSave, time)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onCameraError(CameraError.ERROR_IMAGE_WRITE_FAILED)
                }
            }
        } else {
            throw RuntimeException("Background camera not initialized. Call startCamera() to initialize the camera.")
        }
    }

    /**
     * Stop and release the camera forcefully.
     */
    protected open fun stopCamera() {
        mCachedCameraConfig = null //Remove config.
        mCameraPreview?.stopPreviewAndFreeCamera()
    }

    /**
     * Add camera preview to the root of the activity layout.
     *
     * @return [CameraPreview] that was added to the view.
     */
    open fun addPreView(): CameraPreview? {
        //create fake camera view
        val cameraSourceCameraPreview = CameraPreview(context, this)
        cameraSourceCameraPreview.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        when (val view = (mRootView as ViewGroup).getChildAt(0)) {
            is LinearLayout -> {
                val params = LinearLayout.LayoutParams(1, 1)
                view.addView(cameraSourceCameraPreview, params)
            }
            is RelativeLayout -> {
                val params = RelativeLayout.LayoutParams(1, 1)
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                view.addView(cameraSourceCameraPreview, params)
            }
            is FrameLayout -> {
                val params = FrameLayout.LayoutParams(1, 1)
                view.addView(cameraSourceCameraPreview, params)
            }
            else -> {
                throw RuntimeException("Root view of the activity/fragment cannot be other than Linear/Relative/Frame layout")
            }
        }
        return cameraSourceCameraPreview
    }

    override fun onCameraError(errorCode: Int) {
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

    override fun onImageCapture(imageFile: File, time: String) {
        AppLockHelper(context).addIntruder(
            IntruderPhoto(
                0,
                mAppPackageName,
                imageFile.absolutePath,
                time
            )
        )
        mTakePicture = false
        pinLock.setInputEnabled(true)
        patternLockView.isInputEnabled = true
    }

    fun loadNative() {
        AdmobManager.getInstance().loadNative(context, BuildConfig.native_open_app, frAds,R.layout.custom_banner_native)
    }

    interface OnShowForgotPasswordListener {
        fun showForgotPasswordDialog()
        fun showFailedMorePasswordDialog()
    }

    interface OnFingerprintListener {
        fun onFingerprint()
    }
}