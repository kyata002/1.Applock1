package com.mtg.applock.ui.activity.main.settings.lockscreen

import com.mtg.applock.R
import com.mtg.applock.ui.base.BaseActivity

import kotlinx.android.synthetic.main.activity_lock_screen.*

class LockScreenActivity : BaseActivity<LockScreenViewModel>() {
    override fun getViewModel(): Class<LockScreenViewModel> {
        return LockScreenViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_lock_screen
    }

    override fun initViews() {
//        setSupportActionBar(toolbar);
//
//        supportActionBar?.apply {
//            setDisplayHomeAsUpEnabled(true)
//
//        }
    }

    override fun initListener() {
//        toolbar.setNavigationOnClickListener { onBackPressed() }
//        val vibrator = ContextCompat.getSystemService(this, Vibrator::class.java)
//        vibrator?.let {
//            if (!it.hasVibrator()) {
//                clVibrate.gone()
//            }
//        } ?: clVibrate.gone()
//        switchVibrate.isChecked = viewModel.isVibrate()
//        switchVibrate.setOnCheckedChangeListener { _, isChecked ->
//            viewModel.setVibrate(isChecked)
//        }
//        switchShowPathLine.isChecked = viewModel.isShowPathLine()
//        switchShowPathLine.setOnCheckedChangeListener { _, isChecked ->
//            viewModel.setShowPathLine(isChecked)
//        }
//
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//    }
//
//    companion object {
//        fun newIntent(context: Context): Intent {
//            return Intent(context, LockScreenActivity::class.java)
//        }
//    }
    }
}