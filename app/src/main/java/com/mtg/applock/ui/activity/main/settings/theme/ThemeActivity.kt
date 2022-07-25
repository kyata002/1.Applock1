package com.mtg.applock.ui.activity.main.settings.theme

import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import com.mtg.applock.R
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.model.ThemeModel
import com.mtg.applock.ui.base.BaseActivity
import com.mtg.applock.util.ThemeUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_theme.*

class ThemeActivity : BaseActivity<ThemeViewModel>() {

    override fun getViewModel(): Class<ThemeViewModel> {
        return ThemeViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_theme
    }

    override fun initViews() {

        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        toolbar.setNavigationOnClickListener { onBackPressed() }

        viewPager.adapter = ThemePagerAdapter(this, supportFragmentManager)
        viewPager.offscreenPageLimit = 3
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.theme_list_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if(item.itemId==R.id.themeReset){
//            showResetDialog()
//        }
        return super.onOptionsItemSelected(item)
    }

    private fun showResetDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Restore Theme to Default")
            .setNegativeButton("Cancel") { dialog, which ->
                // Respond to neutral button press
            }
            .setPositiveButton("OK") { dialog, which ->
                resetTheme()
            }.show()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, ThemeActivity::class.java)
        }
    }

    private fun resetTheme() {
        val appLockHelper = AppLockHelper(this)
        val themeDefault = appLockHelper.getThemeDefault()
        val typeTheme = themeDefault?.typeTheme
        themeDefault?.let {
            appLockHelper.deleteTheme(it)
        }
        appLockHelper.getThemeList(ThemeUtils.TYPE_PIN).forEach {
            appLockHelper.deleteTheme(it)
        }
        appLockHelper.getThemeList(ThemeUtils.TYPE_PATTERN).forEach {
            appLockHelper.deleteTheme(it)
        }
        appLockHelper.getThemeList(ThemeUtils.TYPE_WALLPAPER).forEach {
            appLockHelper.deleteTheme(it)
        }
        typeTheme?.let {
            if (typeTheme == ThemeUtils.TYPE_PATTERN) {
                appLockHelper.addTheme(ThemeModel(typeTheme = ThemeUtils.TYPE_PATTERN))
            } else {
                appLockHelper.addTheme(ThemeModel(typeTheme = ThemeUtils.TYPE_PIN, isDeletePadding = true, deletePadding = 30))
            }
        }
        ThemeUtils.initThemeDefault(appLockHelper, false)
    }
}