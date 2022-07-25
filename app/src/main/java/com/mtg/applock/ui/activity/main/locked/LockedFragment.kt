package com.mtg.applock.ui.activity.main.locked

import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.mtg.applock.R
import com.mtg.applock.control.OnAppSelectedListener
import com.mtg.applock.ui.activity.main.MainActivity
import com.mtg.applock.ui.activity.main.OnMainListener
import com.mtg.applock.ui.activity.main.az.model.AppLockItemItemViewState
import com.mtg.applock.ui.activity.main.settings.AdminReceiver
import com.mtg.applock.ui.adapter.all.AppLockListForLockedAdapter
import com.mtg.applock.ui.base.BaseFragment
import com.mtg.applock.util.ApplicationListUtils
import com.mtg.applock.util.CommonUtils
import com.mtg.applock.util.Const
import com.mtg.applock.util.extensions.gone
import com.mtg.applock.util.extensions.removeBlink
import com.mtg.applock.util.extensions.visible
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_unlock_all.view.*
import kotlinx.android.synthetic.main.dialog_unlock_settings.view.*
import kotlinx.android.synthetic.main.fragment_locked.*
import kotlinx.android.synthetic.main.fragment_locked.view.*

class LockedFragment : BaseFragment<LockedViewModel>() {
    private val mAdapter: AppLockListForLockedAdapter = AppLockListForLockedAdapter()
    private var mConfirmUnlockSettingsDialog: AlertDialog? = null
    private var mConfirmUnlockAllDialog: AlertDialog? = null
    private lateinit var mComponentName: ComponentName
    private var mAppLockItemSettings: AppLockItemItemViewState? = null
    private var mOnMainListener: OnMainListener? = null

    override fun getViewModel(): Class<LockedViewModel> = LockedViewModel::class.java

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_locked, container, false)
        view.recyclerViewLocked.adapter = mAdapter
        view.recyclerViewLocked.removeBlink()
        buildUnlockAllDialog()
        buildConfirmUnlockSettingsDialog()
        mComponentName = ComponentName(requireContext(), AdminReceiver::class.java)

        return view
    }

    private fun loadData() {
        ApplicationListUtils.instance?.loadDataLocked()
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

    private fun buildUnlockAllDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val view: View = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_unlock_all, null, false)
        builder.setView(view)
        mConfirmUnlockAllDialog?.dismiss()
        mConfirmUnlockAllDialog = builder.create()
        mConfirmUnlockAllDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelUnlockAll.setOnClickListener { mConfirmUnlockAllDialog?.dismiss() }
        view.btnYesUnlockAll.setOnClickListener {
            mConfirmUnlockAllDialog?.dismiss()
            ApplicationListUtils.instance?.unlockAll(requireContext())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadData()
        ApplicationListUtils.instance?.getAppDataLockedListLiveData()?.observe(requireActivity(), {
            mOnMainListener?.hideAnimation()
            if (it.isEmpty()) {
                imageEmpty.visible()
                recyclerViewLocked.gone()
                btnUnlockAll.gone()
                if (requireActivity() is MainActivity) {
                    (requireActivity() as MainActivity).setShowAction()
                }
            } else {
                imageEmpty.gone()
                recyclerViewLocked.visible()
                btnUnlockAll.visible()
                mAdapter.setEnableSettings(CommonUtils.enableSettings(requireContext()))
                mAdapter.setAppDataList(it)
            }
        })
        mAdapter.setOnAppSelectedListener(object : OnAppSelectedListener {
            override fun onDeleteApp(position: Int, appLockItemItemViewState: AppLockItemItemViewState) {
                // nothing
            }

            override fun onLockApp(position: Int, appLockItemItemViewState: AppLockItemItemViewState) {
                onAppSelected(position, appLockItemItemViewState)
            }
        })
        btnUnlockAll.setOnClickListener {
            if (viewModel.canUnlock(requireContext())) {
                mConfirmUnlockAllDialog?.show()
                dialogLayoutInFragment(mConfirmUnlockAllDialog)
            } else {
                Toasty.showToast(requireContext(), R.string.msg_cannot_unlock_app_all, Toasty.WARNING)
            }
        }
    }

    private fun onAppSelected(position: Int, selectedApp: AppLockItemItemViewState) {
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
        if (selectedApp.isLocked) {
            ApplicationListUtils.instance?.unlockApp(requireContext(), selectedApp)
        } else {
            ApplicationListUtils.instance?.lockApp(requireContext(), selectedApp)
        }
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
        mConfirmUnlockAllDialog?.dismiss()
        mConfirmUnlockSettingsDialog?.dismiss()
        mOnMainListener = null
        super.onDestroy()
    }

    companion object {
        fun newInstance() = LockedFragment()
    }
}