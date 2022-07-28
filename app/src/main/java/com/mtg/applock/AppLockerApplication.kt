package com.mtg.applock

import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.os.StrictMode
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDex
import com.common.control.manager.AdmobManager
import com.common.control.manager.AppOpenManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.llew.huawei.verifier.LoadedApkHuaWei
import com.mtg.applock.di.component.DaggerAppComponent
import com.mtg.applock.service.AppLockerService
import com.mtg.applock.service.worker.WorkerStarter
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class AppLockerApplication : DaggerApplication() {
    companion object {

        lateinit var appContext: Context

    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerAppComponent.builder().create(this)

    override fun onCreate() {
        super.onCreate()
        AppLockerApplication.appContext = applicationContext
        TrafficStats.setThreadStatsTag(Thread.currentThread().id.toInt())
        ContextCompat.startForegroundService(this, Intent(this, AppLockerService::class.java))
        WorkerStarter.startServiceCheckerWorker(this)
        LoadedApkHuaWei.hookHuaWeiVerifier(baseContext)
        MobileAds.initialize(this) {

        }
        val configuration = RequestConfiguration.Builder().build()
        MobileAds.setRequestConfiguration(configuration)
        allowReads {

        }
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())


        AdmobManager.getInstance()
            .init(this, if (isShowAdsTest()) AdmobManager.getInstance().getDeviceId(this) else "")
        AdmobManager.getInstance().hasAds(hasAds())
//        if (enableAdsResume()) {
//            AppOpenManager.getInstance().init(this, getOpenAppAdId())
//        }
        AppOpenManager.getInstance().init(this, getOpenAppAdId())

        AdmobManager.getInstance().setShowLoadingDialog(isShowDialogLoadingAd())
    }

    private fun isShowDialogLoadingAd(): Boolean {
        return false
    }

    private fun getOpenAppAdId(): String {
        return BuildConfig.open_app
    }

    private fun enableAdsResume(): Boolean {
        return false
    }

    private fun hasAds(): Boolean {
        return true
    }

    private fun isShowAdsTest(): Boolean {
        return false
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun <T> allowReads(block: () -> T): T {
        val oldPolicy = StrictMode.allowThreadDiskReads()
        try {
            return block()
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }
}