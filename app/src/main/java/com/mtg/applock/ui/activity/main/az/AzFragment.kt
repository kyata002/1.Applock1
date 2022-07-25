package com.mtg.applock.ui.activity.main.az

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.mtg.applock.R
import com.mtg.applock.control.OnAppSelectedListener
import com.mtg.applock.ui.activity.main.OnMainListener
import com.mtg.applock.ui.activity.main.az.model.AppLockItemItemViewState
import com.mtg.applock.ui.activity.main.settings.AdminReceiver
import com.mtg.applock.ui.adapter.all.AppLockListAdapter
import com.mtg.applock.ui.base.BaseFragment
import com.mtg.applock.util.ApplicationListUtils
import com.mtg.applock.util.CommonUtils
import com.mtg.applock.util.Const
import com.mtg.applock.util.extensions.gone
import com.mtg.applock.util.extensions.removeBlink
import com.mtg.applock.util.extensions.visible
import com.mtg.applock.util.preferences.AppLockerPreferences
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_unlock_settings.view.*
import kotlinx.android.synthetic.main.fragment_az.*
import kotlinx.android.synthetic.main.fragment_az.view.*

class AzFragment : BaseFragment<AzViewModel>() {
    private val mAdapter: AppLockListAdapter = AppLockListAdapter()
    private var mConfirmUnlockSettingsDialog: AlertDialog? = null
    private lateinit var mComponentName: ComponentName
    private var mAppLockItemSettings: AppLockItemItemViewState? = null
    private var mOnMainListener: OnMainListener? = null

    override fun getViewModel(): Class<AzViewModel> = AzViewModel::class.java

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_az, container, false)
        view.recyclerViewFull.adapter = mAdapter
        view.recyclerViewFull.removeBlink()
        buildConfirmUnlockSettingsDialog()
        mComponentName = ComponentName(requireContext(), AdminReceiver::class.java)
        return view
    }

    private fun buildConfirmUnlockSettingsDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val view: View = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_unlock_settings, null, false)
        builder.setView(view)
        mConfirmUnlockSettingsDialog?.dismiss()
        mConfirmUnlockSettingsDialog = builder.create()
        mConfirmUnlockSettingsDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnUnlockUnlockSettings.setOnClickListener {
            mConfirmUnlockSettingsDialog?.dismiss()
            mAppLockItemSettings?.let {
                if (it.isLocked) {
                    ApplicationListUtils.instance?.unlockApp(requireContext(), it)
                }
            }
        }
        view.btnLockUnlockSettings.setOnClickListener {
            mConfirmUnlockSettingsDialog?.dismiss()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ApplicationListUtils.instance?.getAppDataFullLiveData()?.observe(requireActivity(), {
            mOnMainListener?.hideAnimation()
            if (it.appDataList.isEmpty()) {
                textEmpty.visible()
                recyclerViewFull.gone()
            } else {
                textEmpty.gone()
                recyclerViewFull.visible()
                mAdapter.setEnableSettings(CommonUtils.enableSettings(requireContext()))
                mAdapter.setAppDataList(it.appDataList, it.isRemove)
            }
        })
        mAdapter.setOnAppSelectedListener(object : OnAppSelectedListener {
            override fun onDeleteApp(position: Int, appLockItemItemViewState: AppLockItemItemViewState) {
                onAppDelete(appLockItemItemViewState)
            }

            override fun onLockApp(position: Int, appLockItemItemViewState: AppLockItemItemViewState) {
                onAppSelected(appLockItemItemViewState)
            }
        })
    }

    private fun onAppSelected(selectedApp: AppLockItemItemViewState) {
        if (mOnMainListener?.onAppSelected() == false) return
        if (viewModel.isApplicationGroupActive(selectedApp.appData.parsePackageName())) {
            Toasty.showToast(requireContext(), R.string.msg_cannot_unlock_app, Toasty.WARNING)
            return
        }
        if (TextUtils.equals(selectedApp.appData.parsePackageName(), Const.SETTINGS_PACKAGE)) {
            if (selectedApp.isLocked) {
                mAppLockItemSettings = selectedApp
                mConfirmUnlockSettingsDialog?.show()
                dialogLayoutInFragment(mConfirmUnlockSettingsDialog)
                return
            }
        } else {
            mAppLockItemSettings = null
        }
        viewModel.setReload(true)
        viewModel.setLockSettingApp(AppLockerPreferences.LOCK_APP_ALWAYS)
        if (selectedApp.isLocked) {
            ApplicationListUtils.instance?.unlockApp(requireContext(), selectedApp)
        } else {
            ApplicationListUtils.instance?.lockApp(requireContext(), selectedApp)
        }
    }

    private fun onAppDelete(deleteApp: AppLockItemItemViewState) {
        if (mOnMainListener?.onAppSelected() == false) return
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:" + deleteApp.appData.parsePackageName())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        viewModel.setReload(false)
        viewModel.setShowPasswordUninstall(false)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnMainListener) {
            mOnMainListener = context
        }
    }

    override fun onResume() {
        super.onResume()
        mAdapter.setHasPermission(mOnMainListener?.onAppSelected() == true)
    }

    override fun onDestroy() {
        mConfirmUnlockSettingsDialog?.dismiss()
        super.onDestroy()
    }

    companion object {
        fun newInstance() = AzFragment()
    }
}