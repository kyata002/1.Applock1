package com.mtg.applock.api.model

import com.google.gson.annotations.SerializedName

data class PatternResponse(
        @SerializedName("count") val count: Int,

        @SerializedName("results") val patternList: MutableList<PatternItem> = mutableListOf()
)