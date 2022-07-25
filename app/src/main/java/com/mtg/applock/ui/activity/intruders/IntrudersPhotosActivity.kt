package com.mtg.applock.ui.activity.intruders

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import com.mtg.applock.R
import com.mtg.applock.data.sqllite.model.IntruderPhoto
import com.mtg.applock.ui.activity.intruders.detail.IntrudersPhotosDetailActivity
import com.mtg.applock.ui.adapter.intruder.IntruderAdapter
import com.mtg.applock.ui.base.BaseActivity
import com.mtg.applock.util.Const
import com.mtg.applock.util.extensions.dialogLayout
import com.mtg.applock.util.extensions.removeBlink
import com.mtg.applock.util.file.EncryptionFileManager
import com.mtg.applock.util.sharerate.ShareUtils
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.activity_intruders_photos.*
import kotlinx.android.synthetic.main.activity_intruders_photos.toolbar
import kotlinx.android.synthetic.main.dialog_delete.view.*
import kotlinx.android.synthetic.main.dialog_permission_camera.view.*
import kotlinx.android.synthetic.main.popup_more_intruder.view.*
import java.io.File

class IntrudersPhotosActivity : BaseActivity<IntrudersPhotosViewModel>() {
    private var mSettingsIntruderDialog: AlertDialog? = null
    private var mConfirmDeleteDialog: AlertDialog? = null
    private var mCameraPermissionDialog: AlertDialog? = null
    private lateinit var mIntruderAdapter: IntruderAdapter
    private var mIntruderList = mutableListOf<IntrudersPhotoItemViewState>()
    private var mPopupWindow: PopupWindow? = null
    private var mIntrudersPhotoItemViewState: IntrudersPhotoItemViewState? = null

    override fun getViewModel(): Class<IntrudersPhotosViewModel> = IntrudersPhotosViewModel::class.java

    override fun getLayoutId(): Int {
        return R.layout.activity_intruders_photos
    }

    override fun initViews() {
        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        mIntruderAdapter = IntruderAdapter(this, mIntruderList, object : IntruderAdapter.OpenIntrudersPhotosListener {
            override fun openIntrudersPhotosListener(mIntrudersPhotoItemViewState: IntrudersPhotoItemViewState) {
                val intent = IntrudersPhotosDetailActivity.newIntent(this@IntrudersPhotosActivity)
                intent.putExtra(Const.EXTRA_DATA, mIntrudersPhotoItemViewState)
                startActivityForResult(intent, Const.REQUEST_CODE_GO_TO_DETAIL_INTRUDER)
            }

            override fun openMore(view: View?, intrudersPhotoItemViewState: IntrudersPhotoItemViewState) {
                showMoreDialog(view, intrudersPhotoItemViewState)
            }
        })
        viewModel.getIntruderListViewState().observe(this, {
            mIntruderList.clear()
            mIntruderList.addAll(it.intrudersPhotoItemViewStateList)
            mIntruderAdapter.notifyDataSetChanged()
            viewModel.compileName()
            toolbar.menu.findItem(R.id.delete_all)?.setVisible(!it.intrudersPhotoItemViewStateList.isNullOrEmpty())
            llEmpty.visibility = it.getEmptyPageVisibility()
        })
        recyclerViewIntrudersPhotosList.adapter = mIntruderAdapter
        recyclerViewIntrudersPhotosList.removeBlink()
        buildConfirmDeleteDialog()
        buildCameraPermissionDialog()
    }

    override fun initListener() {

        toolbar.setNavigationOnClickListener { onBackPressed() }

        switchIntruders.isChecked = viewModel.getIntrudersCatcherEnabled()
        switchIntruders.setOnCheckedChangeListener { _, isChecked ->
            enableIntrudersCatcher(isChecked)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.intruder_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.delete_all)
        {
            mConfirmDeleteDialog?.show()
            dialogLayout(mConfirmDeleteDialog)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showMoreDialog(view: View?, intrudersPhotoItemViewState: IntrudersPhotoItemViewState) {
        mPopupWindow?.dismiss()
        view?.let {
            val popupView = LayoutInflater.from(this).inflate(R.layout.popup_more_intruder, null)
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            mPopupWindow = PopupWindow(popupView, width, height, true)
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
            popupView.llPreview.setOnClickListener {
                mPopupWindow?.dismiss()
                val intent = IntrudersPhotosDetailActivity.newIntent(this@IntrudersPhotosActivity)
                intent.putExtra(Const.EXTRA_DATA, intrudersPhotoItemViewState)
                startActivityForResult(intent, Const.REQUEST_CODE_GO_TO_DETAIL_INTRUDER)
            }
            popupView.llSaveToGallery.setOnClickListener {
                mPopupWindow?.dismiss()
                saveToGallery(intrudersPhotoItemViewState)
            }
            popupView.llShare.setOnClickListener {
                mPopupWindow?.dismiss()
                viewModel.setReload(false)
                ShareUtils.shareOther(this, intrudersPhotoItemViewState.filePath)
            }
            popupView.llDelete.setOnClickListener {
                mIntrudersPhotoItemViewState = intrudersPhotoItemViewState
                mPopupWindow?.dismiss()
                mConfirmDeleteDialog?.show()
                dialogLayout(mConfirmDeleteDialog)
            }
        }
    }

    private fun saveToGallery(intrudersPhotoItemViewState: IntrudersPhotoItemViewState) {
        if (!File(intrudersPhotoItemViewState.filePath).isHidden) {
            Toasty.showToast(this, R.string.msg_photo_is_already_in_the_gallery, Toasty.WARNING)
            return
        }
        EncryptionFileManager.saveIntruderToGallery(this, intrudersPhotoItemViewState.filePath, object : EncryptionFileManager.OnRenameFileCallback {
            override fun success(path: String) {
                Toasty.showToast(this@IntrudersPhotosActivity, R.string.msg_picture_has_been_saved_to_gallery, Toasty.SUCCESS)
                intrudersPhotoItemViewState.filePath = path
                viewModel.updateIntruder(IntruderPhoto(intrudersPhotoItemViewState.id, intrudersPhotoItemViewState.packageApp, path, intrudersPhotoItemViewState.intruderTime))
                mIntruderAdapter.notifyDataSetChanged()
            }

            override fun failed() {
                Toasty.showToast(this@IntrudersPhotosActivity, R.string.msg_delete_failed, Toasty.ERROR)
            }
        })
    }

    @SuppressLint("CheckResult")
    private fun enableIntrudersCatcher(isChecked: Boolean) {
        if (isChecked) {
            switchIntruders.isChecked = false
            if (isStoragePermissionGranted()) {
                switchIntruders.isChecked = true
                viewModel.setIntrudersCatcherEnable(true)
            }
        } else {
            viewModel.setIntrudersCatcherEnable(false)
        }
    }

    private fun buildConfirmDeleteDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_delete, null, false)
        view.tvMessageDelete.text = getString(R.string.msg_delete_image_intruder)
        builder.setView(view)
        mConfirmDeleteDialog?.dismiss()
        mConfirmDeleteDialog = builder.create()
        mConfirmDeleteDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelDelete.setOnClickListener { mConfirmDeleteDialog?.dismiss() }
        view.btnYesDelete.setOnClickListener {
            mConfirmDeleteDialog?.dismiss()
            mIntrudersPhotoItemViewState?.let {
                if (viewModel.deleteFile(this, it.filePath)) {
                    viewModel.deletePath(it.filePath)
                }
                mIntrudersPhotoItemViewState = null
            } ?:// delete all
            viewModel.deleteAll()
        }
    }

    private fun buildCameraPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_permission_camera, null, false)
        builder.setView(view)
        mCameraPermissionDialog?.dismiss()
        mCameraPermissionDialog = builder.create()
        view.btnDeny.setOnClickListener { mCameraPermissionDialog?.dismiss() }
        view.btnAllowPermissionCamera.setOnClickListener {
            mCameraPermissionDialog?.dismiss()
            viewModel.setReload(false)
            TedPermission.with(this).setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    viewModel.setIntrudersCatcherEnable(true)
                    switchIntruders.isChecked = true
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    viewModel.setIntrudersCatcherEnable(false)
                    switchIntruders.isChecked = false
                }
            }).setDeniedMessage(R.string.msg_denied_permission).setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA).check()
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
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
        mCameraPermissionDialog?.show()
        dialogLayout(mCameraPermissionDialog)
    }


    private fun showDialogCamera() {
        mSettingsIntruderDialog?.show()
        dialogLayout(mSettingsIntruderDialog)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            Const.REQUEST_CODE_GO_TO_DETAIL_INTRUDER -> {
                data?.let { dataPath ->
                    val path = dataPath.getStringExtra(Const.EXTRA_PATH)
                    val pathNew = dataPath.getStringExtra(Const.EXTRA_PATH_NEW)
                    val changePath = dataPath.getBooleanExtra(Const.EXTRA_CHANGE_PATH, false)
                    if (changePath) {
                        pathNew?.let { pathChange ->
                            mIntruderList.forEach { intruder ->
                                if (TextUtils.equals(intruder.filePath, path)) {
                                    intruder.filePath = pathChange
                                    // không cần nữa -> update bên kia luôn
//                                    viewModel.updateIntruder(IntruderPhoto(intruder.id, intruder.packageApp, intruder.filePath, intruder.intruderTime))
                                }
                            }
                            mIntruderAdapter.notifyDataSetChanged()
                        }
                    } else {
                        path?.let { pathRemove ->
                            val intruderList = mIntruderList.filter { element ->
                                !TextUtils.equals(element.filePath, pathRemove)
                            }.toMutableList()
                            viewModel.deletePath(pathRemove)
                            mIntruderList.clear()
                            mIntruderList.addAll(intruderList)
                            mIntruderAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        mSettingsIntruderDialog?.dismiss()
        mConfirmDeleteDialog?.dismiss()
        mCameraPermissionDialog?.dismiss()
        super.onDestroy()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, IntrudersPhotosActivity::class.java)
        }
    }
}