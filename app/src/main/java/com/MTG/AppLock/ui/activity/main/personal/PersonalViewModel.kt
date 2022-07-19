package com.MTG.AppLock.ui.activity.main.personal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.MTG.AppLock.model.VaultGroup
import com.MTG.AppLock.model.VaultItem
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.extensions.doOnBackground
import com.MTG.AppLock.util.file.EncryptionFileManager
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import javax.inject.Inject

class PersonalViewModel @Inject constructor(val appLockerPreferences: AppLockerPreferences) : RxAwareViewModel() {
    private val mVaultGroupLiveData = MutableLiveData<VaultGroup>()

    fun getVaultGroupLiveData(): LiveData<VaultGroup> = mVaultGroupLiveData

    fun setReload(isReload: Boolean) {
        appLockerPreferences.setReload(isReload)
    }

    fun setupNumberType(granted: Boolean, type: Int) {
        doOnBackground {
            when (type) {
                Const.TYPE_IMAGES -> {
                    val albumImageList = EncryptionFileManager.getListAlbumWithType(Const.TYPE_IMAGES, null)
                    var numberImages = 0
                    albumImageList.forEach {
                        numberImages += it.number
                    }
                    mVaultGroupLiveData.postValue(VaultGroup(VaultItem(numberImages, type), granted = granted))
                }
                Const.TYPE_VIDEOS -> {
                    val albumVideoList = EncryptionFileManager.getListAlbumWithType(Const.TYPE_VIDEOS, null)
                    var numberVideos = 0
                    albumVideoList.forEach {
                        numberVideos += it.number
                    }
                    mVaultGroupLiveData.postValue(VaultGroup(VaultItem(numberVideos, type), granted = granted))
                }
                Const.TYPE_AUDIOS -> {
                    val albumAudioList = EncryptionFileManager.getListAlbumWithType(Const.TYPE_AUDIOS, null)
                    var numberAudios = 0
                    albumAudioList.forEach {
                        numberAudios += it.number
                    }
                    mVaultGroupLiveData.postValue(VaultGroup(VaultItem(numberAudios, type), granted = granted))
                }
                Const.TYPE_FILES -> {
                    val albumVFileList = EncryptionFileManager.getListAlbumWithType(Const.TYPE_FILES, null)
                    var numberFiles = 0
                    albumVFileList.forEach {
                        numberFiles += it.number
                    }
                    mVaultGroupLiveData.postValue(VaultGroup(VaultItem(numberFiles, type), granted = granted))
                }
                Const.TYPE_FULL -> {
                    val albumImageList = EncryptionFileManager.getListAlbumWithType(Const.TYPE_IMAGES, null)
                    var numberImages = 0
                    albumImageList.forEach {
                        numberImages += it.number
                    }
                    //
                    val albumVideoList = EncryptionFileManager.getListAlbumWithType(Const.TYPE_VIDEOS, null)
                    var numberVideos = 0
                    albumVideoList.forEach {
                        numberVideos += it.number
                    }
                    //
                    val albumAudioList = EncryptionFileManager.getListAlbumWithType(Const.TYPE_AUDIOS, null)
                    var numberAudios = 0
                    albumAudioList.forEach {
                        numberAudios += it.number
                    }
                    //
                    val albumVFileList = EncryptionFileManager.getListAlbumWithType(Const.TYPE_FILES, null)
                    var numberFiles = 0
                    albumVFileList.forEach {
                        numberFiles += it.number
                    }
                    //
                    val vaultList = mutableListOf<VaultItem>()
                    vaultList.add(VaultItem(numberImages, Const.TYPE_IMAGES))
                    vaultList.add(VaultItem(numberVideos, Const.TYPE_VIDEOS))
                    vaultList.add(VaultItem(numberAudios, Const.TYPE_AUDIOS))
                    vaultList.add(VaultItem(numberFiles, Const.TYPE_FILES))
                    mVaultGroupLiveData.postValue(VaultGroup(vaultList, granted))
                }
            }
        }
    }
}