package com.MTG.AppLock.ui.activity.move

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.MTG.AppLock.data.sqllite.AppLockHelper
import com.MTG.AppLock.model.EncryptorModel
import com.MTG.AppLock.model.MoveData
import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.Const
import com.MTG.AppLock.util.extensions.doOnBackground
import com.MTG.AppLock.util.file.EncryptionFileManager
import javax.inject.Inject

class MoveViewModel @Inject constructor(private val appLockHelper: AppLockHelper) : RxAwareViewModel() {
    private val moveFinishedLiveData = MutableLiveData<Boolean>()
    fun getMoveFinishedLiveData(): LiveData<Boolean> = moveFinishedLiveData
    private val progressLiveData = MutableLiveData<MoveData>()
    fun getProgressLiveData(): LiveData<MoveData> = progressLiveData

    init {
        moveFinishedLiveData.postValue(false)
    }

    fun move(context: Context, encryptorModel: EncryptorModel, pathList: MutableList<String>, type: Int) {

        doOnBackground {
            when (encryptorModel.type) {
                Const.TYPE_ENCODE -> {
                    EncryptionFileManager.encodeBase64(context, pathList, type, appLockHelper, object : EncryptionFileManager.EncodeCallback {
                        override fun start() {
                        }

                        override fun processing(percentage: Int, path: String) {
                            progressLiveData.postValue(MoveData(percentage = percentage, path = path))
                        }

                        override fun complete() {
                            moveFinishedLiveData.postValue(true)
                        }
                    })
                }
                Const.TYPE_DECODE -> {
                    EncryptionFileManager.decodeBase64(context, pathList, type, appLockHelper, object : EncryptionFileManager.EncodeCallback {
                        override fun start() {
                        }

                        override fun processing(percentage: Int, path: String) {
                            progressLiveData.postValue(MoveData(percentage = percentage, path = path))
                        }

                        override fun complete() {
                            moveFinishedLiveData.postValue(true)
                        }
                    })
                }
            }
        }
    }
}