package com.mtg.applock.util

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateUtils
import com.mtg.applock.R
import com.mtg.applock.model.Album
import com.mtg.applock.model.ItemDetail
import com.mtg.applock.util.file.EncryptionFileManager
import com.mtg.applock.util.file.FilePathHelper
import com.mtg.applock.util.file.MediaHelper
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

object LoadDataUtils {
    const val DURATION = "duration"
    const val DATA = "_data"

    // image
    object Image {
        @Suppress("DEPRECATION")
        fun findImageAlbums(context: Context): MutableList<Album> {
            val folderMap: HashMap<Int, Album> = HashMap()
            context.contentResolver?.query(MediaStore.Files.getContentUri("external"), null, MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?", arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString()), null).use { cursor ->
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val pathThumbnail: String = cursor.getString(cursor.getColumnIndex(DATA))
                        val parentId: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.PARENT))
                        val parentPath = pathThumbnail.substring(0, pathThumbnail.lastIndexOf("/"))
                        var albumName = FilePathHelper.getName(parentPath)
                        if (pathThumbnail.startsWith(getExternalAbsolutePath()) && File(pathThumbnail).length().toDouble() != 0.0 && MediaHelper.isSupportImage(pathThumbnail)) {
                            albumName = albumName ?: "No Folder"
                            if (!folderMap.containsKey(parentId)) {
                                val album = Album(name = albumName, path = parentPath, pathThumbnail = pathThumbnail, number = 1, type = Const.TYPE_IMAGES)
                                folderMap[parentId] = album
                            } else {
                                val album = folderMap[parentId]
                                album?.let {
                                    it.number++
                                    folderMap[parentId] = it
                                }
                            }
                        }
                    }
                }
            }
            return ArrayList(folderMap.values)
        }

        @Suppress("DEPRECATION")
        fun loadFullImage(context: Context): MutableList<ItemDetail> {
            val itemDetailList = mutableListOf<ItemDetail>()
            context.contentResolver?.query(MediaStore.Files.getContentUri("external"), null, MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?", arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString()), "_display_name DESC").use { cursor ->
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val pathThumbnail: String = cursor.getString(cursor.getColumnIndex(DATA))
                        if (pathThumbnail.startsWith(getExternalAbsolutePath()) && File(pathThumbnail).length().toDouble() != 0.0 && MediaHelper.isSupportImage(pathThumbnail)) {
                            val image = ItemDetail(name = File(pathThumbnail).name, path = pathThumbnail, type = Const.TYPE_IMAGES)
                            itemDetailList.add(image)
                        }
                    }
                    cursor.close()
                }
            }
            return itemDetailList
        }

        @Suppress("DEPRECATION")
        fun findImageAlbumsSub(context: Context): MutableList<Album> {
            val folderMap: HashMap<Int, Album> = HashMap()
            context.contentResolver?.query(MediaStore.Files.getContentUri("external"), null, MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?", arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString()), null).use { cursor ->
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val pathThumbnail: String = cursor.getString(cursor.getColumnIndex(DATA))
                        val parentId: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.PARENT))
                        val parentPath = pathThumbnail.substring(0, pathThumbnail.lastIndexOf("/"))
                        var albumName = FilePathHelper.getName(parentPath)
                        if (pathThumbnail.startsWith(getExternalAbsolutePath()) && File(pathThumbnail).length().toDouble() != 0.0 && MediaHelper.isSupport(pathThumbnail, MediaHelper.IMAGE_SUPPORT_FORMAT_SUB)) {
                            albumName = albumName ?: "No Folder"
                            if (!folderMap.containsKey(parentId)) {
                                val album = Album(name = albumName, path = parentPath, pathThumbnail = pathThumbnail, number = 1, type = Const.TYPE_IMAGES)
                                folderMap[parentId] = album
                            } else {
                                val album = folderMap[parentId]
                                album?.let {
                                    it.number++
                                    folderMap[parentId] = it
                                }
                            }
                        }
                    }
                }
            }
            return ArrayList(folderMap.values)
        }

        @Suppress("DEPRECATION")
        fun loadFullImageSub(context: Context): MutableList<ItemDetail> {
            val itemDetailList = mutableListOf<ItemDetail>()
            context.contentResolver?.query(MediaStore.Files.getContentUri("external"), null, MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?", arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString()), "_display_name DESC").use { cursor ->
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val pathThumbnail: String = cursor.getString(cursor.getColumnIndex(DATA))
                        if (pathThumbnail.startsWith(getExternalAbsolutePath()) && File(pathThumbnail).length().toDouble() != 0.0 && MediaHelper.isSupport(pathThumbnail, MediaHelper.IMAGE_SUPPORT_FORMAT_SUB)) {
                            val image = ItemDetail(name = File(pathThumbnail).name, path = pathThumbnail, type = Const.TYPE_IMAGES)
                            itemDetailList.add(image)
                        }
                    }
                    cursor.close()
                }
            }
            return itemDetailList
        }
    }

    private fun getExternalAbsolutePath(): String {
        var appPath="";
      //  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    //        appPath= AppLockerApplication.appContext.getExternalFilesDir(null)?.absolutePath.toString()
    //    }
     //   else {
            appPath = Environment.getExternalStorageDirectory().absolutePath.toString()
    //    }
        return appPath

    }

    object Video {
        @Suppress("DEPRECATION")
        fun findVideoAlbums(context: Context): MutableList<Album> {
            val folderMap: HashMap<Int, Album> = HashMap()
            context.contentResolver?.query(MediaStore.Files.getContentUri("external"), null, MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?", arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()), null).use { cursor ->
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val pathThumbnail: String = cursor.getString(cursor.getColumnIndex(DATA))
                        val parentId: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.PARENT))
                        val parentPath = pathThumbnail.substring(0, pathThumbnail.lastIndexOf("/"))
                        var albumName = FilePathHelper.getName(parentPath)
                        if (pathThumbnail.startsWith(getExternalAbsolutePath()) && File(pathThumbnail).length().toDouble() != 0.0 && MediaHelper.isSupportVideo(pathThumbnail)) {
                            albumName = albumName ?: "No Folder"
                            if (!folderMap.containsKey(parentId)) {
                                val album = Album(name = albumName, path = parentPath, pathThumbnail = pathThumbnail, number = 1, type = Const.TYPE_VIDEOS)
                                folderMap[parentId] = album
                            } else {
                                val album = folderMap[parentId]
                                album?.let {
                                    it.number++
                                    folderMap[parentId] = it
                                }
                            }
                        }
                    }
                }
            }
            return ArrayList(folderMap.values)
        }

        @Suppress("DEPRECATION")
        fun loadFullVideo(context: Context): MutableList<ItemDetail> {
            val itemDetailList = mutableListOf<ItemDetail>()
            context.contentResolver?.query(MediaStore.Files.getContentUri("external"), null, MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?", arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()), "_display_name DESC").use { cursor ->
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val pathThumbnail: String = cursor.getString(cursor.getColumnIndex(DATA))
                        if (pathThumbnail.startsWith(getExternalAbsolutePath()) && File(pathThumbnail).length().toDouble() != 0.0 && MediaHelper.isSupportVideo(pathThumbnail)) {
                            try {
                                val duration = cursor.getString(cursor.getColumnIndex(DURATION))
                                val image = ItemDetail(name = File(pathThumbnail).name, path = pathThumbnail, tvSize = DateUtils.formatElapsedTime(duration.toLong() / 1000), type = Const.TYPE_VIDEOS)
                                itemDetailList.add(image)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    cursor.close()
                }
            }
            return itemDetailList
        }
    }

    object Audio {
        @Suppress("DEPRECATION")
        fun findAudioAlbums(context: Context): MutableList<Album> {
            val folderMap: HashMap<Int, Album> = HashMap()
            context.contentResolver?.query(MediaStore.Files.getContentUri("external"), null, MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO + " AND " + DURATION + " > 3000", null, null).use { cursor ->
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val pathThumbnail: String = cursor.getString(cursor.getColumnIndex(DATA))
                        val parentId: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.PARENT))
                        val parentPath = pathThumbnail.substring(0, pathThumbnail.lastIndexOf("/"))
                        var albumName = FilePathHelper.getName(parentPath)
                        if (pathThumbnail.startsWith(getExternalAbsolutePath()) && File(pathThumbnail).length().toDouble() != 0.0 && MediaHelper.isSupportAudio(pathThumbnail)) {
                            albumName = albumName ?: "No Folder"
                            if (!folderMap.containsKey(parentId)) {
                                val album = Album(name = albumName, path = parentPath, resIdThumbnail = R.drawable.ic_music_default, number = 1, type = Const.TYPE_AUDIOS)
                                folderMap[parentId] = album
                            } else {
                                val album = folderMap[parentId]
                                album?.let {
                                    it.number++
                                    folderMap[parentId] = it
                                }
                            }
                        }
                    }
                }
            }
            return ArrayList(folderMap.values)
        }

        @Suppress("DEPRECATION")
        fun loadFullAudio(context: Context): MutableList<ItemDetail> {
            val itemDetailList = mutableListOf<ItemDetail>()
            context.contentResolver?.query(MediaStore.Files.getContentUri("external"), null, MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO + " AND " + DURATION + " > 3000", null, null).use { cursor ->
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val pathThumbnail: String = cursor.getString(cursor.getColumnIndex(DATA))
                        if (pathThumbnail.startsWith(getExternalAbsolutePath()) && File(pathThumbnail).length().toDouble() != 0.0 && MediaHelper.isSupportAudio(pathThumbnail)) {
                            try {
                                val duration = cursor.getString(cursor.getColumnIndex(DURATION))
                                val image = ItemDetail(name = File(pathThumbnail).name, path = pathThumbnail, tvSize = DateUtils.formatElapsedTime(duration.toLong() / 1000), resIdThumbnail = R.drawable.ic_music_default, type = Const.TYPE_AUDIOS)

                                itemDetailList.add(image)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    cursor.close()
                }
            }
            return itemDetailList
        }
    }

    object File {
        fun loadFullFile(): MutableList<ItemDetail> {
            return EncryptionFileManager.getListFileWithType(type = Const.TYPE_FILES, isDecode = false, allHide = false)
        }

        @Suppress("DEPRECATION")
        fun loadFileAlbums(): MutableList<Album> {
            return EncryptionFileManager.getListAlbumWithType(type = Const.TYPE_FILES, folder = File(getExternalAbsolutePath()), allHide = false, progressCallback = null)
        }
    }
}