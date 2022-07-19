package com.MTG.AppLock.ui.activity.main.configuration.edit

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.MTG.AppLock.R
import com.MTG.AppLock.data.sqllite.ConstantDB
import com.MTG.AppLock.permissions.IntentHelper
import com.MTG.AppLock.permissions.PermissionChecker
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemItemViewState
import com.MTG.AppLock.ui.activity.main.configuration.detail.DetailConfigurationActivity
import com.MTG.AppLock.ui.activity.main.configuration.edit.adapter.ConfigurationEditListAdapter
import com.MTG.AppLock.ui.base.BaseActivity
import com.MTG.AppLock.ui.view.ProgressDialog
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.extensions.dialogLayout
import com.MTG.AppLock.util.extensions.removeBlink
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.activity_edit_configuration.*
import kotlinx.android.synthetic.main.activity_edit_configuration.toolbar
import kotlinx.android.synthetic.main.dialog_permission_overlap.view.*
import kotlinx.android.synthetic.main.dialog_permission_usage_data_access.view.*
import kotlinx.android.synthetic.main.dialog_save_configuration.view.*

class EditConfigurationActivity : BaseActivity<EditConfigurationViewModel>() {
    private val mAdapter: ConfigurationEditListAdapter = ConfigurationEditListAdapter()
    private var mOverlapPermissionDialog: AlertDialog? = null
    private var mUsageDataAccessPermissionDialog: AlertDialog? = null
    private var mConfirmSaveConfigurationDialog: AlertDialog? = null
    private var mId: Int = ConstantDB.CONFIGURATION_ID_CREATE
    private var mProgressBar: Dialog? = null

    override fun getViewModel(): Class<EditConfigurationViewModel> {
        return EditConfigurationViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_edit_configuration
    }

    override fun initViews() {
        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
        mId = intent.getIntExtra(Const.EXTRA_ID, ConstantDB.CONFIGURATION_ID_CREATE)
        if (mId == ConstantDB.CONFIGURATION_ID_CREATE) {
            finish()
            return
        }
        viewModel.setId(mId)
        loadConfiguration(true)
        viewModel.getNameLiveData().observe(this, {
            toolbar.setTitle(it)
        })
        recyclerApplication.adapter = mAdapter
        recyclerApplication.removeBlink()
        viewModel.getAppDataListLiveData().observe(this, {
            hideProgressBar()
            mAdapter.setAppDataList(it)
        })
        buildOverlapPermissionDialog()
        buildUsageDataAccessPermissionDialog()
        buildConfirmSaveConfigurationDialog()
    }

    private fun showProgressBar() {
        mProgressBar?.dismiss()
        mProgressBar = ProgressDialog.progressDialogV2(this)
        mProgressBar?.show()
    }

    private fun hideProgressBar() {
        mProgressBar?.dismiss()
    }

    override fun initListener() {

        btnAddApplication.setOnClickListener {
            if (PermissionChecker.checkOverlayPermission(this).not()) {
                viewModel.setReload(false)
                mOverlapPermissionDialog?.show()
                dialogLayout(mOverlapPermissionDialog)
                return@setOnClickListener
            } else if (PermissionChecker.checkUsageAccessPermission(this).not()) {
                viewModel.setReload(false)
                mUsageDataAccessPermissionDialog?.show()
                dialogLayout(mUsageDataAccessPermissionDialog)
                return@setOnClickListener
            }
            val intent = DetailConfigurationActivity.newIntent(this)
            intent.putExtra(Const.EXTRA_ID, mId)
            viewModel.updateConfiguration()
            startActivityForResult(intent, Const.REQUEST_CODE_ADD_CONFIGURATION)
        }
        mAdapter.appItemClicked = this@EditConfigurationActivity::onAppSelected
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_configuration,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.save_configuration){
            if (viewModel.updateConfiguration()) {
                Toasty.showToast(this@EditConfigurationActivity, R.string.msg_update_group_lock_successfully, Toasty.SUCCESS)
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Toasty.showToast(this@EditConfigurationActivity, R.string.msg_update_group_lock_failed, Toasty.ERROR)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun buildOverlapPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_permission_overlap, null, false)
        builder.setView(view)
        mOverlapPermissionDialog?.dismiss()
        mOverlapPermissionDialog = builder.create()
        mOverlapPermissionDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelOverlap.setOnClickListener { mOverlapPermissionDialog?.dismiss() }
        view.btnGotoSettingOverlap.setOnClickListener {
            mOverlapPermissionDialog?.dismiss()
            if (PermissionChecker.checkOverlayPermission(this).not()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivityForResult(intent, Const.REQUEST_CODE_FLOATING_CONTROLS_SYSTEM_WINDOWS)
                }
            }
        }
    }

    private fun buildUsageDataAccessPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_permission_usage_data_access, null, false)
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

    private fun onAppSelected(position: Int, selectedApp: AppLockItemItemViewState) {
        if (mAdapter.getCount() > 1) {
            mAdapter.removeData(selectedApp)
            viewModel.removeData(selectedApp)
        } else {
            Toasty.showToast(this, R.string.msg_requires_at_least_one_application, Toasty.WARNING)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            Const.REQUEST_CODE_ADD_CONFIGURATION -> {
                loadConfiguration(false)
            }
        }
    }

    private fun loadConfiguration(isLoadMain: Boolean) {
        showProgressBar()
        viewModel.loadConfiguration(isLoadMain)
    }

    override fun onDestroy() {
        mOverlapPermissionDialog?.dismiss()
        mUsageDataAccessPermissionDialog?.dismiss()
        mConfirmSaveConfigurationDialog?.dismiss()
        mProgressBar?.dismiss()
        super.onDestroy()
    }

    private fun buildConfirmSaveConfigurationDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_save_configuration, null, false)
        if (viewModel.isConfigurationActive()) {
            view.tvMessageSaveConfiguration.setText(R.string.msg_do_you_want_to_save_and_active_the_group_lock)
        } else {
            view.tvMessageSaveConfiguration.setText(R.string.msg_do_you_want_to_save_the_group_lock)
        }
        builder.setView(view)
        mConfirmSaveConfigurationDialog?.dismiss()
        mConfirmSaveConfigurationDialog = builder.create()
        mConfirmSaveConfigurationDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelSaveConfiguration.setOnClickListener {
            mConfirmSaveConfigurationDialog?.dismiss()
            finish()
        }
        view.btnYesSaveConfiguration.setOnClickListener {
            mConfirmSaveConfigurationDialog?.dismiss()
            if (viewModel.updateConfiguration()) {
                Toasty.showToast(this@EditConfigurationActivity, R.string.msg_update_group_lock_successfully, Toasty.SUCCESS)
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Toasty.showToast(this@EditConfigurationActivity, R.string.msg_update_group_lock_failed, Toasty.ERROR)
            }
        }
    }

    override fun onBackPressed() {
        mConfirmSaveConfigurationDialog?.show()
        dialogLayout(mConfirmSaveConfigurationDialog)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, EditConfigurationActivity::class.java)
        }
    }
}