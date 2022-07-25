package com.mtg.applock.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.mtg.applock.R
import com.mtg.applock.util.extensions.gone
import com.mtg.applock.util.extensions.visible
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.bottom_view.view.*

class BottomView : AppBarLayout, View.OnClickListener {
    private var mRootView: View? = null
    private var mOnSelectedItemListener: OnSelectedItemListener? = null
    private var mModeView: BottomViewType = BottomViewType.HOME
    private var mCurrentType = TYPE_APP_LOCK

    constructor(context: Context) : super(context) {
        initViews(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initViews(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initViews(context, attrs)
    }

    private fun initViews(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BottomView)
            mModeView = BottomViewType.values()[typedArray.getInt(R.styleable.BottomView_modeView, 0)]
            typedArray.recycle()
        }
        mRootView = LayoutInflater.from(context).inflate(R.layout.bottom_view, this, true)
        mRootView?.setOnClickListener {
            // nothing
        }
        mRootView?.isEnabled = false
        mRootView?.findViewById<LinearLayout>(R.id.llAppLock)?.setOnClickListener(this)
        mRootView?.findViewById<LinearLayout>(R.id.llGroupLock)?.setOnClickListener(this)
        mRootView?.findViewById<LinearLayout>(R.id.llSettings)?.setOnClickListener(this)
        mRootView?.findViewById<LinearLayout>(R.id.llDelete)?.setOnClickListener(this)
        mRootView?.findViewById<LinearLayout>(R.id.llUnLock)?.setOnClickListener(this)
        mRootView?.findViewById<LinearLayout>(R.id.llDetail)?.setOnClickListener(this)
        mRootView?.findViewById<LinearLayout>(R.id.llSave)?.setOnClickListener(this)
        mRootView?.findViewById<LinearLayout>(R.id.llShare)?.setOnClickListener(this)
        updateView()
    }

    private fun updateView() {
        setModeView(mModeView)
    }

    fun setModeView(modeView: BottomViewType) {
        llAppLock.gone()
        llGroupLock.gone()
        llSettings.gone()
        llDelete.gone()
        llUnLock.gone()
        llDetail.gone()
        llSave.gone()
        llShare.gone()
        when (modeView) {
            BottomViewType.HOME -> {
                llAppLock.visible()
                llGroupLock.visible()
                llSettings.visible()
            }
            BottomViewType.DELETE_UNLOCK -> {
                llDelete.visible()
                llUnLock.visible()
            }
            BottomViewType.DELETE_UNLOCK_DETAIL -> {
                llDelete.visible()
                llUnLock.visible()
                llDetail.visible()
            }
            BottomViewType.DELETE_SAVE_SHARE -> {
                llDelete.visible()
                llSave.visible()
                llShare.visible()
            }
        }
    }

    private fun onUnSelected() {
        imageAppLock.isSelected = false
        tvAppLock.isSelected = false
        //
        imageGroupLock.isSelected = false
        tvGroupLock.isSelected = false
        //
        imageSettings.isSelected = false
        tvSettings.isSelected = false
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.llAppLock -> {
                if (mCurrentType == TYPE_APP_LOCK) {
                    return
                }
                onUnSelected()
                mCurrentType = TYPE_APP_LOCK
                mOnSelectedItemListener?.onSelectedItem(v, TYPE_APP_LOCK)
                imageAppLock.isSelected = true
                tvAppLock.isSelected = true
            }
            R.id.llGroupLock -> {
                if (mCurrentType == TYPE_GROUP_LOCK) {
                    return
                }
                onUnSelected()
                mCurrentType = TYPE_GROUP_LOCK
                mOnSelectedItemListener?.onSelectedItem(v, TYPE_GROUP_LOCK)
                imageGroupLock.isSelected = true
                tvGroupLock.isSelected = true
            }
            R.id.llSettings -> {
                if (mCurrentType == TYPE_SETTINGS) {
                    return
                }
                onUnSelected()
                mCurrentType = TYPE_SETTINGS
                mOnSelectedItemListener?.onSelectedItem(v, TYPE_SETTINGS)
                imageSettings.isSelected = true
                tvSettings.isSelected = true
            }
            R.id.llDelete -> {
                mOnSelectedItemListener?.onSelectedItem(v, TYPE_DELETE)
                imageDelete.isPressed = true
                tvDelete.isPressed = true
            }
            R.id.llUnLock -> {
                mOnSelectedItemListener?.onSelectedItem(v, TYPE_UNLOCK)
                imageUnlock.isPressed = true
                tvUnlock.isPressed = true
            }
            R.id.llDetail -> {
                mOnSelectedItemListener?.onSelectedItem(v, TYPE_DETAIL)
                imageDetail.isPressed = true
                tvDetail.isPressed = true
            }
            R.id.llSave -> {
                mOnSelectedItemListener?.onSelectedItem(v, TYPE_SAVE)
                imageSave.isPressed = true
                tvSave.isPressed = true
            }
            R.id.llShare -> {
                mOnSelectedItemListener?.onSelectedItem(v, TYPE_SHARE)
                imageShare.isPressed = true
                tvShare.isPressed = true
            }
        }
    }

    fun setViewSelected(type: Int, isSelected: Boolean) {
        when (type) {
            TYPE_APP_LOCK -> {
                imageAppLock.isSelected = isSelected
                tvAppLock.isSelected = isSelected
            }
            TYPE_GROUP_LOCK -> {
                imageGroupLock.isSelected = isSelected
                tvGroupLock.isSelected = isSelected
            }
            TYPE_SETTINGS -> {
                imageSettings.isSelected = isSelected
                tvSettings.isSelected = isSelected
            }
        }
    }

    fun isViewSelected(type: Int): Boolean {
        return when (type) {
            TYPE_APP_LOCK -> {
                imageAppLock.isSelected
            }
            TYPE_GROUP_LOCK -> {
                imageGroupLock.isSelected
            }
            TYPE_SETTINGS -> {
                imageSettings.isSelected
            }
            else -> {
                false
            }
        }
    }

    fun setOnSelectedItemListener(onSelectedItemListener: OnSelectedItemListener) {
        mOnSelectedItemListener = onSelectedItemListener
    }

    interface OnSelectedItemListener {
        fun onSelectedItem(v: View?, type: Int)
    }

    companion object {
        const val TYPE_APP_LOCK = 0
        const val TYPE_GROUP_LOCK = 1
        const val TYPE_SETTINGS = 2
        const val TYPE_DELETE = 3
        const val TYPE_UNLOCK = 4
        const val TYPE_DETAIL = 5
        const val TYPE_SAVE = 6
        const val TYPE_SHARE = 7
    }
}