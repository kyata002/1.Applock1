package com.MTG.AppLock.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class HomeWatcher(private val mContext: Context) {
    private val mFilter: IntentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
    private var mListener: OnHomePressedListener? = null
    private var mReceiver: InnerReceiver? = null
    fun setOnHomePressedListener(listener: OnHomePressedListener?) {
        mListener = listener
        mReceiver = InnerReceiver()
    }

    fun startWatch() {
        if (mReceiver != null) {
            mContext.registerReceiver(mReceiver, mFilter)
        }
    }

    fun stopWatch() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver)
        }
    }

    interface OnHomePressedListener {
        fun onHomePressed()
        fun onRecentAppPressed()
    }

    internal inner class InnerReceiver : BroadcastReceiver() {
        private val systemDialogReasonKey = "reason"
        private val systemDialogReasonGlobalActions = "globalactions"
        private val systemDialogReasonRecentApps = "recentapps"
        private val systemDialogReasonHomeKey = "homekey"
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason = intent.getStringExtra(systemDialogReasonKey)
                if (reason != null) {
                    //                    Log.d("TAG1", "action:$action,reason:$reason")
                    if (mListener != null) {
                        if (reason == systemDialogReasonHomeKey) {
                            mListener?.onHomePressed()
                        } else if (reason == systemDialogReasonRecentApps) {
                            mListener?.onRecentAppPressed()
                        }
                    }
                }
            }
        }
    }
}