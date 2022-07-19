package com.MTG.AppLock.ui.activity.asklocknewapplication

import android.content.Context
import android.content.pm.PackageManager
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.MTG.AppLock.R
import kotlinx.android.synthetic.main.layout_ask_lock_new_application.view.*
import java.util.*

class AskLockNewApplicationView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var mOnAskLockNewApplicationListener: OnAskLockNewApplicationListener? = null
    private var mAppPackageName: String = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_ask_lock_new_application, this, true)
        imageClose.setOnClickListener {
            mOnAskLockNewApplicationListener?.onCloseAskLockNewApplication()
        }
        btnOKLockIt.setOnClickListener {
            mOnAskLockNewApplicationListener?.onLockAskLockNewApplication(mAppPackageName)
        }
        btnOpenApp.setOnClickListener {
            mOnAskLockNewApplicationListener?.onOpenAppAskLockNewApplication(mAppPackageName)
        }
    }

    fun setOnAskLockNewApplicationListener(onAskLockNewApplicationListener: OnAskLockNewApplicationListener) {
        mOnAskLockNewApplicationListener = onAskLockNewApplicationListener
    }

    fun setAppPackageName(appPackageName: String) {
        mAppPackageName = appPackageName
        try {
            val icon = context.packageManager.getApplicationIcon(appPackageName)
            val name = context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(appPackageName, 0))
                    ?: "unknown"
            val fulltext = String.format(Locale.getDefault(), "%s %s", name, context.getString(R.string.msg_ask_locking_new_application))
            setColor(tvMessageAskLockNewApplication, fulltext, name.toString())
            imageLogoAskLockNewApplication.setImageDrawable(icon)
        } catch (e: PackageManager.NameNotFoundException) {
            mOnAskLockNewApplicationListener?.onCloseAskLockNewApplication()
            e.printStackTrace()
        }
    }

    private fun setColor(view: TextView, fulltext: String, subtext: String) {
        view.setText(fulltext, TextView.BufferType.SPANNABLE)
        val str = view.text as Spannable
        val i = fulltext.indexOf(subtext)
        str.setSpan(ForegroundColorSpan(HIGHLIGHT_COLOR), i, i + subtext.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
                    mOnAskLockNewApplicationListener?.onCloseAskLockNewApplication()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    interface OnAskLockNewApplicationListener {
        fun onCloseAskLockNewApplication()
        fun onLockAskLockNewApplication(packageName: String)
        fun onOpenAppAskLockNewApplication(packageName: String)
    }

    companion object {
        private val HIGHLIGHT_COLOR: Int = android.graphics.Color.parseColor("#4464ed")
    }
}
