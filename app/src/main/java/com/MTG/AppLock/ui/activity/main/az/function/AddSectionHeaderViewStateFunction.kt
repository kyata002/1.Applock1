package com.MTG.AppLock.ui.activity.main.az.function

import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemBaseViewState
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemHeaderViewState
import com.MTG.AppLock.ui.activity.main.az.model.AppLockItemItemViewState
import com.MTG.AppLock.util.Const
import io.reactivex.functions.Function
import java.text.Collator
import java.util.*
import kotlin.collections.HashSet

class AddSectionHeaderViewStateFunction(private val context: Context) : Function<MutableList<AppLockItemItemViewState>, MutableList<AppLockItemBaseViewState>> {
    private val recommendedPackages = HashSet<String>().apply {
        add("com.whatsapp")
        add("com.instagram.android")
        add(Const.CH_PLAY_PACKAGE)
        add("com.facebook.orca")
        add("com.facebook.katana")
        add("com.google.android.apps.messaging")
        add("org.telegram.messenger")
        add("com.twitter.android")
        add("com.google.android.apps.photos")
        add("com.google.android.apps.docs")
        add(Const.SETTINGS_PACKAGE)
        add("com.google.android.gm")
        //
        add("com.samsung.android.app.contacts")
        add("com.samsung.android.contacts")
        add("com.android.contacts")
        add("com.zing.zalo")
        add("com.skype.raider")
        add("com.sec.android.gallery3d")
    }

    fun containsRecommended(packageName: String): Boolean {
        return recommendedPackages.contains(packageName)
    }

    override fun apply(appItemList: MutableList<AppLockItemItemViewState>): MutableList<AppLockItemBaseViewState> {
        val nameAppList = mutableListOf<String>()
        val resultAppList = mutableListOf<AppLockItemItemViewState>()

        /**
         * Hash for faster access
         */
        val appListHash = HashMap<String, AppLockItemItemViewState>()
        //
        appItemList.forEach {
            nameAppList.add(it.appName())
        }
        Collections.sort(nameAppList, Collator.getInstance())
        nameAppList.forEach {
            appItemList.forEach { appItem ->
                if (TextUtils.equals(it, appItem.appName()) && !contains(resultAppList, appItem.appData.parsePackageName())) {
                    resultAppList.add(appItem)
                }
            }
        }
        //
        resultAppList.forEach { appItem ->
            appListHash[appItem.appData.parsePackageName()] = appItem
        }
        //
        val resultList = arrayListOf<AppLockItemBaseViewState>()

        /**
         * recommended header view state
         */
        val recommendedHeaderViewState = AppLockItemHeaderViewState(R.string.text_sensitive_applications)
        resultList.add(recommendedHeaderViewState)
        //
        val recommendedList: ArrayList<String> = arrayListOf()
        recommendedPackages.forEach {
            if (isPackageInstalled(it, context.packageManager)) {
                recommendedList.add(it)
            }
        }
        val nameList = mutableListOf<String>()
        recommendedList.forEach {
            appListHash[it]?.let { app ->
                nameList.add(app.appName())
            }
        }
        Collections.sort(nameList, Collator.getInstance())
        nameList.forEach { name ->
            recommendedList.forEach { packages ->
                if (TextUtils.equals(name, appListHash[packages]?.appName())) {
                    appListHash[packages]?.let { app ->
                        resultList.add(app)
                    }
                }
            }
        }
        /**
         * Ads view state
         */
        /**
         * All apps header view state
         */
        val allAppsHeaderViewState = AppLockItemHeaderViewState(R.string.text_general)
        resultList.add(allAppsHeaderViewState)
        resultAppList.forEach {
            if (resultList.contains(it).not()) {
                resultList.add(it)
            }
        }
        return resultList
    }

    /**
     *  function not
     */
    fun applyV2(appItemList: MutableList<AppLockItemItemViewState>): MutableList<AppLockItemBaseViewState> {
        val nameAppList = mutableListOf<String>()
        val resultAppList = mutableListOf<AppLockItemItemViewState>()

        /**
         * Hash for faster access
         */
        val appListHash = HashMap<String, AppLockItemItemViewState>()
        //
        appItemList.forEach {
            nameAppList.add(it.appName())
        }
        Collections.sort(nameAppList, Collator.getInstance())
        nameAppList.forEach {
            appItemList.forEach { appItem ->
                if (TextUtils.equals(it, appItem.appName()) && !contains(resultAppList, appItem.appData.parsePackageName())) {
                    resultAppList.add(appItem)
                }
            }
        }
        //
        resultAppList.forEach { appItem ->
            appListHash[appItem.appData.parsePackageName()] = appItem
        }
        //
        val resultList = arrayListOf<AppLockItemBaseViewState>()
        resultAppList.forEach {
            if (resultList.contains(it).not()) {
                resultList.add(it)
            }
        }
        return resultList
    }

    private fun contains(resultAppList: MutableList<AppLockItemItemViewState>, packageName: String): Boolean {
        resultAppList.forEach {
            if (TextUtils.equals(it.appData.parsePackageName(), packageName)) {
                return true
            }
        }
        return false
    }

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageGids(packageName)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}