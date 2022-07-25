package com.mtg.applock.ui.activity.main.settings.superpassword

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.mtg.applock.R
import com.mtg.applock.ui.base.BaseActivity
import com.mtg.applock.util.extensions.dialogLayout
import com.mtg.applock.util.extensions.gone
import com.mtg.applock.util.extensions.invisible

import com.mtg.keyboard.KeyboardHeightObserver
import com.mtg.keyboard.KeyboardHeightProvider
import kotlinx.android.synthetic.main.activity_recovering_password.*
import kotlinx.android.synthetic.main.dialog_rulues_supper_password.view.*
import java.util.*

class SuperPasswordActivity : BaseActivity<SuperPasswordViewModel>(), KeyboardHeightObserver {
    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null
    private var mScreenHeight = 0
    private var mPasswordRulesDialog: AlertDialog? = null

    override fun getViewModel(): Class<SuperPasswordViewModel> {
        return SuperPasswordViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_recovering_password
    }

    override fun initViews() {
        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
        if (viewModel.noSuperPassword()) {
            tvOldSuperPassword.gone()
            editOldSuperPassword.invisible()
            imageHelp.gone()
            imageOldSuperPasswordShow.gone()
        }
        mKeyboardHeightProvider = KeyboardHeightProvider(this)
        clRoot.post {
            mKeyboardHeightProvider?.start()
        }
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        mScreenHeight = displayMetrics.heightPixels

        buildPasswordRulesDialog()
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

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
//        if (height > 0) {
//            if ((mScreenHeight - clSuperPassword.bottom) > height) return
//            toolbar.invisible()
//            clRoot.y = -abs(clSuperPassword.bottom - mScreenHeight + height.toFloat())
//        } else {
//            toolbar.visible()
//            clRoot.y = 0f
//        }
    }

    override fun initListener() {

        btnSave.setOnClickListener {
            val success = viewModel.saveSuperPassword(this, editOldSuperPassword.text.toString().trim(), editNewSuperPassword.text.toString().trim(), editConfirmSuperPassword.text.toString().trim())
            if (success) {
                hideKeyboard()
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                onBackPressed()
            }
        }
        clRoot.setOnClickListener {
            hideKeyboard()
        }
        imageOldSuperPasswordShow.setOnClickListener {
            imageOldSuperPasswordShow.isSelected = !imageOldSuperPasswordShow.isSelected
            if (imageOldSuperPasswordShow.isSelected) {
                editOldSuperPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                editOldSuperPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
        imageNewSuperPasswordShow.setOnClickListener {
            imageNewSuperPasswordShow.isSelected = !imageNewSuperPasswordShow.isSelected
            if (imageNewSuperPasswordShow.isSelected) {
                editNewSuperPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                editNewSuperPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
        imageConfirmSuperPasswordShow.setOnClickListener {
            imageConfirmSuperPasswordShow.isSelected = !imageConfirmSuperPasswordShow.isSelected
            if (imageConfirmSuperPasswordShow.isSelected) {
                editConfirmSuperPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                editConfirmSuperPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
        imageHelp.setOnClickListener {
            mPasswordRulesDialog?.show()
            dialogLayout(mPasswordRulesDialog)
        }
        imageHelpNew.setOnClickListener {
            mPasswordRulesDialog?.show()
            dialogLayout(mPasswordRulesDialog)
        }
    }

    override fun onBackPressed() {
        hideKeyboard()
        setResult(Activity.RESULT_CANCELED)
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
        mPasswordRulesDialog?.dismiss()
        super.onDestroy()
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        editNewSuperPassword.clearFocus()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SuperPasswordActivity::class.java)
        }
    }
}