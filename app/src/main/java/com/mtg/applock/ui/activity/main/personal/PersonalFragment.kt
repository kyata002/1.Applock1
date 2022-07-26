package com.mtg.applock.ui.activity.main.personal

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.mtg.applock.R
import com.mtg.applock.control.OnDialogListener
import com.mtg.applock.ui.activity.main.MainActivity
import com.mtg.applock.ui.activity.main.OnMainListener
import com.mtg.applock.ui.activity.main.personal.personallist.PersonalListActivity
import com.mtg.applock.ui.base.BaseFragment
import com.mtg.applock.util.Const

import kotlinx.android.synthetic.main.fragment_personal.*

class PersonalFragment : BaseFragment<PersonalViewModel>(), View.OnClickListener, OnDialogListener {
    private var mIsCheckPermissions = false
    private var mIsRequest = false
    private var mNumberImages = 0
    private var mNumberVideos = 0
    private var mNumberAudios = 0
    private var mNumberFiles = 0
    private var mOnRequestWritePermissionsListener: OnRequestWritePermissionsListener? = null
    private var mOnMainListener: OnMainListener? = null
    private var mHasPermissions = false
    private var mIsAttached = false

    override fun getViewModel(): Class<PersonalViewModel> {
        return PersonalViewModel::class.java
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_personal, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ivImages.setOnClickListener(this)
        ivVideos.setOnClickListener(this)
        ivAudios.setOnClickListener(this)
        ivFiles.setOnClickListener(this)
        //
        vImages.setOnClickListener(this)
        vVideos.setOnClickListener(this)
        vAudios.setOnClickListener(this)
        vFiles.setOnClickListener(this)
        //
        tvImages.setOnClickListener(this)
        tvVideos.setOnClickListener(this)
        tvAudios.setOnClickListener(this)
        tvFiles.setOnClickListener(this)
        //
        mHasPermissions = isStoragePermissionGranted(false)
        setupNumberType(mHasPermissions)
        viewModel.getVaultGroupLiveData().observe(requireActivity(), {
            it.vaultItem?.let { item ->
                when (item.type) {
                    Const.TYPE_IMAGES -> {
                        mNumberImages = item.number
                        if (it.granted) {
                            tvImages.text =
                                getString(R.string.tabs_vault_images) + " (" + mNumberImages + ")"
                        } else {
                            tvImages.text = getString(R.string.tabs_vault_images) + " ( - )"
                        }
                        mOnMainListener?.hideAnimation()
                    }
                    Const.TYPE_VIDEOS -> {
                        mNumberVideos = item.number
                        if (it.granted) {
                            tvVideos.text =
                                getString(R.string.tabs_vault_videos) + " (" + mNumberVideos + ")"
                        } else {
                            tvVideos.text = getString(R.string.tabs_vault_videos) + " ( - )"
                        }
                        mOnMainListener?.hideAnimation()
                    }
                    Const.TYPE_AUDIOS -> {
                        mNumberAudios = item.number
                        if (it.granted) {
                            tvAudios.text =
                                getString(R.string.tabs_vault_audios) + " (" + mNumberAudios + ")"
                        } else {
                            tvAudios.text = getString(R.string.tabs_vault_audios) + " ( - )"
                        }
                        mOnMainListener?.hideAnimation()
                    }
                    Const.TYPE_FILES -> {
                        mNumberFiles = item.number
                        if (it.granted) {
                            tvFiles.text =
                                getString(R.string.tabs_vault_files) + " (" + mNumberFiles + ")"
                        } else {
                            tvFiles.text = getString(R.string.tabs_vault_files) + " ( - )"
                        }
                        mOnMainListener?.hideAnimation()
                    }
                    else -> {
                    }
                }
            }
            it.vaultList.forEach { vault ->
                when (vault.type) {
                    Const.TYPE_IMAGES -> mNumberImages = vault.number
                    Const.TYPE_VIDEOS -> mNumberVideos = vault.number
                    Const.TYPE_AUDIOS -> mNumberAudios = vault.number
                    Const.TYPE_FILES -> mNumberFiles = vault.number
                }
                if (it.granted) {
                    tvImages.text =
                        getString(R.string.tabs_vault_images) + " (" + mNumberImages + ")"
                    tvVideos.text =
                        getString(R.string.tabs_vault_videos) + " (" + mNumberVideos + ")"
                    tvAudios.text =
                        getString(R.string.tabs_vault_audios) + " (" + mNumberAudios + ")"
                    tvFiles.text = getString(R.string.tabs_vault_files) + " (" + mNumberFiles + ")"
                } else {
                    tvImages.text = getString(R.string.tabs_vault_images) + " ( - )"
                    tvVideos.text = getString(R.string.tabs_vault_videos) + " ( - )"
                    tvAudios.text = getString(R.string.tabs_vault_audios) + " ( - )"
                    tvFiles.text = getString(R.string.tabs_vault_files) + " ( - )"
                }
                mOnMainListener?.hideAnimation()
            }
        })

    }

    private fun setupNumberType(granted: Boolean) {
        setupNumberType(granted, Const.TYPE_FULL)
    }

    fun setupNumberType() {
        setupNumberType(true)
    }

    fun setupNumberTypeIfChange() {
        if (!mIsAttached) return
        if (mHasPermissions) return
        if (isStoragePermissionGranted(false)) {
            mHasPermissions = true
            setupNumberType()
        }
    }

    private fun setupNumberType(granted: Boolean, type: Int) {
        mOnMainListener?.showAnimation()
        viewModel.setupNumberType(granted, type)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mIsAttached = true
        if (context is MainActivity) {
            mOnRequestWritePermissionsListener = context
        }
        if (context is OnMainListener) {
            mOnMainListener = context
        }
    }

    @SuppressLint("CheckResult")
    override fun onClick(v: View?) {
        viewModel.setReload(false)
        if (isStoragePermissionGranted(true)) {
            val intent = PersonalListActivity.newIntent(requireContext())
            when (v?.id) {
                R.id.ivImages, R.id.tvImages, R.id.vImages -> {
                    intent.putExtra(Const.EXTRA_TYPE, Const.TYPE_IMAGES)
                    intent.putExtra(Const.EXTRA_NUMBER, mNumberImages)
                }
                R.id.ivVideos, R.id.tvVideos, R.id.vVideos -> {
                    intent.putExtra(Const.EXTRA_TYPE, Const.TYPE_VIDEOS)
                    intent.putExtra(Const.EXTRA_NUMBER, mNumberVideos)
                }
                R.id.ivAudios, R.id.tvAudios, R.id.vAudios -> {
                    intent.putExtra(Const.EXTRA_TYPE, Const.TYPE_AUDIOS)
                    intent.putExtra(Const.EXTRA_NUMBER, mNumberAudios)
                }
                R.id.ivFiles, R.id.tvFiles, R.id.vFiles -> {
                    intent.putExtra(Const.EXTRA_TYPE, Const.TYPE_FILES)
                    intent.putExtra(Const.EXTRA_NUMBER, mNumberFiles)
                }
                else -> {
                    intent.putExtra(Const.EXTRA_TYPE, Const.TYPE_IMAGES)
                    intent.putExtra(Const.EXTRA_NUMBER, mNumberImages)
                }
            }
            startActivityForResult(intent, Const.REQUEST_CODE_UPDATE_VAULT)
//            AdManager.instance?.showAdFull(object : AdManager.OnClickAdsListener {
//                override fun onClosed() {
//
//                }
//            })
        }
    }

    private fun isStoragePermissionGranted(isRequest: Boolean): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                if (isRequest) {
                    mOnRequestWritePermissionsListener?.onRequestWritePermissions()
                }
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            Const.REQUEST_CODE_UPDATE_VAULT -> {
                data?.let {
                    val type = it.getIntExtra(Const.EXTRA_TYPE, Const.TYPE_IMAGES)
                    setupNumberType(true, type)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mIsCheckPermissions) {
            mIsCheckPermissions = false
            mOnRequestWritePermissionsListener?.onRequestWritePermissions()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mIsRequest) {
            mIsRequest = false
            mIsCheckPermissions = true
        }
    }

    override fun onDestroy() {
        mOnMainListener = null
        super.onDestroy()
    }

    override fun onDialogPermit() {
        mIsRequest = true
    }

    override fun onDialogCancel() {
        // nothing
    }

    companion object {
        fun newInstance() = PersonalFragment()
    }

    interface OnRequestWritePermissionsListener {
        fun onRequestWritePermissions()
    }
}