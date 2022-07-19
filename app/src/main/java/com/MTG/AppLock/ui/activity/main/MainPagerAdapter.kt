package com.MTG.AppLock.ui.activity.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.activity.main.az.AzFragment
import com.MTG.AppLock.ui.activity.main.locked.LockedFragment
import com.MTG.AppLock.ui.activity.main.personal.PersonalFragment

class MainPagerAdapter(private val context: Context, manager: FragmentManager) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var tabs = mutableListOf(getString(R.string.tab_a_z), getString(R.string.tab_locked), getString(R.string.tab_private))

    override fun getItem(position: Int): Fragment {
        return when (position) {
            INDEX_AZ -> AzFragment.newInstance()
            INDEX_LOCKED -> LockedFragment.newInstance()
            INDEX_PERSONAL -> PersonalFragment.newInstance()
            else -> AzFragment.newInstance()
        }
    }

    override fun getCount(): Int = tabs.size

    override fun getPageTitle(position: Int): CharSequence = tabs[position]
    private fun getString(resId: Int): String {
        return context.getString(resId)
    }

    companion object {
        const val INDEX_AZ = 0
        const val INDEX_LOCKED = 1
        const val INDEX_PERSONAL = 2
    }
}