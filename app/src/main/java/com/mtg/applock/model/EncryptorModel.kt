package com.mtg.applock.model

import com.mtg.applock.util.Const
import java.io.Serializable

class EncryptorModel : Serializable {
    var type: Int = Const.TYPE_DECODE
    var itemDetailList: MutableList<ItemDetail> = mutableListOf()

    constructor(type: Int, itemDetailList: MutableList<ItemDetail>) {
        this.type = type
        this.itemDetailList = itemDetailList
    }
}