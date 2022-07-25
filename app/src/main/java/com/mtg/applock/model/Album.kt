package com.mtg.applock.model

import com.mtg.applock.R

data class Album constructor(
        var name: String = "",
        var path: String = "",
        var pathThumbnail: String = "",
        var number: Int = 0,
        var type: Int = 0,
        var tvSize: String = "",
        var resIdThumbnail: Int = R.drawable.ic_default_txt,
        var isSelected: Boolean = false,
        var extension: String = ""
)
