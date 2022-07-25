package com.mtg.applock.ui.activity.intruders.detail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.mtg.applock.R
import com.mtg.applock.data.sqllite.model.IntruderPhoto
import com.mtg.applock.ui.activity.intruders.IntrudersPhotoItemViewState
import com.mtg.applock.ui.base.BaseActivity
import com.mtg.applock.ui.view.BottomView
import com.mtg.applock.util.Const
import com.mtg.applock.util.extensions.dialogLayout
import com.mtg.applock.util.file.EncryptionFileManager
import com.mtg.applock.util.file.FilePathHelper
import com.mtg.applock.util.sharerate.ShareUtils
import com.bumptech.glide.Glide
import com.mtg.library.customview.imagezoom.ImageViewTouchBase
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.activity_intruders_photos_detail.*
import kotlinx.android.synthetic.main.activity_intruders_photos_detail.toolbar
import kotlinx.android.synthetic.main.dialog_delete.view.*
import java.io.File

class IntrudersPhotosDetailActivity : BaseActivity<IntrudersPhotosDetailViewModel>() {
    private var mConfirmDialog: AlertDialog? = null
    private var mPath: String = ""
    private var mOldPath: String = ""
    private var mIsSaveToGallery = false
    private var mId: Int = 0
    private var mIntruderTime: String = ""
    private var mPackageApp: String = ""

    override fun getViewModel(): Class<IntrudersPhotosDetailViewModel> {
        return IntrudersPhotosDetailViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_intruders_photos_detail
    }

    override fun initViews() {
        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        val data = intent.getSerializableExtra(Const.EXTRA_DATA) as IntrudersPhotoItemViewState
        //
        mId = data.id
        mIntruderTime = data.intruderTime
        mPackageApp = data.packageApp
        //
        mPath = data.filePath
        mOldPath = data.filePath
        Glide.with(this).load(mPath).into(imageIntruder)
        imageIntruder.displayType = ImageViewTouchBase.DisplayType.FIT_TO_SCREEN
        imageIntruder.isDisableZoom = true
        // day
        tvDay.text = data.day
        // time
        tvTime.text = data.time
        // icon
        Glide.with(this).load(FilePathHelper.getIconIcon(this, data.packageApp)).into(imageLogoApp)
        buildConfirmDialog()
    }

    override fun initListener() {
        toolbar.setNavigationOnClickListener { onBackPressed() }
        bottomView.setOnSelectedItemListener(object : BottomView.OnSelectedItemListener {
            override fun onSelectedItem(v: View?, type: Int) {
                when (type) {
                    BottomView.TYPE_DELETE -> {
                        mConfirmDialog?.show()
                        dialogLayout(mConfirmDialog)
                    }
                    BottomView.TYPE_SAVE -> {
                        // save to gallery
                        saveToGallery()
                    }
                    BottomView.TYPE_SHARE -> {
                        viewModel.setReLoad(false)
                        ShareUtils.shareOther(this@IntrudersPhotosDetailActivity, mPath)
                    }
                    else -> {

                    }
                }
            }
        })
    }


    private fun buildConfirmDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_delete, null, false)
        view.tvMessageDelete.text = getString(R.string.msg_delete_image_intruder)
        builder.setView(view)
        mConfirmDialog?.dismiss()
        mConfirmDialog = builder.create()
        mConfirmDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelDelete.setOnClickListener { mConfirmDialog?.dismiss() }
        view.btnYesDelete.setOnClickListener {
            mConfirmDialog?.dismiss()
            val success = viewModel.deleteFile(mPath)
            if (success) {
                MediaScannerConnection.scanFile(this, arrayOf(mPath), null) { _: String?, _: Uri? -> }
                val intent = Intent()
                intent.putExtra(Const.EXTRA_PATH, mOldPath)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                Toasty.showToast(this, R.string.msg_delete_failed, Toasty.ERROR)
            }
        }
    }

    private fun saveToGallery() {
        if (!File(mPath).isHidden) {
            Toasty.showToast(this, R.string.msg_photo_is_already_in_the_gallery, Toasty.WARNING)
            return
        }
        EncryptionFileManager.saveIntruderToGallery(this, mPath, object : EncryptionFileManager.OnRenameFileCallback {
            override fun success(path: String) {
                Toasty.showToast(this@IntrudersPhotosDetailActivity, R.string.msg_picture_has_been_saved_to_gallery, Toasty.SUCCESS)
                mPath = path
                mIsSaveToGallery = true
                viewModel.updateIntruder(IntruderPhoto(mId, mPackageApp, mPath, mIntruderTime))
            }

            override fun failed() {
                Toasty.showToast(this@IntrudersPhotosDetailActivity, R.string.msg_delete_failed, Toasty.ERROR)
            }
        })
    }

    override fun onDestroy() {
        mConfirmDialog?.dismiss()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (mIsSaveToGallery) {
            val intent = Intent()
            intent.putExtra(Const.EXTRA_PATH, mOldPath)
            intent.putExtra(Const.EXTRA_PATH_NEW, mPath)
            intent.putExtra(Const.EXTRA_CHANGE_PATH, true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, IntrudersPhotosDetailActivity::class.java)
        }
    }
}