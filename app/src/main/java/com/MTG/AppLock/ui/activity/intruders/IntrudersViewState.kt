package com.MTG.AppLock.ui.activity.intruders

import android.view.View
import com.MTG.AppLock.data.sqllite.model.IntruderPhoto

data class IntrudersViewState(val intrudersPhotoItemViewStateList: MutableList<IntrudersPhotoItemViewState>, val intruderPackageList: MutableList<IntruderPhoto>) {
    fun getEmptyPageVisibility(): Int {
        return if (intrudersPhotoItemViewStateList.isEmpty()) View.VISIBLE else View.INVISIBLE
    }
}