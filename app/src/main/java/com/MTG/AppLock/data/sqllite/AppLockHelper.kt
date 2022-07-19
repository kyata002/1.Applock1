package com.MTG.AppLock.data.sqllite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.MTG.AppLock.data.sqllite.model.ConfigurationModel
import com.MTG.AppLock.data.sqllite.model.IntruderPhoto
import com.MTG.AppLock.model.ThemeModel
import com.MTG.AppLock.security.AESHelper
import com.MTG.AppLock.util.ThemeUtils
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import com.MTG.pinlock.ThemeConfig
import java.io.File
import javax.inject.Inject

class AppLockHelper @Inject constructor(val context: Context?) : SQLiteOpenHelper(context, ConstantDB.DB_NAME, null, ConstantDB.DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        // tạo bảng intruder
        val sqlIntruder = "CREATE TABLE " + ConstantDB.TABLE_INTRUDER + " (" + ConstantDB.INTRUDER_ID + " INTEGER PRIMARY KEY, " + ConstantDB.INTRUDER_PACKAGE + " TEXT, " + ConstantDB.INTRUDER_PATH + " TEXT, " + ConstantDB.INTRUDER_TIME + " TEXT );"
        db.execSQL(sqlIntruder)
        // tạo bảng configuration
        val sqlConfiguration = "CREATE TABLE " + ConstantDB.TABLE_CONFIGURATION + " (" + ConstantDB.CONFIGURATION_ID + " INTEGER PRIMARY KEY, " + ConstantDB.CONFIGURATION_NAME + " TEXT, " + ConstantDB.CONFIGURATION_PACKAGE_APP_LOOK + " TEXT, " + ConstantDB.CONFIGURATION_PACKAGE_APP_LOOK_TEMP + " TEXT, " + ConstantDB.CONFIGURATION_IS_DEFAULT + " INTEGER, " + ConstantDB.CONFIGURATION_HOUR_START + " INTEGER, " + ConstantDB.CONFIGURATION_MINUTE_START + " INTEGER, " + ConstantDB.CONFIGURATION_HOUR_END + " INTEGER, " + ConstantDB.CONFIGURATION_MINUTE_END + " INTEGER, " + ConstantDB.CONFIGURATION_EVERY + " TEXT," + ConstantDB.CONFIGURATION_IS_ACTIVE + " INTEGER);"
        db.execSQL(sqlConfiguration)
        // tạo bảng theme
        val sqlTheme = "CREATE TABLE " + ConstantDB.TABLE_THEME + " (" + ConstantDB.THEME_ID + " INTEGER PRIMARY KEY, " + ConstantDB.THEME_TYPE + " TEXT, " + ConstantDB.THEME_BACKGROUND_RES_ID + " INTEGER, " + ConstantDB.THEME_THUMBNAIL_RES_ID + " INTEGER, " + ConstantDB.THEME_BUTTON_RES_LIST + " TEXT, " + ConstantDB.THEME_SELECTED_RES_ID + " INTEGER, " + ConstantDB.THEME_UNSELECTED_RES_ID + " INTEGER, " + ConstantDB.THEME_SELECTED_RES_ID_LIST + " TEXT, " + ConstantDB.THEME_UNSELECTED_RES_ID_LIST + " TEXT, " + ConstantDB.THEME_LINE_COLOR_RES_ID + " INTEGER, " + ConstantDB.THEME_TEXT_COLOR_MESSAGE_RES_ID + " INTEGER, " + ConstantDB.THEME_BUTTON_COLOR_RES_ID + " INTEGER, " + ConstantDB.THEME_SELECTOR_CHECKBOX_COLOR_RES_ID + " INTEGER, " + ConstantDB.THEME_IS_DELETE_PADDING + " INTEGER, " + ConstantDB.THEME_IS_NUMBER_PADDING + " INTEGER, " + ConstantDB.THEME_DELETE_PADDING + " INTEGER, " + ConstantDB.THEME_BACKGROUND_URL + " TEXT, " + ConstantDB.THEME_THUMBNAIL_URL + " TEXT, " + ConstantDB.THEME_BUTTON_URL_LIST + " TEXT, " + ConstantDB.THEME_SELECTED_URL + " TEXT, " + ConstantDB.THEME_UNSELECTED_URL + " TEXT, " + ConstantDB.THEME_LINE_COLOR + " TEXT, " + ConstantDB.THEME_TEXT_COLOR_MESSAGE + " TEXT, " + ConstantDB.THEME_SELECTOR_CHECKBOX_COLOR + " TEXT, " + ConstantDB.THEME_POINT_NUMBER + " INTEGER, " + ConstantDB.THEME_IS_SHOW_DELETE + " INTEGER, " + ConstantDB.THEME_BACKGROUND_DOWNLOAD + " TEXT, " + ConstantDB.THEME_THUMBNAIL_DOWNLOAD + " TEXT, " + ConstantDB.THEME_BUTTON_DOWNLOAD_LIST + " TEXT, " + ConstantDB.THEME_SELECTED_DOWNLOAD + " TEXT, " + ConstantDB.THEME_UNSELECTED_DOWNLOAD + " TEXT, " + ConstantDB.THEME_LAST_UPDATE + " TEXT, " + ConstantDB.THEME_CONFIG + " TEXT );"
        db.execSQL(sqlTheme)
        // tạo bảng scan file
        val sqlScanFile = "CREATE TABLE " + ConstantDB.TABLE_SCAN_FILE + " (" + ConstantDB.SCAN_FILE_ID + " INTEGER PRIMARY KEY, " + ConstantDB.SCAN_FILE_PATH + " TEXT );"
        db.execSQL(sqlScanFile)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (newVersion > oldVersion) {
            context?.let {
                AppLockerPreferences(context).setUpdate(true)

            }
        }
    }

    // intruder
    fun addIntruder(intruderPhoto: IntruderPhoto) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(ConstantDB.INTRUDER_PACKAGE, intruderPhoto.intruderPackage)
        values.put(ConstantDB.INTRUDER_PATH, intruderPhoto.path)
        values.put(ConstantDB.INTRUDER_TIME, intruderPhoto.time)
        db.insertWithOnConflict(ConstantDB.TABLE_INTRUDER, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun updateIntruder(intruderPhoto: IntruderPhoto): Boolean {
        return try {
            val db = writableDatabase
            val values = ContentValues()
            values.put(ConstantDB.INTRUDER_PACKAGE, intruderPhoto.intruderPackage)
            values.put(ConstantDB.INTRUDER_PATH, intruderPhoto.path)
            values.put(ConstantDB.INTRUDER_TIME, intruderPhoto.time)
            db.update(ConstantDB.TABLE_INTRUDER, values, ConstantDB.INTRUDER_ID + "=?", arrayOf(intruderPhoto.id.toString())) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteIntruder(path: String): Boolean {
        return try {
            val db = writableDatabase
            db.delete(ConstantDB.TABLE_INTRUDER, ConstantDB.INTRUDER_PATH + "=?", arrayOf(path)) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteAllIntruder(): Boolean {
        return try {
            val db = writableDatabase
            db.delete(ConstantDB.TABLE_INTRUDER, null, null) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getIntruderPackageList(): MutableList<IntruderPhoto> {
        val intruderPackageList: MutableList<IntruderPhoto> = mutableListOf()
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM " + ConstantDB.TABLE_INTRUDER, null)
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val id = cursor.getInt(cursor.getColumnIndex(ConstantDB.INTRUDER_ID))
                val intruderPackage = cursor.getString(cursor.getColumnIndex(ConstantDB.INTRUDER_PACKAGE))
                val path = cursor.getString(cursor.getColumnIndex(ConstantDB.INTRUDER_PATH))
                val time = cursor.getString(cursor.getColumnIndex(ConstantDB.INTRUDER_TIME))
                try {
                    if (File(path).exists()) {
                        intruderPackageList.add(IntruderPhoto(id, intruderPackage, path, time))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                cursor.moveToNext()
            }
        }
        cursor.close()
        db.close()
        return intruderPackageList
    }

    // configuration list
    fun getConfigurationList(): MutableList<ConfigurationModel> {
        val configurationList: MutableList<ConfigurationModel> = mutableListOf()
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM " + ConstantDB.TABLE_CONFIGURATION, null)
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val id = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_ID))
                if (id != ConstantDB.CONFIGURATION_ID_DEFAULT) {
                    val name = cursor.getString(cursor.getColumnIndex(ConstantDB.CONFIGURATION_NAME))
                    val packageAppLock = cursor.getString(cursor.getColumnIndex(ConstantDB.CONFIGURATION_PACKAGE_APP_LOOK))
                    val packageAppLockTemp = cursor.getString(cursor.getColumnIndex(ConstantDB.CONFIGURATION_PACKAGE_APP_LOOK_TEMP))
                    val isDefault = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_IS_DEFAULT))
                    val hourStart = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_HOUR_START))
                    val minuteStart = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_MINUTE_START))
                    val hourEnd = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_HOUR_END))
                    val minuteEnd = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_MINUTE_END))
                    val every = cursor.getString(cursor.getColumnIndex(ConstantDB.CONFIGURATION_EVERY))
                    val isActive = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_IS_ACTIVE))
                    configurationList.add(ConfigurationModel(id = id, name = name, packageAppLock = packageAppLock, packageAppLockTemp = packageAppLockTemp, isDefaultSetting = isDefault == 1, hourStart = hourStart, minuteStart = minuteStart, hourEnd = hourEnd, minuteEnd = minuteEnd, every = every, isActive = isActive == 1))
                }
                cursor.moveToNext()
            }
        }
        cursor.close()
        db.close()
        return configurationList
    }

    // configuration list
    fun getConfigurationActiveList(): MutableList<ConfigurationModel> {
        val configurationList: MutableList<ConfigurationModel> = mutableListOf()
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM " + ConstantDB.TABLE_CONFIGURATION + " WHERE " + ConstantDB.CONFIGURATION_IS_ACTIVE + " = ?", arrayOf("1")) // 1 is active
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val id = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_ID))
                if (id != ConstantDB.CONFIGURATION_ID_DEFAULT) {
                    val name = cursor.getString(cursor.getColumnIndex(ConstantDB.CONFIGURATION_NAME))
                    val packageAppLock = cursor.getString(cursor.getColumnIndex(ConstantDB.CONFIGURATION_PACKAGE_APP_LOOK))
                    val packageAppLockTemp = cursor.getString(cursor.getColumnIndex(ConstantDB.CONFIGURATION_PACKAGE_APP_LOOK_TEMP))
                    val isDefault = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_IS_DEFAULT))
                    val hourStart = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_HOUR_START))
                    val minuteStart = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_MINUTE_START))
                    val hourEnd = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_HOUR_END))
                    val minuteEnd = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_MINUTE_END))
                    val every = cursor.getString(cursor.getColumnIndex(ConstantDB.CONFIGURATION_EVERY))
                    val isActive = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_IS_ACTIVE))
                    configurationList.add(ConfigurationModel(id = id, name = name, packageAppLock = packageAppLock, packageAppLockTemp = packageAppLockTemp, isDefaultSetting = isDefault == 1, hourStart = hourStart, minuteStart = minuteStart, hourEnd = hourEnd, minuteEnd = minuteEnd, every = every, isActive = isActive == 1))
                }
                cursor.moveToNext()
            }
        }
        cursor.close()
        db.close()
        return configurationList
    }

    fun getConfiguration(configurationId: Int): ConfigurationModel? {
        var configurationModel: ConfigurationModel? = null
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM " + ConstantDB.TABLE_CONFIGURATION + " WHERE " + ConstantDB.CONFIGURATION_ID + " =?", arrayOf(configurationId.toString()))
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_ID))
            val name = cursor.getString(cursor.getColumnIndex(ConstantDB.CONFIGURATION_NAME))
            val packageAppLock = cursor.getString(cursor.getColumnIndex(ConstantDB.CONFIGURATION_PACKAGE_APP_LOOK))
            val packageAppLockTemp = cursor.getString(cursor.getColumnIndex(ConstantDB.CONFIGURATION_PACKAGE_APP_LOOK_TEMP))
            val isDefault = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_IS_DEFAULT))
            val hourStart = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_HOUR_START))
            val minuteStart = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_MINUTE_START))
            val hourEnd = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_HOUR_END))
            val minuteEnd = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_MINUTE_END))
            val every = cursor.getString(cursor.getColumnIndex(ConstantDB.CONFIGURATION_EVERY))
            val isActive = cursor.getInt(cursor.getColumnIndex(ConstantDB.CONFIGURATION_IS_ACTIVE))
            configurationModel = ConfigurationModel(id = id, name = name, packageAppLock = packageAppLock, packageAppLockTemp = packageAppLockTemp, isDefaultSetting = isDefault == 1, hourStart = hourStart, minuteStart = minuteStart, hourEnd = hourEnd, minuteEnd = minuteEnd, every = every, isActive = isActive == 1)
            cursor.moveToNext()
        }
        cursor.close()
        db.close()
        return configurationModel
    }

    fun isConfigurationActive(configurationId: Int): Boolean {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM " + ConstantDB.TABLE_CONFIGURATION + " WHERE " + ConstantDB.CONFIGURATION_ID + " = ? AND " + ConstantDB.CONFIGURATION_IS_ACTIVE + " = ?", arrayOf(configurationId.toString(), "1"))
        val active = if (cursor != null) {
            if (cursor.moveToFirst()) {
                cursor.close()
                true
            } else {
                cursor.close()
                false
            }
        } else {
            false
        }
        db.close()
        return active
    }

    fun isApplicationGroupActive(packageName: String): Boolean {
        var isActive = false
        getConfigurationActiveList().forEach {
            it.getPackageAppLockList().forEach { packageAppLock ->
                if (TextUtils.equals(packageAppLock, packageName) && it.isCurrentTimeAboutStartEnd()) {
                    isActive = true
                    return@forEach
                }
            }
        }
        return isActive
    }

    fun isApplicationLocked(packageName: String): Boolean {
        var isActive = false
        getConfigurationActiveList().forEach {
            if (it.getPackageAppLockList().contains(packageName)) {
                isActive = true
            }
        }
        if (getConfiguration(ConstantDB.CONFIGURATION_ID_DEFAULT)?.packageAppLock?.contains(packageName) == true) {
            isActive = true
        }
        return isActive
    }

    fun addConfiguration(configurationModel: ConfigurationModel): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put(ConstantDB.CONFIGURATION_NAME, configurationModel.name)
        values.put(ConstantDB.CONFIGURATION_PACKAGE_APP_LOOK, configurationModel.packageAppLock)
        values.put(ConstantDB.CONFIGURATION_PACKAGE_APP_LOOK_TEMP, configurationModel.packageAppLockTemp)
        values.put(ConstantDB.CONFIGURATION_IS_DEFAULT, configurationModel.isDefaultSetting)
        values.put(ConstantDB.CONFIGURATION_HOUR_START, configurationModel.hourStart)
        values.put(ConstantDB.CONFIGURATION_MINUTE_START, configurationModel.minuteStart)
        values.put(ConstantDB.CONFIGURATION_HOUR_END, configurationModel.hourEnd)
        values.put(ConstantDB.CONFIGURATION_MINUTE_END, configurationModel.minuteEnd)
        values.put(ConstantDB.CONFIGURATION_EVERY, configurationModel.every)
        values.put(ConstantDB.CONFIGURATION_IS_ACTIVE, configurationModel.isActive)
        val success = db.insertWithOnConflict(ConstantDB.TABLE_CONFIGURATION, null, values, SQLiteDatabase.CONFLICT_REPLACE) > 0
        db.close()
        return success
    }

    fun deleteConfiguration(configurationModel: ConfigurationModel): Boolean {
        return try {
            val db = writableDatabase
            val success = db.delete(ConstantDB.TABLE_CONFIGURATION, ConstantDB.CONFIGURATION_ID + "=?", arrayOf(configurationModel.id.toString())) > 0
            db.close()
            success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun updateConfiguration(configurationModel: ConfigurationModel): Boolean {
        return try {
            val db = writableDatabase
            val values = ContentValues()
            values.put(ConstantDB.CONFIGURATION_ID, configurationModel.id)
            values.put(ConstantDB.CONFIGURATION_NAME, configurationModel.name)
            values.put(ConstantDB.CONFIGURATION_PACKAGE_APP_LOOK, configurationModel.packageAppLock)
            values.put(ConstantDB.CONFIGURATION_PACKAGE_APP_LOOK_TEMP, configurationModel.packageAppLockTemp)
            values.put(ConstantDB.CONFIGURATION_IS_DEFAULT, configurationModel.isDefaultSetting)
            values.put(ConstantDB.CONFIGURATION_HOUR_START, configurationModel.hourStart)
            values.put(ConstantDB.CONFIGURATION_MINUTE_START, configurationModel.minuteStart)
            values.put(ConstantDB.CONFIGURATION_HOUR_END, configurationModel.hourEnd)
            values.put(ConstantDB.CONFIGURATION_MINUTE_END, configurationModel.minuteEnd)
            values.put(ConstantDB.CONFIGURATION_EVERY, configurationModel.every)
            values.put(ConstantDB.CONFIGURATION_IS_ACTIVE, configurationModel.isActive)
            val success = db.update(ConstantDB.TABLE_CONFIGURATION, values, ConstantDB.CONFIGURATION_ID + "=?", arrayOf(configurationModel.id.toString())) > 0
            db.close()
            success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun removePackageName(packageName: String) {
        val configurationList = getConfigurationList()
        configurationList.forEach {
            it.removePackageAppLock(packageName)
            if (TextUtils.isEmpty(it.packageAppLock)) {
                deleteConfiguration(it)
            } else {
                updateConfiguration(it)
            }
        }
    }

    fun deleteTheme(themeModel: ThemeModel): Boolean {
        return try {
            val db = writableDatabase
            val success = db.delete(ConstantDB.TABLE_THEME, ConstantDB.THEME_ID + "=?", arrayOf(themeModel.id.toString())) > 0
            db.close()
            success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun addTheme(themeModel: ThemeModel) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(ConstantDB.THEME_TYPE, themeModel.typeTheme)
        values.put(ConstantDB.THEME_BACKGROUND_RES_ID, themeModel.backgroundResId)
        values.put(ConstantDB.THEME_THUMBNAIL_RES_ID, themeModel.thumbnailResId)
        values.put(ConstantDB.THEME_BUTTON_RES_LIST, themeModel.buttonRes(true))
        values.put(ConstantDB.THEME_SELECTED_RES_ID, themeModel.selectedResId)
        values.put(ConstantDB.THEME_UNSELECTED_RES_ID, themeModel.unselectedResId)
        values.put(ConstantDB.THEME_SELECTED_RES_ID_LIST, themeModel.selectedRes())
        values.put(ConstantDB.THEME_UNSELECTED_RES_ID_LIST, themeModel.unselectedRes())
        values.put(ConstantDB.THEME_LINE_COLOR_RES_ID, themeModel.lineColorResId)
        values.put(ConstantDB.THEME_TEXT_COLOR_MESSAGE_RES_ID, themeModel.textMsgColorResId)
        values.put(ConstantDB.THEME_BUTTON_COLOR_RES_ID, themeModel.colorButtonResId)
        values.put(ConstantDB.THEME_SELECTOR_CHECKBOX_COLOR_RES_ID, themeModel.selectorCheckboxColorResId)
        values.put(ConstantDB.THEME_IS_DELETE_PADDING, themeModel.isDeletePadding)
        values.put(ConstantDB.THEME_IS_NUMBER_PADDING, themeModel.isNumberPadding)
        values.put(ConstantDB.THEME_DELETE_PADDING, themeModel.deletePadding)
        //
        if (themeModel.backgroundUrl.startsWith("https://")) {
            AESHelper.encrypt(themeModel.backgroundUrl)?.let {
                values.put(ConstantDB.THEME_BACKGROUND_URL, it)
            } ?: values.put(ConstantDB.THEME_BACKGROUND_URL, "")
        } else {
            values.put(ConstantDB.THEME_BACKGROUND_URL, themeModel.backgroundUrl)
        }
        //
        if (themeModel.thumbnailUrl.startsWith("https://")) {
            AESHelper.encrypt(themeModel.thumbnailUrl)?.let {
                values.put(ConstantDB.THEME_THUMBNAIL_URL, it)
            } ?: values.put(ConstantDB.THEME_THUMBNAIL_URL, "")
        } else {
            values.put(ConstantDB.THEME_THUMBNAIL_URL, themeModel.thumbnailUrl)
        }
        //
        if (themeModel.buttonRes(false).startsWith("https://")) {
            AESHelper.encrypt(themeModel.buttonRes(false))?.let {
                values.put(ConstantDB.THEME_BUTTON_URL_LIST, it)
            } ?: values.put(ConstantDB.THEME_BUTTON_URL_LIST, "")
        } else {
            values.put(ConstantDB.THEME_BUTTON_URL_LIST, themeModel.buttonRes(false))
        }
        //
        if (themeModel.selectedUrl.startsWith("https://")) {
            AESHelper.encrypt(themeModel.selectedUrl)?.let {
                values.put(ConstantDB.THEME_SELECTED_URL, it)
            } ?: values.put(ConstantDB.THEME_SELECTED_URL, "")
        } else {
            values.put(ConstantDB.THEME_SELECTED_URL, themeModel.selectedUrl)
        }
        //
        if (themeModel.unSelectedUrl.startsWith("https://")) {
            AESHelper.encrypt(themeModel.unSelectedUrl)?.let {
                values.put(ConstantDB.THEME_UNSELECTED_URL, it)
            } ?: values.put(ConstantDB.THEME_UNSELECTED_URL, "")
        } else {
            values.put(ConstantDB.THEME_UNSELECTED_URL, themeModel.unSelectedUrl)
        }
        //
        values.put(ConstantDB.THEME_LINE_COLOR, themeModel.lineColor)
        values.put(ConstantDB.THEME_TEXT_COLOR_MESSAGE, themeModel.textMsgColor)
        values.put(ConstantDB.THEME_SELECTOR_CHECKBOX_COLOR, themeModel.selectorCheckboxColor)
        values.put(ConstantDB.THEME_CONFIG, themeModel.config())
        values.put(ConstantDB.THEME_POINT_NUMBER, themeModel.configPointNumber())
        values.put(ConstantDB.THEME_IS_SHOW_DELETE, themeModel.configShowDelete())
        //
        values.put(ConstantDB.THEME_BACKGROUND_DOWNLOAD, themeModel.backgroundDownload)
        values.put(ConstantDB.THEME_THUMBNAIL_DOWNLOAD, themeModel.thumbnailDownload)
        values.put(ConstantDB.THEME_BUTTON_DOWNLOAD_LIST, themeModel.buttonDownload)
        values.put(ConstantDB.THEME_SELECTED_DOWNLOAD, themeModel.selectedDownload)
        values.put(ConstantDB.THEME_UNSELECTED_DOWNLOAD, themeModel.unselectedDownload)
        values.put(ConstantDB.THEME_LAST_UPDATE, System.currentTimeMillis().toString())
        db.insertWithOnConflict(ConstantDB.TABLE_THEME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun updateTheme(themeModel: ThemeModel): Boolean {
        if (themeModel.id == 0) return false
        return try {
            val db = writableDatabase
            if (hasTheme(themeModel.id)) {
                val values = ContentValues()
                values.put(ConstantDB.THEME_TYPE, themeModel.typeTheme)
                values.put(ConstantDB.THEME_BACKGROUND_RES_ID, themeModel.backgroundResId)
                values.put(ConstantDB.THEME_THUMBNAIL_RES_ID, themeModel.thumbnailResId)
                values.put(ConstantDB.THEME_BUTTON_RES_LIST, themeModel.buttonRes(true))
                values.put(ConstantDB.THEME_SELECTED_RES_ID, themeModel.selectedResId)
                values.put(ConstantDB.THEME_UNSELECTED_RES_ID, themeModel.unselectedResId)
                values.put(ConstantDB.THEME_SELECTED_RES_ID_LIST, themeModel.selectedRes())
                values.put(ConstantDB.THEME_UNSELECTED_RES_ID_LIST, themeModel.unselectedRes())
                values.put(ConstantDB.THEME_LINE_COLOR_RES_ID, themeModel.lineColorResId)
                values.put(ConstantDB.THEME_TEXT_COLOR_MESSAGE_RES_ID, themeModel.textMsgColorResId)
                values.put(ConstantDB.THEME_BUTTON_COLOR_RES_ID, themeModel.colorButtonResId)
                values.put(ConstantDB.THEME_SELECTOR_CHECKBOX_COLOR_RES_ID, themeModel.selectorCheckboxColorResId)
                values.put(ConstantDB.THEME_IS_DELETE_PADDING, themeModel.isDeletePadding)
                values.put(ConstantDB.THEME_IS_NUMBER_PADDING, themeModel.isNumberPadding)
                values.put(ConstantDB.THEME_DELETE_PADDING, themeModel.deletePadding)
                if (themeModel.backgroundUrl.startsWith("https://")) {
                    AESHelper.encrypt(themeModel.backgroundUrl)?.let {
                        values.put(ConstantDB.THEME_BACKGROUND_URL, it)
                    } ?: values.put(ConstantDB.THEME_BACKGROUND_URL, "")
                } else {
                    values.put(ConstantDB.THEME_BACKGROUND_URL, themeModel.backgroundUrl)
                }
                //
                if (themeModel.thumbnailUrl.startsWith("https://")) {
                    AESHelper.encrypt(themeModel.thumbnailUrl)?.let {
                        values.put(ConstantDB.THEME_THUMBNAIL_URL, it)
                    } ?: values.put(ConstantDB.THEME_THUMBNAIL_URL, "")
                } else {
                    values.put(ConstantDB.THEME_THUMBNAIL_URL, themeModel.thumbnailUrl)
                }
                //
                if (themeModel.buttonRes(false).startsWith("https://")) {
                    AESHelper.encrypt(themeModel.buttonRes(false))?.let {
                        values.put(ConstantDB.THEME_BUTTON_URL_LIST, it)
                    } ?: values.put(ConstantDB.THEME_BUTTON_URL_LIST, "")
                } else {
                    values.put(ConstantDB.THEME_BUTTON_URL_LIST, themeModel.buttonRes(false))
                }
                //
                if (themeModel.selectedUrl.startsWith("https://")) {
                    AESHelper.encrypt(themeModel.selectedUrl)?.let {
                        values.put(ConstantDB.THEME_SELECTED_URL, it)
                    } ?: values.put(ConstantDB.THEME_SELECTED_URL, "")
                } else {
                    values.put(ConstantDB.THEME_SELECTED_URL, themeModel.selectedUrl)
                }
                //
                if (themeModel.unSelectedUrl.startsWith("https://")) {
                    AESHelper.encrypt(themeModel.unSelectedUrl)?.let {
                        values.put(ConstantDB.THEME_UNSELECTED_URL, it)
                    } ?: values.put(ConstantDB.THEME_UNSELECTED_URL, "")
                } else {
                    values.put(ConstantDB.THEME_UNSELECTED_URL, themeModel.unSelectedUrl)
                }
                //
                values.put(ConstantDB.THEME_LINE_COLOR, themeModel.lineColor)
                values.put(ConstantDB.THEME_TEXT_COLOR_MESSAGE, themeModel.textMsgColor)
                values.put(ConstantDB.THEME_SELECTOR_CHECKBOX_COLOR, themeModel.selectorCheckboxColor)
                values.put(ConstantDB.THEME_CONFIG, themeModel.config())
                values.put(ConstantDB.THEME_POINT_NUMBER, themeModel.configPointNumber())
                values.put(ConstantDB.THEME_IS_SHOW_DELETE, themeModel.configShowDelete())
                //
                values.put(ConstantDB.THEME_BACKGROUND_DOWNLOAD, themeModel.backgroundDownload)
                values.put(ConstantDB.THEME_THUMBNAIL_DOWNLOAD, themeModel.thumbnailDownload)
                values.put(ConstantDB.THEME_BUTTON_DOWNLOAD_LIST, themeModel.buttonDownload)
                values.put(ConstantDB.THEME_SELECTED_DOWNLOAD, themeModel.selectedDownload)
                values.put(ConstantDB.THEME_UNSELECTED_DOWNLOAD, themeModel.unselectedDownload)
                values.put(ConstantDB.THEME_LAST_UPDATE, System.currentTimeMillis().toString())
                val success = db.update(ConstantDB.TABLE_THEME, values, ConstantDB.THEME_ID + "=?", arrayOf(themeModel.id.toString())) > 0
                db.close()
                success
            } else {
                addTheme(themeModel)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun updateThemeDefault(themeModel: ThemeModel): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put(ConstantDB.THEME_TYPE, themeModel.typeTheme)
        values.put(ConstantDB.THEME_BACKGROUND_RES_ID, themeModel.backgroundResId)
        values.put(ConstantDB.THEME_THUMBNAIL_RES_ID, themeModel.thumbnailResId)
        values.put(ConstantDB.THEME_BUTTON_RES_LIST, themeModel.buttonRes(true))
        values.put(ConstantDB.THEME_SELECTED_RES_ID, themeModel.selectedResId)
        values.put(ConstantDB.THEME_UNSELECTED_RES_ID, themeModel.unselectedResId)
        values.put(ConstantDB.THEME_SELECTED_RES_ID_LIST, themeModel.selectedRes())
        values.put(ConstantDB.THEME_UNSELECTED_RES_ID_LIST, themeModel.unselectedRes())
        values.put(ConstantDB.THEME_LINE_COLOR_RES_ID, themeModel.lineColorResId)
        values.put(ConstantDB.THEME_TEXT_COLOR_MESSAGE_RES_ID, themeModel.textMsgColorResId)
        values.put(ConstantDB.THEME_BUTTON_COLOR_RES_ID, themeModel.colorButtonResId)
        values.put(ConstantDB.THEME_SELECTOR_CHECKBOX_COLOR_RES_ID, themeModel.selectorCheckboxColorResId)
        values.put(ConstantDB.THEME_IS_DELETE_PADDING, themeModel.isDeletePadding)
        values.put(ConstantDB.THEME_IS_NUMBER_PADDING, themeModel.isNumberPadding)
        values.put(ConstantDB.THEME_DELETE_PADDING, themeModel.deletePadding)
//        if (themeModel.backgroundUrl.startsWith("https://")) {
//            AESHelper.encrypt(themeModel.backgroundUrl)?.let {
//                values.put(ConstantDB.THEME_BACKGROUND_URL, it)
//            } ?: values.put(ConstantDB.THEME_BACKGROUND_URL, "")
//        } else {
//            values.put(ConstantDB.THEME_BACKGROUND_URL, themeModel.backgroundUrl)
//        }
//        //
//        if (themeModel.thumbnailUrl.startsWith("https://")) {
//            AESHelper.encrypt(themeModel.thumbnailUrl)?.let {
//                values.put(ConstantDB.THEME_THUMBNAIL_URL, it)
//            } ?: values.put(ConstantDB.THEME_THUMBNAIL_URL, "")
//        } else {
//            values.put(ConstantDB.THEME_THUMBNAIL_URL, themeModel.thumbnailUrl)
//        }
//        //
//        if (themeModel.buttonRes(false).startsWith("https://")) {
//            AESHelper.encrypt(themeModel.buttonRes(false))?.let {
//                values.put(ConstantDB.THEME_BUTTON_URL_LIST, it)
//            } ?: values.put(ConstantDB.THEME_BUTTON_URL_LIST, "")
//        } else {
//            values.put(ConstantDB.THEME_BUTTON_URL_LIST, themeModel.buttonRes(false))
//        }
//        //
//        if (themeModel.selectedUrl.startsWith("https://")) {
//            AESHelper.encrypt(themeModel.selectedUrl)?.let {
//                values.put(ConstantDB.THEME_SELECTED_URL, it)
//            } ?: values.put(ConstantDB.THEME_SELECTED_URL, "")
//        } else {
//            values.put(ConstantDB.THEME_SELECTED_URL, themeModel.selectedUrl)
//        }
//        //
//        if (themeModel.unSelectedUrl.startsWith("https://")) {
//            AESHelper.encrypt(themeModel.unSelectedUrl)?.let {
//                values.put(ConstantDB.THEME_UNSELECTED_URL, it)
//            } ?: values.put(ConstantDB.THEME_UNSELECTED_URL, "")
//        } else {
//            values.put(ConstantDB.THEME_UNSELECTED_URL, themeModel.unSelectedUrl)
//        }
        values.put(ConstantDB.THEME_LINE_COLOR, themeModel.lineColor)
        values.put(ConstantDB.THEME_TEXT_COLOR_MESSAGE, themeModel.textMsgColor)
        values.put(ConstantDB.THEME_SELECTOR_CHECKBOX_COLOR, themeModel.selectorCheckboxColor)
//        values.put(ConstantDB.THEME_CONFIG, themeModel.config())
//        values.put(ConstantDB.THEME_POINT_NUMBER, themeModel.configPointNumber())
//        values.put(ConstantDB.THEME_IS_SHOW_DELETE, themeModel.configShowDelete())
        //
        values.put(ConstantDB.THEME_BACKGROUND_DOWNLOAD, themeModel.backgroundDownload)
        values.put(ConstantDB.THEME_THUMBNAIL_DOWNLOAD, themeModel.thumbnailDownload)
        values.put(ConstantDB.THEME_BUTTON_DOWNLOAD_LIST, themeModel.buttonDownload)
        values.put(ConstantDB.THEME_SELECTED_DOWNLOAD, themeModel.selectedDownload)
        values.put(ConstantDB.THEME_UNSELECTED_DOWNLOAD, themeModel.unselectedDownload)
        values.put(ConstantDB.THEME_LAST_UPDATE, System.currentTimeMillis().toString())
        val success = db.update(ConstantDB.TABLE_THEME, values, ConstantDB.THEME_ID + "=?", arrayOf(ConstantDB.THEME_DEFAULT_ID.toString())) > 0
        db.close()
        return success
    }

    fun updateThemeDefault(backgroundResId: Int, backgroundUrl: String, backgroundDownload: String): Boolean {
        return try {
            val db = writableDatabase
            val values = ContentValues()
            values.put(ConstantDB.THEME_BACKGROUND_RES_ID, backgroundResId)
            if (backgroundUrl.startsWith("https://")) {
                values.put(ConstantDB.THEME_BACKGROUND_URL, backgroundUrl)
                AESHelper.encrypt(backgroundUrl)?.let {
                    values.put(ConstantDB.THEME_BACKGROUND_URL, it)
                } ?: values.put(ConstantDB.THEME_BACKGROUND_URL, "")
            } else {
                values.put(ConstantDB.THEME_BACKGROUND_URL, backgroundUrl)
            }
            values.put(ConstantDB.THEME_BACKGROUND_DOWNLOAD, backgroundDownload)
            if (backgroundResId == 0) {
                values.put(ConstantDB.THEME_THUMBNAIL_RES_ID, 0)
            }
            values.put(ConstantDB.THEME_LAST_UPDATE, System.currentTimeMillis().toString())
            val success = db.update(ConstantDB.TABLE_THEME, values, ConstantDB.THEME_ID + "=?", arrayOf(ConstantDB.THEME_DEFAULT_ID.toString())) > 0 // 1 id theme default
            db.close()
            success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getThemeOnline(backgroundUrl: String, thumbnailUrl: String): ThemeModel? {
        var themeModel: ThemeModel? = null
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM " + ConstantDB.TABLE_THEME + " WHERE " + ConstantDB.THEME_BACKGROUND_URL + " =? and " + ConstantDB.THEME_THUMBNAIL_URL + " =?", arrayOf(backgroundUrl, thumbnailUrl))
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_ID))
            val typeTheme = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_TYPE))
            val backgroundResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_BACKGROUND_RES_ID))
            val thumbnailResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_THUMBNAIL_RES_ID))
            val buttonRes = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_BUTTON_RES_LIST))
            val selectedResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_SELECTED_RES_ID))
            val unselectedResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_UNSELECTED_RES_ID))
            val selectedRes = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_SELECTED_RES_ID_LIST))
            val unselectedRes = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_UNSELECTED_RES_ID_LIST))
            val lineColorResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_LINE_COLOR_RES_ID))
            val textMsgColorResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_TEXT_COLOR_MESSAGE_RES_ID))
            val colorButtonResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_BUTTON_COLOR_RES_ID))
            val selectorCheckboxColorResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_SELECTOR_CHECKBOX_COLOR_RES_ID))
            val isDeletePadding = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_IS_DELETE_PADDING))
            val isNumberPadding = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_IS_NUMBER_PADDING))
            val deletePadding = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_DELETE_PADDING))
            //
            val buttonUrl = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_BUTTON_URL_LIST))
            val selectedUrl = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_SELECTED_URL))
            val unSelectedUrl = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_UNSELECTED_URL))
            val lineColor = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_LINE_COLOR))
            val textMsgColor = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_TEXT_COLOR_MESSAGE))
            val selectorCheckboxColor = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_SELECTOR_CHECKBOX_COLOR))
            //
            val backgroundDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_BACKGROUND_DOWNLOAD))
            val thumbnailDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_THUMBNAIL_DOWNLOAD))
            val buttonDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_BUTTON_DOWNLOAD_LIST))
            val selectedDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_SELECTED_DOWNLOAD))
            val unselectedDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_UNSELECTED_DOWNLOAD))
            //
            val config = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_CONFIG))
            val pointNumber = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_POINT_NUMBER))
            val isShowDelete = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_IS_SHOW_DELETE))
            //
            val lastUpdate = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_LAST_UPDATE))
            val configList = mutableListOf<Int>()
            config.split(",").forEach {
                if (!TextUtils.isEmpty(it)) {
                    try {
                        configList.add(it.toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val selectedResIdList = mutableListOf<Int>()
            selectedRes.split(",").forEach {
                if (!TextUtils.isEmpty(it)) {
                    try {
                        selectedResIdList.add(it.toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val unselectedResIdList = mutableListOf<Int>()
            unselectedRes.split(",").forEach {
                if (!TextUtils.isEmpty(it)) {
                    try {
                        unselectedResIdList.add(it.toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val buttonResList = mutableListOf<Int>()
            buttonRes.split(",").forEach {
                if (!TextUtils.isEmpty(it)) {
                    try {
                        buttonResList.add(it.toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val buttonUrlListDecode = mutableListOf<String>()
            buttonUrl.split(",").forEach {
                if (!TextUtils.isEmpty(it)) {
                    try {
                        buttonUrlListDecode.add(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            var selectedUrlDecode = selectedUrl
            if (!TextUtils.isEmpty(selectedUrl)) {
                selectedUrlDecode = AESHelper.decrypt(selectedUrl) ?: selectedUrl
            }
            var unselectedUrlDecode = unSelectedUrl
            if (!TextUtils.isEmpty(unSelectedUrl)) {
                unselectedUrlDecode = AESHelper.decrypt(unSelectedUrl) ?: unSelectedUrl
            }
            themeModel = ThemeModel(
                    id = id,
                    typeTheme = typeTheme,
                    backgroundUrl = backgroundUrl, // bản chất truyền vào đã giải mã rồi ko cần giải mã lần nữa
                    thumbnailUrl = thumbnailUrl,  // bản chất truyền vào đã giải mã rồi ko cần giải mã lần nữa
                    buttonUrlList = buttonUrlListDecode,
                    selectedUrl = selectedUrlDecode,
                    unSelectedUrl = unselectedUrlDecode,
                    lineColor = lineColor,
                    textMsgColor = textMsgColor,
                    selectorCheckboxColor = selectorCheckboxColor,
                    themeConfig = ThemeConfig(pointNumber, isShowDelete == 1, configList),
                    backgroundResId = backgroundResId,
                    thumbnailResId = thumbnailResId,
                    buttonResList = buttonResList,
                    selectedResId = selectedResId,
                    unselectedResId = unselectedResId,
                    selectedResIdList = selectedResIdList,
                    unselectedResIdList = unselectedResIdList,
                    lineColorResId = lineColorResId,
                    textMsgColorResId = textMsgColorResId,
                    colorButtonResId = colorButtonResId,
                    selectorCheckboxColorResId = selectorCheckboxColorResId,
                    isDeletePadding = isDeletePadding == 1,
                    isNumberPadding = isNumberPadding == 1,
                    deletePadding = deletePadding,
                    backgroundDownload = backgroundDownload,
                    thumbnailDownload = thumbnailDownload,
                    buttonDownload = buttonDownload,
                    selectedDownload = selectedDownload,
                    unselectedDownload = unselectedDownload,
                    lastUpdate = lastUpdate
            )
        }
        cursor.close()
        db.close()
        return themeModel
    }

    fun getThemeDefault(): ThemeModel? {
        var themeModel: ThemeModel? = null
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM " + ConstantDB.TABLE_THEME + " WHERE " + ConstantDB.THEME_ID + " =?", arrayOf(ConstantDB.THEME_DEFAULT_ID.toString()))
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_ID))
            val typeTheme = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_TYPE))
            val backgroundResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_BACKGROUND_RES_ID))
            val thumbnailResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_THUMBNAIL_RES_ID))
            val buttonRes = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_BUTTON_RES_LIST))
            val selectedResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_SELECTED_RES_ID))
            val unselectedResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_UNSELECTED_RES_ID))
            val selectedRes = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_SELECTED_RES_ID_LIST))
            val unselectedRes = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_UNSELECTED_RES_ID_LIST))
            val lineColorResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_LINE_COLOR_RES_ID))
            val textMsgColorResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_TEXT_COLOR_MESSAGE_RES_ID))
            val colorButtonResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_BUTTON_COLOR_RES_ID))
            val selectorCheckboxColorResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_SELECTOR_CHECKBOX_COLOR_RES_ID))
            val isDeletePadding = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_IS_DELETE_PADDING))
            val isNumberPadding = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_IS_NUMBER_PADDING))
            val deletePadding = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_DELETE_PADDING))
            //
            val backgroundUrl = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_BACKGROUND_URL))
            val thumbnailUrl = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_THUMBNAIL_URL))
            val buttonUrl = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_BUTTON_URL_LIST))
            val selectedUrl = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_SELECTED_URL))
            val unSelectedUrl = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_UNSELECTED_URL))
            val lineColor = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_LINE_COLOR))
            val textMsgColor = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_TEXT_COLOR_MESSAGE))
            val selectorCheckboxColor = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_SELECTOR_CHECKBOX_COLOR))
            //
            val backgroundDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_BACKGROUND_DOWNLOAD))
            val thumbnailDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_THUMBNAIL_DOWNLOAD))
            val buttonDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_BUTTON_DOWNLOAD_LIST))
            val selectedDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_SELECTED_DOWNLOAD))
            val unselectedDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_UNSELECTED_DOWNLOAD))
            //
            val config = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_CONFIG))
            val pointNumber = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_POINT_NUMBER))
            val isShowDelete = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_IS_SHOW_DELETE))
            //
            val lastUpdate = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_LAST_UPDATE))
            val configList = mutableListOf<Int>()
            config.split(",").forEach {
                if (!TextUtils.isEmpty(it)) {
                    try {
                        configList.add(it.toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val selectedResIdList = mutableListOf<Int>()
            selectedRes.split(",").forEach {
                if (!TextUtils.isEmpty(it)) {
                    try {
                        selectedResIdList.add(it.toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val unselectedResIdList = mutableListOf<Int>()
            unselectedRes.split(",").forEach {
                if (!TextUtils.isEmpty(it)) {
                    try {
                        unselectedResIdList.add(it.toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val buttonResList = mutableListOf<Int>()
            buttonRes.split(",").forEach {
                if (!TextUtils.isEmpty(it)) {
                    try {
                        buttonResList.add(it.toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val buttonUrlListDecode = mutableListOf<String>()
            if (!TextUtils.isEmpty(buttonUrl)) {
                val buttonUrlDecode = AESHelper.decrypt(buttonUrl)
                buttonUrlDecode.split(",").forEach {
                    if (!TextUtils.isEmpty(it)) {
                        try {
                            buttonUrlListDecode.add(it)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            var backgroundUrlDecode = backgroundUrl
            if (!TextUtils.isEmpty(backgroundUrl)) {
                backgroundUrlDecode = AESHelper.decrypt(backgroundUrl) ?: backgroundUrl
            }
            var thumbnailUrlDecode = thumbnailUrl
            if (!TextUtils.isEmpty(thumbnailUrl)) {
                thumbnailUrlDecode = AESHelper.decrypt(thumbnailUrl) ?: thumbnailUrl
            }
            var selectedUrlDecode = selectedUrl
            if (!TextUtils.isEmpty(selectedUrl)) {
                selectedUrlDecode = AESHelper.decrypt(selectedUrl) ?: selectedUrl
            }
            var unselectedUrlDecode = unSelectedUrl
            if (!TextUtils.isEmpty(unSelectedUrl)) {
                unselectedUrlDecode = AESHelper.decrypt(unSelectedUrl) ?: unSelectedUrl
            }
            themeModel = ThemeModel(
                    id = id,
                    typeTheme = typeTheme,
                    backgroundUrl = backgroundUrlDecode,
                    thumbnailUrl = thumbnailUrlDecode,
                    buttonUrlList = buttonUrlListDecode,
                    selectedUrl = selectedUrlDecode,
                    unSelectedUrl = unselectedUrlDecode,
                    lineColor = lineColor,
                    textMsgColor = textMsgColor,
                    selectorCheckboxColor = selectorCheckboxColor,
                    themeConfig = ThemeConfig(pointNumber, isShowDelete == 1, configList),
                    backgroundResId = backgroundResId,
                    thumbnailResId = thumbnailResId,
                    buttonResList = buttonResList,
                    selectedResId = selectedResId,
                    unselectedResId = unselectedResId,
                    selectedResIdList = selectedResIdList,
                    unselectedResIdList = unselectedResIdList,
                    lineColorResId = lineColorResId,
                    textMsgColorResId = textMsgColorResId,
                    colorButtonResId = colorButtonResId,
                    selectorCheckboxColorResId = selectorCheckboxColorResId,
                    isDeletePadding = isDeletePadding == 1,
                    isNumberPadding = isNumberPadding == 1,
                    deletePadding = deletePadding,
                    backgroundDownload = backgroundDownload,
                    thumbnailDownload = thumbnailDownload,
                    buttonDownload = buttonDownload,
                    selectedDownload = selectedDownload,
                    unselectedDownload = unselectedDownload,
                    lastUpdate = lastUpdate
            )
        }
        cursor.close()
        db.close()
        return themeModel
    }

    fun getThemeLastId(): Int {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT  * FROM " + ConstantDB.TABLE_THEME, null)
        cursor.moveToLast()
        val lastId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_ID))
        cursor.close()
        db.close()
        return lastId
    }

    private fun hasTheme(id: Int): Boolean {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM " + ConstantDB.TABLE_THEME + " WHERE " + ConstantDB.THEME_ID + " =?", arrayOf(id.toString()))
        val hasTheme = cursor == null
        cursor.close()
        db.close()
        return hasTheme
    }

    fun getThemeList(typeTheme: Int): MutableList<ThemeModel> {
        val themeList = mutableListOf<ThemeModel>()
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM " + ConstantDB.TABLE_THEME + " WHERE " + ConstantDB.THEME_TYPE + " =? and " + ConstantDB.THEME_ID + " !=" + ConstantDB.THEME_DEFAULT_ID, arrayOf(typeTheme.toString()))
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val id = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_ID))
                val backgroundResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_BACKGROUND_RES_ID))
                val thumbnailResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_THUMBNAIL_RES_ID))
                val buttonRes = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_BUTTON_RES_LIST))
                val selectedResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_SELECTED_RES_ID))
                val unselectedResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_UNSELECTED_RES_ID))
                val selectedRes = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_SELECTED_RES_ID_LIST))
                val unselectedRes = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_UNSELECTED_RES_ID_LIST))
                val lineColorResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_LINE_COLOR_RES_ID))
                val textMsgColorResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_TEXT_COLOR_MESSAGE_RES_ID))
                val colorButtonResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_BUTTON_COLOR_RES_ID))
                val selectorCheckboxColorResId = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_SELECTOR_CHECKBOX_COLOR_RES_ID))
                val isDeletePadding = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_IS_DELETE_PADDING))
                val isNumberPadding = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_IS_NUMBER_PADDING))
                val deletePadding = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_DELETE_PADDING))
                //
                val backgroundUrl = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_BACKGROUND_URL))
                val thumbnailUrl = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_THUMBNAIL_URL))
                val buttonUrl = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_BUTTON_URL_LIST))
                val selectedUrl = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_SELECTED_URL))
                val unSelectedUrl = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_UNSELECTED_URL))
                val lineColor = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_LINE_COLOR))
                val textMsgColor = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_TEXT_COLOR_MESSAGE))
                val selectorCheckboxColor = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_SELECTOR_CHECKBOX_COLOR))
                //
                val backgroundDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_BACKGROUND_DOWNLOAD))
                val thumbnailDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_THUMBNAIL_DOWNLOAD))
                val buttonDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_BUTTON_DOWNLOAD_LIST))
                val selectedDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_SELECTED_DOWNLOAD))
                val unselectedDownload = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_UNSELECTED_DOWNLOAD))
                //
                val config = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_CONFIG))
                val pointNumber = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_POINT_NUMBER))
                val isShowDelete = cursor.getInt(cursor.getColumnIndex(ConstantDB.THEME_IS_SHOW_DELETE))
                //
                val lastUpdate = cursor.getString(cursor.getColumnIndex(ConstantDB.THEME_LAST_UPDATE))
                val configList = mutableListOf<Int>()
                config.split(",").forEach {
                    if (!TextUtils.isEmpty(it)) {
                        try {
                            configList.add(it.toInt())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                val selectedResIdList = mutableListOf<Int>()
                selectedRes.split(",").forEach {
                    if (!TextUtils.isEmpty(it)) {
                        try {
                            selectedResIdList.add(it.toInt())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                val unselectedResIdList = mutableListOf<Int>()
                unselectedRes.split(",").forEach {
                    if (!TextUtils.isEmpty(it)) {
                        try {
                            unselectedResIdList.add(it.toInt())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                val buttonResList = mutableListOf<Int>()
                buttonRes.split(",").forEach {
                    if (!TextUtils.isEmpty(it)) {
                        try {
                            buttonResList.add(it.toInt())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                val buttonUrlListDecode = mutableListOf<String>()
                if (!TextUtils.isEmpty(buttonUrl)) {
                    val buttonUrlDecode = AESHelper.decrypt(buttonUrl)
                    buttonUrlDecode.split(",").forEach {
                        if (!TextUtils.isEmpty(it)) {
                            try {
                                buttonUrlListDecode.add(it)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                var backgroundUrlDecode = backgroundUrl
                if (!TextUtils.isEmpty(backgroundUrl)) {
                    backgroundUrlDecode = AESHelper.decrypt(backgroundUrl) ?: backgroundUrl
                }
                var thumbnailUrlDecode = thumbnailUrl
                if (!TextUtils.isEmpty(thumbnailUrl)) {
                    thumbnailUrlDecode = AESHelper.decrypt(thumbnailUrl) ?: thumbnailUrl
                }
                var selectedUrlDecode = selectedUrl
                if (!TextUtils.isEmpty(selectedUrl)) {
                    selectedUrlDecode = AESHelper.decrypt(selectedUrl) ?: selectedUrl
                }
                var unselectedUrlDecode = unSelectedUrl
                if (!TextUtils.isEmpty(unSelectedUrl)) {
                    unselectedUrlDecode = AESHelper.decrypt(unSelectedUrl) ?: unSelectedUrl
                }
                val themeModel = ThemeModel(
                        id = id,
                        typeTheme = typeTheme,
                        backgroundUrl = backgroundUrlDecode,
                        thumbnailUrl = thumbnailUrlDecode,
                        buttonUrlList = buttonUrlListDecode,
                        selectedUrl = selectedUrlDecode,
                        unSelectedUrl = unselectedUrlDecode,
                        lineColor = lineColor,
                        textMsgColor = textMsgColor,
                        selectorCheckboxColor = selectorCheckboxColor,
                        themeConfig = ThemeConfig(pointNumber, isShowDelete == 1, configList),
                        backgroundResId = backgroundResId,
                        thumbnailResId = thumbnailResId,
                        buttonResList = buttonResList,
                        selectedResId = selectedResId,
                        unselectedResId = unselectedResId,
                        selectedResIdList = selectedResIdList,
                        unselectedResIdList = unselectedResIdList,
                        lineColorResId = lineColorResId,
                        textMsgColorResId = textMsgColorResId,
                        colorButtonResId = colorButtonResId,
                        selectorCheckboxColorResId = selectorCheckboxColorResId,
                        isDeletePadding = isDeletePadding == 1,
                        isNumberPadding = isNumberPadding == 1,
                        deletePadding = deletePadding,
                        backgroundDownload = backgroundDownload,
                        thumbnailDownload = thumbnailDownload,
                        buttonDownload = buttonDownload,
                        selectedDownload = selectedDownload,
                        unselectedDownload = unselectedDownload,
                        lastUpdate = lastUpdate
                )
                themeList.add(themeModel)
                cursor.moveToNext()
            }
        }
        cursor.close()
        db.close()
        return themeList
    }

    fun checkExistThemeDownloaded() {
        deletePattern()
        deletePin()
        deleteWallpaper()
    }

    private fun deletePattern() {
        val patternList = getThemeList(ThemeUtils.TYPE_PATTERN)
        patternList.forEach {
            if (!TextUtils.isEmpty(it.backgroundDownload)) {
                if (!File(it.backgroundDownload).exists()) {
                    deleteTheme(it)
                }
            }
            if (!TextUtils.isEmpty(it.thumbnailDownload)) {
                if (!File(it.thumbnailDownload).exists()) {
                    deleteTheme(it)
                }
            }
            if (!TextUtils.isEmpty(it.selectedDownload)) {
                if (!File(it.selectedDownload).exists()) {
                    deleteTheme(it)
                }
            }
            if (!TextUtils.isEmpty(it.unselectedDownload)) {
                if (!File(it.unselectedDownload).exists()) {
                    deleteTheme(it)
                }
            }
            if (!TextUtils.isEmpty(it.buttonDownload)) {
                it.backgroundDownload.split(",").forEach { path ->
                    if (!TextUtils.isEmpty(path)) {
                        if (!File(path).exists()) {
                            deleteTheme(it)
                        }
                    }
                }
            }
        }
    }

    private fun deletePin() {
        val pinList = getThemeList(ThemeUtils.TYPE_PIN)
        pinList.forEach {
            if (!TextUtils.isEmpty(it.backgroundDownload)) {
                if (!File(it.backgroundDownload).exists()) {
                    deleteTheme(it)
                }
            }
            if (!TextUtils.isEmpty(it.thumbnailDownload)) {
                if (!File(it.thumbnailDownload).exists()) {
                    deleteTheme(it)
                }
            }
            if (!TextUtils.isEmpty(it.buttonDownload)) {
                it.backgroundDownload.split(",").forEach { path ->
                    if (!TextUtils.isEmpty(path)) {
                        if (!File(path).exists()) {
                            deleteTheme(it)
                        }
                    }
                }
            }
        }
    }

    private fun deleteWallpaper() {
        val pinList = getThemeList(ThemeUtils.TYPE_WALLPAPER)
        pinList.forEach {
            if (!TextUtils.isEmpty(it.backgroundDownload)) {
                if (!File(it.backgroundDownload).exists()) {
                    deleteTheme(it)
                }
            }
            if (!TextUtils.isEmpty(it.thumbnailDownload)) {
                if (!File(it.thumbnailDownload).exists()) {
                    deleteTheme(it)
                }
            }
        }
    }

    // scan file
    fun addScanFile(path: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(ConstantDB.SCAN_FILE_PATH, path)
        db.insertWithOnConflict(ConstantDB.TABLE_SCAN_FILE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun getScanFileList(): MutableList<String> {
        val scanFileList: MutableList<String> = mutableListOf()
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM " + ConstantDB.TABLE_SCAN_FILE, null)
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val path = cursor.getString(cursor.getColumnIndex(ConstantDB.SCAN_FILE_PATH))
                scanFileList.add(path)
                cursor.moveToNext()
            }
        }
        cursor.close()
        db.close()
        return scanFileList
    }

    fun deleteScanFile(): Boolean {
        return try {
            val db = writableDatabase
            db.delete(ConstantDB.TABLE_SCAN_FILE, null, null) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}