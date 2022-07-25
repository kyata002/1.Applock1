package com.mtg.applock.api.model

import com.google.gson.annotations.SerializedName

data class RepresentationResponse(
        @SerializedName("patterns") val patternList: MutableList<PatternItem> = mutableListOf(),

        @SerializedName("pins") val pinList: MutableList<PatternItem> = mutableListOf(),

        @SerializedName("backgrounds") val backgroundList: MutableList<PatternItem> = mutableListOf()
)