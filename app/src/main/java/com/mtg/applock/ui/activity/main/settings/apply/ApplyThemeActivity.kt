package com.mtg.applock.ui.activity.main.settings.apply

import android.Manifest
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
import com.mtg.applock.R
import com.mtg.applock.model.ThemeModel
import com.mtg.applock.ui.activity.password.newpattern.CreateNewPatternActivity
import com.mtg.applock.ui.base.BaseActivity
import com.mtg.applock.ui.view.ProgressDialog
import com.mtg.applock.util.CommonUtils
import com.mtg.applock.util.Const
import com.mtg.applock.util.ThemeUtils
import com.mtg.applock.util.extensions.dialogLayout
import com.mtg.applock.util.extensions.gone
import com.mtg.applock.util.extensions.visible
import com.mtg.applock.util.file.EncryptionFileManager
import com.mtg.applock.util.network.NetworkUtils
import com.mtg.patternlockview.PatternLockView
import com.bumptech.glide.Glide
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.mtg.pinlock.PinLockConfiguration
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.activity_apply_theme.*
import kotlinx.android.synthetic.main.dialog_apply_theme.view.*

class ApplyThemeActivity : BaseActivity<ApplyThemeViewModel>() {
    private lateinit var mThemeModel: ThemeModel
    private var mConfirmDialog: AlertDialog? = null
    private var mProgressBar: Dialog? = null
    private var hasChange: Boolean = false
    override fun getViewModel(): Class<ApplyThemeViewModel> {
        return ApplyThemeViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_apply_theme
    }

    override fun initViews() {
        mThemeModel = intent.getSerializableExtra(Const.EXTRA_DATA) as ThemeModel
        btnApply.setText(R.string.txt_apply)
        if (mThemeModel.backgroundResId != 0) {
            imageWallpaper.setImageResource(mThemeModel.backgroundResId)
        } else if (!TextUtils.isEmpty(mThemeModel.backgroundDownload)) {
            Glide.with(this).load(mThemeModel.backgroundDownload).into(imageWallpaper)
        } else {
            Glide.with(this).load(mThemeModel.backgroundUrl).into(imageWallpaper)
            if (mThemeModel.typeTheme != ThemeUtils.TYPE_WALLPAPER) {
                if (viewModel.isPatternLock() && mThemeModel.typeTheme == ThemeUtils.TYPE_PATTERN) {
                    // nothing
                } else if (!viewModel.isPatternLock() && mThemeModel.typeTheme == ThemeUtils.TYPE_PIN) {
                    // nothing
                } else {
                    btnApply.setText(R.string.title_download)
                }
            }
        }
        if (mThemeModel.typeTheme == ThemeUtils.TYPE_WALLPAPER) {

            imageWallpaper.visible()
            clTheme.gone()
        } else {

            clTheme.visible()
            imageWallpaper.gone()
            if (mThemeModel.typeTheme == ThemeUtils.TYPE_PIN) {
                clPatternView.gone()
                pinLock.visible()
            } else {
                clPatternView.visible()
                pinLock.gone()
            }
            if (mThemeModel.selectedResId != 0) {
                patternLockView.setBitmapSelected(AppCompatResources.getDrawable(this, mThemeModel.selectedResId)?.toBitmap())
            } else {
                viewModel.loadSelectedBitmap(this, if (TextUtils.isEmpty(mThemeModel.selectedDownload)) mThemeModel.selectedUrl else mThemeModel.selectedDownload)
            }
            if (mThemeModel.unselectedResId != 0) {
                patternLockView.setBitmapUnSelected(AppCompatResources.getDrawable(this, mThemeModel.unselectedResId)?.toBitmap())
            } else {
                viewModel.loadUnSelectedBitmap(this, if (TextUtils.isEmpty(mThemeModel.unselectedDownload)) mThemeModel.unSelectedUrl else mThemeModel.unselectedDownload)
            }
            //
            val bitmapSelectedList = mutableListOf<Bitmap>()
            if (mThemeModel.selectedResIdList.isNotEmpty()) {
                mThemeModel.selectedResIdList.forEach {
                    AppCompatResources.getDrawable(this, it)?.toBitmap()?.let { it1 -> bitmapSelectedList.add(it1) }
                }
            }
            patternLockView.setBitmapSelectedList(bitmapSelectedList)
            val bitmapUnselectedList = mutableListOf<Bitmap>()
            if (mThemeModel.unselectedResIdList.isNotEmpty()) {
                mThemeModel.unselectedResIdList.forEach {
                    AppCompatResources.getDrawable(this, it)?.toBitmap()?.let { it1 -> bitmapUnselectedList.add(it1) }
                }
            }
            patternLockView.setBitmapUnSelectedList(bitmapUnselectedList)
            //
            if (mThemeModel.lineColorResId != 0) {
                patternLockView.normalStateColor = ContextCompat.getColor(this, mThemeModel.lineColorResId)
                patternLockView.correctStateColor = ContextCompat.getColor(this, mThemeModel.lineColorResId)
            } else {
                try {
                    if (!TextUtils.isEmpty(mThemeModel.lineColor) && mThemeModel.lineColor.startsWith("#")) {
                        patternLockView.normalStateColor = Color.parseColor(mThemeModel.lineColor)
                        patternLockView.correctStateColor = Color.parseColor(mThemeModel.lineColor)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            mThemeModel.themeConfig?.let {
                val pattern = mutableListOf<PatternLockView.Dot>()
                for (point in it.pointSelectedList) {
                    pattern.add(convertPointToDot(point))
                }
                patternLockView.setPattern(PatternLockView.PatternViewMode.CORRECT, pattern)
                patternLockView.isEnabled = false
            }
            if (mThemeModel.backgroundResId != 0) {
                imageBackground.setImageResource(mThemeModel.backgroundResId)
            } else if (!TextUtils.isEmpty(mThemeModel.backgroundDownload)) {
                Glide.with(this).load(mThemeModel.backgroundDownload).into(imageBackground)
            } else {
                Glide.with(this).load(mThemeModel.backgroundUrl).into(imageBackground)
            }
            val builder = PinLockConfiguration.Builder()
                    .setMode(PinLockConfiguration.Config.MODE_AUTH)
                    .setDeletePadding(mThemeModel.isDeletePadding)
                    .setNumberPadding(mThemeModel.isNumberPadding)
                    .setDeletePadding(mThemeModel.deletePadding)
                    .setResPinList(mThemeModel.buttonResList)
                    .setPinUrlList(if (mThemeModel.buttonDownloadList().size == 0) mThemeModel.buttonUrlList else mThemeModel.buttonDownloadList())
                    .setResColorButton(mThemeModel.colorButtonResId)
                    .setResSelectorCheckbox(mThemeModel.selectorCheckboxColorResId)
                    .setColorCheckbox(mThemeModel.selectorCheckboxColor)
                    .setResColorMessage(mThemeModel.textMsgColorResId)
                    .setColorMessage(mThemeModel.textMsgColor)
                    .setCodeLength(6)
                    .setResBackgroundId(mThemeModel.backgroundResId)
                    .setBackgroundUrl(mThemeModel.backgroundUrl)
                    .setThemeConfig(mThemeModel.themeConfig).setNextTitle(getString(R.string.text_create))
            pinLock.applyConfiguration(builder.build())
            pinLock.showFingerprint(false)
            pinLock.showImageIcon(false)
        }
        btnApply.setOnClickListener {
            apply()
        }


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
        buildConfirmDialog()
        //
        viewModel.getDownloadBackgroundBitmapLiveData().observe(this, {
            if (it == null) {
                finishScreen()
                return@observe
            }
            EncryptionFileManager.saveTheme(this, mThemeModel.typeTheme, mThemeModel.id, it, "background_" + mThemeModel.id, object : EncryptionFileManager.OnSaveFileListener {
                override fun success(path: String) {
                    mThemeModel.backgroundDownload = path
                    when (mThemeModel.typeTheme) {
                        ThemeUtils.TYPE_PATTERN -> {
                            if (!TextUtils.isEmpty(mThemeModel.selectedDownload) && !TextUtils.isEmpty(mThemeModel.unselectedDownload) && !TextUtils.isEmpty(mThemeModel.backgroundDownload) && !TextUtils.isEmpty(mThemeModel.thumbnailDownload)) {
                                hideProgressBar()
                                viewModel.updateTheme(mThemeModel)
                                viewModel.updateThemeDefault(mThemeModel)
                                Toasty.showToast(this@ApplyThemeActivity, R.string.msg_theme_has_been_changed, Toasty.SUCCESS)
                            }
                        }
                        ThemeUtils.TYPE_PIN -> {
                            if (!TextUtils.isEmpty(mThemeModel.buttonDownload) && !TextUtils.isEmpty(mThemeModel.backgroundDownload) && !TextUtils.isEmpty(mThemeModel.thumbnailDownload)) {
                                hideProgressBar()
                                viewModel.updateTheme(mThemeModel)
                                viewModel.updateThemeDefault(mThemeModel)
                                Toasty.showToast(this@ApplyThemeActivity, R.string.msg_theme_has_been_changed, Toasty.SUCCESS)
                            }
                        }
                        ThemeUtils.TYPE_WALLPAPER -> {
                            if (!TextUtils.isEmpty(mThemeModel.backgroundDownload) && !TextUtils.isEmpty(mThemeModel.thumbnailDownload)) {
                                hideProgressBar()
                                viewModel.updateTheme(mThemeModel)
                                viewModel.updateThemeDefault(0, mThemeModel.backgroundUrl, mThemeModel.backgroundDownload)
                                Toasty.showToast(this@ApplyThemeActivity, R.string.msg_change_lock_wallpaper_succeed, Toasty.SUCCESS)
                            }
                        }
                        else -> {
                            // nothing
                        }
                    }
                }

                override fun failed() {
                    finishScreen()
                    runOnUiThread {
                        if (mThemeModel.typeTheme == ThemeUtils.TYPE_WALLPAPER) {
                            Toasty.showToast(this@ApplyThemeActivity, R.string.msg_change_lock_wallpaper_failed, Toasty.ERROR)
                        }
                    }
                }
            })
        })
        viewModel.getDownloadThumbnailBitmapLiveData().observe(this, {
            if (it == null) {
                finishScreen()
                return@observe
            }
            mThemeModel.let { theme ->
                EncryptionFileManager.saveTheme(this, theme.typeTheme, theme.id, it, "thumbnail_" + theme.id, object : EncryptionFileManager.OnSaveFileListener {
                    override fun success(path: String) {
                        theme.thumbnailDownload = path
                        when (theme.typeTheme) {
                            ThemeUtils.TYPE_PATTERN -> {
                                if (!TextUtils.isEmpty(theme.selectedDownload) && !TextUtils.isEmpty(theme.unselectedDownload) && !TextUtils.isEmpty(theme.backgroundDownload) && !TextUtils.isEmpty(theme.thumbnailDownload)) {
                                    hideProgressBar()
                                    viewModel.updateTheme(mThemeModel)
                                    viewModel.updateThemeDefault(mThemeModel)
                                    Toasty.showToast(this@ApplyThemeActivity, R.string.msg_theme_has_been_changed, Toasty.SUCCESS)
                                }
                            }
                            ThemeUtils.TYPE_PIN -> {
                                if (!TextUtils.isEmpty(theme.buttonDownload) && !TextUtils.isEmpty(theme.backgroundDownload) && !TextUtils.isEmpty(theme.thumbnailDownload)) {
                                    hideProgressBar()
                                    viewModel.updateTheme(mThemeModel)
                                    viewModel.updateThemeDefault(mThemeModel)
                                    Toasty.showToast(this@ApplyThemeActivity, R.string.msg_theme_has_been_changed, Toasty.SUCCESS)
                                }
                            }
                            ThemeUtils.TYPE_WALLPAPER -> {
                                if (!TextUtils.isEmpty(mThemeModel.backgroundDownload) && !TextUtils.isEmpty(mThemeModel.thumbnailDownload)) {
                                    hideProgressBar()
                                    viewModel.updateTheme(mThemeModel)
                                    viewModel.updateThemeDefault(0, mThemeModel.backgroundUrl, mThemeModel.backgroundDownload)
                                    Toasty.showToast(this@ApplyThemeActivity, R.string.msg_change_lock_wallpaper_succeed, Toasty.SUCCESS)
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
            }
        })
        viewModel.getDownloadSelectedBitmapLiveData().observe(this, {
            if (it == null) {
                finishScreen()
                return@observe
            }
            mThemeModel.let { theme ->
                EncryptionFileManager.saveTheme(this@ApplyThemeActivity, theme.typeTheme, theme.id, it, "selected_" + theme.id, object : EncryptionFileManager.OnSaveFileListener {
                    override fun success(path: String) {
                        theme.selectedDownload = path
                        if (!TextUtils.isEmpty(theme.selectedDownload) && !TextUtils.isEmpty(theme.unselectedDownload) && !TextUtils.isEmpty(theme.backgroundDownload) && !TextUtils.isEmpty(theme.thumbnailDownload)) {
                            hideProgressBar()
                            viewModel.updateTheme(mThemeModel)
                            viewModel.updateThemeDefault(mThemeModel)
                            Toasty.showToast(this@ApplyThemeActivity, R.string.msg_theme_has_been_changed, Toasty.SUCCESS)
                        }
                    }

                    override fun failed() {
                        finishScreen()
                    }
                })
            }
        })
        viewModel.getDownloadUnSelectedBitmapLiveData().observe(this, {
            if (it == null) {
                finishScreen()
                return@observe
            }
            mThemeModel.let { theme ->
                EncryptionFileManager.saveTheme(this@ApplyThemeActivity, theme.typeTheme, theme.id, it, "unselected_" + theme.id, object : EncryptionFileManager.OnSaveFileListener {
                    override fun success(path: String) {
                        theme.unselectedDownload = path
                        if (!TextUtils.isEmpty(theme.selectedDownload) && !TextUtils.isEmpty(theme.unselectedDownload) && !TextUtils.isEmpty(theme.backgroundDownload) && !TextUtils.isEmpty(theme.thumbnailDownload)) {
                            hideProgressBar()
                            viewModel.updateTheme(mThemeModel)
                            viewModel.updateThemeDefault(mThemeModel)
                            Toasty.showToast(this@ApplyThemeActivity, R.string.msg_theme_has_been_changed, Toasty.SUCCESS)
                        }
                    }

                    override fun failed() {
                        finishScreen()
                    }
                })
            }
        })
        viewModel.getDownloadButtonBitmapListLiveData().observe(this, {
            if (it.isEmpty()) {
                finishScreen()
                return@observe
            }
            mThemeModel.let { theme ->
                val buttonDownload = StringBuilder("")
                var success = 0
                it.forEachIndexed { index, bitmap ->
                    if (bitmap == null) {
                        finishScreen()
                        return@observe
                    }
                    EncryptionFileManager.saveTheme(this, theme.typeTheme, theme.id, bitmap, "pin_" + index + "_" + theme.id, object : EncryptionFileManager.OnSaveFileListener {
                        override fun success(path: String) {
                            success++
                            buttonDownload.append(path).append(",")
                            if (success == it.size) {
                                theme.buttonDownload = buttonDownload.toString()
                                if (!TextUtils.isEmpty(theme.buttonDownload) && !TextUtils.isEmpty(theme.backgroundDownload) && !TextUtils.isEmpty(theme.thumbnailDownload)) {
                                    hideProgressBar()
                                    viewModel.updateTheme(mThemeModel)
                                    viewModel.updateThemeDefault(mThemeModel)
                                    Toasty.showToast(this@ApplyThemeActivity, R.string.msg_theme_has_been_changed, Toasty.SUCCESS)
                                }
                            }
                        }

                        override fun failed() {
                            finishScreen()
                        }
                    })
                }
            }
        })
    }

    private fun finishScreen() {
        runOnUiThread {
            hideProgressBar()
            Toasty.showToast(this@ApplyThemeActivity, R.string.msg_change_theme_failed, Toasty.ERROR)
            finish()
        }
    }

    private fun showProgressBar() {
        mProgressBar?.dismiss()
        mProgressBar = ProgressDialog.progressDialog(this)
        mProgressBar?.show()
    }

    private fun hideProgressBar() {
        mProgressBar?.dismiss()
    }

    private fun convertPointToDot(point: Int): PatternLockView.Dot {
        val pointConvert = point - 1
        val row = pointConvert / 3
        val col = pointConvert % 3
        return PatternLockView.Dot.of(row, col)
    }

    private fun apply() {
        if (mThemeModel.backgroundResId != 0 || !TextUtils.isEmpty(mThemeModel.backgroundDownload)) {
            // default offline
            when (mThemeModel.typeTheme) {
                ThemeUtils.TYPE_WALLPAPER -> {
                    if (mThemeModel.backgroundResId != 0) {
                        viewModel.updateThemeDefault(mThemeModel.backgroundResId, "", "")
                    } else {
                        viewModel.updateThemeDefault(0, "", mThemeModel.backgroundDownload)
                    }
                    Toasty.showToast(this, R.string.msg_change_lock_wallpaper_succeed, Toasty.SUCCESS)
                }
                else -> {
                    if (viewModel.isPatternLock() && mThemeModel.typeTheme == ThemeUtils.TYPE_PATTERN) {
                        if (mThemeModel.backgroundResId != 0) {
                            mThemeModel.backgroundDownload = ""
                            mThemeModel.backgroundUrl = ""
                        }
                        viewModel.updateThemeDefault(mThemeModel)
                        Toasty.showToast(this, R.string.msg_theme_has_been_changed, Toasty.SUCCESS)
                    } else if (!viewModel.isPatternLock() && mThemeModel.typeTheme == ThemeUtils.TYPE_PIN) {
                        if (mThemeModel.backgroundResId != 0) {
                            mThemeModel.backgroundDownload = ""
                            mThemeModel.backgroundUrl = ""
                        }
                        viewModel.updateThemeDefault(mThemeModel)
                        Toasty.showToast(this, R.string.msg_theme_has_been_changed, Toasty.SUCCESS)
                    } else {
                        val intent = CreateNewPatternActivity.newIntent(this)
                        intent.putExtra(Const.EXTRA_DATA, mThemeModel)
                        startActivityForResult(intent, Const.REQUEST_CODE_CHANGE_THEME)
                        return
                    }
                }
            }
        } else {
            // online
            if (isStoragePermissionGranted()) {
                mConfirmDialog?.show()
                dialogLayout(mConfirmDialog)
            }
        }
    }

    private fun checkTheme() {
        when (mThemeModel.typeTheme) {
            ThemeUtils.TYPE_WALLPAPER -> {
                download()
            }
            else -> {
                if (viewModel.isPatternLock() && mThemeModel.typeTheme == ThemeUtils.TYPE_PATTERN) {
                    download()
                } else if (!viewModel.isPatternLock() && mThemeModel.typeTheme == ThemeUtils.TYPE_PIN) {
                    download()
                } else {
                    val intent = CreateNewPatternActivity.newIntent(this)
                    intent.putExtra(Const.EXTRA_DATA, mThemeModel)
                    intent.putExtra(Const.EXTRA_SHOW_DIALOG, true)
                    startActivityForResult(intent, Const.REQUEST_CODE_CHANGE_THEME)
                }
            }
        }
    }

    private fun download() {
        if (!CommonUtils.isCanSaveFile(this, R.string.msg_not_enough_memory_download)) {
            return
        }
        showProgressBar()
        NetworkUtils.hasInternetAccessCheck(this@ApplyThemeActivity, object : NetworkUtils.OnCallbackCheckNetwork {
            override fun hasInternetAccess() {
                // download
                when (mThemeModel.typeTheme) {
                    ThemeUtils.TYPE_PATTERN -> {
                        viewModel.getTheme(mThemeModel.backgroundUrl, mThemeModel.thumbnailUrl)?.let {
                            if (TextUtils.isEmpty(it.backgroundDownload) && TextUtils.isEmpty(it.thumbnailDownload)) {
                                // download
                                downloadPattern()
                            } else {
                                // downloaded pattern
                                runOnUiThread {
                                    hideProgressBar()
                                    viewModel.updateThemeDefault(mThemeModel)
                                    Toasty.showToast(this@ApplyThemeActivity, R.string.msg_theme_has_been_changed, Toasty.SUCCESS)
                                }
                            }
                        } ?: downloadPattern()
                    }
                    ThemeUtils.TYPE_PIN -> {
                        viewModel.getTheme(mThemeModel.backgroundUrl, mThemeModel.thumbnailUrl)?.let {
                            if (TextUtils.isEmpty(it.backgroundDownload) && TextUtils.isEmpty(it.thumbnailDownload)) {
                                // download
                                downloadPin()
                            } else {
                                // downloaded pin
                                runOnUiThread {
                                    hideProgressBar()
                                    viewModel.updateThemeDefault(mThemeModel)
                                    Toasty.showToast(this@ApplyThemeActivity, R.string.msg_theme_has_been_changed, Toasty.SUCCESS)
                                }
                            }
                        } ?: downloadPin()
                    }
                    ThemeUtils.TYPE_WALLPAPER -> {
                        viewModel.getTheme(mThemeModel.backgroundUrl, mThemeModel.thumbnailUrl)?.let {
                            if (TextUtils.isEmpty(it.backgroundDownload) && TextUtils.isEmpty(it.thumbnailDownload)) {
                                // download
                                downloadWallpaper()
                            } else {
                                // downloaded wallpaper
                                runOnUiThread {
                                    hideProgressBar()
                                    viewModel.updateThemeDefault(0, mThemeModel.backgroundUrl, mThemeModel.backgroundDownload)
                                    Toasty.showToast(this@ApplyThemeActivity, R.string.msg_change_lock_wallpaper_succeed, Toasty.SUCCESS)
                                }
                            }
                        } ?: downloadWallpaper()
                    }
                }
            }

            private fun downloadPattern() {
                if (mThemeModel.id == 0) {
                    viewModel.getThemeLastId() + 1
                }
                viewModel.downloadPattern(this@ApplyThemeActivity, mThemeModel.backgroundUrl, mThemeModel.thumbnailUrl, mThemeModel.selectedUrl, mThemeModel.unSelectedUrl)
            }

            private fun downloadPin() {
                if (mThemeModel.id == 0) {
                    viewModel.getThemeLastId() + 1
                }
                viewModel.downloadPin(this@ApplyThemeActivity, mThemeModel.backgroundUrl, mThemeModel.thumbnailUrl, mThemeModel.buttonRes(false))
            }

            private fun downloadWallpaper() {
                if (mThemeModel.id == 0) {
                    viewModel.getThemeLastId() + 1
                }
                viewModel.downloadWallpaper(this@ApplyThemeActivity, mThemeModel.backgroundUrl, mThemeModel.thumbnailUrl)
            }

            override fun errorInternetAccess() {
                hideProgressBar()
                runOnUiThread {
                    Toasty.showToast(this@ApplyThemeActivity, R.string.check_network_connection, Toasty.ERROR)
                }
            }
        })
    }

    private fun buildConfirmDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_apply_theme, null, false)
        if (mThemeModel.typeTheme == ThemeUtils.TYPE_WALLPAPER) {
            view.tvMessageApplyTheme.text = getString(R.string.msg_would_you_like_to_apply_this_background)
        } else {
            if (viewModel.isPatternLock() && mThemeModel.typeTheme == ThemeUtils.TYPE_PATTERN) {
                view.tvMessageApplyTheme.text = getString(R.string.msg_would_you_like_to_apply_this_theme)
            } else if (!viewModel.isPatternLock() && mThemeModel.typeTheme == ThemeUtils.TYPE_PIN) {
                view.tvMessageApplyTheme.text = getString(R.string.msg_would_you_like_to_apply_this_theme)
            } else {
                view.tvMessageApplyTheme.text = getString(R.string.msg_would_you_like_to_download_this_theme)
            }
        }
        builder.setView(view)
        mConfirmDialog?.dismiss()
        mConfirmDialog = builder.create()
        mConfirmDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelApplyTheme.setOnClickListener { mConfirmDialog?.dismiss() }
        view.btnYesApplyTheme.setOnClickListener {
            mConfirmDialog?.dismiss()
            checkTheme()
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



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            Const.REQUEST_CODE_CHANGE_THEME -> {
                data?.getSerializableExtra(Const.EXTRA_DATA)?.let {
                    hasChange = true
                    mThemeModel = it as ThemeModel
                    mThemeModel.let { theme ->
                        btnApply.setText(R.string.txt_apply)
                        if (theme.backgroundResId != 0) {
                            imageWallpaper.setImageResource(theme.backgroundResId)
                        } else if (!TextUtils.isEmpty(theme.backgroundDownload)) {
                            Glide.with(this).load(theme.backgroundDownload).into(imageWallpaper)
                        } else {
                            Glide.with(this).load(theme.backgroundUrl).into(imageWallpaper)
                            if (theme.typeTheme != ThemeUtils.TYPE_WALLPAPER) {
                                if (viewModel.isPatternLock() && theme.typeTheme == ThemeUtils.TYPE_PATTERN) {
                                    // nothing
                                } else if (!viewModel.isPatternLock() && theme.typeTheme == ThemeUtils.TYPE_PIN) {
                                    // nothing
                                } else {
                                    btnApply.setText(R.string.title_download)
                                }
                            } else {
                                // nothing
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (hasChange) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        mProgressBar?.dismiss()
        mConfirmDialog?.dismiss()
        super.onDestroy()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, ApplyThemeActivity::class.java)
        }
    }
}