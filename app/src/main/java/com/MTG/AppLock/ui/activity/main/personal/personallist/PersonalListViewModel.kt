package com.MTG.AppLock.ui.activity.main.personal.personallist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.file.EncryptionFileManager
import javax.inject.Inject

class PersonalListViewModel @Inject constructor() : RxAwareViewModel() {
    private val vaultListViewStateLiveData = MutableLiveData<PersonalListViewState>()
    private val progressLiveData = MutableLiveData<Int>()

    fun getVaultListViewStateLiveData(): LiveData<PersonalListViewState> = vaultListViewStateLiveData

    fun getProgressLiveData(): LiveData<Int> = progressLiveData

    fun loadDataWithType(type: Int) {
        val albumList = EncryptionFileManager.getListAlbumWithType(type, object : EncryptionFileManager.ProgressCallback {
            override fun progress(progress: Int) {
                progressLiveData.postValue(progress)
            }
        })
        if (albumList.isNullOrEmpty()) {
            vaultListViewStateLiveData.postValue(PersonalListViewState.empty(type))
        } else {
            vaultListViewStateLiveData.postValue(PersonalListViewState(type, albumList))
        }
    }
}