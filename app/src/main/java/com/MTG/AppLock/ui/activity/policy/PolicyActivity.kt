package com.MTG.AppLock.ui.activity.policy

import android.content.Context
import android.content.Intent
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_policy.*

class PolicyActivity : BaseActivity<PolicyViewModel>() {
    override fun getViewModel(): Class<PolicyViewModel> {
        return PolicyViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_policy
    }

    override fun initViews() {
        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
        webView.loadUrl(getString(R.string.policy_url))
        webView.apply {
            settings.javaScriptEnabled = true
        }
    }

    override fun initListener() {

    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PolicyActivity::class.java)
        }
    }
}