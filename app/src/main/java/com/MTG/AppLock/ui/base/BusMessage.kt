package com.MTG.AppLock.ui.base

class BusMessage {
    var message: String = ""
    var data: String = ""

    constructor(message: String) {
        this.message = message
    }

    constructor(message: String, data: String) {
        this.message = message
        this.data = data
    }
}
