package com.MTG.AppLock.ui.activity.main.settings.theme.pattern

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.MTG.AppLock.R
import com.MTG.AppLock.model.ThemeModel
import com.MTG.AppLock.ui.activity.main.settings.apply.ApplyThemeActivity
import com.MTG.AppLock.ui.adapter.theme.ThemeAdapter
import com.MTG.AppLock.ui.base.BaseFragment
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.extensions.removeBlink
import com.MTG.AppLock.util.network.NetworkUtils
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_pattern.*
import kotlinx.android.synthetic.main.fragment_pattern.view.*

class PatternFragment : BaseFragment<PatternViewModel>(), ThemeAdapter.OnSelectedThemeListener, SwipeRefreshLayout.OnRefreshListener {
    private var mPatternAdapter: ThemeAdapter? = null
    private var mLastClickTime: Long = 0

    override fun getViewModel(): Class<PatternViewModel> {
        return PatternViewModel::class.java
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pattern, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPatternAdapter = ThemeAdapter(requireContext(), this)
        view.recyclerPattern.adapter = mPatternAdapter
        view.recyclerPattern.removeBlink()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getPatternLiveData().observe(requireActivity(), {
            mPatternAdapter?.addData(it)
            swipeRefreshLayout.isRefreshing = false
        })
        viewModel.getStopReloadLiveData().observe(requireActivity(), {
            if (it) {
                swipeRefreshLayout.isRefreshing = false
            }
        })
        swipeRefreshLayout.setOnRefreshListener(this)
    }

    override fun onRefresh() {
        mPatternAdapter?.clear()
        viewModel.reloadPattern()
    }

    override fun onSelectedTheme(themeModel: ThemeModel) {
        val currentClickTime = SystemClock.uptimeMillis()
        val elapsedTime = currentClickTime - mLastClickTime
        mLastClickTime = currentClickTime
        if (elapsedTime <= Const.MIN_CLICK_INTERVAL) return
        if (themeModel.backgroundResId != 0) {
            val intent = ApplyThemeActivity.newIntent(requireContext())
            intent.putExtra(Const.EXTRA_DATA, themeModel)
            startActivityForResult(intent, Const.REQUEST_CODE_REFRESH)
        } else if (!TextUtils.isEmpty(themeModel.backgroundDownload)) {
            if (isStoragePermissionGranted()) {
                val intent = ApplyThemeActivity.newIntent(requireContext())
                intent.putExtra(Const.EXTRA_DATA, themeModel)
                startActivityForResult(intent, Const.REQUEST_CODE_REFRESH)
            }
        } else {
            NetworkUtils.hasInternetAccessCheck(requireContext(), object : NetworkUtils.OnCallbackCheckNetwork {
                override fun hasInternetAccess() {
                    val intent = ApplyThemeActivity.newIntent(requireContext())
                    intent.putExtra(Const.EXTRA_DATA, themeModel)
                    startActivityForResult(intent, Const.REQUEST_CODE_REFRESH)
                    requireActivity().runOnUiThread {
                        Toasty.hideToast()
                    }
                }

                override fun errorInternetAccess() {
                    requireActivity().runOnUiThread {
                        Toasty.showToast(requireContext(), R.string.check_network_connection, Toasty.ERROR)
                    }
                }
            })
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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
        TedPermission.with(requireContext()).setPermissionListener(object : PermissionListener {
            override fun onPermissionGranted() {
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
            }
        }).setDeniedMessage(R.string.msg_denied_permission)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Const.REQUEST_CODE_REFRESH -> {
                if (resultCode == Activity.RESULT_OK) {
                    onRefresh()
                }
            }
        }
    }

    companion object {
        fun newInstance() = PatternFragment()
    }
}