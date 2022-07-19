package com.MTG.AppLock.ui.activity.main.settings.email

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.inputmethod.InputMethodManager
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.base.BaseActivity
import com.MTG.AppLock.util.extensions.gone
import com.MTG.AppLock.util.extensions.invisible
import com.MTG.AppLock.util.extensions.visible
import com.MTG.keyboard.KeyboardHeightObserver
import com.MTG.keyboard.KeyboardHeightProvider
import kotlinx.android.synthetic.main.activity_email.*
import kotlin.math.abs

class EmailActivity : BaseActivity<EmailViewModel>(), KeyboardHeightObserver {
    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null
    private var mScreenHeight = 0

    override fun getViewModel(): Class<EmailViewModel> {
        return EmailViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_email
    }

    override fun initViews() {
        oldEmail.setText(viewModel.getEmail())
        if (TextUtils.isEmpty(viewModel.getEmail())) {
            oldEmail.invisible()
            tvOldEmail.gone()
        }
        mKeyboardHeightProvider = KeyboardHeightProvider(this)
        clRoot.post {
            mKeyboardHeightProvider?.start()
        }
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        mScreenHeight = displayMetrics.heightPixels

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

    override fun initListener() {
        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        toolbar.setNavigationOnClickListener { onBackPressed() }

        btnSave.setOnClickListener {
            val success = viewModel.saveEmail(this, newEmail.text.toString().trim())
            if (success) {
                onBackPressed()
            }
            hideKeyboard()
        }
        clRoot.setOnClickListener {
            hideKeyboard()
        }
    }

    override fun onBackPressed() {
        hideKeyboard()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onPause() {
        mKeyboardHeightProvider?.setKeyboardHeightObserver(null)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mKeyboardHeightProvider?.setKeyboardHeightObserver(this)
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
        newEmail.clearFocus()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, EmailActivity::class.java)
        }
    }
}
