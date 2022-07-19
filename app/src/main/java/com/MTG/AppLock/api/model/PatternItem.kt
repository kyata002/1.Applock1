package com.MTG.AppLock.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PatternItem(
        @SerializedName("id") val id: Int,

        @SerializedName("name") val name: String,

        @SerializedName("background_url") val backgroundUrl: String,

        @SerializedName("thumbnail_url") val thumbnailUrl: String,

        @SerializedName("bitmap_selected_url") val bitmapSelectedUrl: String,

        @SerializedName("bitmap_unselected_url") val bitmapUnselectedUrl: String,

        @SerializedName("line_color") val lineColor: String,

        @SerializedName("text_msg_color") val textMsgColor: String,

        @SerializedName("list_selected_items") val listSelectedItems: String,

        @SerializedName("index") val index: Int
) : Serializable