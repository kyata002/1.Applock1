package com.MTG.AppLock.model

import android.text.TextUtils
import com.MTG.AppLock.util.ThemeUtils
import com.MTG.pinlock.ThemeConfig
import java.io.Serializable

data class ThemeModel(
        var id: Int = 0,
        // online
        var typeTheme: Int = ThemeUtils.TYPE_WALLPAPER,
        var backgroundUrl: String = "",  // background url
        var thumbnailUrl: String = "", // thumbnail url
        var buttonUrlList: MutableList<String> = mutableListOf(),
        var selectedUrl: String = "",
        var unSelectedUrl: String = "",
        var lineColor: String = "",
        var textMsgColor: String = "",
        var selectorCheckboxColor: String = "",
        var themeConfig: ThemeConfig? = null,

        // offline
        var backgroundResId: Int = 0, // background with res id
        var thumbnailResId: Int = 0, // thumbnail with res id
        var buttonResList: MutableList<Int> = mutableListOf(),
        var selectedResId: Int = 0,
        var unselectedResId: Int = 0,
        var selectedResIdList: MutableList<Int> = mutableListOf(),
        var unselectedResIdList: MutableList<Int> = mutableListOf(),
        var lineColorResId: Int = 0,
        var textMsgColorResId: Int = 0,
        var colorButtonResId: Int = 0,
        var selectorCheckboxColorResId: Int = 0,
        var isDeletePadding: Boolean = false,
        var isNumberPadding: Boolean = true,
        var deletePadding: Int = 0,

        // download
        var backgroundDownload: String = "",
        var thumbnailDownload: String = "",
        var buttonDownload: String = "",
        var selectedDownload: String = "",
        var unselectedDownload: String = "",

        //
        var lastUpdate: String = "",

        //
        var exist: Boolean = false

) : Serializable {

    fun buttonRes(isRes: Boolean): String {
        val builder = StringBuilder()
        if (isRes) {
            buttonResList.forEach {
                builder.append(it).append(",")
            }
        } else {
            buttonUrlList.forEach {
                builder.append(it).append(",")
            }
        }
        return builder.toString()
    }

    fun selectedRes(): String {
        val builder = StringBuilder()
        selectedResIdList.forEach {
            builder.append(it).append(",")
        }
        return builder.toString()
    }

    fun unselectedRes(): String {
        val builder = StringBuilder()
        unselectedResIdList.forEach {
            builder.append(it).append(",")
        }
        return builder.toString()
    }

    fun buttonDownloadList(): MutableList<String> {
        val buttonDownloadList = mutableListOf<String>()
        buttonDownload.split(",").forEach {
            if (!TextUtils.isEmpty(it)) {
                buttonDownloadList.add(it)
            }
        }
        return buttonDownloadList
    }

    fun config(): String {
        val builder = StringBuilder()
        themeConfig?.pointSelectedList?.forEach {
            builder.append(it).append(",")
        }
        return builder.toString()
    }

    fun configPointNumber(): Int {
        return themeConfig?.pointNumber ?: 2
    }

    fun configShowDelete(): Int {
        if (themeConfig == null) return 2
        return if (themeConfig?.isShowDelete == true) {
            1
        } else {
            0
        }
    }
}