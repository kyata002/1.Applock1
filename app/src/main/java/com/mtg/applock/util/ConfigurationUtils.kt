package com.mtg.applock.util

import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.data.sqllite.model.ConfigurationModel

object ConfigurationUtils {
    fun initConfigurationDefault(appLockHelper: AppLockHelper) {
        appLockHelper.addConfiguration(ConfigurationModel(0, "", "", "")) // cấu hình 1 là cấu hình ẩn có tác dụng để khi người dùng thay đổi
    }
}