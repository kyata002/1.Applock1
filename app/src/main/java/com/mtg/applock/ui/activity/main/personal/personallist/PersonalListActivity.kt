package com.mtg.applock.ui.activity.main.personal.personallist

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.GridLayoutManager
import com.mtg.applock.R
import com.mtg.applock.model.Album
import com.mtg.applock.ui.activity.detail.DetailListActivity
import com.mtg.applock.ui.activity.main.MainActivity
import com.mtg.applock.ui.activity.move.MoveActivity
import com.mtg.applock.ui.activity.selected.SelectedActivity
import com.mtg.applock.ui.adapter.vaultlist.VaultListAdapter
import com.mtg.applock.ui.base.BaseActivity
import com.mtg.applock.util.CommonUtils
import com.mtg.applock.util.Const
import com.mtg.applock.util.extensions.background
import com.mtg.applock.util.extensions.gone
import com.mtg.applock.util.extensions.removeBlink
import com.mtg.applock.util.extensions.visible
import kotlinx.android.synthetic.main.activity_personal_list.*
import kotlinx.android.synthetic.main.activity_personal_list.toolbar

class PersonalListActivity : BaseActivity<PersonalListViewModel>(),
    VaultListAdapter.OnSelectedAlbumListener {
    private var mVaultListAdapter: VaultListAdapter? = null
    private var mVaultList = mutableListOf<Album>()
    private var mType = Const.TYPE_IMAGES
    private var mNumber = 0
    private var mName: String = ""

    override fun getViewModel(): Class<PersonalListViewModel> {
        return PersonalListViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_personal_list
    }

    override fun initViews() {
        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
        /* toolbar.setOnActionToolbarFull(object : CustomToolbar.OnActionToolbarFull {
             override fun onAction() {
                 mVaultListAdapter?.setSelectedAll(false)
                 mVaultListAdapter?.setShow(true)
                 toolbar.setShowAction(false)
                 btnAddVault.gone()
             }

             override fun onBack() {
                 onBackPressed()
             }
         })*/
        mType = intent.getIntExtra(Const.EXTRA_TYPE, Const.TYPE_IMAGES)
        mNumber = intent.getIntExtra(Const.EXTRA_NUMBER, 0)
        when (mType) {
            Const.TYPE_IMAGES -> {
                mName = getString(R.string.tabs_vault_images)
            }
            Const.TYPE_VIDEOS -> {
                mName = getString(R.string.tabs_vault_videos)
            }
            Const.TYPE_AUDIOS -> {
                mName = getString(R.string.tabs_vault_audios)
            }
            Const.TYPE_FILES -> {
                mName = getString(R.string.tabs_vault_files)
            }
        }
        setupToolbar()
        mVaultListAdapter = VaultListAdapter(this, mVaultList, this)
        recyclerList.adapter = mVaultListAdapter
        recyclerList.layoutManager = GridLayoutManager(this, 2)
        recyclerList.removeBlink()
        loadData(mType)
        viewModel.getVaultListViewStateLiveData().observe(this, {
            hideProgressBar()
            mVaultList.clear()
            mVaultList.addAll(it.albumList)
            mVaultListAdapter?.notifyDataSetChanged()
            recyclerList.visibility = it.getEmptyRecyclerVisibility()
            llEmpty.visibility = it.getEmptyPageVisibility()
            imageEmpty.setImageDrawable(it.getEmptyPageDrawable(this, mType))
            tvTitleEmpty.text = it.getEmptyPageTitle(this, mType)
            tvDescriptionEmpty.text = it.getEmptyPageDescription(this, mType)
            setupToolbar()
        })
        btnAddVault.setOnClickListener {
            if (CommonUtils.isCanSaveFile(this@PersonalListActivity)) {
                val intent = SelectedActivity.newIntent(this)
                intent.putExtra(Const.EXTRA_TYPE, mType)
                startActivityForResult(intent, Const.REQUEST_CODE_SELECTED)
            }
        }
        //
        viewModel.getProgressLiveData().observe(this, {
            if (mNumber != 0) {
                val builder = StringBuilder()
                builder.append("" + it * 100 / mNumber)
                builder.append(" %")
                tvAnimationLoading.text = builder.toString()
                if (it > mNumber) {
                    tvAnimationLoading.gone()
                } else {
                    tvAnimationLoading.visible()
                }
            }
        })
    }

    private fun loadData(type: Int) {
        // load data
        showProgressBar()
        background.submit {
            viewModel.loadDataWithType(type)
        }
    }

    override fun onSelectedAlbum(album: Album) {
        val intent = DetailListActivity.newIntent(this)
        intent.putExtra(Const.EXTRA_NAME, album.name)
        intent.putExtra(Const.EXTRA_PATH, album.path)
        intent.putExtra(Const.EXTRA_NUMBER, album.number)
        intent.putExtra(Const.EXTRA_TYPE, mType)
        intent.putExtra(Const.EXTRA_EXTENSION, album.extension)
        startActivityForResult(intent, Const.REQUEST_CODE_GO_TO_DETAIL_LIST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            Const.REQUEST_CODE_SELECTED -> {
                data?.let {
                    val type = it.getIntExtra(Const.EXTRA_TYPE, Const.TYPE_IMAGES)
                    loadData(type)
                }
            }
            Const.REQUEST_CODE_GO_TO_DETAIL_LIST -> {
                data?.let {
                    val type = it.getIntExtra(Const.EXTRA_TYPE, Const.TYPE_IMAGES)
                    loadData(type)
                }
            }
            Const.REQUEST_CODE_MOVE_FILE -> {
                mVaultListAdapter?.setShow(false)
                btnAddVault.visible()
                llEmpty.gone()
                data?.let {
                    val type = it.getIntExtra(Const.EXTRA_TYPE, Const.TYPE_IMAGES)
                    loadData(type)
                }
            }
        }
    }

    private fun setupToolbar() {
        toolbar.setTitle(mName)
    }

    private fun showProgressBar() {
        rlLoading.visible()
    }

    private fun hideProgressBar() {
        rlLoading.gone()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        var number = 0
        mVaultList.forEach {
            number += it.number
        }
        if (MoveActivity.check == 1) {
            MoveActivity.check = 0
            val intent = MainActivity.newIntent(this)
            startActivityForResult(intent, 2)
        } else {
            when {
                mNumber != number -> {
                    val intent = Intent()
                    intent.putExtra(Const.EXTRA_TYPE, mType)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                else -> super.onBackPressed()
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PersonalListActivity::class.java)
        }
    }
}