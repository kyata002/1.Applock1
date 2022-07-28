package com.mtg.applock.util.file

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.text.format.DateUtils
import com.mtg.applock.AppLockerApplication
import com.mtg.applock.R
import com.mtg.applock.data.sqllite.AppLockHelper
import com.mtg.applock.model.Album
import com.mtg.applock.model.ItemDetail
import com.mtg.applock.util.Const
import com.mtg.applock.util.ThemeUtils
import com.mtg.pinlock.extension.decodeBase64
import com.mtg.pinlock.extension.encodeBase64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.util.*

object EncryptionFileManager {
    const val EXTERNAL_FOLDER_PRIVATE = "/.HN/applock/private"
    private const val EXTERNAL_FOLDER_THEME = "/.HN/applock/theme"
    const val EXTERNAL_FOLDER_INTRUDERS = "/HN/applock/intruders"
    private const val EXTERNAL_FOLDER_IMAGES = "/Images"
    private const val EXTERNAL_FOLDER_VIDEOS = "/Videos"
    private const val EXTERNAL_FOLDER_AUDIOS = "/Audios"
    private const val EXTERNAL_FOLDER_FILES = "/Files"

    private fun getExternalDirectory(subFolder: String): File {
        var appPath="";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            appPath=AppLockerApplication.appContext.getExternalFilesDir(null).toString()+subFolder
        }
        else {
            appPath = Environment.getExternalStorageDirectory().toString() + subFolder
        }
        val folder = File(appPath)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }



    private fun getFolderWithPath(pathFolder: String): File {
        val folder = File(pathFolder)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }

    @Suppress("DEPRECATION")
    private fun listFilesForFolder(folder: File, itemDetailList: MutableList<ItemDetail>, type: Int, isFull: Boolean, isDecode: Boolean, extension: String, allHide: Boolean, progressCallback: ProgressCallback?) {
        val list = folder.listFiles()
        list?.let {
            for (fileEntry in it) {
                val hide = if (allHide) {
                    true
                } else {
                    !fileEntry.isHidden
                }
                if (hide) {
                    if (fileEntry.isDirectory) {
                        if (isFull) {
                            listFilesForFolder(folder = fileEntry, itemDetailList = itemDetailList, type = type, isFull = isFull, isDecode = isDecode, extension, allHide, progressCallback)
                        }
                    } else {
                        val path = fileEntry.absolutePath
                        val isSupport = if (isDecode) {
                            MediaHelper.isSupport(checkDecodeBase64(path, type), type)
                        } else {
                            MediaHelper.isSupport(path, type)
                        }
                        if (isSupport) {
                            val file = File(path)
                            if (file.length() != 0L) {
                                if (type == Const.TYPE_VIDEOS || type == Const.TYPE_AUDIOS) {
                                    try {
                                        val mediaMetadataRetriever = MediaMetadataRetriever()
                                        mediaMetadataRetriever.setDataSource(file.absolutePath)
                                        val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                        if (path.startsWith(getExternalAbsolutePath())) {

                                            itemDetailList.add(ItemDetail(name = file.name, path = path, tvSize = DateUtils.formatElapsedTime((duration?.toLong()
                                                    ?: 0 / 1000)/1000), resIdThumbnail = getResIdThumbnail(path, type), type = type))

                                            progressCallback?.progress(itemDetailList.size)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    if (path.startsWith(getExternalAbsolutePath())) {
                                        if (TextUtils.isEmpty(extension)) {
                                            itemDetailList.add(ItemDetail(name = file.name, path = path, tvSize = FilePathHelper.getReadableFileSize(file.length().toDouble()), resIdThumbnail = getResIdThumbnail(path, type), type = type))
                                            progressCallback?.progress(itemDetailList.size)
                                        } else {
                                            if (TextUtils.equals(extension, MediaHelper.EXTENSION_OTHER_SUPPORT_FORMAT)) {
                                                if (MediaHelper.FILES_SUPPORT_FORMAT_OTHER.contains(FilePathHelper.getExtension(path))) {
                                                    itemDetailList.add(ItemDetail(name = file.name, path = path, tvSize = FilePathHelper.getReadableFileSize(file.length().toDouble()), resIdThumbnail = getResIdThumbnail(path, type), type = type))
                                                    progressCallback?.progress(itemDetailList.size)
                                                }
                                            } else {
                                                if (TextUtils.equals(FilePathHelper.getExtension(path), extension)) {
                                                    itemDetailList.add(ItemDetail(name = file.name, path = path, tvSize = FilePathHelper.getReadableFileSize(file.length().toDouble()), resIdThumbnail = getResIdThumbnail(path, type), type = type))
                                                    progressCallback?.progress(itemDetailList.size)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getResIdThumbnail(path: String, type: Int): Int {
        when (type) {
            Const.TYPE_IMAGES -> {
            }
            Const.TYPE_VIDEOS -> {
            }
            Const.TYPE_AUDIOS -> {
                return R.drawable.ic_avatar_audio
            }
            Const.TYPE_FILES -> {
                val pathTemp = path.toLowerCase(Locale.getDefault())
                when {
                    pathTemp.endsWith(".docx") -> {
                        return R.drawable.ic_default_word
                    }
                    pathTemp.endsWith(".html") -> {
                        return R.drawable.ic_default_html
                    }
                    pathTemp.endsWith(".pdf") -> {
                        return R.drawable.ic_default_pdf
                    }
                    pathTemp.endsWith(".ppt") -> {
                        return R.drawable.ic_default_ppt
                    }
                    pathTemp.endsWith(".rar") -> {
                        return R.drawable.ic_default_rar
                    }
                    pathTemp.endsWith(".txt") -> {
                        return R.drawable.ic_default_txt
                    }
                    pathTemp.endsWith(".xlsx") -> {
                        return R.drawable.ic_default_excel
                    }
                    else -> {
                        return R.drawable.ic_default_unknow
                    }
                }
            }
        }
        return R.drawable.ic_avatar_audio
    }

    fun getListFileWithType(type: Int, isDecode: Boolean, allHide: Boolean): MutableList<ItemDetail> {
        return getListFileWithType("", type, isDecode, "", allHide, null)
    }

    fun getListFileWithType(pathFolder: String, type: Int, isDecode: Boolean, extension: String, allHide: Boolean, progressCallback: ProgressCallback?): MutableList<ItemDetail> {
        val isFull: Boolean
        val path = if (TextUtils.isEmpty(pathFolder)) {
            isFull = true
            getExternalAbsolutePath()
        } else {
            isFull = !TextUtils.isEmpty(extension)
            pathFolder
        }
        val folder = getFolderWithPath(path)
        val itemDetailList: MutableList<ItemDetail> = mutableListOf()
        listFilesForFolder(folder, itemDetailList, type, isFull, isDecode, extension, allHide, progressCallback)
        return itemDetailList
    }

    fun getListAlbumWithType(type: Int, progressCallback: ProgressCallback?): MutableList<Album> {
        val folder = getFolderWithType(type)
        val albumList: MutableList<Album> = mutableListOf()
        listAlbumForFolder(folder, albumList, type, true, progressCallback)
        return albumList
    }

    fun getListAlbumWithType(type: Int, folder: File, allHide: Boolean, progressCallback: ProgressCallback?): MutableList<Album> {
        val albumList: MutableList<Album> = mutableListOf()
        listAlbumForFolder(folder, albumList, type, allHide, progressCallback)
        return albumList
    }

    fun getFolderWithType(type: Int): File {
        return getFolderWithType(type, null)
    }

    private fun getFolderWithType(type: Int, name: String?): File {
        val subName = if (!TextUtils.isEmpty(name)) {
            name
        } else {
            ""
        }
        return when (type) {
            Const.TYPE_IMAGES -> {
                getExternalDirectory(EXTERNAL_FOLDER_PRIVATE + EXTERNAL_FOLDER_IMAGES + subName)
            }
            Const.TYPE_VIDEOS -> {
                getExternalDirectory(EXTERNAL_FOLDER_PRIVATE + EXTERNAL_FOLDER_VIDEOS + subName)
            }
            Const.TYPE_AUDIOS -> {
                getExternalDirectory(EXTERNAL_FOLDER_PRIVATE + EXTERNAL_FOLDER_AUDIOS + subName)
            }
            Const.TYPE_FILES -> {
                getExternalDirectory(EXTERNAL_FOLDER_PRIVATE + EXTERNAL_FOLDER_FILES + subName)
            }
            else -> {
                getExternalDirectory(EXTERNAL_FOLDER_PRIVATE + EXTERNAL_FOLDER_IMAGES + subName)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun listAlbumForFolder(folder: File, albumList: MutableList<Album>, type: Int, allHide: Boolean, progressCallback: ProgressCallback?) {
        val list = folder.listFiles()
        list?.let {
            for (fileEntry in it) {
                val hide = if (allHide) {
                    true
                } else {
                    !fileEntry.isHidden
                }
                if (hide) {
                    if (fileEntry.isDirectory) {
                        listAlbumForFolder(fileEntry, albumList, type, allHide, progressCallback)
                    } else {
                        val pathThumbnail = fileEntry.absolutePath
                        if (MediaHelper.isSupport(checkDecodeBase64(pathThumbnail, type), type)) {
                            val file = File(pathThumbnail)
                            if (file.length() != 0L) {
                                if (pathThumbnail.startsWith(getExternalAbsolutePath())) {
                                    when (type) {
                                        Const.TYPE_FILES -> {
                                            var indexFile = -1
                                            var extensionFile = ""
                                            MediaHelper.FILES_SUPPORT_FORMAT.forEachIndexed { index, extension ->
                                                if (TextUtils.equals(FilePathHelper.getExtension(pathThumbnail), extension)) {
                                                    indexFile = index
                                                    extensionFile = extension
                                                    return@forEachIndexed
                                                }
                                            }
                                            if (!TextUtils.isEmpty(extensionFile) && indexFile != -1) {
                                                if (albumList.isEmpty()) {
                                                    if (MediaHelper.FILES_SUPPORT_FORMAT_OTHER.contains(extensionFile)) {
                                                        albumList.add(Album(name = MediaHelper.FILES_SUPPORT_FORMAT_NAME[indexFile], resIdThumbnail = getResIdThumbnail(pathThumbnail, type), number = 1, type = type, extension = MediaHelper.EXTENSION_OTHER_SUPPORT_FORMAT))
                                                    } else {
                                                        albumList.add(Album(name = MediaHelper.FILES_SUPPORT_FORMAT_NAME[indexFile], resIdThumbnail = getResIdThumbnail(pathThumbnail, type), number = 1, type = type, extension = extensionFile))
                                                    }
                                                    progressCallback?.progress(getNumber(albumList))
                                                } else {
                                                    var albumTemp: Album? = null
                                                    for (album in albumList) {
                                                        if (TextUtils.equals(album.extension, MediaHelper.EXTENSION_OTHER_SUPPORT_FORMAT)) {
                                                            if (MediaHelper.FILES_SUPPORT_FORMAT_OTHER.contains(extensionFile)) {
                                                                albumTemp = album
                                                            }
                                                        } else {
                                                            if (TextUtils.equals(album.extension, extensionFile)) {
                                                                albumTemp = album
                                                            }
                                                        }
                                                    }
                                                    albumTemp?.let { album ->
                                                        album.number += 1
                                                    }
                                                            ?: addNewAlbum(albumList = albumList, extensionFile = extensionFile, indexFile = indexFile, pathThumbnail = pathThumbnail, type = type)
                                                    progressCallback?.progress(getNumber(albumList))
                                                }
                                            }
                                        }
                                        Const.TYPE_AUDIOS -> {
                                            val fileParent = file.parentFile
                                            fileParent?.let { parent ->
                                                if (parent.absolutePath.startsWith(getExternalAbsolutePath())) {
                                                    if (albumList.isEmpty()) {
                                                        albumList.add(Album(name = parent.name, path = parent.absolutePath, resIdThumbnail = R.drawable.ic_album_audio, number = 1, type = type))
                                                        progressCallback?.progress(getNumber(albumList))
                                                    } else {
                                                        var albumTemp: Album? = null
                                                        for (album in albumList) {
                                                            if (TextUtils.equals(album.path, parent.absolutePath)) {
                                                                albumTemp = album
                                                            }
                                                        }
                                                        albumTemp?.let { album ->
                                                            album.number += 1
                                                        }
                                                                ?: albumList.add(Album(name = parent.name, path = parent.absolutePath, resIdThumbnail = R.drawable.ic_avatar_audio, number = 1, type = type))
                                                        progressCallback?.progress(getNumber(albumList))
                                                    }
                                                }
                                            }
                                        }
                                        else -> {
                                            val fileParent = file.parentFile
                                            fileParent?.let { parent ->
                                                if (parent.absolutePath.startsWith(getExternalAbsolutePath())) {
                                                    if (albumList.isEmpty()) {
                                                        albumList.add(Album(parent.name, parent.absolutePath, pathThumbnail, 1, type))
                                                        progressCallback?.progress(getNumber(albumList))
                                                    } else {
                                                        var albumTemp: Album? = null
                                                        for (album in albumList) {
                                                            if (TextUtils.equals(album.path, parent.absolutePath)) {
                                                                albumTemp = album
                                                            }
                                                        }
                                                        albumTemp?.let { album ->
                                                            album.number += 1
                                                        }
                                                                ?: albumList.add(Album(parent.name, parent.absolutePath, pathThumbnail, 1, type))
                                                        progressCallback?.progress(getNumber(albumList))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addNewAlbum(albumList: MutableList<Album>, extensionFile: String, indexFile: Int, pathThumbnail: String, type: Int) {
        if (MediaHelper.FILES_SUPPORT_FORMAT_OTHER.contains(extensionFile)) {
            albumList.add(Album(name = MediaHelper.FILES_SUPPORT_FORMAT_NAME[indexFile], resIdThumbnail = getResIdThumbnail(pathThumbnail, type), number = 1, type = type, extension = MediaHelper.EXTENSION_OTHER_SUPPORT_FORMAT))
        } else {
            albumList.add(Album(name = MediaHelper.FILES_SUPPORT_FORMAT_NAME[indexFile], resIdThumbnail = getResIdThumbnail(pathThumbnail, type), number = 1, type = type, extension = extensionFile))
        }
    }

    private fun getNumber(albumList: MutableList<Album>): Int {
        var number = 0
        albumList.forEach {
            number += it.number
        }
        return number
    }

    fun getPathFromName(name: String): String {
        val folder = getExternalDirectory(EXTERNAL_FOLDER_PRIVATE)
        return File(folder, name).absolutePath
    }

    fun encodeBase64(context: Context, pathList: MutableList<String>, type: Int, appLockHelper: AppLockHelper, encodeCallback: EncodeCallback?) {
        encodeCallback?.start()
        var index = 0
        val size = pathList.size
        while (index < size) {
            val pathNew = encodeBase64(context, pathList[index], type, appLockHelper)
            index++
            encodeCallback?.processing(index, pathNew)
            Thread.sleep(getSleep(size))
        }
        encodeCallback?.complete()
    }

    private fun getSleep(size: Int): Long {
        if (size > 100) return 0L
        if (size > 40) return 50L
        return 100L
    }

    private fun encodeBase64(context: Context, path: String, type: Int, appLockHelper: AppLockHelper): String {
        when (type) {
            Const.TYPE_AUDIOS, Const.TYPE_FILES -> {
                val fileFrom = File(path)
                val folderTo = getFolderWithType(type, fileFrom.parent)
                val fileTo = FilePathHelper.generateFileName(fileFrom.name, folderTo)
                // no encode
                fileTo?.let {
                    rename(fileFrom, it)
                    MediaScannerConnection.scanFile(context, arrayOf(fileFrom.absolutePath), null) { _: String?, _: Uri? -> }
                    MediaScannerConnection.scanFile(context, arrayOf(it.absolutePath), null) { _: String?, _: Uri? -> }
                    //
                    appLockHelper.addScanFile(fileFrom.absolutePath)
                    appLockHelper.addScanFile(it.absolutePath)
                    return it.toString()
                } ?: return ""
            }
            else -> {

                val fileFrom = File(path)
                val folderTo = getFolderWithType(type, fileFrom.parent)
                // encode
                val fileTo = FilePathHelper.generateFileNameEncode(fileFrom.name.encodeBase64(), folderTo)

                fileTo?.let {
                    var result = rename(fileFrom, it)
                    MediaScannerConnection.scanFile(context, arrayOf(fileFrom.absolutePath), null) { _: String?, _: Uri? -> }
                    MediaScannerConnection.scanFile(context, arrayOf(it.absolutePath), null) { _: String?, _: Uri? -> }
                    //
                    appLockHelper.addScanFile(fileFrom.absolutePath)
                    appLockHelper.addScanFile(it.absolutePath)
                    return it.toString()
                } ?: return ""
            }
        }
    }

    fun decodeBase64(context: Context, pathList: MutableList<String>, type: Int, appLockHelper: AppLockHelper, encodeCallback: EncodeCallback?) {
        encodeCallback?.start()
        var index = 0
        val size = pathList.size
        while (index < size) {
            val pathNew = decodeBase64(context, pathList[index], type, appLockHelper)
            index++
            encodeCallback?.processing(index, pathNew)
            Thread.sleep(getSleep(size))
        }
        encodeCallback?.complete()
    }

    private fun decodeBase64(context: Context, path: String, type: Int, appLockHelper: AppLockHelper): String {
        val fileFrom = File(path)
        val subPath: String = when (type) {
            Const.TYPE_IMAGES -> {
                getExternalDirectory(EXTERNAL_FOLDER_PRIVATE + EXTERNAL_FOLDER_IMAGES).absolutePath
            }
            Const.TYPE_VIDEOS -> {
                getExternalDirectory(EXTERNAL_FOLDER_PRIVATE + EXTERNAL_FOLDER_VIDEOS).absolutePath
            }
            Const.TYPE_AUDIOS -> {
                getExternalDirectory(EXTERNAL_FOLDER_PRIVATE + EXTERNAL_FOLDER_AUDIOS).absolutePath
            }
            Const.TYPE_FILES -> {
                getExternalDirectory(EXTERNAL_FOLDER_PRIVATE + EXTERNAL_FOLDER_FILES).absolutePath
            }
            else -> {
                getExternalDirectory(EXTERNAL_FOLDER_PRIVATE + EXTERNAL_FOLDER_IMAGES).absolutePath
            }
        }
        fileFrom.parent?.let {
            val index = it.indexOf(subPath)
            val pathTo = it.substring(subPath.length + index, it.length)
            val folderTo = getFolderWithPath(pathTo)
            val fileTo = when (type) {
                Const.TYPE_AUDIOS, Const.TYPE_FILES -> {
                    FilePathHelper.generateFileName(fileFrom.name, folderTo)
                }
                else -> {
                    val lastIndex = fileFrom.name.lastIndexOf("(")
                    if (lastIndex < 0) {
                        FilePathHelper.generateFileName(fileFrom.name.decodeBase64(), folderTo)
                    } else {
                        val nameFirst = fileFrom.name.substring(0, lastIndex)
                        val extendFile = fileFrom.name.substring(lastIndex + 1, fileFrom.name.length - 1)
                        //
                        val nameDecode = nameFirst.decodeBase64()
                        //
                        val indexX = nameDecode.lastIndexOf(".")
                        val extension = nameDecode.substring(indexX, nameDecode.length)
                        val name = nameDecode.substring(0, indexX)
                        var file = File(folderTo, "$name($extendFile)$extension")
                        var count = 1
                        while (file.exists()) {
                            val extend = extendFile.toInt() + count
                            file = File(folderTo, "$name($extend)$extension")
                            count++
                        }
                        file
                    }
                }
            }
            fileTo?.let { file ->
                rename(fileFrom, file)
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { _: String?, _: Uri? -> }
                appLockHelper.addScanFile(file.absolutePath)
                return file.toString()
            } ?: return ""
        }

        return ""
    }

    private fun checkDecodeBase64(path: String, type: Int): String {
        val fileFrom = File(path)
        return when (type) {
            Const.TYPE_AUDIOS, Const.TYPE_FILES -> {
                fileFrom.name
            }
            else -> {
                var index = fileFrom.name.lastIndexOf("(")
                if (index >= 0 && index < fileFrom.name.length) {
                    val extendFile = fileFrom.name.substring(index, fileFrom.name.length)
                    var name = fileFrom.name.substring(0, index)
                    name = name.decodeBase64()
                    index = name.lastIndexOf(".")
                    val extension = name.substring(index, name.length)
                    name = name.substring(0, index)
                    return name + extendFile + extension
                }
                fileFrom.name.decodeBase64()
            }
        }
    }

    private fun rename(from: File, to: File): Boolean {
        return from.parentFile?.let {
            it.exists() && from.exists() && from.renameTo(to)
        } ?: false
    }

    fun saveIntruderToGallery(context: Context, path: String?, onRenameFileCallback: OnRenameFileCallback) {
        path?.let {
            val fileOld = File(it)
            var name = fileOld.name
            while (name.startsWith(".")) {
                name = name.substring(1, name.length)
            }
            val fileGallery = FilePathHelper.generateFileName(name, getExternalDirectory(EXTERNAL_FOLDER_INTRUDERS))
            fileGallery?.let { fileNew ->
                val success = rename(fileOld, fileNew)
                if (success) {
                    MediaScannerConnection.scanFile(context, arrayOf(fileNew.absolutePath), null) { _: String?, _: Uri? -> }
                    onRenameFileCallback.success(fileNew.absolutePath)
                    return
                }
            }
        }
        return onRenameFileCallback.failed()
    }

    fun deleteAllIntruderFolder() {
        val dir: File = getExternalDirectory(EXTERNAL_FOLDER_INTRUDERS)
        if (dir.isDirectory) {
            val children = dir.list()
            children?.let {
                for (i in it.indices) {
                    File(dir, it[i]).delete()
                }
            }
        }
    }


    private fun copyFile(sourceFile: File, destFile: File): Boolean {
        if (destFile.parentFile?.exists() == false) destFile.parentFile?.mkdirs()
        if (!destFile.exists()) {
            destFile.createNewFile()
        }
        var source: FileChannel? = null
        var destination: FileChannel? = null
        return try {
            source = FileInputStream(sourceFile).channel
            destination = FileOutputStream(destFile).channel
            destination.transferFrom(source, 0, source.size())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            source?.close()
            destination?.close()
        }
    }

    fun saveTheme(context: Context, type: Int, id: Int, bitmap: Bitmap, name: String, onSaveFileListener: OnSaveFileListener) {
        val subFolder = when (type) {
            ThemeUtils.TYPE_PATTERN -> "/pattern/$id"
            ThemeUtils.TYPE_PIN -> "/pin/$id"
            ThemeUtils.TYPE_WALLPAPER -> "/background/$id"
            else -> "/pattern/$id"
        }
        val folder = getExternalDirectory(EXTERNAL_FOLDER_THEME + subFolder)
        val file = File(folder, "$name.png")
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { _: String?, _: Uri? -> }
            onSaveFileListener.success(file.absolutePath)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            onSaveFileListener.failed()
        }
    }

    interface ProgressCallback {
        fun progress(progress: Int)
    }

    interface EncodeCallback {
        fun start()
        fun processing(percentage: Int, path: String)
        fun complete()
    }

    interface OnRenameFileCallback {
        fun success(path: String)
        fun failed()
    }

    interface OnSaveFileListener {
        fun success(path: String)
        fun failed()
    }

    private fun getExternalAbsolutePath(): String {
        var appPath="";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            appPath= AppLockerApplication.appContext.getExternalFilesDir(null)?.absolutePath.toString()
        }
        else {
            appPath = Environment.getExternalStorageDirectory().absolutePath.toString()
        }
        return appPath

    }
}