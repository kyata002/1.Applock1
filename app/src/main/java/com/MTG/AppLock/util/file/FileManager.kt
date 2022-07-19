package com.MTG.AppLock.util.file

import android.content.Context
import android.os.Build
import android.os.Environment
import com.MTG.AppLock.AppLockerApplication
import io.reactivex.Completable
import java.io.File
import javax.inject.Inject

class FileManager @Inject constructor(val context: Context) {
    enum class SubFolder(val subFolderPath: String) {
        VAULT(EncryptionFileManager.EXTERNAL_FOLDER_PRIVATE), INTRUDERS(EncryptionFileManager.EXTERNAL_FOLDER_INTRUDERS)
    }

    fun createFile(fileOperationRequest: FileOperationRequest, subFolder: SubFolder): File {
        val folder = when (fileOperationRequest.directoryType) {
            DirectoryType.CACHE -> getCacheDir()
            DirectoryType.EXTERNAL -> getExternalDirectory(subFolder)
        }

        return File(
                folder, fileOperationRequest.fileName + fileOperationRequest.fileExtension.extension
        )
    }

    fun deleteFile(filePath: String): Completable {
        return Completable.create {
            File(filePath).delete()
            it.onComplete()
        }
    }

    fun getFile(fileOperationRequest: FileOperationRequest, subFolder: SubFolder): File? {
        val folder = when (fileOperationRequest.directoryType) {
            DirectoryType.CACHE -> getCacheDir()
            DirectoryType.EXTERNAL -> getExternalDirectory(subFolder)
        }

        return File(folder.absolutePath + "/" + fileOperationRequest.fileName + fileOperationRequest.fileExtension.extension)
    }

    fun getSubFiles(folder: File, extension: FileExtension): List<File> {
        if (folder.isFile || folder.exists().not()) {
            return arrayListOf()
        }

        val files = folder.listFiles() ?: arrayOf()
        return files.filter {
            it.name.toLowerCase().endsWith(extension.extension.toLowerCase())
        }.filter {
            it.length().toDouble() != 0.0
        }
    }

    fun createFileInCache(fileName: String): File {
        return File(context.cacheDir, "$fileName.jpg")
    }

    fun isFileExist(filePath: String): Boolean {
        return File(filePath).exists()
    }

    fun isFileInCache(fileName: String): Boolean {
        val cacheFile = File(context.cacheDir, fileName)
        return cacheFile.exists()
    }

    fun getExternalDirectory(subFolder: SubFolder): File {
        var appPath="";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            appPath= AppLockerApplication.appContext.getExternalFilesDir(null).toString()+subFolder.subFolderPath
        }
        else {
            appPath = Environment.getExternalStorageDirectory().toString() + subFolder.subFolderPath
        }

        val folder = File(appPath)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }

    private fun getCacheDir(): File = context.cacheDir
}