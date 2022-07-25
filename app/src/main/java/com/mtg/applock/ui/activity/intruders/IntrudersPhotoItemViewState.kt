package com.mtg.applock.ui.activity.intruders

import java.io.File
import java.io.Serializable

data class IntrudersPhotoItemViewState(val file: File) : Serializable {
    var time: String = ""
    var day: String = ""
    var packageApp: String = ""
    var filePath: String = file.absolutePath
    var id: Int = 0
    var intruderTime = ""
}