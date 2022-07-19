package com.MTG.AppLock.ui.activity.password.changepassword

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.activity.password.newpattern.CreateNewPatternViewModel
import com.MTG.AppLock.ui.activity.password.newpattern.SimplePatternListener
import com.MTG.AppLock.ui.base.BaseActivity
import com.MTG.AppLock.util.extensions.gone
import com.MTG.AppLock.util.extensions.invisible
import com.MTG.AppLock.util.extensions.visible
import com.MTG.patternlockview.PatternLockView
import com.MTG.pinlock.PinLockConfiguration
import com.MTG.pinlock.PinLockView
import com.MTG.pinlock.control.OnLockScreenCodeCreateListener
import kotlinx.android.synthetic.main.activity_change_password.*

class ChangePasswordActivity : BaseActivity<ChangePasswordViewModel>() {
    private lateinit var mColorAnimator: ObjectAnimator

    override fun getViewModel(): Class<ChangePasswordViewModel> {
        return ChangePasswordViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_change_password
    }

    override fun initViews() {
        mColorAnimator = ObjectAnimator.ofInt(tvTitle, "textColor", ContextCompat.getColor(this, R.color.color_error), ContextCompat.getColor(this, R.color.color_error), Color.WHITE)
        mColorAnimator.setEvaluator(ArgbEvaluator())
        mColorAnimator.duration = 1000
        viewModel.getTypeThemeLiveData().observe(this, {
            if (it) {
                pinLock.gone()
                patternLockView.visible()
                tvTitle.setText(R.string.pattern_draw_your_unlock_pattern)
                tvMessage.setText(R.string.pattern_connect_at_least_4_dots)
            } else {
                pinLock.visible()
                patternLockView.gone()
                tvTitle.setText(R.string.msg_pin_lock_set_your_pin_code)
                tvMessage.setText(R.string.msg_pin_lock_enter_numbers)
            }
        })
    }

    override fun initListener() {
        viewModel.getPatternEventLiveData().observe(this, { viewState ->
            when (viewState.patternEvent) {
                CreateNewPatternViewModel.PatternEvent.INITIALIZE -> {
                    imageCreate1.isSelected = true
                    imageCreate2.isSelected = false
                }
                CreateNewPatternViewModel.PatternEvent.ERROR_SHORT_FIRST -> {
                    imageCreate1.isSelected = true
                    imageCreate2.isSelected = false
                    mColorAnimator.start()
                }
                CreateNewPatternViewModel.PatternEvent.ERROR_SHORT_SECOND -> {
                    imageCreate1.isSelected = false
                    imageCreate2.isSelected = true
                    mColorAnimator.start()
                }
                CreateNewPatternViewModel.PatternEvent.ERROR -> {
                    mColorAnimator.start()
                }
                CreateNewPatternViewModel.PatternEvent.SECOND_COMPLETED -> {
                    createPassSuccess(true)
                }
                else -> {
                    imageCreate1.isSelected = false
                    imageCreate2.isSelected = true
                    tvReset.visible()
                }
            }
            if (!patternLockView.isVisible) return@observe
            tvTitle.text = viewState.getTitleText(this)
            tvMessage.text = viewState.getMessageText(this)
        })
        patternLockView.addPatternLockListener(object : SimplePatternListener() {
            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                if (viewModel.isFirstPattern()) {
                    viewModel.setFirstDrawPattern(pattern)
                } else {
                    viewModel.setRedrawnPattern(pattern)
                }
                patternLockView.clearPattern()
            }
        })
        tvReset.setOnClickListener {
            reset()
        }
        pinLock.setOnPinLockViewListener(object : PinLockView.OnPinLockViewListener {
            override fun onNextPinLock(visibility: Int) {
                tvNext.visibility = visibility
            }

            override fun onStatusPinLock(status: Int) {
                if (!pinLock.isVisible) return
                when (status) {
                    PinLockConfiguration.Config.TYPE_START -> {
                        tvTitle.setText(R.string.msg_pin_lock_set_your_pin_code)
                        tvMessage.setText(R.string.msg_pin_lock_enter_numbers)
                    }
                    PinLockConfiguration.Config.TYPE_CONFIRM -> {
                        tvTitle.setText(R.string.msg_pin_lock_confirm_your_pin)
                        tvMessage.setText(R.string.msg_pin_lock_confirm)
                    }
                    PinLockConfiguration.Config.TYPE_ERROR -> {
                        tvTitle.setText(R.string.msg_pin_lock_confirm_your_pin)
                        tvMessage.setText(R.string.msg_pin_lock_wrong_pin_code)
                        mColorAnimator.start()
                    }
                }
            }

            override fun onReplayVisibility(status: Int) {
                if (status == View.VISIBLE) {
                    imageCreate1.isSelected = false
                    imageCreate2.isSelected = true
                } else {
                    imageCreate1.isSelected = true
                    imageCreate2.isSelected = false
                }
            }
        })
        pinLock.setOnLockScreenCodeCreateListener(object : OnLockScreenCodeCreateListener {
            override fun onCodeCreated(encodedCode: String?) {
                encodedCode?.let {
                    viewModel.setPinLock(it)
                    createPassSuccess(false)
                }
            }

            override fun onNewCodeValidationFailed() {
                // nothing
            }
        })
        tvNext.setOnClickListener {
            pinLock.onNext()
        }
    }

    private fun createPassSuccess(isPattern: Boolean) {
        viewModel.completed(isPattern)
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun reset() {
        imageCreate1.isSelected = true
        imageCreate2.isSelected = false
        tvReset.invisible()
        pinLock.gone()
        patternLockView.visible()
        tvTitle.visible()
        tvMessage.visible()
        tvNext.gone()
        viewModel.reset()
        pinLock.replay()
    }

    override fun onDestroy() {
        mColorAnimator.cancel()
        super.onDestroy()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, ChangePasswordActivity::class.java)
        }
    }
}