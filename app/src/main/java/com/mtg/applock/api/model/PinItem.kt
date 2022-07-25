package com.mtg.applock.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PinItem(
        @SerializedName("id") val id: Int,

        @SerializedName("name") val name: String,

        @SerializedName("background_url") val backgroundUrl: String,

        @SerializedName("thumbnail_url") val thumbnailUrl: String,

        @SerializedName("bitmap_buttons") val bitmapButtons: MutableList<String>,

        @SerializedName("text_msg_color") val textMsgColor: String,

        @SerializedName("selector_checkbox_color") val selectorCheckboxColor: String,

        @SerializedName("list_selected_items") val listSelectedItems: String,

        @SerializedName("index") val index: Int,

        @SerializedName("is_delete_padding") val isDeletePadding: Boolean,

        @SerializedName("ratio") val ratio: Int,

        @SerializedName("is_show_delete") val isShowDelete: Boolean,

        @SerializedName("point_number") val pointNumber: Int
) : Serializable