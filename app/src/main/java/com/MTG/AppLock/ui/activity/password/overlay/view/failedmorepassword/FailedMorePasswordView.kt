package com.MTG.AppLock.ui.activity.password.overlay.view.failedmorepassword

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.MTG.AppLock.R
import kotlinx.android.synthetic.main.dialog_failed.view.*

class FailedMorePasswordView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    private var mOnCloseFailedMorePasswordView: OnFailedMorePasswordListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.dialog_failed, this, true)
        btnOkFailedMorePassword.setOnClickListener {
            mOnCloseFailedMorePasswordView?.onCloseFailedMorePasswordView()
        }
    }

    fun setOnFailedMorePasswordListener(onFailedMorePasswordListener: OnFailedMorePasswordListener) {
        mOnCloseFailedMorePasswordView = onFailedMorePasswordListener
    }

    interface OnFailedMorePasswordListener {
        fun onCloseFailedMorePasswordView()
    }
}