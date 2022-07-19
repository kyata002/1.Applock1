package com.MTG.AppLock.ui.activity.main.settings.theme

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.activity.main.settings.theme.background.BackgroundFragment
import com.MTG.AppLock.ui.activity.main.settings.theme.keypad.KeypadFragment
import com.MTG.AppLock.ui.activity.main.settings.theme.pattern.PatternFragment

class ThemePagerAdapter(private val context: Context, manager: FragmentManager) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var tabs = mutableListOf(getString(R.string.tab_pattern), getString(R.string.tab_keypad), getString(R.string.tab_wallpaper))

    override fun getItem(position: Int): Fragment {
        return when (position) {
            INDEX_PATTERN -> PatternFragment.newInstance()
            INDEX_KEYPAD -> KeypadFragment.newInstance()
            INDEX_BACKGROUND -> BackgroundFragment.newInstance()
            else -> PatternFragment.newInstance()
        }
    }

    override fun getCount(): Int = tabs.size

    override fun getPageTitle(position: Int): CharSequence = tabs[position]
    private fun getString(resId: Int): String {
        return context.getString(resId)
    }

    companion object {
        const val INDEX_PATTERN = 0
        const val INDEX_KEYPAD = 1
        const val INDEX_BACKGROUND = 2
    }
}