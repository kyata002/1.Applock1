package com.MTG.AppLock.ui.activity.main.settings.superpassword.validate

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
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.activity.main.MainActivity
import com.MTG.AppLock.ui.activity.password.changepassword.ChangePasswordActivity
import com.MTG.AppLock.ui.base.BaseActivity
import com.MTG.AppLock.util.ApplicationListUtils
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.extensions.dialogLayout
import com.MTG.AppLock.util.extensions.invisible
import com.MTG.AppLock.util.extensions.visible
import com.MTG.AppLock.util.preferences.AppLockerPreferences

import com.MTG.keyboard.KeyboardHeightObserver
import com.MTG.keyboard.KeyboardHeightProvider
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.dialog_rulues_supper_password.view.*
import java.util.*
import kotlin.math.abs

class SuperPasswordValidateActivity : BaseActivity<SuperPasswordValidateViewModel>(), KeyboardHeightObserver {
    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null
    private var mScreenHeight = 0
    private var mPasswordRulesDialog: AlertDialog? = null

    override fun getViewModel(): Class<SuperPasswordValidateViewModel> {
        return SuperPasswordValidateViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_forgot_password
    }

    override fun initViews() {
        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
        mKeyboardHeightProvider = KeyboardHeightProvider(this)
        clRoot.post {
            mKeyboardHeightProvider?.start()
        }
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        mScreenHeight = displayMetrics.heightPixels

        buildPasswordRulesDialog()
    }

    override fun initListener() {

        btnCheck.setOnClickListener {
            if (viewModel.checkSuperPassword(this, editSuperPassword.text.toString().trim())) {
                // goto change password
                val intent = ChangePasswordActivity.newIntent(this)
                startActivityForResult(intent, Const.REQUEST_CODE_VALIDATE_SUPPER_PASSWORD)
            }
        }
        imageSuperPasswordShow.setOnClickListener {
            imageSuperPasswordShow.isSelected = !imageSuperPasswordShow.isSelected
            if (imageSuperPasswordShow.isSelected) {
                editSuperPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                editSuperPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
        clRoot.setOnClickListener {
            hideKeyboard()
        }
        imageHelp.setOnClickListener {
            mPasswordRulesDialog?.show()
            dialogLayout(mPasswordRulesDialog)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            Const.REQUEST_CODE_VALIDATE_SUPPER_PASSWORD -> {
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
            if ((mScreenHeight - clSuperPassword.bottom) > height) return
            toolbar.invisible()
            clRoot.y = -abs(clSuperPassword.bottom - mScreenHeight + height.toFloat())
        } else {
            toolbar.visible()
            clRoot.y = 0f
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
        editSuperPassword.clearFocus()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SuperPasswordValidateActivity::class.java)
        }
    }
}