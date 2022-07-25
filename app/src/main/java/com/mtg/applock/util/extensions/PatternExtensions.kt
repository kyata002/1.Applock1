package com.mtg.applock.util.extensions

import com.mtg.applock.data.database.pattern.PatternDot
import com.mtg.patternlockview.PatternLockView

fun List<PatternLockView.Dot>.convertToPatternDot(): List<PatternDot> {
    val patternDotList: ArrayList<PatternDot> = arrayListOf()
    forEach {
        patternDotList.add(PatternDot(column = it.column, row = it.row))
    }
    return patternDotList
}