package com.MTG.AppLock.util.extensions

import com.MTG.AppLock.data.database.pattern.PatternDot
import com.MTG.patternlockview.PatternLockView

fun List<PatternLockView.Dot>.convertToPatternDot(): List<PatternDot> {
    val patternDotList: ArrayList<PatternDot> = arrayListOf()
    forEach {
        patternDotList.add(PatternDot(column = it.column, row = it.row))
    }
    return patternDotList
}