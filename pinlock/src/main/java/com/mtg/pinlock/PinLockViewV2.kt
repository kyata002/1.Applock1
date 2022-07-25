package com.mtg.pinlock

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Base64
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestOptions
import com.mtg.pinlock.control.OnLockScreenCodeCreateListener
import com.mtg.pinlock.control.OnLockScreenLoginListener
import kotlinx.android.synthetic.main.pin_lock_view_v2.view.*

class PinLockViewV2 : ConstraintLayout, View.OnClickListener {
    private lateinit var mContext: Context
    private lateinit var mRootView: View
    private var mIsCreateMode: Boolean = true
    private var mCode = ""
    private var mCodeValidation = ""
    private var mPinLockConfiguration: PinLockConfiguration? = null
    private var mLoginListener: OnLockScreenLoginListener? = null
    private var mCreateListener: OnLockScreenCodeCreateListener? = null
    private var mOnFingerprintClick: OnFingerprintClick? = null
    private var mIsLoginFailed = false
    private var mImageNumberList = mutableListOf<ImageView>()
    private var mTvNumberList = mutableListOf<TextView>()
    private var mTvReplay = ""
    private var mTvNext = ""
    private var mMode = MODE_FULL
    private var mIsInputEnabled = true
    private var mIsGoneNext = false
    private var mOnPinLockViewListenerV2: OnPinLockViewListenerV2? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        mContext = context
        if (attrs != null) {
            val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PinLockViewV2)
            mTvReplay = typedArray.getString(R.styleable.PinLockViewV2_pinLockReplayV2) ?: "Replay"
            mTvNext = typedArray.getString(R.styleable.PinLockViewV2_pinLockNextV2) ?: "Next"
            mMode = typedArray.getInt(R.styleable.PinLockViewV2_pinLockModeV2, MODE_FULL)
            typedArray.recycle()
        }
        mRootView = LayoutInflater.from(context).inflate(R.layout.pin_lock_view_v2, this, true)
        // setup code view
        codeView.setCodeLength(6)
        codeView.setListener(object : PFCodeView.OnPFCodeListener {
            override fun onCodeCompleted(code: String?) {
                if (mPinLockConfiguration?.isCreate() == true) {
                    if (mIsCreateMode) {
                        mCode = code?.let {
                            it
                        } ?: ""
                        tvNext.visibility = View.INVISIBLE
                        mOnPinLockViewListenerV2?.onNextPinLock(View.INVISIBLE)
                        mCodeValidation = mCode.encodeBase64()
                        mIsCreateMode = false
                        cleanCode()
                        configureMessage(PinLockConfiguration.Config.TYPE_CONFIRM)
                        configureRightButton(0)
                        return
                    }
                    mCode = code?.let {
                        it
                    } ?: ""
                    if (TextUtils.equals(mCode.encodeBase64(), mCodeValidation)) {
                        mCreateListener?.onCodeCreated(mCodeValidation)
                    } else {
                        cleanCode()
                        configureMessage(PinLockConfiguration.Config.TYPE_ERROR)
                        configureRightButton(0)
                        mIsLoginFailed = true
                        mCreateListener?.onNewCodeValidationFailed()
                        mCode = ""
                    }
                } else {
                    mCode = code?.let {
                        it
                    } ?: ""
                    if (TextUtils.equals(mCode, mCodeValidation.decodeBase64())) {
                        mLoginListener?.onCodeInputSuccessful()
                    } else {
                        cleanCode()
                        configureMessage(PinLockConfiguration.Config.TYPE_ERROR)
                        configureRightButton(0)
                        mIsLoginFailed = true
                        mLoginListener?.onPinLoginFailed()
                        mCode = ""
                    }
                }
            }

            override fun onCodeNotCompleted(code: String?) {
                if (mIsCreateMode) {
                    tvNext.visibility = View.INVISIBLE
                    mOnPinLockViewListenerV2?.onNextPinLock(View.INVISIBLE)
                    return
                }
            }
        })
        tvNext.text = mTvNext
        tvNext.setOnClickListener {
            tvNext.visibility = View.INVISIBLE
            if (mPinLockConfiguration?.isCreate() == true && TextUtils.isEmpty(mCodeValidation)) {
                mCode = codeView.code
                val length = mCode.length
                mCodeValidation = mCode.encodeBase64()
                mIsCreateMode = false
                if (length != 0) {
                    mPinLockConfiguration?.setCodeLengthSuccess(length)
                    codeView.setCodeLengthSuccess(length)
                }
                cleanCode()
                configureMessage(PinLockConfiguration.Config.TYPE_CONFIRM)
                configureRightButton(0)
                return@setOnClickListener
            }
            mCodeValidation = ""
            mCreateListener?.onCodeCreated(mCode)
        }
        tvRelay.setOnClickListener {
            cleanCode()
            codeView.setCodeLengthSuccess(PinLockConfiguration.Config.CODE_LENGTH_DEFAULT)
            configureMessage(PinLockConfiguration.Config.TYPE_START)
            configureRightButton(0)
            mIsLoginFailed = false
            mCodeValidation = ""
            mCode = ""
            mIsCreateMode = true
            tvRelay.visibility = View.GONE
            mOnPinLockViewListenerV2?.onReplayVisibility(View.GONE)
        }
        imageDelete.setOnClickListener(this)
        imageDelete.setOnLongClickListener {
            codeView.clearCode()
            configureRightButton(0)
            configureMessage(getTypeMessage())
            true
        }
        initKey()
        fingerPattern.setOnClickListener {
            mOnFingerprintClick?.onFingerprintClick()
        }
        tvRelay.text = mTvReplay
        when (mMode) {
            MODE_MINI -> {
                fingerPattern.visibility = View.GONE
                imageIcon.visibility = View.GONE
            }
            else -> {

            }
        }
    }

    fun showFingerprint(show: Boolean) {
        fingerPattern.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun showImageIcon(show: Boolean) {
        imageIcon.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun setCodeValidation(codeValidation: String?) {
        mCodeValidation = codeValidation?.let {
            it
        } ?: ""
    }

    private fun getTypeMessage(): Int {
        return when {
            mIsLoginFailed -> {
                PinLockConfiguration.Config.TYPE_ERROR
            }
            mIsCreateMode -> {
                PinLockConfiguration.Config.TYPE_START
            }
            else -> {
                PinLockConfiguration.Config.TYPE_CONFIRM
            }
        }
    }

    private fun configureMessage(type: Int) {
        mOnPinLockViewListenerV2?.onStatusPinLock(type)
        when (type) {
            PinLockConfiguration.Config.TYPE_CONFIRM -> {
                imageCreate1.isSelected = true
                imageCreate2.isSelected = true
            }
            PinLockConfiguration.Config.TYPE_START -> {
                imageCreate1.isSelected = true
                imageCreate2.isSelected = false
            }

            PinLockConfiguration.Config.TYPE_ERROR -> {
                imageCreate1.isSelected = true
                imageCreate2.isSelected = true
            }
        }
    }

    fun hideDelete() {
        imageDelete.visibility = View.GONE
    }

    fun applyConfiguration() {
        mPinLockConfiguration?.let {
            applyConfiguration(it)
        }
    }

    fun applyConfiguration(pinLockConfiguration: PinLockConfiguration) {
        mPinLockConfiguration = pinLockConfiguration
        mPinLockConfiguration?.let {
            val resPinList = it.getResPinList()
            if (resPinList.size > 0) {
                updateImage(resPinList)
            }
            //
            if (it.getPinUrlList().size > 0) {
                updateImageOnline(it.getPinUrlList())
            }
            //
            if (it.getResBackgroundId() != 0) {
                imageBackground.setBackgroundResource(it.getResBackgroundId())
            } else if (!TextUtils.isEmpty(it.getBackgroundUrl())) {
                loadImage(imageBackground, it.getBackgroundUrl(), true)
            }
            //
            codeView.setCodeLength(it.getCodeLength())
            if (it.getResSelectorCheckbox() != 0) {
                codeView.setCheckbox(it.getResSelectorCheckbox())
            } else {
                when (it.getColorCheckbox()) {
                    SELECTOR_CHECKBOX_1 -> {
                        codeView.setCheckbox(R.drawable.selector_checkbox_pin_online_1)
                    }
                    SELECTOR_CHECKBOX_2 -> {
                        codeView.setCheckbox(R.drawable.selector_checkbox_default)
                    }
                    SELECTOR_CHECKBOX_3 -> {
                        codeView.setCheckbox(R.drawable.selector_checkbox_pin_online_3)
                    }
                    SELECTOR_CHECKBOX_4 -> {
                        codeView.setCheckbox(R.drawable.selector_checkbox_pin_online_4)
                    }
                    SELECTOR_CHECKBOX_5 -> {
                        codeView.setCheckbox(R.drawable.selector_checkbox_pin_online_5)
                    }
                    SELECTOR_CHECKBOX_6 -> {
                        codeView.setCheckbox(R.drawable.selector_checkbox_pin_online_6)
                    }
                    SELECTOR_CHECKBOX_7 -> {
                        codeView.setCheckbox(R.drawable.selector_checkbox_pin_online_7)
                    }
                    SELECTOR_CHECKBOX_8 -> {
                        codeView.setCheckbox(R.drawable.selector_checkbox_pin_online_8)
                    }
                    else -> {
                        codeView.setCheckbox(R.drawable.selector_checkbox_default)
                    }
                }
            }
            //
            configureMessage(PinLockConfiguration.Config.TYPE_START)
            //
            if (it.getResColorMessage() == 0) {
                tvRelay.setTextColor(Color.WHITE)
                tvNext.setTextColor(Color.WHITE)
                try {
                    if (!TextUtils.isEmpty(it.getColorMessage()) && it.getColorMessage().startsWith(
                            "#"
                        )) {
                        val color = Color.parseColor(it.getColorMessage())
                        tvRelay.setTextColor(color)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                tvRelay.setTextColor(ContextCompat.getColor(mContext, it.getResColorMessage()))
            }
            if (it.getResColorButton() == 0) {
                mTvNumberList.forEach { tv ->
                    tv.setTextColor(Color.WHITE)
                }
            } else {
                mTvNumberList.forEach { tv ->
                    tv.setTextColor(ContextCompat.getColor(mContext, it.getResColorButton()))
                }
            }
            //
            if (!TextUtils.isEmpty(it.getNextTitle())) {
                mTvNext = it.getNextTitle()
                tvNext.text = mTvNext
            }
            //
            mIsCreateMode = it.isCreate()
            if (mIsCreateMode) {
                imageCreate1.visibility = View.VISIBLE
                imageCreate2.visibility = View.VISIBLE
                imageLine1.visibility = View.VISIBLE
                tvMessage.visibility = View.VISIBLE
                tvTitle.visibility = View.VISIBLE
            }
            setGoneNext(mIsGoneNext)
            // setup view of apply theme activity
            it.getThemeConfig()?.let { themeConfig ->
                if (themeConfig.isShowDelete) {
                    imageDelete.visibility = View.VISIBLE
                } else {
                    imageDelete.visibility = View.GONE
                }
                var index = 0
                while (index < themeConfig.pointNumber && themeConfig.pointNumber > 0) {
                    codeView.input("a")
                    index++
                }
                val pointSelectedList = themeConfig.pointSelectedList
                for (point in pointSelectedList) {
                    if (point < mTvNumberList.size) {
                        mTvNumberList[point].isSelected = true
                    }
                }
                mTvNumberList.forEach { tv ->
                    tv.isEnabled = false
                }
                mImageNumberList.forEach { image ->
                    image.isEnabled = false
                }
                imageDelete.isEnabled = false
            }
            if (it.isDeletePadding()) {
                val deletePadding = it.getDeletePadding().toFloat()
                val padding = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    deletePadding,
                    resources.displayMetrics
                ).toInt()
                imageDelete.setPadding(padding, padding, padding, padding)
            } else {
                val padding = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    10f,
                    resources.displayMetrics
                ).toInt()
                imageDelete.setPadding(padding, padding, padding, padding)
            }
            if (it.isNumberPadding()) {
                val padding = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    10f,
                    resources.displayMetrics
                ).toInt()
                mImageNumberList.forEach { image ->
                    image.setPadding(padding, padding, padding, padding)
                }
            } else {
                mImageNumberList.forEach { image ->
                    image.setPadding(0, 0, 0, 0)
                }
            }
        }
    }

    private fun updateImage(resPinList: MutableList<Int>) {
        when (resPinList.size) {
            2 -> {
                mImageNumberList.forEachIndexed { _, ImageView ->
                    ImageView.visibility = View.INVISIBLE
                }
                resPinList[1].let { resId ->
                    imageDelete.setImageResource(resId)
                }
                mTvNumberList.forEach {
                    resPinList[0].let { resId ->
                        it.setBackgroundResource(resId)
                    }
                }
                mTvNumberList.forEach {
                    it.visibility = View.VISIBLE
                }
            }
            11 -> {
                resPinList.forEachIndexed { index, res ->
                    res.let { resId ->
                        if (index < mImageNumberList.size) {
                            mImageNumberList[index].setImageResource(resId)
                            mImageNumberList[index].visibility = View.VISIBLE
                        } else {
                            imageDelete.setImageResource(resId)
                        }
                    }
                }
                mTvNumberList.forEach {
                    it.visibility = View.INVISIBLE
                }
            }
            else -> {
                mImageNumberList.forEach {
                    it.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun updateImageOnline(pinUrlList: MutableList<String>) {
        when (pinUrlList.size) {
            2 -> {
                var hide = true
                mImageNumberList.forEachIndexed { _, ImageView ->
                    pinUrlList[0].let { url ->
                        if ((url.startsWith(STORAGE) && !isStoragePermissionGranted())) {
                            hide = false
                        } else {
                            val success = loadImage(ImageView, url)
                            if (!success) {
                                hide = false
                            }
                        }
                    }
                }
                pinUrlList[1].let { url ->
                    if (!TextUtils.isEmpty(url)) {
                        if ((url.startsWith(STORAGE) && !isStoragePermissionGranted())) {
                            hide = false
                        } else {
                            var success = loadImage(imageDelete, url)
                            if (!success) {
                                hide = false
                            }
                        }
                    }
                }
                if (hide) {
                    mTvNumberList.forEach {
                        pinUrlList[0].let { _ ->
                            it.background = null
                        }
                    }
                } else {
                    mTvNumberList.forEach {
                        it.visibility = View.VISIBLE
                    }
                    mImageNumberList.forEach {
                        it.visibility = View.VISIBLE
                    }
                }
            }
            11 -> {
                var hide = true
                pinUrlList.forEachIndexed { index, res ->
                    res.let { url ->
                        if ((url.startsWith(STORAGE) && !isStoragePermissionGranted())) {
                            hide = false
                        } else {
                            if (index < mImageNumberList.size) {
                                if (!TextUtils.isEmpty(url)) {
                                    val success = loadImage(mImageNumberList[index], url)
                                    mImageNumberList[index].visibility = View.VISIBLE
                                    if (!success) {
                                        hide = false
                                    }
                                }
                            } else {
                                if (!TextUtils.isEmpty(url)) {
                                    var success = loadImage(imageDelete, url)
                                    if (!success) {
                                        hide = false
                                    }
                                }
                            }
                        }
                    }
                }
                if (hide) {
                    mTvNumberList.forEach {
                        it.visibility = View.INVISIBLE
                    }
                    mImageNumberList.forEach {
                        it.visibility = View.VISIBLE
                    }
                } else {
                    mTvNumberList.forEach {
                        it.visibility = View.VISIBLE
                    }
                    mImageNumberList.forEach {
                        it.visibility = View.INVISIBLE
                    }
                    imageDelete.setImageResource(R.drawable.image_delete)
                }
            }
            else -> {
                mImageNumberList.forEach {
                    it.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // permission is automatically granted on sdk < 23 upon installation
            true
        }
    }

    private fun initKey() {
        mImageNumberList.add(imageNumber0)
        mImageNumberList.add(imageNumber1)
        mImageNumberList.add(imageNumber2)
        mImageNumberList.add(imageNumber3)
        mImageNumberList.add(imageNumber4)
        mImageNumberList.add(imageNumber5)
        mImageNumberList.add(imageNumber6)
        mImageNumberList.add(imageNumber7)
        mImageNumberList.add(imageNumber8)
        mImageNumberList.add(imageNumber9)
        //
        mImageNumberList.forEach {
            it.setOnClickListener(this)
        }
        mTvNumberList.add(tvNumber0)
        mTvNumberList.add(tvNumber1)
        mTvNumberList.add(tvNumber2)
        mTvNumberList.add(tvNumber3)
        mTvNumberList.add(tvNumber4)
        mTvNumberList.add(tvNumber5)
        mTvNumberList.add(tvNumber6)
        mTvNumberList.add(tvNumber7)
        mTvNumberList.add(tvNumber8)
        mTvNumberList.add(tvNumber9)
        //
        mTvNumberList.forEach {
            it.setOnClickListener(this)
            it.bringToFront()
        }
    }

    fun setInputEnabled(isInputEnabled: Boolean) {
        mIsInputEnabled = isInputEnabled
        invalidate()
    }

    override fun onClick(v: View?) {
        if (!mIsInputEnabled) return
        when (v?.id) {
            R.id.imageDeleteDefault, R.id.imageDelete -> {
                val codeLength = codeView.delete()
                configureRightButton(codeLength)
                configureMessage(getTypeMessage())
            }
            else -> {
                if (v is TextView) {
                    val string = v.text.toString()
                    if (string.length != 1) {
                        return
                    }
                    val codeLength: Int = codeView.input(string)
                    configureRightButton(codeLength)
                    v.performHapticFeedback(
                        HapticFeedbackConstants.VIRTUAL_KEY,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                or HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                    )
                } else if (v is ImageView) {
                    val string = v.getTag().toString()
                    if (string.length != 1) {
                        return
                    }
                    val codeLength: Int = codeView.input(string)
                    configureRightButton(codeLength)
                    v.performHapticFeedback(
                        HapticFeedbackConstants.VIRTUAL_KEY,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                or HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                    )
                }
            }
        }
    }

    private fun configureRightButton(codeLength: Int) {
        mPinLockConfiguration?.let {
            if (codeLength > 0) {
                imageDelete.visibility = View.VISIBLE
            } else {
                imageDelete.visibility = View.GONE
            }
            if (codeLength >= 4 && codeLength < it.getCodeLength() && mIsCreateMode) {
                tvNext.visibility = View.VISIBLE
                mOnPinLockViewListenerV2?.onNextPinLock(View.VISIBLE)
            } else {
                if (mIsGoneNext) {
                    tvNext.visibility = View.GONE
                    mOnPinLockViewListenerV2?.onNextPinLock(View.GONE)
                } else {
                    tvNext.visibility = View.INVISIBLE
                    mOnPinLockViewListenerV2?.onNextPinLock(View.INVISIBLE)
                }
            }
            if (!TextUtils.isEmpty(mCodeValidation) && mPinLockConfiguration?.isCreate() == true) {
                tvRelay.visibility = View.VISIBLE
                mOnPinLockViewListenerV2?.onReplayVisibility(View.VISIBLE)
            } else {
                tvRelay.visibility = View.GONE
                mOnPinLockViewListenerV2?.onReplayVisibility(View.GONE)
            }
        }
    }

    fun setGoneNext(isGoneNext: Boolean) {
        mIsGoneNext = isGoneNext
        if (mIsGoneNext) {
            tvNext.visibility = View.GONE
        } else {
            tvNext.visibility = View.INVISIBLE
        }
        invalidate()
    }

    private fun cleanCode() {
        mCode = ""
        codeView.clearCode()
    }

    fun setIcon(drawable: Drawable?) {
        imageIcon.setImageDrawable(drawable)
    }

    fun setImageResourcePinLock(resId: Int) {
        imageBackground.setImageResource(resId)
    }

    fun setImageDrawablePinLock(drawable: Drawable?) {
//        imageBackground.setImageDrawable(drawable)
        Glide.with(this).load(drawable).apply(
            RequestOptions()
                .downsample(DownsampleStrategy.CENTER_INSIDE)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
        )
                .into(imageBackground)
    }

    fun setOnLockScreenCodeCreateListener(onLockScreenCodeCreateListener: OnLockScreenCodeCreateListener) {
        mCreateListener = onLockScreenCodeCreateListener
    }

    fun setOnLockScreenLoginListener(onLockScreenLoginListener: OnLockScreenLoginListener) {
        mLoginListener = onLockScreenLoginListener
    }

    fun setOnFingerprintClick(onFingerprintClick: OnFingerprintClick) {
        mOnFingerprintClick = onFingerprintClick
    }

    private fun loadImage(imageView: ImageView, url: String): Boolean {
        return loadImage(imageView, url, false)
    }

    private fun loadImage(
        imageView: ImageView,
        url: String,
        isBackground: Boolean
    ): Boolean {
        return if (url.startsWith(STORAGE)) {
            if (isBackground) {
                val drawable = Drawable.createFromPath(url)
                return if (drawable != null) {
                    imageView.setImageDrawable(drawable)
                    true
                } else {
                    false
                }
            } else {
                val bitmap = BitmapFactory.decodeFile(url)
                return if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                    true
                } else {
                    false
                }
            }
        } else {
            Glide.with(mContext).load(url).into(imageView)
            true
        }
    }

    fun setTitle(title: String) {
        tvTitle.text = title
        invalidate()
    }

    fun setMessage(message: String) {
        tvMessage.text = message
        invalidate()
    }

    fun setOnPinLockViewListenerV2(onPinLockViewListenerV2: OnPinLockViewListenerV2) {
        mOnPinLockViewListenerV2 = onPinLockViewListenerV2
        invalidate()
    }

    interface OnLockScreenLoginCompactListener {
        fun onSuccess()
        fun onFailed(isFingerprint: Boolean)
        fun onClose()
    }

    interface OnPinLockViewListenerV2 {
        fun onNextPinLock(visibility: Int)
        fun onStatusPinLock(status: Int)
        fun onReplayVisibility(status: Int)
    }

    interface OnFingerprintClick {
        fun onFingerprintClick()
    }

    private fun String.decodeBase64(): String {
        return Base64.decode(this, Base64.DEFAULT).toString(charset("UTF-8"))
    }

    private fun String.encodeBase64(): String {
        return Base64.encodeToString(this.toByteArray(charset("UTF-8")), Base64.DEFAULT)
    }

    companion object {
        const val STORAGE = "/storage/"
        const val SELECTOR_CHECKBOX_1 = "1"
        const val SELECTOR_CHECKBOX_2 = "2"
        const val SELECTOR_CHECKBOX_3 = "3"
        const val SELECTOR_CHECKBOX_4 = "4"
        const val SELECTOR_CHECKBOX_5 = "5"
        const val SELECTOR_CHECKBOX_6 = "6"
        const val SELECTOR_CHECKBOX_7 = "7"
        const val SELECTOR_CHECKBOX_8 = "8"
        const val MODE_FULL = 1
        const val MODE_MINI = 2
    }
}