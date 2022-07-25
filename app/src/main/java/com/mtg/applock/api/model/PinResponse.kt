package com.mtg.applock.api.model

import com.google.gson.annotations.SerializedName

data class PinResponse(
        @SerializedName("count") val count: Int,

        @SerializedName("results") val pinList: MutableList<PinItem> = mutableListOf()
)