package com.MTG.AppLock.ui.view.time

import android.app.TimePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.MTG.AppLock.R
import kotlinx.android.synthetic.main.layout_time_selected.view.*
import java.util.*

class TimeSelectedView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    private var mHour = 0
    private var mMinute = 0
    private var mOnUpdateTimeSelectedListener: OnUpdateTimeSelectedListener? = null
    private var mTimePickerDialog: TimePickerDialog? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_time_selected, this, true)
        imageHourUp.setOnClickListener {
            upHour()
        }
        imageHourDown.setOnClickListener {
            downHour()
        }
        imageMinuteUp.setOnClickListener {
            upMinute()
        }
        imageMinuteDown.setOnClickListener {
            downMinute()
        }
        tvHour.setOnClickListener {
            mTimePickerDialog = TimePickerDialog(context, { _, hourOfDay, minute ->
                setHour(hourOfDay)
                setMinute(minute)
            }, mHour, mMinute, true)
            mTimePickerDialog?.show()
        }
        tvMinute.setOnClickListener {
            mTimePickerDialog = TimePickerDialog(context, { _, hourOfDay, minute ->
                setHour(hourOfDay)
                setMinute(minute)
            }, mHour, mMinute, true)
            mTimePickerDialog?.show()
        }
        setHour(mHour)
        setMinute(mMinute)
    }

    private fun upHour() {
        mHour -= 1
        if (mHour < 0) {
            mHour = 23
        }
        tvHour.text = String.format(Locale.getDefault(), "%02d", mHour)
        mOnUpdateTimeSelectedListener?.updateTime(mHour, mMinute)
        invalidate()
    }

    private fun downHour() {
        mHour += 1
        if (mHour > 23) {
            mHour = 0
        }
        tvHour.text = String.format(Locale.getDefault(), "%02d", mHour)
        mOnUpdateTimeSelectedListener?.updateTime(mHour, mMinute)
        invalidate()
    }

    private fun upMinute() {
        mMinute -= 1
        if (mMinute < 0) {
            mMinute = 59
        }
        tvMinute.text = String.format(Locale.getDefault(), "%02d", mMinute)
        mOnUpdateTimeSelectedListener?.updateTime(mHour, mMinute)
        invalidate()
    }

    private fun downMinute() {
        mMinute += 1
        if (mMinute > 59) {
            mMinute = 0
        }
        tvMinute.text = String.format(Locale.getDefault(), "%02d", mMinute)
        mOnUpdateTimeSelectedListener?.updateTime(mHour, mMinute)
        invalidate()
    }

    fun setOnUpdateTimeSelectedListener(onUpdateTimeSelectedListener: OnUpdateTimeSelectedListener) {
        mOnUpdateTimeSelectedListener = onUpdateTimeSelectedListener
    }

    fun setHour(hour: Int) {
        mHour = hour
        tvHour.text = String.format(Locale.getDefault(), "%02d", mHour)
        mOnUpdateTimeSelectedListener?.updateTime(mHour, mMinute)
        invalidate()
    }

    fun setMinute(minute: Int) {
        mMinute = minute
        tvMinute.text = String.format(Locale.getDefault(), "%02d", mMinute)
        mOnUpdateTimeSelectedListener?.updateTime(mHour, mMinute)
        invalidate()
    }

    fun getHour(): Int {
        return mHour
    }

    fun getMinute(): Int {
        return mMinute
    }

    interface OnUpdateTimeSelectedListener {
        fun updateTime(hour: Int, minute: Int)
    }
}