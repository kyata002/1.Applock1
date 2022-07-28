package com.mtg.applock.ui.activity.main.configuration

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.mtg.applock.R
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.data.sqllite.model.ConfigurationModel
import com.mtg.applock.permissions.IntentHelper
import com.mtg.applock.permissions.PermissionChecker
import com.mtg.applock.ui.activity.main.OnMainListener
import com.mtg.applock.ui.activity.main.OnUpdateConfigurationListener
import com.mtg.applock.ui.activity.main.configuration.detail.DetailConfigurationActivity
import com.mtg.applock.ui.activity.main.configuration.detail.DetailConfigurationViewModel
import com.mtg.applock.ui.activity.main.configuration.edit.EditConfigurationActivity
import com.mtg.applock.ui.adapter.config.ConfigAdapter
import com.mtg.applock.ui.base.BaseActivity
import com.mtg.applock.ui.view.time.TimeSelectedView
import com.mtg.applock.util.Const
import com.mtg.applock.util.extensions.dialogLayout
import com.mtg.applock.util.extensions.gone
import com.mtg.applock.util.extensions.removeBlink
import com.mtg.applock.util.extensions.visible
import com.mtg.applock.util.preferences.AppLockerPreferences
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.activity_grouplock.*
import kotlinx.android.synthetic.main.dialog_group_lock.view.*
import kotlinx.android.synthetic.main.dialog_customize_time.view.*
import kotlinx.android.synthetic.main.dialog_delete.view.*
import kotlinx.android.synthetic.main.dialog_permission_overlap.view.*
import kotlinx.android.synthetic.main.dialog_permission_usage_data_access.view.*
import kotlinx.android.synthetic.main.dialog_rename.view.*
import kotlinx.android.synthetic.main.popup_more_configuration.view.*

class ConfigurationActivity : BaseActivity<ConfigurationViewModel>(), ConfigAdapter.OnSelectedConfigListener,
    OnUpdateConfigurationListener {

    private lateinit var mConfigAdapter: ConfigAdapter
    private var mConfirmDialog: AlertDialog? = null
    private var mRenameDialog: AlertDialog? = null
    private var mCustomizeTimeDialog: AlertDialog? = null
    private var mConfirmDeleteDialog: AlertDialog? = null
    private var mOverlapPermissionDialog: AlertDialog? = null
    private var mUsageDataAccessPermissionDialog: AlertDialog? = null
    private var mPopupWindow: PopupWindow? = null
    private var mConfigurationList = mutableListOf<Any?>()
    private lateinit var mAppLockerPreferences: AppLockerPreferences
    private lateinit var mAppLockHelper: AppLockHelper
    private var mConfigurationModelSelected: ConfigurationModel? = null
    private var mOnMainListener: OnMainListener? = null

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, ConfigurationActivity::class.java)
        }
    }

    override fun getViewModel(): Class<ConfigurationViewModel> {
        return ConfigurationViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_grouplock
    }

    override fun initViews() {
        initMyViews()
        initMyListener()
    }


    private fun initMyViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        toolbar.setNavigationOnClickListener { onBackPressed() }


        mConfigAdapter = ConfigAdapter(this, mConfigurationList, this)
        recyclerConfiguration.adapter = mConfigAdapter
        recyclerConfiguration.layoutManager = LinearLayoutManager(this)
        recyclerConfiguration.removeBlink()
        viewModel.getConfigurationListLiveData().observe(this) {
            if (it.isEmpty()) {
                llEmpty.visible()
                recyclerConfiguration.gone()
            } else {
                llEmpty.gone()
                recyclerConfiguration.visible()
                mConfigurationList.clear()
                mConfigurationList.addAll(it)
                try {
                    mConfigurationList.add(1, null)
                } catch (e: Exception) {
                }
                mConfigAdapter.notifyDataSetChanged()
            }
        }
        mAppLockerPreferences = AppLockerPreferences(this)
        mAppLockHelper = AppLockHelper(this)
        buildOverlapPermissionDialog()
        buildUsageDataAccessPermissionDialog()
        buildConfirmDeleteDialog()

    }


    override fun onBackPressed() {
        super.onBackPressed()
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
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.packageName))
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


    private fun buildCustomizeTimeDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_customize_time, null, false)
        view.tvTimeStart.isSelected = true
        view.tvTimeEnd.isSelected = false
        builder.setView(view)
        view.btnSaveCustomizeTime.setOnClickListener {
            mConfigurationModelSelected?.let {
                it.isDefaultSetting = view.switchDefaultSetting.isChecked
                if (it.isDefaultSetting) {
                    // nothing
                } else {
                    if (!isTimeCorrect(view.timeSelectedStart.getHour(), view.timeSelectedStart.getMinute(), view.timeSelectedEnd.getHour(), view.timeSelectedEnd.getMinute())) {
                        Toasty.showToast(this, R.string.msg_time_start_less_time_end, Toasty.WARNING)
                        return@setOnClickListener
                    }
                    it.hourStart = view.timeSelectedStart.getHour()
                    it.minuteStart = view.timeSelectedStart.getMinute()
                    it.hourEnd = view.timeSelectedEnd.getHour()
                    it.minuteEnd = view.timeSelectedEnd.getMinute()
                    it.every = view.everyTimeView.getEvery()
                }
                mCustomizeTimeDialog?.dismiss()
                //
                if (viewModel.updateConfiguration(it)) {
                    Toasty.showToast(this, R.string.msg_update_group_lock_successfully, Toasty.SUCCESS)
                    mConfigAdapter.notifyDataSetChanged()
                    mOnMainListener?.onChangeStatusGroupLock()
                } else {
                    Toasty.showToast(this, R.string.msg_update_group_lock_failed, Toasty.ERROR)
                }
            }
        }
        view.imageCloseCustomizeTime.setOnClickListener {
            mCustomizeTimeDialog?.dismiss()
        }
        view.tvTimeStart.setOnClickListener {
            view.tvTimeStart.isSelected = !view.tvTimeStart.isSelected
            if (view.tvTimeStart.isSelected) {
                view.timeSelectedStart.visible()
            } else {
                view.timeSelectedStart.gone()
            }
        }
        view.tvTimeEnd.setOnClickListener {
            view.tvTimeEnd.isSelected = !view.tvTimeEnd.isSelected
            if (view.tvTimeEnd.isSelected) {
                view.timeSelectedEnd.visible()
            } else {
                view.timeSelectedEnd.gone()
            }

        }
        view.timeSelectedStart.setOnUpdateTimeSelectedListener(object : TimeSelectedView.OnUpdateTimeSelectedListener {
            override fun updateTime(hour: Int, minute: Int) {
                view.tvTimeStart.text = String.format("%02d:%02d", hour, minute)
            }
        })
        view.timeSelectedEnd.setOnUpdateTimeSelectedListener(object : TimeSelectedView.OnUpdateTimeSelectedListener {
            override fun updateTime(hour: Int, minute: Int) {
                view.tvTimeEnd.text = String.format("%02d:%02d", hour, minute)

            }
        })
        view.switchDefaultSetting.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                view.viewEnable.visible()
            } else {
                view.viewEnable.gone()
            }
        }
        // setup default
        mConfigurationModelSelected?.let {
            view.timeSelectedStart.setHour(it.hourStart)
            view.timeSelectedStart.setMinute(it.minuteStart)
            view.tvTimeStart.text = String.format("%02d:%02d", it.hourStart, it.minuteStart)
            view.timeSelectedEnd.setHour(it.hourEnd)
            view.timeSelectedEnd.setMinute(it.minuteEnd)
            view.tvTimeEnd.text = String.format("%02d:%02d", it.hourEnd, it.minuteEnd)
            view.switchDefaultSetting.isChecked = it.isDefaultSetting
            if (it.isDefaultSetting) {
                view.viewEnable.visible()
            } else {
                view.viewEnable.gone()
            }
            view.everyTimeView.setEvery(it.getEveryList())
            if (it.isActive) {
                view.btnSaveCustomizeTime.setText(R.string.text_save_and_active)
            } else {
                view.btnSaveCustomizeTime.setText(R.string.text_save)
            }
        }
        //
        mCustomizeTimeDialog?.dismiss()
        mCustomizeTimeDialog = builder.create()
        mCustomizeTimeDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun buildConfirmDeleteDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_delete, null, false)
        builder.setView(view)
        mConfirmDeleteDialog?.dismiss()
        mConfirmDeleteDialog = builder.create()
        mConfirmDeleteDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelDelete.setOnClickListener { mConfirmDeleteDialog?.dismiss() }
        view.btnYesDelete.setOnClickListener {
            mConfirmDeleteDialog?.dismiss()
            mConfigurationModelSelected?.let {
                viewModel.deleteConfiguration(it)
                mOnMainListener?.onChangeStatusGroupLock()
            }
        }
    }

    private fun isTimeCorrect(hourStart: Int, minuteStart: Int, hourEnd: Int, minuteEnd: Int): Boolean {
        return when {
            hourStart < hourEnd -> {
                if (hourStart == hourEnd - 1) {
                    minuteStart <= minuteEnd + 30
                } else {
                    true
                }
            }
            hourStart == hourEnd -> {
                if (minuteEnd - 30 < 0) {
                    false
                } else {
                    minuteStart <= minuteEnd - 30
                }
            }
            else -> {
                false
            }
        }
    }

    private fun buildRenameDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_rename, null, false)
        mConfigurationModelSelected?.name?.let { name ->
            view.editName.setText(name)
        } ?: view.editName.setText(DetailConfigurationViewModel.nameDefault)
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
                mConfigurationModelSelected?.let {
                    if (viewModel.renameConfiguration(it)) {
                        Toasty.showToast(this, R.string.msg_rename_group_lock_successfully, Toasty.SUCCESS)
                        mConfigurationList.forEach { configuration ->
                            if (configuration is ConfigurationModel) {
                                if (configuration.id == it.id) {
                                    configuration.name = name
                                }
                            }
                        }
                        viewModel.updateConfiguration(it)
                        mConfigAdapter.notifyDataSetChanged()
                    } else {
                        Toasty.showToast(this, R.string.msg_rename_group_lock_failed, Toasty.ERROR)
                    }
                }
            }
        }
    }

    fun initMyListener() {
        btnAddGroupLock.setOnClickListener {
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
            startActivityForResult(intent, Const.REQUEST_CODE_ADD_CONFIGURATION)
        }
    }

    override fun onSelectedMoreConfig(view: View?, configurationModel: ConfigurationModel) {
        if (PermissionChecker.checkOverlayPermission(this).not()) {
            mOverlapPermissionDialog?.show()
            dialogLayout(mOverlapPermissionDialog)
            return
        } else if (PermissionChecker.checkUsageAccessPermission(this).not()) {
            mUsageDataAccessPermissionDialog?.show()
            dialogLayout(mUsageDataAccessPermissionDialog)
            return
        }
        showMoreDialog(view, configurationModel)
    }

    override fun onSelectedConfig(view: View?, configurationModel: ConfigurationModel) {
        if (PermissionChecker.checkOverlayPermission(this).not()) {
            mOverlapPermissionDialog?.show()
            dialogLayout(mOverlapPermissionDialog)
            return
        } else if (PermissionChecker.checkUsageAccessPermission(this).not()) {
            mUsageDataAccessPermissionDialog?.show()
            dialogLayout(mUsageDataAccessPermissionDialog)
            return
        }
        activeGroup(configurationModel)
    }

    private fun activeGroup(configurationModel: ConfigurationModel) {
        val builder = AlertDialog.Builder(this)
        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.dialog_group_lock, null, false)
        if (configurationModel.isActive) {
            dialogView.tvMessageActivate.text = String.format(getString(R.string.msg_deactivate_group_lock) + " %s ?", configurationModel.name)
        } else {
            dialogView.tvMessageActivate.text = String.format(getString(R.string.msg_activate_group_lock) + " %s ?", configurationModel.name)
        }
        builder.setView(dialogView)
        mConfirmDialog?.dismiss()
        mConfirmDialog = builder.create()
        mConfirmDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogView.btnCancelActivate.setOnClickListener { mConfirmDialog?.dismiss() }
        dialogView.btnYesActivate.setOnClickListener {
            mConfirmDialog?.dismiss()
            if (configurationModel.isActive) {
                configurationModel.isActive = false
                viewModel.updateConfiguration(configurationModel)
                Toasty.showToast(this, String.format("%s " + getString(R.string.msg_successfully_deactivated), configurationModel.name), Toasty.SUCCESS)
            } else {
                configurationModel.isActive = true
                viewModel.updateConfiguration(configurationModel)
                Toasty.showToast(this, String.format("%s " + getString(R.string.msg_successfully_activated), configurationModel.name), Toasty.SUCCESS)
                if (viewModel.getNumberActiveGroup() == 1) {
                    viewModel.getConfigurationDefault()?.let {
                        if (!it.getPackageAppLockList().contains(Const.SETTINGS_PACKAGE)) {
                            it.addPackageAppLock(Const.SETTINGS_PACKAGE)
                            viewModel.updateConfiguration(it)
                        }
                        if (!it.getPackageAppLockList().contains(Const.CH_PLAY_PACKAGE)) {
                            it.addPackageAppLock(Const.CH_PLAY_PACKAGE)
                            viewModel.updateConfiguration(it)
                        }
                    }
                }
            }
            mConfigAdapter.notifyDataSetChanged()
            mOnMainListener?.onChangeStatusGroupLock()
        }
        mConfirmDialog?.show()
        dialogLayout(mConfirmDialog)
    }

    private fun showMoreDialog(view: View?, configurationModel: ConfigurationModel) {
        mPopupWindow?.dismiss()
        view?.let {
            val popupView = LayoutInflater.from(this).inflate(R.layout.popup_more_configuration, null)
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            mPopupWindow = PopupWindow(popupView, width, height, true)
            if (configurationModel.isActive) {
                popupView.llDeactivate.visible()
                popupView.llActivate.gone()
            } else {
                popupView.llDeactivate.gone()
                popupView.llActivate.visible()
            }
            //
            popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val location = IntArray(2)
            it.getLocationOnScreen(location)
            val heightPixels: Int = resources.displayMetrics.heightPixels
            val heightBottom: Int = resources.getDimensionPixelSize(R.dimen.height_bottom)
            if (location[1] + popupView.measuredHeight > heightPixels - heightBottom) {
                val top = popupView.measuredHeight + it.height / 2
                mPopupWindow?.showAsDropDown(it, -popupView.measuredWidth + 50, -top)
            } else {
                mPopupWindow?.showAsDropDown(it, -popupView.measuredWidth + 50, -it.height / 2)
            }
            popupView.setOnTouchListener { _, _ ->
                mPopupWindow?.dismiss()
                true
            }
            popupView.llEdit.setOnClickListener {
                mPopupWindow?.dismiss()
                val intent = EditConfigurationActivity.newIntent(this)
                intent.putExtra(Const.EXTRA_ID, configurationModel.id)
                startActivityForResult(intent, Const.REQUEST_CODE_CHANGE_CONFIGURATION)
            }
            popupView.llRename.setOnClickListener {
                mPopupWindow?.dismiss()
                mConfigurationModelSelected = configurationModel
                buildRenameDialog()
                mRenameDialog?.show()
                dialogLayout(mRenameDialog)
            }
            popupView.llCustomTime.setOnClickListener {
                mPopupWindow?.dismiss()
                mConfigurationModelSelected = configurationModel
                buildCustomizeTimeDialog()
                mCustomizeTimeDialog?.show()
                dialogLayout(mCustomizeTimeDialog)
            }
            popupView.llDelete.setOnClickListener {
                mPopupWindow?.dismiss()
                mConfigurationModelSelected = configurationModel
                mConfirmDeleteDialog?.show()
                dialogLayout(mConfirmDeleteDialog)
            }
            popupView.llDeactivate.setOnClickListener {
                mPopupWindow?.dismiss()
                activeGroup(configurationModel)
            }
            popupView.llActivate.setOnClickListener {
                mPopupWindow?.dismiss()
                activeGroup(configurationModel)
            }
        }
    }

    override fun onDestroy() {
        mConfirmDialog?.dismiss()
        mCustomizeTimeDialog?.dismiss()
        mRenameDialog?.dismiss()
        mConfirmDeleteDialog?.dismiss()
        mOverlapPermissionDialog?.dismiss()
        mUsageDataAccessPermissionDialog?.dismiss()
        mPopupWindow?.dismiss()
        super.onDestroy()
    }


    override fun onUpdateConfiguration(packageName: String) {
        viewModel.removeAppLockInConfiguration(packageName)
        val iterator = mConfigurationList.listIterator()
        while (iterator.hasNext()) {
            when (val configurationModel = iterator.next()) {
                is ConfigurationModel -> {
                    configurationModel.removePackageAppLock(packageName)
                    if (TextUtils.isEmpty(configurationModel.packageAppLock)) {
                        iterator.remove()
                    }
                }
                else -> {
                }
            }
        }
        mConfigAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            Const.REQUEST_CODE_ADD_CONFIGURATION -> {
                viewModel.loadConfiguration(this)
            }
            Const.REQUEST_CODE_CHANGE_CONFIGURATION -> {
                viewModel.loadConfiguration(this)
                mOnMainListener?.onChangeStatusGroupLock()
            }
        }
    }


}