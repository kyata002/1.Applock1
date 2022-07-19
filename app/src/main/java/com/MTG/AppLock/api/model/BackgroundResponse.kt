package com.MTG.AppLock.api.model

import com.google.gson.annotations.SerializedName

data class BackgroundResponse(
        @SerializedName("count") val count: Int,

        @SerializedName("results") val backgroundList: MutableList<BackgroundItem> = mutableListOf()
)