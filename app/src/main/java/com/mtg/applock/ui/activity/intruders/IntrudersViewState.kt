package com.mtg.applock.ui.activity.intruders

import android.view.View
import com.mtg.applock.data.sqllite.model.IntruderPhoto

data class IntrudersViewState(val intrudersPhotoItemViewStateList: MutableList<IntrudersPhotoItemViewState>, val intruderPackageList: MutableList<IntruderPhoto>) {
    fun getEmptyPageVisibility(): Int {
        return if (intrudersPhotoItemViewStateList.isEmpty()) View.VISIBLE else View.INVISIBLE
    }
}