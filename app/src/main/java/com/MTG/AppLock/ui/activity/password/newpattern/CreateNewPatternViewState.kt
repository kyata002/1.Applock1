package com.MTG.AppLock.ui.activity.password.newpattern

import android.content.Context
import com.MTG.AppLock.R
import com.MTG.AppLock.ui.activity.password.newpattern.CreateNewPatternViewModel.PatternEvent.*

data class CreateNewPatternViewState(val patternEvent: CreateNewPatternViewModel.PatternEvent) {

    fun getTitleText(context: Context): String = when (patternEvent) {
        INITIALIZE -> context.getString(R.string.pattern_draw_your_unlock_pattern)
        FIRST_COMPLETED -> context.getString(R.string.pattern_confirm_your_pattern)
        SECOND_COMPLETED -> context.getString(R.string.create_pattern_completed)
        ERROR -> context.getString(R.string.pattern_confirm_your_pattern)
        ERROR_SHORT_FIRST -> context.getString(R.string.pattern_draw_your_unlock_pattern)
        ERROR_SHORT_SECOND -> context.getString(R.string.pattern_confirm_your_pattern)
    }

    fun getMessageText(context: Context): String = when (patternEvent) {
        INITIALIZE -> context.getString(R.string.pattern_connect_at_least_4_dots)
        FIRST_COMPLETED -> context.getString(R.string.redraw_pattern_title)
        SECOND_COMPLETED -> context.getString(R.string.create_pattern_successful)
        ERROR -> context.getString(R.string.recreate_pattern_error)
        ERROR_SHORT_FIRST -> context.getString(R.string.pattern_connect_at_least_4_dots)
        ERROR_SHORT_SECOND -> context.getString(R.string.pattern_connect_at_least_4_dots)
    }

    fun isCreatedNewPattern(): Boolean = patternEvent == SECOND_COMPLETED
}