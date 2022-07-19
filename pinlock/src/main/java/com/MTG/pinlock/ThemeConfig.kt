package com.MTG.pinlock

import java.io.Serializable

class ThemeConfig : Serializable {
    var pointNumber = 2
    var isShowDelete = true
    var pointSelectedList: MutableList<Int> = mutableListOf()

    constructor()

    constructor(pointSelectedList: MutableList<Int>) {
        this.pointSelectedList = pointSelectedList
    }

    constructor(pointNumber: Int, isShowDelete: Boolean, pointSelectedList: MutableList<Int>) {
        this.pointNumber = pointNumber
        this.isShowDelete = isShowDelete
        this.pointSelectedList = pointSelectedList
    }
}