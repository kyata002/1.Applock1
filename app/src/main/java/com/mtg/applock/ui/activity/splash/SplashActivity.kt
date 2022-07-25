package com.mtg.applock.ui.activity.splash

import android.content.Context
import android.content.Intent
import android.os.Handler
import com.mtg.applock.R
import com.mtg.applock.ui.activity.password.overlay.activity.OverlayValidationActivity
import com.mtg.applock.ui.base.BaseActivity

class SplashActivity : BaseActivity<SplashModel>() {
    private var mHandler: Handler = Handler()
    private var mIsFinish = false
    override fun getViewModel(): Class<SplashModel> {
        return SplashModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun initViews() {
        mHandler.postDelayed({
            mIsFinish = true
            checkLockApp()
        }, 500)
    }

    private fun checkLockApp() {
        if (!mIsFinish) return
        val intent = OverlayValidationActivity.newIntent(this)
        startActivity(intent)
        finishAfterTransition()
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacksAndMessages(null)
        finishAfterTransition()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SplashActivity::class.java)
        }
    }
}