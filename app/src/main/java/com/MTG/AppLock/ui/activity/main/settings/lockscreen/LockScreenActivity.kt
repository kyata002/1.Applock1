package com.MTG.AppLock.ui.activity.main.settings.lockscreen

import android.content.Context
import android.content.Intent
import android.os.Vibrator
import androidx.core.content.ContextCompat
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.base.BaseActivity
import com.MTG.AppLock.util.extensions.gone

import kotlinx.android.synthetic.main.activity_lock_screen.*

class LockScreenActivity : BaseActivity<LockScreenViewModel>() {
    override fun getViewModel(): Class<LockScreenViewModel> {
        return LockScreenViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_lock_screen
    }

    override fun initViews() {
        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
    }

    override fun initListener() {
        toolbar.setNavigationOnClickListener { onBackPressed() }
        val vibrator = ContextCompat.getSystemService(this, Vibrator::class.java)
        vibrator?.let {
            if (!it.hasVibrator()) {
                clVibrate.gone()
            }
        } ?: clVibrate.gone()
        switchVibrate.isChecked = viewModel.isVibrate()
        switchVibrate.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setVibrate(isChecked)
        }
        switchShowPathLine.isChecked = viewModel.isShowPathLine()
        switchShowPathLine.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setShowPathLine(isChecked)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, LockScreenActivity::class.java)
        }
    }
}