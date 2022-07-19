package com.MTG.AppLock.util

import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.data.sqllite.model.ConfigurationModel

object ConfigurationUtils {
    fun initConfigurationDefault(appLockHelper: AppLockHelper) {
        appLockHelper.addConfiguration(ConfigurationModel(0, "", "", "")) // cấu hình 1 là cấu hình ẩn có tác dụng để khi người dùng thay đổi
    }
}