package com.MTG.AppLock.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BackgroundItem(
        @SerializedName("id") val id: Int,

        @SerializedName("name") val name: String,

        @SerializedName("index") val index: Int,

        @SerializedName("image_url") val imageUrl: String,

        @SerializedName("image_thumbnail_url") val thumbnailUrl: String


) : Serializable

