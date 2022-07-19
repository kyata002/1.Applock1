package com.MTG.AppLock.ui.activity.password.newpattern

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import com.MTG.AppLock.R
import com.MTG.AppLock.model.ThemeModel
import com.MTG.AppLock.ui.base.BaseActivity
import com.MTG.AppLock.ui.view.ProgressDialog
import com.MTG.AppLock.util.CommonUtils
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.ThemeUtils
import com.MTG.AppLock.util.extensions.dialogLayout
import com.MTG.AppLock.util.extensions.gone
import com.MTG.AppLock.util.extensions.invisible
import com.MTG.AppLock.util.extensions.visible
import com.MTG.AppLock.util.file.EncryptionFileManager
import com.MTG.AppLock.util.network.NetworkUtils
import com.MTG.patternlockview.PatternLockView
import com.bumptech.glide.Glide
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.MTG.pinlock.PinLockConfiguration
import com.MTG.pinlock.PinLockViewV2
import com.MTG.pinlock.control.OnLockScreenCodeCreateListener
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.activity_create_new_pattern.imageBackground
import kotlinx.android.synthetic.main.activity_create_new_pattern.imageCreate1
import kotlinx.android.synthetic.main.activity_create_new_pattern.imageCreate2
import kotlinx.android.synthetic.main.activity_create_new_pattern.patternLockView
import kotlinx.android.synthetic.main.activity_create_new_pattern.pinLock
import kotlinx.android.synthetic.main.activity_create_new_pattern.tvMessage
import kotlinx.android.synthetic.main.activity_create_new_pattern.tvReset
import kotlinx.android.synthetic.main.activity_create_new_pattern.tvTitle
import kotlinx.android.synthetic.main.dialog_download_theme.view.*

class CreateNewPatternActivity : BaseActivity<CreateNewPatternViewModel>() {
    private lateinit var mColorAnimator: ObjectAnimator
    private var mThemeModel: ThemeModel? = null
    private var mConfirmDialog: AlertDialog? = null
    private var mProgressBar: Dialog? = null
    private var mIsShowDownload: Boolean = false
    override fun getViewModel(): Class<CreateNewPatternViewModel> = CreateNewPatternViewModel::class.java
    override fun getLayoutId(): Int {
        return R.layout.activity_create_new_pattern
    }

    override fun initViews() {
        mColorAnimator = ObjectAnimator.ofInt(tvTitle, "textColor", ContextCompat.getColor(this, R.color.color_error), ContextCompat.getColor(this, R.color.color_error), Color.WHITE)
        mColorAnimator.setEvaluator(ArgbEvaluator())
        mColorAnimator.duration = 1000
        mIsShowDownload = intent.getBooleanExtra(Const.EXTRA_SHOW_DIALOG, false)
        imageCreate1.isSelected = true
        imageCreate2.isSelected = false
        viewModel.getPatternEventLiveData().observe(this, { viewState ->
            when (viewState.patternEvent) {
                CreateNewPatternViewModel.PatternEvent.INITIALIZE -> {
                    imageCreate1.isSelected = true
                    imageCreate2.isSelected = false
                    tvReset.invisible()
                }
                CreateNewPatternViewModel.PatternEvent.ERROR_SHORT_FIRST -> {
                    imageCreate1.isSelected = true
                    imageCreate2.isSelected = false
                    tvReset.invisible()
                    mColorAnimator.start()
                }
                CreateNewPatternViewModel.PatternEvent.ERROR_SHORT_SECOND -> {
                    imageCreate1.isSelected = false
                    imageCreate2.isSelected = true
                    tvReset.visible()
                    mColorAnimator.start()
                }
                CreateNewPatternViewModel.PatternEvent.ERROR -> {
                    mColorAnimator.start()
                }
                else -> {
                    imageCreate1.isSelected = false
                    imageCreate2.isSelected = true
                    tvReset.visible()
                }
            }
            if (patternLockView.isVisible) {
                if (!mIsShowDownload) {
                    tvTitle.text = viewState.getTitleText(this)
                    tvMessage.text = viewState.getMessageText(this)
                }
            }
            if (viewState.isCreatedNewPattern()) {
                onPatternCreateCompleted()
            }
        })
        mThemeModel = intent.getSerializableExtra(Const.EXTRA_DATA) as ThemeModel?
        mThemeModel?.let { theme ->
            setupView(theme.typeTheme != ThemeUtils.TYPE_PIN)
            if (theme.backgroundResId != 0) {
                imageBackground.setImageResource(theme.backgroundResId)
            } else {
                Glide.with(this).load(theme.backgroundUrl).into(imageBackground)
            }
            if (theme.selectedResId != 0) {
                patternLockView.setBitmapSelected(AppCompatResources.getDrawable(this, theme.selectedResId)?.toBitmap())
            } else {
                viewModel.loadSelectedBitmap(this, if (TextUtils.isEmpty(theme.selectedDownload)) theme.selectedUrl else theme.selectedDownload)
            }
            if (theme.unselectedResId != 0) {
                patternLockView.setBitmapUnSelected(AppCompatResources.getDrawable(this, theme.unselectedResId)?.toBitmap())
            } else {
                viewModel.loadUnSelectedBitmap(this, if (TextUtils.isEmpty(theme.selectedDownload)) theme.unSelectedUrl else theme.unselectedDownload)
            }
            //
            val bitmapSelectedList = mutableListOf<Bitmap>()
            if (theme.selectedResIdList.isNotEmpty()) {
                theme.selectedResIdList.forEach {
                    AppCompatResources.getDrawable(this, it)?.toBitmap()?.let { it1 -> bitmapSelectedList.add(it1) }
                }
            }
            patternLockView.setBitmapSelectedList(bitmapSelectedList)
            val bitmapUnselectedList = mutableListOf<Bitmap>()
            if (theme.unselectedResIdList.isNotEmpty()) {
                theme.unselectedResIdList.forEach {
                    AppCompatResources.getDrawable(this, it)?.toBitmap()?.let { it1 -> bitmapUnselectedList.add(it1) }
                }
            }
            patternLockView.setBitmapUnSelectedList(bitmapUnselectedList)
            if (theme.lineColorResId != 0) {
                patternLockView.normalStateColor = ContextCompat.getColor(this, theme.lineColorResId)
                patternLockView.correctStateColor = ContextCompat.getColor(this, theme.lineColorResId)
            } else {
                try {
                    if (!TextUtils.isEmpty(theme.lineColor) && theme.lineColor.startsWith("#")) {
                        patternLockView.normalStateColor = Color.parseColor(theme.lineColor)
                        patternLockView.correctStateColor = Color.parseColor(theme.lineColor)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (theme.textMsgColorResId != 0) {
                val color = ContextCompat.getColor(this, theme.textMsgColorResId)
                tvReset.setTextColor(color)
            } else {
                try {
                    val color = Color.parseColor(theme.textMsgColor)
                    tvReset.setTextColor(color)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val builder = PinLockConfiguration.Builder()
                    .setDeletePadding(theme.isDeletePadding)
                    .setNumberPadding(theme.isNumberPadding)
                    .setDeletePadding(theme.deletePadding)
                    .setResPinList(theme.buttonResList)
                    .setResColorButton(theme.colorButtonResId)
                    .setPinUrlList(theme.buttonUrlList)
                    .setResSelectorCheckbox(theme.selectorCheckboxColorResId)
                    .setColorCheckbox(theme.selectorCheckboxColor)
                    .setResColorMessage(theme.textMsgColorResId)
                    .setColorMessage(theme.textMsgColor)
                    .setResBackgroundId(theme.backgroundResId)
                    .setBackgroundUrl(theme.backgroundUrl)
                    .setNextTitle(getString(R.string.text_next))
            pinLock.applyConfiguration(builder.build())
            pinLock.showFingerprint(false)
            pinLock.showImageIcon(false)
            pinLock.setTitle(getString(R.string.msg_pin_lock_set_your_pin_code))
            pinLock.setMessage(getString(R.string.msg_pin_lock_enter_numbers))
            pinLock.setOnPinLockViewListenerV2(object : PinLockViewV2.OnPinLockViewListenerV2 {
                override fun onNextPinLock(visibility: Int) {
                }

                override fun onReplayVisibility(status: Int) {

                }

                override fun onStatusPinLock(status: Int) {
                    if (!pinLock.isVisible) return
                    when (status) {
                        PinLockConfiguration.Config.TYPE_START -> {
                            pinLock.setTitle(getString(R.string.msg_pin_lock_set_your_pin_code))
                            pinLock.setMessage(getString(R.string.msg_pin_lock_enter_numbers))
                        }
                        PinLockConfiguration.Config.TYPE_CONFIRM -> {
                            pinLock.setTitle(getString(R.string.msg_pin_lock_confirm_your_pin))
                            pinLock.setMessage(getString(R.string.msg_pin_lock_confirm))
                        }
                        PinLockConfiguration.Config.TYPE_ERROR -> {
                            pinLock.setTitle(getString(R.string.msg_pin_lock_confirm_your_pin))
                            pinLock.setMessage(getString(R.string.msg_pin_lock_confirm))
                        }
                    }
                }
            })
        } ?: setupView(true)
        viewModel.getSelectedBitmapLiveData().observe(this, {
            if (it == null) {
                return@observe
            }
            patternLockView.setBitmapSelected(it)
        })
        viewModel.getUnSelectedBitmapLiveData().observe(this, {
            if (it == null) {
                return@observe
            }
            patternLockView.setBitmapUnSelected(it)
        })
        if (mIsShowDownload) {
            // online
            if (isStoragePermissionGranted()) {
                if (!CommonUtils.isCanSaveFile(this, R.string.msg_not_enough_memory_download)) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return
                }
                download()
            }
        }
        //
        viewModel.getDownloadBackgroundBitmapLiveData().observe(this, {
            if (it == null) {
                finishScreen()
                return@observe
            }
            mThemeModel?.let { theme ->
                EncryptionFileManager.saveTheme(this, theme.typeTheme, theme.id, it, "background_" + theme.id, object : EncryptionFileManager.OnSaveFileListener {
                    override fun success(path: String) {
                        theme.backgroundDownload = path
                        when (theme.typeTheme) {
                            ThemeUtils.TYPE_PATTERN -> {
                                if (!TextUtils.isEmpty(theme.selectedDownload) && !TextUtils.isEmpty(theme.unselectedDownload) && !TextUtils.isEmpty(theme.backgroundDownload) && !TextUtils.isEmpty(theme.thumbnailDownload)) {
                                    hideProgressBar()
                                    viewModel.updateTheme(theme)
                                    showConfirmDialog()
                                }
                            }
                            ThemeUtils.TYPE_PIN -> {
                                if (!TextUtils.isEmpty(theme.buttonDownload) && !TextUtils.isEmpty(theme.backgroundDownload) && !TextUtils.isEmpty(theme.thumbnailDownload)) {
                                    hideProgressBar()
                                    viewModel.updateTheme(theme)
                                    showConfirmDialog()
                                }
                            }
                            else -> {
                                // nothing
                            }
                        }
                    }

                    override fun failed() {
                        finishScreen()
                    }
                })
            } ?: finishScreen()
        })
        viewModel.getDownloadThumbnailBitmapLiveData().observe(this, {
            if (it == null) {
                finishScreen()
                return@observe
            }
            mThemeModel?.let { theme ->
                EncryptionFileManager.saveTheme(this, theme.typeTheme, theme.id, it, "thumbnail_" + theme.id, object : EncryptionFileManager.OnSaveFileListener {
                    override fun success(path: String) {
                        theme.thumbnailDownload = path
                        when (theme.typeTheme) {
                            ThemeUtils.TYPE_PATTERN -> {
                                if (!TextUtils.isEmpty(theme.selectedDownload) && !TextUtils.isEmpty(theme.unselectedDownload) && !TextUtils.isEmpty(theme.backgroundDownload) && !TextUtils.isEmpty(theme.thumbnailDownload)) {
                                    hideProgressBar()
                                    viewModel.updateTheme(theme)
                                    showConfirmDialog()
                                }
                            }
                            ThemeUtils.TYPE_PIN -> {
                                if (!TextUtils.isEmpty(theme.buttonDownload) && !TextUtils.isEmpty(theme.backgroundDownload) && !TextUtils.isEmpty(theme.thumbnailDownload)) {
                                    hideProgressBar()
                                    viewModel.updateTheme(theme)
                                    showConfirmDialog()
                                }
                            }
                            else -> {
                                // nothing
                            }
                        }
                    }

                    override fun failed() {
                        finishScreen()
                    }
                })
            } ?: finishScreen()
        })
        viewModel.getDownloadSelectedBitmapLiveData().observe(this, {
            if (it == null) {
                finishScreen()
                return@observe
            }
            mThemeModel?.let { theme ->
                EncryptionFileManager.saveTheme(this@CreateNewPatternActivity, theme.typeTheme, theme.id, it, "selected_" + theme.id, object : EncryptionFileManager.OnSaveFileListener {
                    override fun success(path: String) {
                        theme.selectedDownload = path
                        if (!TextUtils.isEmpty(theme.selectedDownload) && !TextUtils.isEmpty(theme.unselectedDownload) && !TextUtils.isEmpty(theme.backgroundDownload) && !TextUtils.isEmpty(theme.thumbnailDownload)) {
                            hideProgressBar()
                            viewModel.updateTheme(theme)
                            showConfirmDialog()
                        }
                    }

                    override fun failed() {
                        finishScreen()
                    }
                })
            } ?: finishScreen()
        })
        viewModel.getDownloadUnSelectedBitmapLiveData().observe(this, {
            if (it == null) {
                finishScreen()
                return@observe
            }
            mThemeModel?.let { theme ->
                EncryptionFileManager.saveTheme(this@CreateNewPatternActivity, theme.typeTheme, theme.id, it, "unselected_" + theme.id, object : EncryptionFileManager.OnSaveFileListener {
                    override fun success(path: String) {
                        theme.unselectedDownload = path
                        if (!TextUtils.isEmpty(theme.selectedDownload) && !TextUtils.isEmpty(theme.unselectedDownload) && !TextUtils.isEmpty(theme.backgroundDownload) && !TextUtils.isEmpty(theme.thumbnailDownload)) {
                            hideProgressBar()
                            viewModel.updateTheme(theme)
                            showConfirmDialog()
                        }
                    }

                    override fun failed() {
                        finishScreen()
                    }
                })
            } ?: finishScreen()
        })
        viewModel.getDownloadButtonBitmapListLiveData().observe(this, {
            if (it.isEmpty()) {
                finishScreen()
                return@observe
            }
            mThemeModel?.let { theme ->
                val buttonDownload = StringBuilder("")
                var success = 0
                it.forEachIndexed { index, bitmap ->
                    if (bitmap == null) {
                        finishScreen()
                        return@forEachIndexed
                    }
                    EncryptionFileManager.saveTheme(this, theme.typeTheme, theme.id, bitmap, "pin_" + index + "_" + theme.id, object : EncryptionFileManager.OnSaveFileListener {
                        override fun success(path: String) {
                            success++
                            buttonDownload.append(path).append(",")
                            if (success == it.size) {
                                theme.buttonDownload = buttonDownload.toString()
                                if (!TextUtils.isEmpty(theme.buttonDownload) && !TextUtils.isEmpty(theme.backgroundDownload) && !TextUtils.isEmpty(theme.thumbnailDownload)) {
                                    hideProgressBar()
                                    viewModel.updateTheme(theme)
                                    showConfirmDialog()
                                }
                            }
                        }

                        override fun failed() {
                            finishScreen()
                        }
                    })
                }
            } ?: finishScreen()
        })
    }

    private fun finishScreen() {
        runOnUiThread {
            hideProgressBar()
            Toasty.showToast(this@CreateNewPatternActivity, R.string.msg_change_theme_failed, Toasty.ERROR)
            finish()
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                requestWritePermissions()
                false
            }
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
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
            }
        }).setDeniedMessage(R.string.msg_denied_permission).setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).check()
    }

    private fun download() {
        showProgressBar()
        mThemeModel?.let { theme ->
            NetworkUtils.hasInternetAccessCheck(this@CreateNewPatternActivity, object : NetworkUtils.OnCallbackCheckNetwork {
                override fun hasInternetAccess() {
                    // download
                    when (theme.typeTheme) {
                        ThemeUtils.TYPE_PATTERN -> {
                            viewModel.getTheme(theme.backgroundUrl, theme.thumbnailUrl)?.let {
                                if (TextUtils.isEmpty(theme.backgroundDownload) && TextUtils.isEmpty(theme.thumbnailDownload)) {
                                    // download
                                    downloadPattern()
                                } else {
                                    // downloaded pattern
                                    hideProgressBar()
                                }
                            } ?: downloadPattern()
                        }
                        ThemeUtils.TYPE_PIN -> {
                            viewModel.getTheme(theme.backgroundUrl, theme.thumbnailUrl)?.let {
                                if (TextUtils.isEmpty(theme.backgroundDownload) && TextUtils.isEmpty(theme.thumbnailDownload)) {
                                    // download
                                    downloadPin()
                                } else {
                                    // downloaded pin
                                    runOnUiThread {
                                        hideProgressBar()
                                    }
                                }
                            } ?: downloadPin()
                        }
                        ThemeUtils.TYPE_WALLPAPER -> {
                            viewModel.getTheme(theme.backgroundUrl, theme.thumbnailUrl)?.let {
                                if (TextUtils.isEmpty(theme.backgroundDownload) && TextUtils.isEmpty(theme.thumbnailDownload)) {
                                    // download
                                    downloadWallpaper()
                                } else {
                                    // downloaded wallpaper
                                    runOnUiThread {
                                        hideProgressBar()
                                    }
                                }
                            } ?: downloadWallpaper()
                        }
                    }
                }

                private fun downloadPattern() {
                    if (theme.id == 0) {
                        viewModel.getThemeLastId() + 1
                    }
                    viewModel.downloadPattern(this@CreateNewPatternActivity, theme.backgroundUrl, theme.thumbnailUrl, theme.selectedUrl, theme.unSelectedUrl)
                }

                private fun downloadPin() {
                    if (theme.id == 0) {
                        viewModel.getThemeLastId() + 1
                    }
                    viewModel.downloadPin(this@CreateNewPatternActivity, theme.backgroundUrl, theme.thumbnailUrl, theme.buttonRes(false))
                }

                private fun downloadWallpaper() {
                    // nothing
                    hideProgressBar()
                }

                override fun errorInternetAccess() {
                    finishScreen()
                    runOnUiThread {
                        Toasty.showToast(this@CreateNewPatternActivity, R.string.check_network_connection, Toasty.ERROR)
                    }
                }
            })
        }
    }

    private fun showConfirmDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_download_theme, null, false)
        builder.setView(view)
        mConfirmDialog?.dismiss()
        mConfirmDialog = builder.create()
        mConfirmDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnOkSuccessDownloadTheme.setOnClickListener {
            mConfirmDialog?.dismiss()
        }
        mConfirmDialog?.show()
        dialogLayout(mConfirmDialog)
    }

    override fun initListener() {
        pinLock.setOnLockScreenCodeCreateListener(object : OnLockScreenCodeCreateListener {
            override fun onCodeCreated(encodedCode: String?) {
                encodedCode?.let {
                    viewModel.setPinLock(it)
                }
                onPatternCreateCompleted()
            }

            override fun onNewCodeValidationFailed() {
                //
                Toasty.showToast(this@CreateNewPatternActivity, R.string.msg_pin_lock_error, Toasty.ERROR)
            }
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
    }

    private fun setupView(isPattern: Boolean) {
        if (isPattern) {
            patternLockView.visible()
            pinLock.gone()
        } else {
            patternLockView.gone()
            pinLock.visible()
        }
    }

    private fun onPatternCreateCompleted() {
        mThemeModel?.let { viewModel.updateThemeDefault(it) }
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun showProgressBar() {
        mProgressBar?.dismiss()
        mProgressBar = ProgressDialog.progressDialog(this)
        mProgressBar?.show()
    }

    private fun hideProgressBar() {
        mProgressBar?.dismiss()
    }

    private fun reset() {
        imageCreate1.isSelected = true
        imageCreate2.isSelected = false
        viewModel.reset()
    }

    override fun onDestroy() {
        mConfirmDialog?.dismiss()
        mColorAnimator.cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (mIsShowDownload) {
            val intent = Intent()
            intent.putExtra(Const.EXTRA_DATA, mThemeModel)
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, CreateNewPatternActivity::class.java)
        }
    }
}