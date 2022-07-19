package com.MTG.pinlock

import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.*


class PinLockMasterView : ConstraintLayout {
    private lateinit var mContext: Context
    private var mNextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mReplayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mDotPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mTvReplay = ""
    private var mTvNext = ""
    private var mNumberList = mutableListOf<String>().apply {
        add("1")
        add("2")
        add("3")
        add("4")
        add("5")
        add("6")
        add("7")
        add("8")
        add("9")
        add("")
        add("0")
    }
    private var mDeleteBitmap: Bitmap? = null
    private var mBottomNext = 0f
    private var mBottomDelete = 0f
    private var mIsShowReplay = false
    private var mIsShowNext = false
    private var mNumberDot = 6
    private var mNumberSelected = 0
    private var mSelectedDotBitmap: Bitmap? = null
    private var mUnSelectedDotBitmap: Bitmap? = null
    private var mSelectedColor: Int = Color.WHITE
    private var mUnSelectedColor: Int = Color.parseColor("#80FFFFFF")

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        mContext = context
        if (attrs != null) {
            val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PinLockMasterView)
            mTvReplay = typedArray.getString(R.styleable.PinLockMasterView_pinLockMasterReplay)
                    ?: "Replay"
            mTvNext = typedArray.getString(R.styleable.PinLockMasterView_pinLockMasterNext)
                    ?: "Next"
            typedArray.recycle()
        }
        mNumberList.forEachIndexed { index, number ->
            if (TextUtils.isEmpty(number)) {
                mNumberList[index] = mTvReplay
            }
        }
        mDeleteBitmap = BitmapFactory.decodeResource(resources, R.drawable.image_delete)
        initPaint()
    }

    private fun initPaint() {
        // number
        mNumberPaint.color = Color.WHITE
        mNumberPaint.style = Paint.Style.FILL
        mNumberPaint.textSize = 60f
        //
        mReplayPaint.color = Color.WHITE
        mReplayPaint.style = Paint.Style.FILL
        mReplayPaint.textSize = 40f
        // next
        mNextPaint.color = Color.WHITE
        mNextPaint.style = Paint.Style.FILL
        mNextPaint.textSize = 50f
        // dot
        mDotPaint.isAntiAlias = true
        mDotPaint.isDither = true
        // test
        mNumberSelected = 4
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            drawNext(it)
            drawDelete(it)
            drawNumber(it)
            drawDotSelected(it)
        }
    }

    private fun drawNumber(canvas: Canvas) {
        mNumberList.forEachIndexed { index, number ->
            val xPos: Float
            val yPos: Float
            when (index % 3) {
                0 -> {
                    xPos = width / 4f - mNumberPaint.measureText(number) / 2f
                    yPos = mBottomDelete - (3 - index / 3) * 150 - (mNumberPaint.descent() + mNumberPaint.ascent()) / 2f
                }
                1 -> {
                    xPos = width / 2f - mNumberPaint.measureText(number) / 2f
                    yPos = mBottomDelete - (3 - index / 3) * 150 - (mNumberPaint.descent() + mNumberPaint.ascent()) / 2f
                }
                else -> {
                    xPos = 3 * width / 4f - mNumberPaint.measureText(number) / 2f
                    yPos = mBottomDelete - (3 - index / 3) * 150 - (mNumberPaint.descent() + mNumberPaint.ascent()) / 2f
                }
            }
            if (TextUtils.equals(number, mTvReplay)) {
                if (mIsShowReplay) {
                    canvas.drawText(number, xPos, yPos, mReplayPaint)
                }
            } else {
                canvas.drawText(number, xPos, yPos, mNumberPaint)
            }
        }
    }

    private fun drawNext(canvas: Canvas) {
        if (!TextUtils.isEmpty(mTvNext)) {
            if (mBottomNext == 0f && height != 0) {
                mBottomNext = height - 100f
            }
            if (!mIsShowNext) return
            val xPos = (width / 2f - mNextPaint.measureText(mTvNext) / 2f).toInt()
            val yPos = (mBottomNext - (mNextPaint.descent() + mNextPaint.ascent()) / 2f)
            canvas.drawText(mTvNext.toUpperCase(Locale.getDefault()), xPos.toFloat(), yPos, mNextPaint)
        }
    }

    private fun drawDelete(canvas: Canvas) {
        mDeleteBitmap?.let {
            if (mBottomDelete == 0f && height != 0) {
                mBottomDelete = mBottomNext - 250f
            }
            val left = 3 * width / 4f - it.width / 2
            val top = mBottomDelete - it.height / 2
            canvas.drawBitmap(it, left, top, mNumberPaint)
        }
    }

    private fun drawDotSelected(canvas: Canvas) {
        if (mSelectedDotBitmap == null && mUnSelectedDotBitmap == null) {
            for (i in 0 until mNumberDot) {
                if (i < mNumberSelected) {
                    mDotPaint.color = mSelectedColor
                } else {
                    mDotPaint.color = mUnSelectedColor
                }
                canvas.drawCircle(width / 2f, mBottomDelete - 4 * 150, 20f, mDotPaint)
            }
        }
    }

    companion object {
        const val COLOR_BLACK = "#000000"
        const val COLOR_WHITE = "#ffff"
        const val COLOR_CHOCOLATE = "#79524f"
        const val COLOR_PINK = "#f996c1"
        const val COLOR_BLUE = "#3086b9"
        const val STORAGE = "/storage/"
        const val MODE_MINI = 1
        const val MODE_FULL = 2
    }
}