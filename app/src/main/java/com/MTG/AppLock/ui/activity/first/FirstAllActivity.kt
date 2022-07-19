package com.MTG.AppLock.ui.activity.first

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.style.UnderlineSpan
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.activity.main.MainActivity
import com.MTG.AppLock.ui.activity.password.newpattern.CreateNewPatternViewModel
import com.MTG.AppLock.ui.activity.password.newpattern.SimplePatternListener
import com.MTG.AppLock.ui.activity.policy.PolicyActivity
import com.MTG.AppLock.ui.base.BaseActivity
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.extensions.*
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import com.MTG.patternlockview.PatternLockView
import com.google.android.gms.ads.AdView
import com.MTG.keyboard.KeyboardHeightObserver
import com.MTG.keyboard.KeyboardHeightProvider
import com.MTG.pinlock.PinLockConfiguration
import com.MTG.pinlock.PinLockView
import com.MTG.pinlock.control.OnLockScreenCodeCreateListener
import kotlinx.android.synthetic.main.activity_start.*
import kotlinx.android.synthetic.main.dialog_save_recover_password.view.*
import kotlinx.android.synthetic.main.dialog_rulues_supper_password.view.*
import java.util.*
import kotlin.math.abs


/*
*  create password + supper password first time
*/
class FirstAllActivity : BaseActivity<FistAllViewModel>(), KeyboardHeightObserver {
    private lateinit var mColorAnimator: ObjectAnimator
    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null
    private var mConfirmSavePasswordDialog: AlertDialog? = null
    private var mPasswordRulesDialog: AlertDialog? = null
    private var mScreenHeight = 0
    private var mIsPattern = true
    private var mBannerAd: AdView? = null

    override fun getViewModel(): Class<FistAllViewModel> {
        return FistAllViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_start
    }

    override fun initViews() {
        mColorAnimator = ObjectAnimator.ofInt(tvTitle, "textColor", ContextCompat.getColor(this, R.color.color_error), ContextCompat.getColor(this, R.color.color_error), Color.WHITE)
        mColorAnimator.setEvaluator(ArgbEvaluator())
        mColorAnimator.duration = 1000
        mKeyboardHeightProvider = KeyboardHeightProvider(this)
        clRoot.post {
            mKeyboardHeightProvider?.start()
        }
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        mScreenHeight = displayMetrics.heightPixels
        buildPasswordRulesDialog()
    }

    override fun onEventFinishAllActivity() {
        resetPattern()
    }

    private fun buildConfirmSavePasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_save_recover_password, null, false)
        builder.setView(view)
        mConfirmSavePasswordDialog?.dismiss()
        mConfirmSavePasswordDialog = builder.create()
        mConfirmSavePasswordDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelSavePassword.setOnClickListener { mConfirmSavePasswordDialog?.dismiss() }
        view.btnYesSavePassword.setOnClickListener {
            mConfirmSavePasswordDialog?.dismiss()
            val success = viewModel.completed(this, editPassword.editText?.text.toString().trim(), editConfirmPassword.editText?.text.toString().trim(), mIsPattern)
            if (success) {
                clSplash.visible()
                btnCompleted.gone()
            }
        }
    }

    private fun buildPasswordRulesDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_rulues_supper_password, null, false)
        view.tvRules1.text = String.format(Locale.getDefault(), "- %s", getString(R.string.text_rules_1))
        view.tvRules2.text = String.format(Locale.getDefault(), "- %s", getString(R.string.text_rules_2))
        view.tvRules3.text = String.format(Locale.getDefault(), "- %s", getString(R.string.text_rules_3))
        view.tvRules4.text = String.format(Locale.getDefault(), "- %s (!@#$%%&)", getString(R.string.text_rules_4))
        view.tvRules5.text = String.format(Locale.getDefault(), "- %s", getString(R.string.text_rules_5))
        builder.setView(view)
        mPasswordRulesDialog?.dismiss()
        mPasswordRulesDialog = builder.create()
        mPasswordRulesDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnUnderstandSuperPassword.setOnClickListener {
            mPasswordRulesDialog?.dismiss()
        }
    }

    override fun initListener() {
        btnCompleted.setOnClickListener {
            if (viewModel.passwordValidator(this, editPassword.editText?.text.toString().trim(), editConfirmPassword.editText?.text.toString().trim())) {
                buildConfirmSavePasswordDialog()
                mConfirmSavePasswordDialog?.show()
                dialogLayout(mConfirmSavePasswordDialog)
            }
        }
        imagePin.setOnClickListener {
            resetPin()
        }
        imagePattern.setOnClickListener {
            resetPattern()
        }
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
                    createPassSuccess()
                }
                else -> {
                    imageCreate1.isSelected = false
                    imageCreate2.isSelected = true
                    tvReset.visible()
                }
            }
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
            resetPattern()
        }
        setColor(tvPolicy, String.format(Locale.getDefault(), "%s%s%s", getString(R.string.splash_policy), " ", getString(R.string.title_settings_privacy_policy)), getString(R.string.title_settings_privacy_policy))
        tvPolicy.setOnClickListener {
            startActivity(PolicyActivity.newIntent(this))
        }
        btnAccept.setOnClickListener {
            viewModel.setFinishAllActivity(false)
            viewModel.setFirstCreatePassword(false)
            val intent = MainActivity.newIntent(this)
            intent.putExtra(Const.IS_FROM_FIRST_ALL, true)
            startActivity(intent)
            finish()
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
                        tvMessage.setText(R.string.msg_pin_lock_confirm)
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
                    createPassSuccess()
                }
            }

            override fun onNewCodeValidationFailed() {
                // nothing
            }
        })
        tvNext.setOnClickListener {
            pinLock.onNext()
        }
        imageHelp.setOnClickListener {
            mPasswordRulesDialog?.show()
            dialogLayout(mPasswordRulesDialog)
        }


    }

    private fun resetPin() {
        reset()
        pinLock.visible()
        patternLockView.gone()
        imagePin.gone()
        imagePattern.visible()
        mIsPattern = false
        tvTitle.setText(R.string.msg_pin_lock_set_your_pin_code)
        tvMessage.setText(R.string.msg_pin_lock_enter_numbers)
        clSplash.gone()
    }

    private fun resetPattern() {
        reset()
        pinLock.gone()
        patternLockView.visible()
        imagePin.visible()
        imagePattern.gone()
        mIsPattern = true
        tvTitle.setText(R.string.pattern_draw_your_unlock_pattern)
        tvMessage.setText(R.string.pattern_connect_at_least_4_dots)
        clSplash.gone()
    }

    private fun createPassSuccess() {
        tvReset.invisible()
        pinLock.gone()
        patternLockView.gone()
        tvTitle.gone()
        tvMessage.gone()
        imagePattern.gone()
        imagePin.gone()
        //
        tvTitlePassword.visible()
        imageHelp.visible()
        tvMessagePassword.visible()
        editPassword.visible()
        editConfirmPassword.visible()
        btnCompleted.visible()
        imageCreate1.isSelected = true
        imageCreate2.isSelected = true
        imageCreate3.isSelected = true
    }

    private fun setColor(view: TextView, fulltext: String, subtext: String) {
        view.setText(fulltext, TextView.BufferType.SPANNABLE)
        val str = view.text as Spannable
        val i = fulltext.indexOf(subtext)
        str.setSpan(UnderlineSpan(), i, i + subtext.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun reset() {
        imageCreate1.isSelected = true
        imageCreate2.isSelected = false
        imageCreate3.isSelected = false
        tvReset.invisible()
        pinLock.gone()
        patternLockView.visible()
        imagePattern.gone()
        imagePin.visible()
        tvTitle.visible()
        tvMessage.visible()
        tvNext.gone()
        //
        btnCompleted.gone()
        tvTitlePassword.gone()
        tvMessagePassword.gone()
        imageHelp.gone()
        editPassword.gone()
        editConfirmPassword.gone()
        viewModel.reset()
        editPassword.editText?.let { hideKeyboard(it) }
        editPassword.editText?.setText("")
        editConfirmPassword.editText?.setText("")
        pinLock.replay()
        mConfirmSavePasswordDialog?.dismiss()
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        if (height > 0) {
            if ((mScreenHeight - editConfirmPassword.bottom) > height) return
            clRoot.y = -abs(editConfirmPassword.bottom - mScreenHeight + height.toFloat())
        } else {
            clRoot.y = 0f
        }
    }

    override fun onPause() {
        mKeyboardHeightProvider?.setKeyboardHeightObserver(null)
        editPassword.editText?.let { hideKeyboard(it) }
        editPassword.clearFocus()
        editConfirmPassword.clearFocus()
        mBannerAd?.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mKeyboardHeightProvider?.setKeyboardHeightObserver(this)
        mBannerAd?.resume()
    }

    override fun onDestroy() {
        mKeyboardHeightProvider?.close()
        mColorAnimator.cancel()
        mConfirmSavePasswordDialog?.dismiss()
        mPasswordRulesDialog?.dismiss()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (imageCreate2.isSelected && !imageCreate3.isSelected) {
            if (pinLock.isVisible) {
                resetPin()
            } else {
                resetPattern()
            }
            return
        }
        viewModel.setLockSettingApp(AppLockerPreferences.LOCK_APP_ALWAYS)
        val intent = Intent("android.intent.action.MAIN")
        intent.addCategory("android.intent.category.HOME")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, FirstAllActivity::class.java)
        }
    }
}