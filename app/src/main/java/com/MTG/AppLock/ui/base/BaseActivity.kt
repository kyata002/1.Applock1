package com.MTG.AppLock.ui.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.MTG.AppLock.ui.activity.password.overlay.activity.settings.OverlayValidationForSettingsActivity
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import dagger.android.support.DaggerAppCompatActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

abstract class BaseActivity<VM : ViewModel> : DaggerAppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    protected lateinit var viewModel: VM

    abstract fun getViewModel(): Class<VM>
    abstract fun getLayoutId(): Int
    abstract fun initViews()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  adjustFontScale(resources.configuration)
        if (getLayoutId() != 0) {
            setContentView(getLayoutId())
        }
       viewModel = ViewModelProvider(this, viewModelFactory).get(getViewModel())
        /*    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
           if (Build.VERSION.SDK_INT >= 21) {
               setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
               window.statusBarColor = Color.TRANSPARENT
           }*/
        initViews()
        initListener()
    }

    open fun initListener() {

    }

    open fun onEventFinishAllActivity() {
        finishAfterTransition()
    }

    //in base activity add this code.
    private fun adjustFontScale(configuration: Configuration) {
        configuration.fontScale = 1.0.toFloat()
        val metrics: DisplayMetrics = resources.displayMetrics
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        metrics.scaledDensity = configuration.fontScale * metrics.density
        configuration.densityDpi = resources.displayMetrics.xdpi.toInt()
        baseContext.resources.updateConfiguration(configuration, metrics)
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if (this is OverlayValidationForSettingsActivity) {
            return
        }
        val appLockerPreferences = AppLockerPreferences(this)
        if (appLockerPreferences.isFinishAllActivity()) {
            onEventFinishAllActivity()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    open fun onEvent(event: BusMessage) {
    }
}