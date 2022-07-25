package com.mtg.applock.ui.view.time

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEachIndexed
import com.mtg.applock.R
import es.MTG.toasty.Toasty
import java.text.DateFormatSymbols
import java.util.*

class EveryTimeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : HorizontalScrollView(context, attrs, defStyleAttr) {
    private var mEverySelectedList = mutableListOf<Int>()
    private var mCurrentDay: String = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_every_time, this, true)
        // setup first day of week
        val dateFormat = DateFormatSymbols()
        val shortNameList = mutableListOf<String>()
        dateFormat.shortWeekdays.forEach {
            if (!TextUtils.isEmpty(it)) {
                shortNameList.add(it)
            }
        }
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        mCurrentDay = shortNameList[dayOfWeek - 1]
        //
        val root = getChildAt(0) as HorizontalScrollView
        val dayRoot = root.getChildAt(0) as ConstraintLayout
        val count = dayRoot.childCount
        val size = shortNameList.size
        // hide view
        for (x in 0 until count) {
            val view = dayRoot.getChildAt(x)
            if (view is TextView) {
                view.visibility = View.GONE
            }
        }
        for (x in 0 until size) {
            if (x < count) {
                val view = dayRoot.getChildAt(x)
                if (view is TextView) {
                    view.text = shortNameList[x]
                    view.visibility = View.VISIBLE
                    if (TextUtils.equals(shortNameList[x], mCurrentDay) && !TextUtils.isEmpty(mCurrentDay)) {
                        view.isSelected = true
                        mEverySelectedList.add(x)
                    } else {
                        view.isSelected = false
                    }
                }
            }
        }
        dayRoot.forEachIndexed { index, viewChild ->
            viewChild.setOnClickListener { view ->
                view.isSelected = !view.isSelected
                if (view is TextView) {
                    if (view.isSelected) {
                        mEverySelectedList.add(index)
                    } else {
                        if (mEverySelectedList.size > 1) {
                            mEverySelectedList.remove(index)
                        } else {
                            view.isSelected = true
                            Toasty.showToast(context, R.string.msg_selected_every_time, Toasty.WARNING)
                        }
                    }
                }
            }
        }
    }

    fun getEvery(): String {
        val builder = StringBuilder()
        mEverySelectedList.sort()
        mEverySelectedList.forEach {
            builder.append(it).append(",")
        }
        return builder.toString()
    }

    fun setEvery(everyList: List<String>) {
        if (everyList.isNullOrEmpty()) {
            return
        }
        mEverySelectedList.clear()
        everyList.forEach {
            if (!TextUtils.isEmpty(it)) {
                mEverySelectedList.add(it.toInt())
            }
        }
        if (!mEverySelectedList.isNullOrEmpty()) {
            mCurrentDay = ""
        }
        val root = getChildAt(0) as HorizontalScrollView
        val dayRoot = root.getChildAt(0) as ConstraintLayout
        val count = dayRoot.childCount
        for (x in 0 until count) {
            val view = dayRoot.getChildAt(x)
            if (view is TextView) {
                view.isSelected = mEverySelectedList.contains(x)
            }
        }
        invalidate()
    }
}