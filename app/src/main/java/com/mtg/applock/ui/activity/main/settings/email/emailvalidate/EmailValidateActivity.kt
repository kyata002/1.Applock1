package com.mtg.applock.ui.activity.main.settings.email.emailvalidate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.DisplayMetrics
import android.view.inputmethod.InputMethodManager
import com.mtg.applock.R
import com.mtg.applock.ui.activity.main.MainActivity
import com.mtg.applock.ui.activity.password.changepassword.ChangePasswordActivity
import com.mtg.applock.ui.base.BaseActivity
import com.mtg.applock.util.ApplicationListUtils
import com.mtg.applock.util.Const
import com.mtg.applock.util.extensions.invisible
import com.mtg.applock.util.extensions.visible
import com.mtg.applock.util.preferences.AppLockerPreferences

import com.mtg.keyboard.KeyboardHeightObserver
import com.mtg.keyboard.KeyboardHeightProvider
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.activity_email_validate.*
import kotlin.math.abs

class EmailValidateActivity : BaseActivity<EmailValidateViewModel>(), KeyboardHeightObserver {
    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null
    private var mScreenHeight = 0

    override fun getViewModel(): Class<EmailValidateViewModel> {
        return EmailValidateViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_email_validate
    }

    override fun initViews() {
        viewModel.getEmailValidateLiveData().observe(this, {
            when (it) {
                SUCCESS -> {
                    // email exactly
                    // goto change password
                    val intent = ChangePasswordActivity.newIntent(this)
                    startActivityForResult(intent, Const.REQUEST_CODE_VALIDATE_EMAIL)
                    Toasty.showToast(this, R.string.msg_email_correct, Toasty.SUCCESS)
                }
                FAILED_FORMAT -> {
                    Toasty.showToast(this, R.string.text_invalid_email_address, Toasty.ERROR)
                }
                FAILED -> {
                    Toasty.showToast(this, R.string.msg_email_incorrect, Toasty.ERROR)
                }
                FAILED_EMPTY -> {
                    Toasty.showToast(this, R.string.msg_please_enter_text, Toasty.ERROR)
                }
            }
        })
        mKeyboardHeightProvider = KeyboardHeightProvider(this)
        clRoot.post {
            mKeyboardHeightProvider?.start()
        }
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        mScreenHeight = displayMetrics.heightPixels

    }

    override fun initListener() {
        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
        btnCheck.setOnClickListener {
            viewModel.checkEmail(editEmail.text.toString().trim())
            hideKeyboard()
        }
        clRoot.setOnClickListener {
            hideKeyboard()
        }
    }

    override fun onPause() {
        mKeyboardHeightProvider?.setKeyboardHeightObserver(null)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mKeyboardHeightProvider?.setKeyboardHeightObserver(this)
    }

    override fun onBackPressed() {
        hideKeyboard()
        onLauncher()
    }

    private fun onLauncher() {
        ApplicationListUtils.instance?.destroy()
        viewModel.setLockSettingApp(AppLockerPreferences.LOCK_APP_ALWAYS)
        val intent = Intent("android.intent.action.MAIN")
        intent.addCategory("android.intent.category.HOME")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        hideKeyboard()
        mKeyboardHeightProvider?.close()
        super.onDestroy()
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        editEmail.clearFocus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            Const.REQUEST_CODE_VALIDATE_EMAIL -> {
                hideKeyboard()
                viewModel.setFinishAllActivity(false)
                val intent = MainActivity.newIntent(this)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        if (height > 0) {
            if ((mScreenHeight - clEmail.bottom) > height) return
            toolbar.invisible()
            clRoot.y = -abs(clEmail.bottom - mScreenHeight + height.toFloat())
        } else {
            toolbar.visible()
            clRoot.y = 0f
        }
    }

    companion object {
        const val SUCCESS = 1
        const val FAILED = 2
        const val FAILED_FORMAT = 3
        const val FAILED_EMPTY = 4
        fun newIntent(context: Context): Intent {
            return Intent(context, EmailValidateActivity::class.java)
        }
    }
}
