package com.MTG.AppLock.data.sqllite.model

import android.text.TextUtils
import com.MTG.AppLock.data.sqllite.ConstantDB
import java.text.DateFormatSymbols
import java.util.*

data class ConfigurationModel(
        val id: Int,
        var name: String = "",
        var packageAppLock: String = "",
        var packageAppLockTemp: String = "",
        var isDefaultSetting: Boolean = true,
        var hourStart: Int = 8,
        var minuteStart: Int = 0,
        var hourEnd: Int = 18,
        var minuteEnd: Int = 0,
        var every: String = "",
        var isActive: Boolean = false
) {
    fun getPackageAppLockList(): List<String> {
        return packageAppLock.split(",")
    }

    fun getPackageAppLockTempList(): List<String> {
        return packageAppLockTemp.split(",")
    }

    fun addPackageAppLock(packageAppLockChild: String) {
        if (!packageAppLock.contains(packageAppLockChild)) {
            packageAppLock = if (TextUtils.isEmpty(packageAppLock)) {
                packageAppLockChild
            } else {
                "$packageAppLock,$packageAppLockChild"
            }
        }
    }

    fun addPackageAppLockTemp(packageAppLockChild: String) {
        if (!packageAppLockTemp.contains(packageAppLockChild)) {
            packageAppLockTemp = if (TextUtils.isEmpty(packageAppLockTemp)) {
                packageAppLockChild
            } else {
                "$packageAppLockTemp,$packageAppLockChild"
            }
        }
    }

    fun removePackageAppLock(packageAppLockChild: String) {
        if (packageAppLock.contains(packageAppLockChild)) {
            var packageAppLockNew = ""
            val list = getPackageAppLockList()
            list.forEach { packageName ->
                if (!TextUtils.equals(packageName, packageAppLockChild)) {
                    packageAppLockNew = if (TextUtils.isEmpty(packageAppLockNew)) {
                        packageName
                    } else {
                        "$packageAppLockNew,$packageName"
                    }
                }
            }
            packageAppLock = packageAppLockNew
        }
    }

    fun removePackageAppLockTemp(packageAppLockChild: String) {
        if (packageAppLockTemp.contains(packageAppLockChild)) {
            var packageAppLockNew = ""
            val list = getPackageAppLockTempList()
            list.forEach { packageName ->
                if (!TextUtils.equals(packageName, packageAppLockChild)) {
                    packageAppLockNew = if (TextUtils.isEmpty(packageAppLockNew)) {
                        packageName
                    } else {
                        "$packageAppLockNew,$packageName"
                    }
                }
            }
            packageAppLockTemp = packageAppLockNew
        }
    }

    fun getEveryList(): List<String> {
        val everyList = mutableListOf<String>()
        every.split(",").forEach {
            if (!TextUtils.isEmpty(it)) {
                everyList.add(it)
            }
        }
        return everyList
    }

    fun getTime(): String {
        return String.format(Locale.getDefault(), "%d:%02d - %d:%02d", hourStart, minuteStart, hourEnd, minuteEnd)
    }

    fun getDate(): String {
        val dateFormat = DateFormatSymbols()
        val shortNameList = mutableListOf<String>()
        dateFormat.shortWeekdays.forEach {
            if (!TextUtils.isEmpty(it)) {
                shortNameList.add(it)
            }
        }
        val builder = StringBuilder()
        val everyList = getEveryList()
        everyList.forEachIndexed { index, name ->
            builder.append(shortNameList[name.toInt()])
            if (index < everyList.size - 1) {
                builder.append(", ")
            }
        }
        return builder.toString()
    }

    fun isCurrentTimeAboutStartEnd(): Boolean {
        if (isDefaultSetting) {
            return true
        } else {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.firstDayOfWeek = Calendar.SUNDAY
            val day = calendar.get(Calendar.DAY_OF_WEEK) - 1
            //
            var daySuccess = false
            val everyList = getEveryList()
            everyList.forEach {
                if (it.toInt() == day) {
                    daySuccess = true
                    return@forEach
                }
            }
            // không đúng ngày thì trả về false luôn
            if (!daySuccess) {
                return false
            }
            //
            calendar.set(Calendar.HOUR_OF_DAY, hourStart)
            calendar.set(Calendar.MINUTE, minuteStart)
            calendar.set(Calendar.SECOND, 0)
            val timeStart = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, hourEnd)
            calendar.set(Calendar.MINUTE, minuteEnd)
            calendar.set(Calendar.SECOND, 59)
            val timeEnd = calendar.timeInMillis
            val timeCurrent = Calendar.getInstance().timeInMillis
            if (timeCurrent in (timeStart + 1) until timeEnd) {
                return true
            }
            return false
        }
    }

    fun isCurrentTimeAboutStartEndReload(): Boolean {
        if (isDefaultSetting) {
            return true
        } else {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.firstDayOfWeek = Calendar.SUNDAY
            val day = calendar.get(Calendar.DAY_OF_WEEK) - 1
            //
            var daySuccess = false
            val everyList = getEveryList()
            everyList.forEach {
                if (it.toInt() == day) {
                    daySuccess = true
                    return@forEach
                }
            }
            // không đúng ngày thì trả về false luôn
            if (!daySuccess) {
                return false
            }
            //
            calendar.set(Calendar.HOUR_OF_DAY, hourStart)
            calendar.set(Calendar.MINUTE, minuteStart)
            calendar.set(Calendar.SECOND, 0)
            val timeStart = calendar.timeInMillis
            calendar.add(Calendar.MINUTE, 1)
            val timeStart2 = calendar.timeInMillis
            //
            calendar.set(Calendar.HOUR_OF_DAY, hourEnd)
            calendar.set(Calendar.MINUTE, minuteEnd)
            calendar.set(Calendar.SECOND, 59)
            val timeEnd = calendar.timeInMillis
            calendar.add(Calendar.MINUTE, 1)
            val timeEnd2 = calendar.timeInMillis
            val timeCurrent = Calendar.getInstance().timeInMillis
            if (timeCurrent in timeStart..timeStart2 || timeCurrent in timeEnd..timeEnd2) {
                return true
            }
            return false
        }
    }

    companion object {
        fun createEmpty(): ConfigurationModel {
            return ConfigurationModel(ConstantDB.CONFIGURATION_ID_CREATE)
        }
    }
}