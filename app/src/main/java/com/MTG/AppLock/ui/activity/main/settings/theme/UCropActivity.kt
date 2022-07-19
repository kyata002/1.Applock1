package com.MTG.AppLock.ui.activity.main.settings.theme

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap.CompressFormat
import android.graphics.PorterDuff
import android.text.TextUtils
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.base.BaseActivity
import com.MTG.AppLock.ui.view.ProgressDialog
import com.MTG.AppLock.util.extensions.visible
import com.MTG.ucrop.UCrop
import com.MTG.ucrop.callback.BitmapCropCallback
import com.MTG.ucrop.model.AspectRatio
import com.MTG.ucrop.view.CropImageView
import com.MTG.ucrop.view.GestureCropImageView
import com.MTG.ucrop.view.OverlayView
import com.MTG.ucrop.view.TransformImageView.TransformImageListener
import com.MTG.ucrop.view.UCropView
import com.MTG.ucrop.view.widget.HorizontalProgressWheelView
import com.MTG.ucrop.view.widget.HorizontalProgressWheelView.ScrollingListener
import kotlinx.android.synthetic.main.ucrop_activity_photobox.*
import java.util.*

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 */
open class UCropActivity : BaseActivity<UCropViewModel>() {
    private var mToolbarTitle: String? = null

    @ColorInt
    private var mRootViewBackgroundColor = 0

    @DrawableRes
    private var mToolbarCancelDrawable = 0

    private var mLogoColor = 0
    private var mShowBottomControls = false
    private var mShowLoader = true
    private var mUCropView: UCropView? = null
    private var mGestureCropImageView: GestureCropImageView? = null
    private var mOverlayView: OverlayView? = null
    private var mWrapperStateRotate: ViewGroup? = null
    private var mWrapperStateScale: ViewGroup? = null
    private var mLayoutRotate: ViewGroup? = null
    private var mLayoutScale: ViewGroup? = null
    private var mTextViewRotateAngle: TextView? = null
    private var mTextViewScalePercent: TextView? = null
    private var mBlockingView: View? = null
    private var mControlsTransition: Transition? = null
    private var mCompressFormat = DEFAULT_COMPRESS_FORMAT
    private var mCompressQuality = DEFAULT_COMPRESS_QUALITY
    private var mAllowedGestures = intArrayOf(SCALE, ROTATE, ALL)
    private var mProgressBar: Dialog? = null

    companion object {
        const val DEFAULT_COMPRESS_QUALITY = 90
        val DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG
        const val SCALE = 1
        const val ROTATE = 2
        const val ALL = 3
        private const val CONTROLS_ANIMATION_DURATION: Long = 50
        private const val TABS_COUNT = 3
        private const val SCALE_WIDGET_SENSITIVITY_COEFFICIENT = 15000
        private const val ROTATE_WIDGET_SENSITIVITY_COEFFICIENT = 42

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    override fun getViewModel(): Class<UCropViewModel> {
        return UCropViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.ucrop_activity_photobox
    }

    override fun initViews() {
        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
        setupViews(intent)
        setImageData(intent)
        setInitialState()
        addBlockingView()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.ucrop_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.ucrop_done)
            cropAndSaveImage()
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        if (mGestureCropImageView != null) {
            mGestureCropImageView?.cancelAllAnimations()
        }
    }

    /**
     * This method extracts all data from the incoming intent and setups views properly.
     */
    private fun setImageData(intent: Intent) {
        val inputPath = intent.getStringExtra(UCrop.EXTRA_INPUT_PATH)
        val outputPath = intent.getStringExtra(UCrop.EXTRA_OUTPUT_PATH)
        processOptions(intent)
        if (!TextUtils.isEmpty(inputPath) && !TextUtils.isEmpty(outputPath)) {
            try {
                inputPath?.let { outputPath?.let { it1 -> mGestureCropImageView?.setImageUri(it, it1) } }
            } catch (e: Exception) {
                finish()
            }
        } else {
            finish()
        }
    }

    /**
     * This method extracts [#optionsBundle][com.MTG.ucrop.UCrop.Options] from incoming intent
     * and setups Activity, [OverlayView] and [CropImageView] properly.
     */
    private fun processOptions(intent: Intent) {
        // Bitmap compression options
        val compressionFormatName = intent.getStringExtra(UCrop.Options.EXTRA_COMPRESSION_FORMAT_NAME)
        var compressFormat: CompressFormat? = null
        if (!TextUtils.isEmpty(compressionFormatName)) {
            compressFormat = compressionFormatName?.let { CompressFormat.valueOf(it) }
        }
        mCompressFormat = compressFormat ?: DEFAULT_COMPRESS_FORMAT
        mCompressQuality = intent.getIntExtra(UCrop.Options.EXTRA_COMPRESSION_QUALITY, DEFAULT_COMPRESS_QUALITY)

        // Gestures options
        val allowedGestures = intent.getIntArrayExtra(UCrop.Options.EXTRA_ALLOWED_GESTURES)
        if (allowedGestures != null && allowedGestures.size == TABS_COUNT) {
            mAllowedGestures = allowedGestures
        }

        // Crop image view options
        mGestureCropImageView?.maxBitmapSize = intent.getIntExtra(UCrop.Options.EXTRA_MAX_BITMAP_SIZE, CropImageView.DEFAULT_MAX_BITMAP_SIZE)
        mGestureCropImageView?.setMaxScaleMultiplier(intent.getFloatExtra(UCrop.Options.EXTRA_MAX_SCALE_MULTIPLIER, CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER))
        mGestureCropImageView?.setImageToWrapCropBoundsAnimDuration(intent.getIntExtra(UCrop.Options.EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION, CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION).toLong())

        // Overlay view options
        mOverlayView?.isFreestyleCropEnabled = intent.getBooleanExtra(UCrop.Options.EXTRA_FREE_STYLE_CROP, OverlayView.DEFAULT_FREESTYLE_CROP_MODE != OverlayView.FREESTYLE_CROP_MODE_DISABLE)
        mOverlayView?.setDimmedColor(intent.getIntExtra(UCrop.Options.EXTRA_DIMMED_LAYER_COLOR, resources.getColor(R.color.ucrop_color_default_dimmed)))
        mOverlayView?.setCircleDimmedLayer(intent.getBooleanExtra(UCrop.Options.EXTRA_CIRCLE_DIMMED_LAYER, OverlayView.DEFAULT_CIRCLE_DIMMED_LAYER))
        mOverlayView?.setShowCropFrame(intent.getBooleanExtra(UCrop.Options.EXTRA_SHOW_CROP_FRAME, OverlayView.DEFAULT_SHOW_CROP_FRAME))
        mOverlayView?.setCropFrameColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_FRAME_COLOR, resources.getColor(R.color.ucrop_color_default_crop_frame)))
        mOverlayView?.setCropFrameStrokeWidth(intent.getIntExtra(UCrop.Options.EXTRA_CROP_FRAME_STROKE_WIDTH, resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_frame_stoke_width)))
        mOverlayView?.setShowCropGrid(intent.getBooleanExtra(UCrop.Options.EXTRA_SHOW_CROP_GRID, OverlayView.DEFAULT_SHOW_CROP_GRID))
        mOverlayView?.setCropGridRowCount(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_ROW_COUNT, OverlayView.DEFAULT_CROP_GRID_ROW_COUNT))
        mOverlayView?.setCropGridColumnCount(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_COLUMN_COUNT, OverlayView.DEFAULT_CROP_GRID_COLUMN_COUNT))
        mOverlayView?.setCropGridColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_COLOR, resources.getColor(R.color.ucrop_color_default_crop_grid)))
        mOverlayView?.setCropGridCornerColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_CORNER_COLOR, resources.getColor(R.color.ucrop_color_default_crop_grid)))
        mOverlayView?.setCropGridStrokeWidth(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_STROKE_WIDTH, resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_grid_stoke_width)))

        // Aspect ratio options
        val aspectRatioX = intent.getFloatExtra(UCrop.EXTRA_ASPECT_RATIO_X, 0f)
        val aspectRatioY = intent.getFloatExtra(UCrop.EXTRA_ASPECT_RATIO_Y, 0f)
        val aspectRationSelectedByDefault = intent.getIntExtra(UCrop.Options.EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, 0)
        val aspectRatioList: ArrayList<AspectRatio>? = intent.getParcelableArrayListExtra(UCrop.Options.EXTRA_ASPECT_RATIO_OPTIONS)
        if (aspectRatioX > 0 && aspectRatioY > 0) {
            mGestureCropImageView?.targetAspectRatio = aspectRatioX / aspectRatioY
        } else if (aspectRatioList != null && aspectRationSelectedByDefault < aspectRatioList.size) {
            mGestureCropImageView?.targetAspectRatio = aspectRatioList[aspectRationSelectedByDefault].aspectRatioX / aspectRatioList[aspectRationSelectedByDefault].aspectRatioY
        } else {
            mGestureCropImageView?.targetAspectRatio = 9 / 16f
        }

        // Result bitmap max size options
        val maxSizeX = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_X, 0)
        val maxSizeY = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_Y, 0)
        if (maxSizeX > 0 && maxSizeY > 0) {
            mGestureCropImageView?.setMaxResultImageSizeX(maxSizeX)
            mGestureCropImageView?.setMaxResultImageSizeY(maxSizeY)
        }
    }

    private fun setupViews(intent: Intent) {
        mToolbarCancelDrawable = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_WIDGET_CANCEL_DRAWABLE, R.drawable.ucrop_ic_cross)
        mToolbarTitle = intent.getStringExtra(UCrop.Options.EXTRA_UCROP_TITLE_TEXT_TOOLBAR)
        mToolbarTitle = if (mToolbarTitle != null) mToolbarTitle else resources.getString(R.string.ucrop_label_edit_photo)
        mLogoColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_LOGO_COLOR, ContextCompat.getColor(this, R.color.ucrop_color_default_logo))
        mShowBottomControls = !intent.getBooleanExtra(UCrop.Options.EXTRA_HIDE_BOTTOM_CONTROLS, false)
        mRootViewBackgroundColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_ROOT_VIEW_BACKGROUND_COLOR, ContextCompat.getColor(this, R.color.ucrop_color_crop_background))
        initiateRootViews()
        if (mShowBottomControls) {
            val viewGroup = findViewById<ViewGroup>(R.id.rlRoot)
            val wrapper = viewGroup.findViewById<ViewGroup>(R.id.controls_wrapper)
            wrapper.visible()
            LayoutInflater.from(this).inflate(R.layout.ucrop_controls, wrapper, true)
            mControlsTransition = AutoTransition()
            mControlsTransition?.duration = CONTROLS_ANIMATION_DURATION
            mWrapperStateRotate = findViewById(R.id.state_rotate)
            mWrapperStateRotate?.setOnClickListener(mStateClickListener)
            mWrapperStateScale = findViewById(R.id.state_scale)
            mWrapperStateScale?.setOnClickListener(mStateClickListener)
            mLayoutRotate = findViewById(R.id.layout_rotate_wheel)
            mLayoutScale = findViewById(R.id.layout_scale_wheel)
            setupAspectRatioWidget()
            setupRotateWidget()
            setupScaleWidget()
        }
    }

    private fun initiateRootViews() {
        mUCropView = findViewById(R.id.ucrop)
        mGestureCropImageView = mUCropView?.cropImageView
        mOverlayView = mUCropView?.overlayView
        mGestureCropImageView?.setTransformImageListener(mImageListener)
        (findViewById<View>(R.id.image_view_logo) as ImageView).setColorFilter(mLogoColor, PorterDuff.Mode.SRC_ATOP)
        findViewById<View>(R.id.ucrop_frame).setBackgroundColor(mRootViewBackgroundColor)
        if (!mShowBottomControls) {
            val params = findViewById<View>(R.id.ucrop_frame).layoutParams as RelativeLayout.LayoutParams
            params.bottomMargin = 0
            findViewById<View>(R.id.ucrop_frame).requestLayout()
        }
    }

    private val mImageListener: TransformImageListener = object : TransformImageListener {
        override fun onRotate(currentAngle: Float) {
            setAngleText(currentAngle)
        }

        override fun onScale(currentScale: Float) {
            setScaleText(currentScale)
        }

        override fun onLoadComplete() {
            mUCropView?.animate()?.alpha(1f)?.setDuration(300)?.interpolator = AccelerateInterpolator()
            mBlockingView?.isClickable = false
            mShowLoader = false
        }

        override fun onLoadFailure() {
            finish()
        }
    }

    private fun setupAspectRatioWidget() {
        mGestureCropImageView?.targetAspectRatio = 9 / 16f
        mGestureCropImageView?.setImageToWrapCropBounds()
    }

    private fun setupRotateWidget() {
        mTextViewRotateAngle = findViewById(R.id.text_view_rotate)
        (findViewById<View>(R.id.rotate_scroll_wheel) as HorizontalProgressWheelView).setScrollingListener(object : ScrollingListener {
            override fun onScroll(delta: Float, totalDistance: Float) {
                mGestureCropImageView?.postRotate(delta / ROTATE_WIDGET_SENSITIVITY_COEFFICIENT)
            }

            override fun onScrollEnd() {
                mGestureCropImageView?.setImageToWrapCropBounds()
            }

            override fun onScrollStart() {
                mGestureCropImageView?.cancelAllAnimations()
            }
        })
        findViewById<View>(R.id.wrapper_reset_rotate).setOnClickListener { resetRotation() }
        findViewById<View>(R.id.wrapper_rotate_by_angle).setOnClickListener { rotateByAngle(90) }
    }

    private fun setupScaleWidget() {
        mTextViewScalePercent = findViewById(R.id.text_view_scale)
        (findViewById<View>(R.id.scale_scroll_wheel) as HorizontalProgressWheelView).setScrollingListener(object : ScrollingListener {
            override fun onScroll(delta: Float, totalDistance: Float) {
                if (delta > 0) {
                    mGestureCropImageView?.let {
                        it.zoomInImage(it.currentScale + delta * ((it.maxScale - it.minScale) / SCALE_WIDGET_SENSITIVITY_COEFFICIENT))
                    }
                } else {
                    mGestureCropImageView?.let {
                        it.zoomOutImage(it.currentScale + delta * ((it.maxScale - it.minScale) / SCALE_WIDGET_SENSITIVITY_COEFFICIENT))
                    }
                }
            }

            override fun onScrollEnd() {
                mGestureCropImageView?.setImageToWrapCropBounds()
            }

            override fun onScrollStart() {
                mGestureCropImageView?.cancelAllAnimations()
            }
        })
    }

    private fun setAngleText(angle: Float) {
        if (mTextViewRotateAngle != null) {
            mTextViewRotateAngle?.text = String.format(Locale.getDefault(), "%.1f°", angle)
        }
    }

    private fun setScaleText(scale: Float) {
        if (mTextViewScalePercent != null) {
            mTextViewScalePercent?.text = String.format(Locale.getDefault(), "%d%%", (scale * 100).toInt())
        }
    }

    private fun resetRotation() {
        mGestureCropImageView?.let {
            it.postRotate(-it.currentAngle)
        }
        mGestureCropImageView?.setImageToWrapCropBounds()
    }

    private fun rotateByAngle(angle: Int) {
        mGestureCropImageView?.let {
            it.postRotate(angle.toFloat())
        }
        mGestureCropImageView?.setImageToWrapCropBounds()
    }

    private val mStateClickListener = View.OnClickListener { v ->
        if (!v.isSelected) {
            setWidgetState(v.id)
        }
    }

    private fun setInitialState() {
        if (mShowBottomControls) {
            setWidgetState(R.id.state_scale)
        } else {
            setAllowedGestures(0)
        }
    }

    private fun setWidgetState(@IdRes stateViewId: Int) {
        if (!mShowBottomControls) return
        mWrapperStateRotate?.isSelected = stateViewId == R.id.state_rotate
        mWrapperStateScale?.isSelected = stateViewId == R.id.state_scale
        mLayoutRotate?.visibility = if (stateViewId == R.id.state_rotate) View.VISIBLE else View.GONE
        mLayoutScale?.visibility = if (stateViewId == R.id.state_scale) View.VISIBLE else View.GONE
        changeSelectedTab(stateViewId)
        if (stateViewId == R.id.state_scale) {
            setAllowedGestures(0)
        } else if (stateViewId == R.id.state_rotate) {
            setAllowedGestures(1)
        } else {
            setAllowedGestures(2)
        }
    }

    private fun changeSelectedTab(stateViewId: Int) {
        TransitionManager.beginDelayedTransition((findViewById<View>(R.id.rlRoot) as ViewGroup), mControlsTransition)
        mWrapperStateScale?.findViewById<View>(R.id.text_view_scale)?.visibility = if (stateViewId == R.id.state_scale) View.VISIBLE else View.GONE
        mWrapperStateRotate?.findViewById<View>(R.id.text_view_rotate)?.visibility = if (stateViewId == R.id.state_rotate) View.VISIBLE else View.GONE
    }

    private fun setAllowedGestures(tab: Int) {
        mGestureCropImageView?.isScaleEnabled = mAllowedGestures[tab] == ALL || mAllowedGestures[tab] == SCALE
        mGestureCropImageView?.isRotateEnabled = mAllowedGestures[tab] == ALL || mAllowedGestures[tab] == ROTATE
    }

    /**
     * Adds view that covers everything below the Toolbar.
     * When it's clickable - user won't be able to click/touch anything below the Toolbar.
     * Need to block user input while loading and cropping an image.
     */
    private fun addBlockingView() {
        if (mBlockingView == null) {
            mBlockingView = View(this)
            val lp = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            lp.addRule(RelativeLayout.BELOW, R.id.toolbar)
            mBlockingView?.layoutParams = lp
            mBlockingView?.isClickable = true
        }
        (findViewById<View>(R.id.rlRoot) as RelativeLayout).addView(mBlockingView)
    }

    private fun cropAndSaveImage() {
        showProgressBar()
        mBlockingView?.isClickable = true
        mShowLoader = true
        mGestureCropImageView?.cropAndSaveImage(mCompressFormat, mCompressQuality, object : BitmapCropCallback {
            override fun onBitmapCropped(path: String, offsetX: Int, offsetY: Int, imageWidth: Int, imageHeight: Int) {
                mGestureCropImageView?.targetAspectRatio?.let { setResultUri(path, it, offsetX, offsetY, imageWidth, imageHeight) }
                hideProgressBar()
                finish()
            }

            override fun onCropFailure(t: Throwable) {
                hideProgressBar()
                finish()
            }
        })
    }

    protected fun setResultUri(path: String, resultAspectRatio: Float, offsetX: Int, offsetY: Int, imageWidth: Int, imageHeight: Int) {
        setResult(Activity.RESULT_OK, Intent().putExtra(UCrop.EXTRA_OUTPUT_PATH, path)
                .putExtra(UCrop.EXTRA_OUTPUT_CROP_ASPECT_RATIO, resultAspectRatio)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_X, offsetX)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_Y, offsetY)
        )
    }

    private fun showProgressBar() {
        mProgressBar?.dismiss()
        mProgressBar = ProgressDialog.progressDialogV2(this)
        mProgressBar?.show()
    }

    private fun hideProgressBar() {
        mProgressBar?.dismiss()
    }

    override fun onDestroy() {
        mProgressBar?.dismiss()
        super.onDestroy()
    }
}