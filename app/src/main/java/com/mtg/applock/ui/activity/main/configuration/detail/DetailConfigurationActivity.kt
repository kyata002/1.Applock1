package com.mtg.applock.ui.activity.main.configuration.detail

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.mtg.applock.R
import com.mtg.applock.control.OnLockListener
import com.mtg.applock.data.sqllite.ConstantDB
import com.mtg.applock.permissions.IntentHelper
import com.mtg.applock.permissions.PermissionChecker
import com.mtg.applock.ui.activity.main.az.model.AppLockItemItemViewState
import com.mtg.applock.ui.activity.main.configuration.detail.adapter.ConfigurationDetailListAdapter
import com.mtg.applock.ui.base.BaseActivity
import com.mtg.applock.ui.view.ProgressDialog
import com.mtg.applock.util.Const
import com.mtg.applock.util.extensions.*
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.activity_detail_configuration.*
import kotlinx.android.synthetic.main.activity_detail_configuration.toolbar
import kotlinx.android.synthetic.main.dialog_permission_overlap.view.*
import kotlinx.android.synthetic.main.dialog_permission_usage_data_access.view.*
import kotlinx.android.synthetic.main.dialog_rename.view.*
import java.util.*

class DetailConfigurationActivity : BaseActivity<DetailConfigurationViewModel>() {
    private val mAdapter: ConfigurationDetailListAdapter = ConfigurationDetailListAdapter()
    private var mOverlapPermissionDialog: AlertDialog? = null
    private var mUsageDataAccessPermissionDialog: AlertDialog? = null
    private var mRenameDialog: AlertDialog? = null
    private var mProgressBar: Dialog? = null

    override fun getViewModel(): Class<DetailConfigurationViewModel> {
        return DetailConfigurationViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_detail_configuration
    }

    override fun initViews() {
        recyclerViewAppLockList.adapter = mAdapter
        recyclerViewAppLockList.removeBlink()
        viewModel.getAppDataListLiveData().observe(this, {
            hideProgressBar()
            mAdapter.setAppDataList(it)
            if (it.isEmpty()) {
                textEmpty.visible()
                recyclerViewAppLockList.visible()
            } else {
                textEmpty.gone()
                recyclerViewAppLockList.visible()
            }
            getTitleSave()
        })
        val id = intent.getIntExtra(Const.EXTRA_ID, ConstantDB.CONFIGURATION_ID_CREATE)
        viewModel.setId(id)
        loadConfiguration()
        viewModel.getNameLiveData().observe(this, {
            toolbar.setTitle(it)
            buildRenameDialog()
        })
        buildOverlapPermissionDialog()
        buildUsageDataAccessPermissionDialog()
    }

    private fun buildOverlapPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_permission_overlap, null, false)
        builder.setView(view)
        mOverlapPermissionDialog?.dismiss()
        mOverlapPermissionDialog = builder.create()
        mOverlapPermissionDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelOverlap.setOnClickListener { mOverlapPermissionDialog?.dismiss() }
        view.btnGotoSettingOverlap.setOnClickListener {
            mOverlapPermissionDialog?.dismiss()
            if (PermissionChecker.checkOverlayPermission(this).not()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivityForResult(
                        intent,
                        Const.REQUEST_CODE_FLOATING_CONTROLS_SYSTEM_WINDOWS
                    )
                }
            }
        }
    }

    private fun buildUsageDataAccessPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this)
            .inflate(R.layout.dialog_permission_usage_data_access, null, false)
        builder.setView(view)
        builder.setCancelable(false)
        mUsageDataAccessPermissionDialog?.dismiss()
        mUsageDataAccessPermissionDialog = builder.create()
        mUsageDataAccessPermissionDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelUsageDataAccess.setOnClickListener { mUsageDataAccessPermissionDialog?.dismiss() }
        view.btnGotoSettingUsageDataAccess.setOnClickListener {
            mUsageDataAccessPermissionDialog?.dismiss()
            if (PermissionChecker.checkUsageAccessPermission(this).not()) {
                startActivity(IntentHelper.usageAccessIntent())
            }
        }
    }

    private fun buildRenameDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_rename, null, false)
        view.editName.setText(toolbar.getTitle())
        builder.setView(view)
        mRenameDialog?.dismiss()
        mRenameDialog = builder.create()
        mRenameDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelRename.setOnClickListener { mRenameDialog?.dismiss() }
        view.btnSaveRename.setOnClickListener {
            val name = view.editName.text.toString()
            if (TextUtils.isEmpty(name)) {
                Toasty.showToast(this, R.string.msg_group_lock_name_cannot_be_empty, Toasty.WARNING)
            } else {
                mRenameDialog?.dismiss()
                save(name)
            }
        }
    }

    private fun save(name: String) {
        if (viewModel.saveConfiguration(name)) {
            if (viewModel.isIdDefault()) {
                Toasty.showToast(this, R.string.msg_create_group_lock_successfully, Toasty.SUCCESS)
            } else {
                Toasty.showToast(this, R.string.msg_update_group_lock_successfully, Toasty.SUCCESS)
            }
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            if (viewModel.isIdDefault()) {
                Toasty.showToast(this, R.string.msg_create_group_lock_failed, Toasty.ERROR)
            } else {
                Toasty.showToast(this, R.string.msg_update_group_lock_failed, Toasty.ERROR)
            }
        }
    }

    override fun initListener() {
        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
        mAdapter.appItemClicked = this@DetailConfigurationActivity::onAppSelected
        btnSave.setOnClickListener {
            if (viewModel.isIdDefault()) {
                if (viewModel.hasAppLock(this)) {
                    mRenameDialog?.show()
                    dialogLayout(mRenameDialog)
                } else {
                    Toasty.showToast(this, R.string.msg_please_choose_at_least_one, Toasty.WARNING)
                }
            } else {
                if (viewModel.getCountSelected() > 0) {
                    save(toolbar.title.toString())
                } else {
                    Toasty.showToast(this, R.string.msg_please_choose_at_least_one, Toasty.WARNING)
                }
            }
        }
    }

    private fun getTitleSave() {
        val count = viewModel.getCountSelected()
        val title = if (count > 1) {
            String.format(
                Locale.getDefault(),
                "%s (%d) %s",
                getString(R.string.text_click_here_to_add),
                count
//                getString(R.string.text_apps_to_the_group)
            )
        } else {
            String.format(
                Locale.getDefault(),
                "%s (%d) %s",
                getString(R.string.text_click_here_to_add),
                count
//                getString(R.string.text_app_to_the_group)
            )
        }
        btnSave.text = title
    }

    private fun onAppSelected(position: Int, selectedApp: AppLockItemItemViewState) {
        if (PermissionChecker.checkOverlayPermission(this).not()) {
            mOverlapPermissionDialog?.show()
            dialogLayout(mOverlapPermissionDialog)
            return
        } else if (PermissionChecker.checkUsageAccessPermission(this).not()) {
            mUsageDataAccessPermissionDialog?.show()
            dialogLayout(mUsageDataAccessPermissionDialog)
        } else {
            if (selectedApp.isLocked) {
                viewModel.unlockApp(selectedApp, object : OnLockListener {
                    override fun onLockListener(lock: Boolean, positionChange: Int) {
                        runOnUiThread {
                            mAdapter.notifyItemChanged(positionChange)
                        }
                    }
                })
            } else {
                viewModel.lockApp(selectedApp, object : OnLockListener {
                    override fun onLockListener(lock: Boolean, positionChange: Int) {
                        runOnUiThread {
                            mAdapter.notifyItemChanged(positionChange)
                        }
                    }
                })
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        mOverlapPermissionDialog?.dismiss()
        mUsageDataAccessPermissionDialog?.dismiss()
        mRenameDialog?.dismiss()
        mProgressBar?.dismiss()
        super.onDestroy()
    }

    override fun onBackPressed() {

        super.onBackPressed()

    }

    private fun loadConfiguration() {
        showProgressBar()
        viewModel.loadConfiguration()
    }

    private fun showProgressBar() {
        mProgressBar?.dismiss()
        mProgressBar = ProgressDialog.progressDialogV2(this)
        mProgressBar?.show()
    }

    private fun hideProgressBar() {
        mProgressBar?.dismiss()
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, DetailConfigurationActivity::class.java)
        }
    }
}
