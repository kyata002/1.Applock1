package com.MTG.AppLock.ui.activity.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.MTG.AppLock.R
import com.MTG.AppLock.permissions.IntentHelper
import com.MTG.AppLock.permissions.PermissionChecker
import com.MTG.AppLock.ui.activity.intruders.IntrudersPhotoItemViewState
import com.MTG.AppLock.ui.activity.intruders.IntrudersPhotosActivity
import com.MTG.AppLock.ui.activity.main.configuration.ConfigurationActivity
import com.MTG.AppLock.ui.activity.main.personal.PersonalFragment
import com.MTG.AppLock.ui.activity.main.settings.SettingsActivity
import com.MTG.AppLock.ui.activity.main.settings.superpassword.SuperPasswordActivity
import com.MTG.AppLock.ui.base.BaseActivity
import com.MTG.AppLock.ui.view.ProgressDialog
import com.MTG.AppLock.util.ApplicationListUtils
import com.MTG.AppLock.util.CommonUtils
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.alarms.AlarmsUtils
import com.MTG.AppLock.util.extensions.dialogLayout
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import com.MTG.AppLock.util.sharerate.SharePreferenceUtils
import com.bumptech.glide.Glide
import com.common.control.dialog.RateAppDialog
import com.common.control.interfaces.RateCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_intruder_photo.view.*
import kotlinx.android.synthetic.main.dialog_not_enough_storage.view.*
import kotlinx.android.synthetic.main.dialog_permission_overlap.view.*
import kotlinx.android.synthetic.main.dialog_permission_usage_data_access.view.*
import kotlinx.android.synthetic.main.dialog_permission_write.view.*
import kotlinx.android.synthetic.main.dialog_rating_app.view.*
import kotlinx.android.synthetic.main.dialog_supper_password.view.*
import kotlinx.android.synthetic.main.dialog_warning_close_app.view.*


class MainActivity : BaseActivity<MainViewModel>(), PersonalFragment.OnRequestWritePermissionsListener, OnMainListener, InstallStateUpdatedListener {
    val MY_REQUEST_CODE = 123;
    private var mIntruderPhotoDialog: AlertDialog? = null
    private var mStoragePermissionDialog: AlertDialog? = null
    private var mOverlapPermissionDialog: AlertDialog? = null
    private var mUsageDataAccessPermissionDialog: AlertDialog? = null
    private var mExitDialog: AlertDialog? = null
    private var mRatingDialog: AlertDialog? = null
    private var mSuperPasswordDialog: AlertDialog? = null
    private var mAdView: NativeAd? = null
    private var mHasGroupLock = false
    private var mHasSettings = false
    private var mOnUpdateConfigurationListener: OnUpdateConfigurationListener? = null
    private var mHasShowWritePermission: Boolean = false
    private var mProgressBar: Dialog? = null

    private lateinit var appUpdateManager: AppUpdateManager



    private var mRemoveReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (TextUtils.equals(it.action, Intent.ACTION_PACKAGE_FULLY_REMOVED)) {
                    it.dataString?.let { packageNameFull ->
                        val packageNameUninstall = packageNameFull.replaceFirst("package:", "")
                        context?.let { _ -> onEventUninstallApplication(packageNameUninstall) }
                    }
                }
            }
        }
    }

    private fun registerRemoveReceiver() {
        val installFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addDataScheme("package")
        }
        registerReceiver(mRemoveReceiver, installFilter)
    }

    private fun unregisterRemoveReceiver() {
        unregisterReceiver(mRemoveReceiver)
    }

    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initViews() {

        setSupportActionBar(toolbar);

        if (intent.getBooleanExtra(Const.IS_FROM_FIRST_ALL, false)) {
            showProgressBar()
            ApplicationListUtils.instance?.reload(this)
        }





        viewPager.adapter = MainPagerAdapter(this, supportFragmentManager)
        viewPager.offscreenPageLimit = 2
        tabLayout.setupWithViewPager(viewPager)
        viewModel.getIntruderPhotoLiveData().observe(this, {
            showIntruderPhotoDialog(it)
        })
        if (!CommonUtils.isCanSaveFile(this, 0) && viewModel.getIntrudersCatcherEnabled()) {
            val builder = AlertDialog.Builder(this)
            val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_not_enough_storage, null, false)
            view.tvMessageNotEnoughStorage.setText(R.string.msg_not_enough_memory_intruder)
            view.btnYesNotEnoughStorage.setOnClickListener {
                mIntruderPhotoDialog?.dismiss()
            }
            builder.setView(view)
            mIntruderPhotoDialog?.dismiss()
            mIntruderPhotoDialog = builder.create()
            mIntruderPhotoDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mIntruderPhotoDialog?.setCancelable(false)
            mIntruderPhotoDialog?.show()
            dialogLayout(mIntruderPhotoDialog)
        }
        if (viewModel.useThemeDownload()) {
            if (!isStoragePermissionGranted()) {
                showStoragePermissionDialog()
            }
        }
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (position == MainPagerAdapter.INDEX_PERSONAL) {
                    if (!isStoragePermissionGranted()) {
                        showStoragePermissionDialog()
                    } else {
                        (viewPager.adapter?.instantiateItem(viewPager, MainPagerAdapter.INDEX_PERSONAL) as PersonalFragment).setupNumberTypeIfChange()
                    }
                } else if (position == MainPagerAdapter.INDEX_LOCKED) {
                    showDialogOverlapOrUsageDataAccessPermission()
                } else {
                    showDialogOverlapOrUsageDataAccessPermission()
                }
            }
        })

        viewModel.setLockSettingApp(AppLockerPreferences.LOCK_APP_ALWAYS)
        buildOverlapPermissionDialog()
        buildUsageDataAccessPermissionDialog()
        if (isStoragePermissionGranted()) {
            viewModel.scanFileAndDelete(this)
        }
        AlarmsUtils.startAllAlarms(this, viewModel.getConfigurationActiveList())
        registerRemoveReceiver()
        if (!viewModel.hasSuperPassword()) {
            showSuperPasswordDialog()
        }

        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    this,
                    MY_REQUEST_CODE)

            }
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_screen_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.group_lock){
            startActivity(ConfigurationActivity.newIntent(this))
        }
        if(item.itemId==R.id.settings){
            startActivity(SettingsActivity.newIntent(this))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSuperPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_supper_password, null, false)
        builder.setView(view)
        builder.setCancelable(false)
        mSuperPasswordDialog?.dismiss()
        mSuperPasswordDialog = builder.create()
        mSuperPasswordDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnNextSuperPassword.setOnClickListener {
            mSuperPasswordDialog?.dismiss()
            startActivityForResult(SuperPasswordActivity.newIntent(this), Const.REQUEST_CODE_CREATE_SUPPER_PASSWORD)
        }
        mSuperPasswordDialog?.show()
        dialogLayout(mSuperPasswordDialog)
    }

    fun setShowAction() {
        if (viewPager.currentItem == MainPagerAdapter.INDEX_LOCKED) {

        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            // permission is automatically granted on sdk < 23 upon installation
            true
        }
    }

    @SuppressLint("CheckResult")
    private fun requestWritePermissions() {
        viewModel.setReload(false)
        TedPermission.with(this).setPermissionListener(object : PermissionListener {
            override fun onPermissionGranted() {
                mHasShowWritePermission = false
                (viewPager.adapter?.instantiateItem(viewPager, MainPagerAdapter.INDEX_PERSONAL) as PersonalFragment).setupNumberType()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                mHasShowWritePermission = false
            }
        }).setDeniedMessage(R.string.msg_denied_permission).setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).check()
    }


    private fun onEventUninstallApplication(packageName: String) {
        ApplicationListUtils.instance?.removePackageName(packageName)
        viewModel.removePackageName(packageName)
        mOnUpdateConfigurationListener?.onUpdateConfiguration(packageName)
    }

    private fun showStoragePermissionDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_permission_write, null, false)
        //
        view.btnDenyPermissionWrite.setOnClickListener {
            mStoragePermissionDialog?.dismiss()
        }
        view.btnAllowPermissionWrite.setOnClickListener {
            mHasShowWritePermission = true
            mStoragePermissionDialog?.dismiss()
            requestWritePermissions()
        }
        builder.setView(view)
        mStoragePermissionDialog?.dismiss()
        mStoragePermissionDialog = builder.create()
        mStoragePermissionDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mStoragePermissionDialog?.setCancelable(true)
        mStoragePermissionDialog?.show()
        dialogLayout(mStoragePermissionDialog)
    }

    private fun showIntruderPhotoDialog(intrudersPhotoItemViewState: IntrudersPhotoItemViewState) {
        if (CommonUtils.isCanSaveFile(this, 0)) {
            val builder = AlertDialog.Builder(this)
            val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_intruder_photo, null, false)
            Glide.with(this).load(intrudersPhotoItemViewState.filePath).into(view.imageIntruder)
            view.btnCheckIntruder.setOnClickListener {
                mIntruderPhotoDialog?.dismiss()
                // go to intruder photo
                val intent = IntrudersPhotosActivity.newIntent(this)
                startActivity(intent)
                viewModel.setShowIntruderDialog(false)
            }
            view.btnSkipIntruder.setOnClickListener {
                mIntruderPhotoDialog?.dismiss()
            }
            builder.setView(view)
            mIntruderPhotoDialog?.dismiss()
            mIntruderPhotoDialog = builder.create()
            mIntruderPhotoDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mIntruderPhotoDialog?.setCancelable(false)
            mIntruderPhotoDialog?.show()
            dialogLayout(mIntruderPhotoDialog)
        }
    }

    private fun buildOverlapPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_permission_overlap, null, false)
        builder.setView(view)
        builder.setCancelable(false)
        mOverlapPermissionDialog?.dismiss()
        mOverlapPermissionDialog = builder.create()
     //   mOverlapPermissionDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
       // mUsageDataAccessPermissionDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelUsageDataAccess.setOnClickListener { mUsageDataAccessPermissionDialog?.dismiss() }
        view.btnGotoSettingUsageDataAccess.setOnClickListener {
            mUsageDataAccessPermissionDialog?.dismiss()
            if (PermissionChecker.checkUsageAccessPermission(this).not()) {
                startActivity(IntentHelper.usageAccessIntent())
            }
        }
    }

    private fun showDialogOverlapOrUsageDataAccessPermission(): Boolean {
        if (PermissionChecker.checkOverlayPermission(this).not()) {
            viewModel.setReload(false)
            viewModel.setLockSettingApp(AppLockerPreferences.LOCK_APP_WAIT)
            //
            mOverlapPermissionDialog?.show()
          //  dialogLayout(mOverlapPermissionDialog)
            return false
        } else if (!PermissionChecker.checkUsageAccessPermission(this)) {
            viewModel.setReload(false)
            //
            mUsageDataAccessPermissionDialog?.show()
           // dialogLayout(mUsageDataAccessPermissionDialog)
            return false
        }
        return true
    }

    override fun onBackPressed() {
        exitApp()
    }

    private fun exitApp() {
//        val index: Int = if (viewModel.isRating()) {
//            1
//        } else {
//            if (viewModel.checkShowRateAfterLater()) {
//                0
//            } else {
//                1
//            }
//        }
//        when (index) {
//            0 -> {
                showRateDialog(true)
//            }
//            else -> {
//                if (mExitDialog == null) {
//                    buildAlertMessageCloseApp()
//                }
//                mExitDialog?.show()
//                dialogLayout(mExitDialog)
//            }
//        }

    }

    fun showRateDialog(isFinish: Boolean) {
        val dialog = RateAppDialog(this)
        dialog.setCallback(object : RateCallback {
            override fun onMaybeLater() {
                if (isFinish) {
                    SharePreferenceUtils.increaseCountRate(this@MainActivity)
                    finishAffinity()
                }
            }

            override fun onSubmit(review: String?) {
                Toast.makeText(this@MainActivity, R.string.thank_you, Toast.LENGTH_SHORT).show()
                SharePreferenceUtils.setRated(this@MainActivity)
                if (isFinish) {
                    finishAffinity()
                }
            }

            override fun onRate() {
                com.MTG.AppLock.util.sharerate.CommonUtils.getInstance().rateApp(this@MainActivity)
                SharePreferenceUtils.setRated(this@MainActivity)
            }
        })
        dialog.show()
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

    private fun buildAlertMessageCloseApp() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_warning_close_app, null, false)
        builder.setView(view)
        mExitDialog?.dismiss()
        mExitDialog = builder.create()
        view.tvNo.setOnClickListener {
            viewModel.setLockSettingApp(AppLockerPreferences.LOCK_APP_ALWAYS)
            mExitDialog?.dismiss()
        }
        view.tvYes.setOnClickListener {
            viewModel.setLockSettingApp(AppLockerPreferences.LOCK_APP_ALWAYS)
            mExitDialog?.dismiss()
            onLauncher()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Const.REQUEST_CODE_SEARCH_APPLICATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    ApplicationListUtils.instance?.search(this, "")
                }
            }
            Const.REQUEST_CODE_CREATE_SUPPER_PASSWORD -> {
                if (resultCode == Activity.RESULT_OK) {
                    // todo
                } else {
                    finishAffinity()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {

                if (viewPager.currentItem == MainPagerAdapter.INDEX_PERSONAL) {
                    (viewPager.adapter?.instantiateItem(viewPager, MainPagerAdapter.INDEX_PERSONAL) as PersonalFragment).setupNumberTypeIfChange()
                } else {
                    showDialogOverlapOrUsageDataAccessPermission()
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdate()
                }
            }

    }

    override fun onRequestWritePermissions() {
        if (mHasShowWritePermission.not()) {
            showStoragePermissionDialog()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            mStoragePermissionDialog?.dismiss()
            mIntruderPhotoDialog?.dismiss()
            mOverlapPermissionDialog?.dismiss()
            mUsageDataAccessPermissionDialog?.dismiss()
            mExitDialog?.dismiss()
            mAdView?.destroy()
            mRatingDialog?.dismiss()
            mProgressBar?.dismiss()
            AlarmsUtils.finishAllAlarms(this, viewModel.getConfigurationActiveList())
            ApplicationListUtils.instance?.destroy()
            unregisterRemoveReceiver()
        }
    }

    override fun onAppSelected(): Boolean {
        return showDialogOverlapOrUsageDataAccessPermission()
    }

    override fun onChangeStatusGroupLock() {
        // update alarms
        AlarmsUtils.startAllAlarms(this, viewModel.getConfigurationActiveList())
        //
        showProgressBar()
        ApplicationListUtils.instance?.reload(this)
    }

    override fun showAnimation() {
        showProgressBar()
    }

    override fun hideAnimation() {
        hideProgressBar()
    }

    private fun showProgressBar() {
        mProgressBar?.dismiss()
        mProgressBar = ProgressDialog.progressDialogV2(this)
        mProgressBar?.show()
    }

    private fun hideProgressBar() {
        mProgressBar?.dismiss()
    }

    fun setOnUpdateConfigurationListener(onUpdateConfigurationListener: OnUpdateConfigurationListener?) {
        mOnUpdateConfigurationListener = onUpdateConfigurationListener
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    override fun onStateUpdate(state: InstallState) {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate()
        }
    }

    private fun popupSnackbarForCompleteUpdate() {
        Snackbar.make(
            findViewById(R.id.activity_main_layout),
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { appUpdateManager.completeUpdate() }
            setActionTextColor(ContextCompat.getColor(context,R.color.colorGreen))
            show()
        }
    }
}
