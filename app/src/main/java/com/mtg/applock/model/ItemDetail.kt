package com.mtg.applock.model

import com.mtg.applock.R
import com.mtg.applock.util.Const
import java.io.Serializable

data class ItemDetail constructor(
        var name: String = "",
        var path: String = "",
        var type: Int = Const.TYPE_FILES,
        var isSelected: Boolean = false,
        var tvSize: String = "",
        var resIdThumbnail: Int = R.drawable.ic_default_txt
) : Serializable