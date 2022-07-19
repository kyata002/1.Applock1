package com.MTG.pinlock

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.MTG.pinlock.control.OnLockScreenCodeCreateListener
import com.MTG.pinlock.extension.encodeBase64
import kotlinx.android.synthetic.main.pin_lock_view.view.*

class PinLockView : ConstraintLayout, View.OnClickListener {
    private lateinit var mContext: Context
    private lateinit var mRootView: View
    private var mIsCreateMode: Boolean = true
    private var mCode = ""
    private var mCodeValidation = ""
    private var mCreateListener: OnLockScreenCodeCreateListener? = null
    private var mIsLoginFailed = false
    private var mImageNumberList = mutableListOf<ImageView>()
    private var mTvNumberList = mutableListOf<TextView>()
    private var mTvReplay = ""
    private var mOnPinLockViewListener: OnPinLockViewListener? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        mContext = context
        if (attrs != null) {
            val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PinLockView)
            mTvReplay = typedArray.getString(R.styleable.PinLockView_pinLockReplay) ?: "Replay"
            typedArray.recycle()
        }
        mRootView = LayoutInflater.from(context).inflate(R.layout.pin_lock_view, this, true)
        // setup code view
        codeView.setCodeLength(6)
        codeView.setListener(object : PFCodeView.OnPFCodeListener {
            override fun onCodeCompleted(code: String?) {
                if (mIsCreateMode) {
                    mCode = code?.let {
                        it
                    } ?: ""
                    mOnPinLockViewListener?.onNextPinLock(View.INVISIBLE)
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
            }

            override fun onCodeNotCompleted(code: String?) {
                if (mIsCreateMode) {
                    mOnPinLockViewListener?.onNextPinLock(View.INVISIBLE)
                    return
                }
            }
        })
        tvRelay.setOnClickListener {
            replay()
        }
        imageDeleteDefault.setOnClickListener(this)
        imageDeleteDefault.setOnLongClickListener {
            codeView.clearCode()
            configureRightButton(0)
            configureMessage(getTypeMessage())
            true
        }
        initKey()
        tvRelay.text = mTvReplay
    }

    fun onNext() {
        if (TextUtils.isEmpty(mCodeValidation)) {
            mCode = codeView.code
            val length = mCode.length
            mCodeValidation = mCode.encodeBase64()
            mIsCreateMode = false
            if (length != 0) {
                codeView.setCodeLengthSuccess(length)
            }
            cleanCode()
            configureMessage(PinLockConfiguration.Config.TYPE_CONFIRM)
            configureRightButton(0)
            return
        }
        mCodeValidation = ""
        mCreateListener?.onCodeCreated(mCode)
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
        mOnPinLockViewListener?.onStatusPinLock(type)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imageDeleteDefault -> {
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
                } else if (v is ImageView) {
                    val string = v.getTag().toString()
                    if (string.length != 1) {
                        return
                    }
                    val codeLength: Int = codeView.input(string)
                    configureRightButton(codeLength)
                }
            }
        }
    }

    private fun configureRightButton(codeLength: Int) {
        if (codeLength > 0) {
            imageDeleteDefault.visibility = View.VISIBLE
        } else {
            imageDeleteDefault.visibility = View.GONE
        }
        if (codeLength >= 4 && mIsCreateMode) {
            mOnPinLockViewListener?.onNextPinLock(View.VISIBLE)
        } else {
            mOnPinLockViewListener?.onNextPinLock(View.INVISIBLE)
        }
        if (!TextUtils.isEmpty(mCodeValidation)) {
            tvRelay.visibility = View.VISIBLE
            mOnPinLockViewListener?.onReplayVisibility(View.VISIBLE)
        } else {
            tvRelay.visibility = View.GONE
            mOnPinLockViewListener?.onReplayVisibility(View.GONE)
        }
    }

    private fun cleanCode() {
        mCode = ""
        codeView.clearCode()
    }

    fun replay() {
        cleanCode()
        codeView.setCodeLengthSuccess(PinLockConfiguration.Config.CODE_LENGTH_DEFAULT)
        configureMessage(PinLockConfiguration.Config.TYPE_START)
        configureRightButton(0)
        mIsLoginFailed = false
        mCodeValidation = ""
        mCode = ""
        mIsCreateMode = true
        tvRelay.visibility = View.GONE
        mOnPinLockViewListener?.onReplayVisibility(View.GONE)
    }

    fun setOnLockScreenCodeCreateListener(onLockScreenCodeCreateListener: OnLockScreenCodeCreateListener) {
        mCreateListener = onLockScreenCodeCreateListener
    }

    fun setOnPinLockViewListener(onPinLockViewListener: OnPinLockViewListener) {
        mOnPinLockViewListener = onPinLockViewListener
    }

    interface OnPinLockViewListener {
        fun onNextPinLock(visibility: Int)
        fun onStatusPinLock(status: Int)
        fun onReplayVisibility(status: Int)
    }
}