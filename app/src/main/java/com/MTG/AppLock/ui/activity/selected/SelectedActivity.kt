package com.MTG.AppLock.ui.activity.selected

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.MTG.AppLock.R
import com.MTG.AppLock.model.Album
import com.MTG.AppLock.model.EncryptorModel
import com.MTG.AppLock.model.ItemDetail
import com.MTG.AppLock.ui.activity.move.MoveActivity
import com.MTG.AppLock.ui.adapter.detaillist.DetailListAdapter
import com.MTG.AppLock.ui.adapter.vaultlist.VaultListAdapter
import com.MTG.AppLock.ui.base.BaseActivity
import com.MTG.AppLock.ui.view.ProgressDialog
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.extensions.dialogLayout
import com.MTG.AppLock.util.extensions.gone
import com.MTG.AppLock.util.extensions.removeBlink
import com.MTG.AppLock.util.extensions.visible
import com.MTG.library.customview.CustomToolbar
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.activity_selected.*
import kotlinx.android.synthetic.main.dialog_lock.view.*
import kotlinx.android.synthetic.main.dialog_permission_overlap.view.*
import java.util.*

class SelectedActivity : BaseActivity<SelectedViewModel>(), VaultListAdapter.OnSelectedAlbumListener, DetailListAdapter.OnSelectedDetailListener {
    private var mVaultListAdapter: VaultListAdapter? = null
    private var mDetailAdapterV2: DetailListAdapter? = null
    private var mAllFileAccessDialog: AlertDialog? = null
    private var mAlbumList = mutableListOf<Album>()
    private var mDetailList = mutableListOf<ItemDetail>()
    private var mType = Const.TYPE_IMAGES
    private var mAlbumSelected: Album? = null
    private var mConfirmDialog: AlertDialog? = null
    private var mThread: Thread? = null
    private var mThreadLoadAll: Thread? = null
    private var mProgressBar: Dialog? = null

    override fun getViewModel(): Class<SelectedViewModel> {
        return SelectedViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_selected
    }

    override fun initViews() {
        mType = intent.getIntExtra(Const.EXTRA_TYPE, Const.TYPE_IMAGES)
        setupToolbar()
        buildAllFileAccessDialog()
        mVaultListAdapter = VaultListAdapter(this, mAlbumList, this)
        recyclerAlbum.adapter = mVaultListAdapter
        recyclerAlbum.layoutManager = GridLayoutManager(this, 2)
        recyclerAlbum.removeBlink()
        //
        mDetailAdapterV2 = DetailListAdapter(this, mDetailList, this)
        mDetailAdapterV2?.setShow(true)
        recyclerDetail.adapter = mDetailAdapterV2
        recyclerDetail.layoutManager = GridLayoutManager(this, getCountWithType())
        recyclerDetail.removeBlink()
        // load data
        showProgressBar()
        mThreadLoadAll = object : Thread() {
            override fun run() {
                viewModel.loadDetailWithType(this@SelectedActivity, mType)
                viewModel.loadAlbumWithType(this@SelectedActivity, mType)
            }
        }
        mThreadLoadAll?.start()
        viewModel.getAlbumListLiveData().observe(this, Observer {
            hideProgressBar()
            if (it.isNullOrEmpty()) {
                llEmpty.visible()
                recyclerAlbum.gone()
                return@Observer
            }
            llEmpty.gone()
            recyclerAlbum.visible()
            mAlbumList.clear()
            mAlbumList.addAll(it)
            mVaultListAdapter?.notifyDataSetChanged()
        })
        viewModel.getDetailWithAlbumListLiveData().observe(this, Observer {
            hideProgressBar()
            if (it.isNullOrEmpty()) {
                llEmpty.visible()
                recyclerDetail.gone()
                btnLock.gone()
                toolbar.setShowTvActionExtend(false)
                return@Observer
            }
            btnLock.visible()
            toolbar.setShowTvActionExtend(true)
            llEmpty.gone()
            recyclerDetail.visible()
            mDetailList.clear()
            mDetailList.addAll(it)
            mDetailAdapterV2?.notifyDataSetChanged()
        })
        toolbar.setOnActionToolbarBack(object : CustomToolbar.OnActionToolbarBack {
            override fun onBack() {
                onBackPressed()
            }
        })
        btnLock.setOnClickListener {
            mDetailAdapterV2?.let {
                if (it.getSelectedNumber() > 0) {
                    mConfirmDialog?.show()
                    dialogLayout(mConfirmDialog)
                } else {
                    showToastWarning()
                }
            } ?: showToastWarning()
        }
        //
        buildConfirmDialog()
    }

    override fun initListener() {
        toolbar.setOnTvActionExtendToolbar(object : CustomToolbar.OnTvActionExtendToolbar {
            override fun onTvActionExtend(selectedAll: Boolean) {
                mDetailAdapterV2?.setSelectedAll(selectedAll)
                setupToolbar()
            }
        })
    }

    private fun showToastWarning() {
        Toasty.showToast(this@SelectedActivity, R.string.msg_please_choose_at_least_one, Toasty.WARNING)
    }

    private fun buildConfirmDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_lock, null, false)
        builder.setView(view)
        mConfirmDialog?.dismiss()
        mConfirmDialog = builder.create()
        mConfirmDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelLock.setOnClickListener { mConfirmDialog?.dismiss() }
        view.btnYesLock.setOnClickListener {
            mConfirmDialog?.dismiss()
            lock()
        }
    }

    private fun buildAllFileAccessDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_permission_all_access, null, false)
        builder.setView(view)
        builder.setCancelable(false)
        mAllFileAccessDialog?.dismiss()
        mAllFileAccessDialog = builder.create()
        //   mOverlapPermissionDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelOverlap.setOnClickListener { mAllFileAccessDialog?.dismiss() }
        view.btnGotoSettingOverlap.setOnClickListener {
            mAllFileAccessDialog?.dismiss()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivityForResult(intent, 2296)
                }

        }
    }

    private fun lock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                val itemDetailList: MutableList<ItemDetail> = mutableListOf()
                for (detail in mDetailList) {
                    if (detail.isSelected) {
                        itemDetailList.add(detail)
                    }
                }

                val encryptor = EncryptorModel(Const.TYPE_ENCODE, itemDetailList)
                val intent = MoveActivity.newIntent(this)
                intent.putExtra(Const.EXTRA_DATA, encryptor)
                intent.putExtra(Const.EXTRA_TYPE, mType)
                startActivityForResult(intent, Const.REQUEST_CODE_MOVE_FILE)
            } else {
                mAllFileAccessDialog?.show()

            }

        } else {
            val itemDetailList: MutableList<ItemDetail> = mutableListOf()
            for (detail in mDetailList) {
                if (detail.isSelected) {
                    itemDetailList.add(detail)
                }
            }

            val encryptor = EncryptorModel(Const.TYPE_ENCODE, itemDetailList)
            val intent = MoveActivity.newIntent(this)
            intent.putExtra(Const.EXTRA_DATA, encryptor)
            intent.putExtra(Const.EXTRA_TYPE, mType)
            startActivityForResult(intent, Const.REQUEST_CODE_MOVE_FILE)
        }
    }

    private fun getTitleToolbar(): String {
        mAlbumSelected?.let {
            return it.name
        } ?: return when (mType) {
            Const.TYPE_IMAGES, Const.TYPE_VIDEOS -> {
                getString(R.string.title_gallery)
            }
            Const.TYPE_AUDIOS -> {
                getString(R.string.title_audio_library)
            }
            Const.TYPE_FILES -> {
                getString(R.string.title_file_library)
            }
            else -> getString(R.string.title_gallery)
        }
    }

    private fun setupToolbar() {
        toolbar.setTitle(getTitleToolbar())
        val number = mDetailAdapterV2?.getSelectedNumber()
        val type = when (mType) {
            Const.TYPE_VIDEOS -> {
                if (number == 1) {
                    getString(R.string.text_video)
                } else {
                    getString(R.string.text_videos)
                }
            }
            Const.TYPE_AUDIOS -> {
                if (number == 1) {
                    getString(R.string.text_audio)
                } else {
                    getString(R.string.text_audios)
                }
            }
            Const.TYPE_FILES -> {
                if (number == 1) {
                    getString(R.string.text_file)
                } else {
                    getString(R.string.text_files)
                }
            }
            else -> {
                if (number == 1) {
                    getString(R.string.text_photo)
                } else {
                    getString(R.string.text_photos)
                }
            }
        }
        if (number == null) {
            btnLock.gone()
            toolbar.setShowTvActionExtend(false)
        } else {
            btnLock.visible()
            toolbar.setShowTvActionExtend(true)
            btnLock.text = String.format(Locale.getDefault(), "%s (%d) %s", getString(R.string.text_click_here_to_lock), number, type)
            toolbar.setStatusTvActionExtend(number == mDetailList.size && number != 0)
        }
    }

    private fun getCountWithType(): Int {
        var count = 3
        when (mType) {
            Const.TYPE_IMAGES, Const.TYPE_VIDEOS -> {
                count = 3
            }
            Const.TYPE_AUDIOS, Const.TYPE_FILES -> {
                count = 1
            }
        }
        return count
    }

    override fun onSelectedAlbum(album: Album) {
        recyclerAlbum.gone()
        recyclerDetail.visible()
        btnLock.visible()
        toolbar.setShowTvActionExtend(true)
        showProgressBar()
        mAlbumSelected = album
        setupToolbar()
        mThread = object : Thread() {
            override fun run() {
                viewModel.loadDetailWithAlbum(album.path, album.type, album.extension)
            }
        }
        mThread?.start()
    }

    override fun onSelectedDetail(itemDetail: ItemDetail, position: Int) {
        setupToolbar()
    }

    override fun onMoreDetail(view: View, itemDetail: ItemDetail, position: Int) {
        // nothing
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            Const.REQUEST_CODE_MOVE_FILE -> {
                val intent = Intent()
                intent.putExtra(Const.EXTRA_TYPE, mType)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun showProgressBar() {
        mProgressBar?.dismiss()
        mProgressBar = ProgressDialog.progressDialogV2(this)
        mProgressBar?.show()
    }

    private fun hideProgressBar() {
        mProgressBar?.dismiss()
    }

    override fun onDestroy() {
        mConfirmDialog?.dismiss()
        mThread?.interrupt()
        mThread = null
        mThreadLoadAll?.interrupt()
        mThreadLoadAll = null
        mProgressBar?.dismiss()
        super.onDestroy()
    }

    override fun onBackPressed() {
        mThread?.interrupt()
        mThread = null
        if (recyclerDetail.visibility == View.VISIBLE) {
            recyclerAlbum.visible()
            recyclerDetail.gone()
            mDetailAdapterV2?.setSelectedAll(false)
            mAlbumSelected = null
            setupToolbar()
            hideProgressBar()
            btnLock.gone()
            toolbar.setShowTvActionExtend(false)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SelectedActivity::class.java)
        }
    }
}
