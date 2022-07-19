package com.MTG.AppLock.ui.activity.main.settings.theme.selected

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.MTG.AppLock.R
import com.MTG.AppLock.model.Album
import com.MTG.AppLock.model.ItemDetail
import com.MTG.AppLock.ui.adapter.detaillist.DetailImageListAdapter
import com.MTG.AppLock.ui.adapter.vaultlist.VaultListAdapter
import com.MTG.AppLock.ui.base.BaseActivity
import com.MTG.AppLock.ui.view.ProgressDialog
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.extensions.gone
import com.MTG.AppLock.util.extensions.removeBlink
import com.MTG.AppLock.util.extensions.visible
import com.MTG.library.customview.CustomToolbar
import kotlinx.android.synthetic.main.activity_selected_image.*

class SelectedImageActivity : BaseActivity<SelectedImageViewModel>(), VaultListAdapter.OnSelectedAlbumListener, DetailImageListAdapter.OnSelectedDetailListener {
    private var mVaultListAdapter: VaultListAdapter? = null
    private var mDetailImageAdapter: DetailImageListAdapter? = null
    private var mAlbumList = mutableListOf<Album>()
    private var mDetailList = mutableListOf<ItemDetail>()
    private var mAlbumSelected: Album? = null
    private var mProgressBar: Dialog? = null
    private var mThread: Thread? = null
    private var mThreadLoadAll: Thread? = null

    override fun getViewModel(): Class<SelectedImageViewModel> {
        return SelectedImageViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_selected_image
    }

    override fun initViews() {
        setupToolbar()
        mVaultListAdapter = VaultListAdapter(this, mAlbumList, this)
        recyclerAlbum.adapter = mVaultListAdapter
        recyclerAlbum.layoutManager = GridLayoutManager(this, 2)
        recyclerAlbum.removeBlink()
        //
        mDetailImageAdapter = DetailImageListAdapter(this, mDetailList, this)
        recyclerDetail.adapter = mDetailImageAdapter
        recyclerDetail.layoutManager = GridLayoutManager(this, 3)
        recyclerDetail.removeBlink()
        // load data
        showProgressBar()
        mThreadLoadAll = object : Thread() {
            override fun run() {
                viewModel.loadAlbumWithType(this@SelectedImageActivity)
                viewModel.loadDetailWithType(this@SelectedImageActivity)
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
                return@Observer
            }
            llEmpty.gone()
            recyclerDetail.visible()
            mDetailList.clear()
            mDetailList.addAll(it)
            mDetailImageAdapter?.notifyDataSetChanged()
        })
        toolbar.setOnActionToolbarBack(object : CustomToolbar.OnActionToolbarBack {
            override fun onBack() {
                onBackPressed()
            }
        })
    }

    private fun getTitleToolbar(): String {
        mAlbumSelected?.let {
            return it.name
        } ?: return getString(R.string.title_gallery)
    }

    private fun setupToolbar() {
        toolbar.setTitle(getTitleToolbar())
    }

    private fun showProgressBar() {
        mProgressBar?.dismiss()
        mProgressBar = ProgressDialog.progressDialogV2(this)
        mProgressBar?.show()
    }

    private fun hideProgressBar() {
        mProgressBar?.dismiss()
    }

    override fun onSelectedAlbum(album: Album) {
        recyclerAlbum.gone()
        recyclerDetail.visible()
        showProgressBar()
        mAlbumSelected = album
        setupToolbar()
        mThread = object : Thread() {
            override fun run() {
                viewModel.loadDetailWithAlbum(album.path)
            }
        }
        mThread?.start()
    }

    override fun onSelectedDetail(itemDetail: ItemDetail) {
        val intent = Intent()
        intent.putExtra(Const.EXTRA_PATH, itemDetail.path)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onBackPressed() {
        mThread?.interrupt()
        mThread = null
        if (recyclerDetail.visibility == View.VISIBLE) {
            recyclerAlbum.visible()
            recyclerDetail.gone()
            mAlbumSelected = null
            setupToolbar()
            hideProgressBar()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        mProgressBar?.dismiss()
        mThread?.interrupt()
        mThread = null
        mThreadLoadAll?.interrupt()
        mThreadLoadAll = null
        super.onDestroy()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SelectedImageActivity::class.java)
        }
    }
}
