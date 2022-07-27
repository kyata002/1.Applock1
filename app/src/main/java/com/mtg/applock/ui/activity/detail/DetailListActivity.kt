package com.mtg.applock.ui.activity.detail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.mtg.applock.model.EncryptorModel
import com.mtg.applock.model.ItemDetail
import com.mtg.applock.ui.adapter.detaillist.DetailListAdapter
import com.mtg.applock.ui.base.BaseActivity
import com.mtg.applock.ui.view.BottomView
import com.mtg.applock.ui.view.BottomViewType
import com.mtg.applock.util.Const
import com.mtg.applock.util.audio.AudioFocusManager
import com.mtg.applock.util.extensions.*
import com.mtg.applock.util.file.EncryptionFileManager
import com.mtg.applock.util.file.FilePathHelper
import com.mtg.applock.util.file.MediaHelper
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

import com.mtg.library.customview.CustomToolbar
import com.mtg.pinlock.extension.decodeBase64
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.activity_detail_list.*
import kotlinx.android.synthetic.main.dialog_delete.view.*
import kotlinx.android.synthetic.main.dialog_detail.view.*
import kotlinx.android.synthetic.main.dialog_unlock.view.*
import kotlinx.android.synthetic.main.layout_view_controller_for_audio.*
import kotlinx.android.synthetic.main.popup_more_audio.view.*
import java.io.File
import com.mtg.applock.R
import com.mtg.applock.ui.activity.move.MoveActivity

class DetailListActivity : BaseActivity<DetailListViewModel>(), DetailListAdapter.OnSelectedDetailListener {
    private var mDetailListAdapterV2: DetailListAdapter? = null
    private var mDetailList = mutableListOf<ItemDetail>()
    private var mName: String = ""
    private var mPath: String = ""
    private var mType = Const.TYPE_IMAGES
    private var mNumber: Int = 0
    private var mExtension: String = ""
    private var mConfirmDialog: AlertDialog? = null
    private var mConfirmOneDialog: AlertDialog? = null
    private var mConfirmDeleteDialog: AlertDialog? = null
    private var mExoPlayer: SimpleExoPlayer? = null
    private var mFactory: DataSource.Factory? = null
    private var mConcatenatingMediaSource: ConcatenatingMediaSource? = null
    private var mAudioFocusManager: AudioFocusManager? = null
    private var mItemDetail: ItemDetail? = null
    private var mDetailDialog: AlertDialog? = null

    private val mEventListener: Player.EventListener = object : Player.EventListener {
        private var currentWindowIndex = -1
        private var currentWindowCount = 0
        private var mediaSource: MediaSource? = null

        // copy code from Mai Dai Dien
        override fun onTimelineChanged(timeline: Timeline?, @Nullable manifest: Any?, @Player.TimelineChangeReason reason: Int) {
            val windowCount = mExoPlayer?.currentTimeline?.windowCount
            val windowIndex = mExoPlayer?.currentWindowIndex
            if (currentWindowCount != windowCount || currentWindowIndex != windowIndex) {
                windowIndex?.let {
                    currentWindowIndex = it
                }
                invalidateMediaSessionPlaybackState()
            }
            if (windowCount != null) {
                currentWindowCount = windowCount
            }
            if (currentWindowIndex < 0) return
            mConcatenatingMediaSource?.let {
                if (it.size <= 0) return
                mediaSource = it.getMediaSource(currentWindowIndex)
                invalidateMediaSessionMetadata(mediaSource)
            }
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            invalidateMediaSessionPlaybackState()
        }

        override fun onRepeatModeChanged(@Player.RepeatMode repeatMode: Int) {
            invalidateMediaSessionPlaybackState()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            invalidateMediaSessionPlaybackState()
        }

        override fun onPositionDiscontinuity(@Player.DiscontinuityReason reason: Int) {
            exo_prev.isEnabled = mExoPlayer?.currentWindowIndex != 0
            if (currentWindowIndex != mExoPlayer?.currentWindowIndex) {
                mExoPlayer?.currentWindowIndex?.let {
                    currentWindowIndex = it
                    exo_prev.isEnabled = currentWindowIndex != 0
                    invalidateMediaSessionPlaybackState()
                    mediaSource = mConcatenatingMediaSource?.getMediaSource(currentWindowIndex)
                    invalidateMediaSessionMetadata(mediaSource)
                    return
                }
            }
            invalidateMediaSessionPlaybackState()
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            invalidateMediaSessionPlaybackState()
        }
    }

    private fun invalidateMediaSessionMetadata(mediaSource: MediaSource?) {
        if (mediaSource == null) return
        when (val tag = mediaSource.tag) {
            is ItemDetail -> {
                tvNameAudio.text = tag.name
            }
        }
    }

    private fun invalidateMediaSessionPlaybackState() {
        //  detect thay đổi state
    }

    override fun getViewModel(): Class<DetailListViewModel> {
        return DetailListViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_detail_list
    }

    override fun initViews() {
        mName = intent.getStringExtra(Const.EXTRA_NAME) ?: ""
        mPath = intent.getStringExtra(Const.EXTRA_PATH) ?: ""
        mType = intent.getIntExtra(Const.EXTRA_TYPE, Const.TYPE_IMAGES)
        mNumber = intent.getIntExtra(Const.EXTRA_NUMBER, 0)
        mExtension = intent.getStringExtra(Const.EXTRA_EXTENSION) ?: ""
        if (mType == Const.TYPE_VIDEOS || mType == Const.TYPE_IMAGES) {
            bottomView.setModeView(BottomViewType.DELETE_UNLOCK)
        }
        setupToolbar()
        mDetailListAdapterV2 = DetailListAdapter(this, mDetailList, this)
        recyclerList.adapter = mDetailListAdapterV2
        recyclerList.layoutManager = GridLayoutManager(this, getCountWithType())
        recyclerList.removeBlink()
        loadData()
        viewModel.getDetailListLiveData().observe(this) {
            hideProgressBar()
            mDetailList.clear()
            mDetailList.addAll(it)
            mDetailListAdapterV2?.notifyDataSetChanged()
            if (it.isNullOrEmpty()) {
                onBackPressed()
                return@observe
            }
            setupToolbar()
            // exoplayer
            initExoPlayer()
            updateMediaSource()
        }
        toolbar.setOnActionToolbarFull(object : CustomToolbar.OnActionToolbarFull {
            override fun onBack() {
                onBackPressed()
            }

            override fun onAction() {
                mDetailListAdapterV2?.setShow(true)
                mDetailListAdapterV2?.setSelectedAll(false)
                toolbar.setShowTvActionExtend(true)
                bottomView.visible()
                playerView.gone()
                releaseExo()
                setupToolbar()
            }
        })
        toolbar.setOnTvActionExtendToolbar(object : CustomToolbar.OnTvActionExtendToolbar {
            override fun onTvActionExtend(selectedAll: Boolean) {
                mDetailListAdapterV2?.setSelectedAll(selectedAll)
                setupToolbar()
            }
        })
        bottomView.setOnSelectedItemListener(object : BottomView.OnSelectedItemListener {
            override fun onSelectedItem(v: View?, type: Int) {
                when (type) {
                    BottomView.TYPE_UNLOCK -> {
                        mDetailListAdapterV2?.let {
                            when {
                                it.getSelectedNumber() > 1 -> {
                                    mConfirmDialog?.show()
                                    dialogLayout(mConfirmDialog)
                                }
                                it.getSelectedNumber() == 1 -> {
                                    mConfirmOneDialog?.show()
                                    dialogLayout(mConfirmOneDialog)
                                }
                                else -> {
                                    Toasty.showToast(this@DetailListActivity, R.string.msg_please_choose_at_least_one, Toasty.WARNING)
                                }
                            }
                        }
                                ?: Toasty.showToast(this@DetailListActivity, R.string.msg_please_choose_at_least_one, Toasty.WARNING)
                    }
                    BottomView.TYPE_DELETE -> {
                        mDetailListAdapterV2?.let {
                            if (it.getSelectedNumber() > 0) {
                                mConfirmDeleteDialog?.show()
                                dialogLayout(mConfirmDeleteDialog)
                            } else {
                                Toasty.showToast(this@DetailListActivity, R.string.msg_please_choose_at_least_one, Toasty.WARNING)
                            }
                        }
                                ?: Toasty.showToast(this@DetailListActivity, R.string.msg_please_choose_at_least_one, Toasty.WARNING)
                    }
                    else -> {
                        // nothing
                    }
                }
            }
        })
        buildConfirmDialog()
        buildConfirmOneDialog()
        buildConfirmDeleteDialog()
        viewModel.getProgressLiveData().observe(this) {
            if (mNumber != 0) {
                val builder = StringBuilder()
                builder.append("" + it * 100 / mNumber)
                builder.append(" %")
                tvAnimationLoading.text = builder.toString()
                if (it > mNumber) {
                    tvAnimationLoading.gone()
                } else {
                    tvAnimationLoading.visible()
                }
            }
        }
    }

    private fun updateMediaSource() {
        mConcatenatingMediaSource = ConcatenatingMediaSource()
        for (vault in mDetailList) {
            mFactory?.let { factory ->
                val videoSource = ProgressiveMediaSource.Factory(factory).setTag(vault).createMediaSource(Uri.parse(vault.path))
                mConcatenatingMediaSource?.addMediaSource(videoSource)
            }
        }
        playerView.controllerHideOnTouch = false
        mExoPlayer?.prepare(mConcatenatingMediaSource)
    }

    private fun releaseExo() {
        mExoPlayer?.stop()
        mExoPlayer?.release()
        mExoPlayer = null
        mAudioFocusManager?.abandonAudioFocus()
    }

    private fun initExoPlayer() {
        if (mExoPlayer == null) {
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, DefaultTrackSelector(), DefaultLoadControl())
            mExoPlayer?.playWhenReady = false
            playerView.player = mExoPlayer
            mFactory = getDataSourceFactory(this)
            mExoPlayer?.addListener(mEventListener)
            //
            mExoPlayer?.let {
                val audioManager: AudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                mAudioFocusManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val audioAttributes: AudioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
                    AudioFocusManager(audioManager, audioAttributes, it)
                } else {
                    AudioFocusManager(audioManager, null, it)
                }
            }
        }
    }

    private fun getDataSourceFactory(context: Context?): DataSource.Factory? {
        return if (context == null) null else DefaultDataSourceFactory(context, Util.getUserAgent(context, BuildConfig.APPLICATION_ID), null)
    }

    private fun openFile(detail: ItemDetail, position: Int) {
        when (mType) {
            Const.TYPE_IMAGES -> {
                val intent = DetailActivity.newIntent(this)
                intent.putExtra(Const.EXTRA_PATH, detail.path)
                intent.putExtra(Const.EXTRA_TYPE, mType)
                startActivityForResult(intent, Const.REQUEST_CODE_GO_TO_DETAIL)
            }
            Const.TYPE_VIDEOS -> {
                val intent = DetailActivity.newIntent(this)
                intent.putExtra(Const.EXTRA_PATH, detail.path)
                intent.putExtra(Const.EXTRA_TYPE, mType)
                startActivityForResult(intent, Const.REQUEST_CODE_GO_TO_DETAIL)
            }
            Const.TYPE_AUDIOS -> {
                //
                if (mExoPlayer == null) {
                    initExoPlayer()
                    updateMediaSource()
                }
                mConcatenatingMediaSource?.let {
                    mExoPlayer?.seekTo(position, 0)
                    mExoPlayer?.playWhenReady = true
                    tvNameAudio.text = detail.name
                    playerView.visible()
                    mAudioFocusManager?.requestAudioFocus()
                }
            }
            Const.TYPE_FILES -> {
                try {
                    if (detail.path.endsWith(MediaHelper.EXTENSION_TXT_SUPPORT_FORMAT)) {
                        val intent = DetailActivity.newIntent(this)
                        intent.putExtra(Const.EXTRA_PATH, detail.path)
                        intent.putExtra(Const.EXTRA_TYPE, mType)
                        startActivityForResult(intent, Const.REQUEST_CODE_GO_TO_DETAIL)
                        return
                    }
                    val intent = FilePathHelper.getViewIntent(this, File(detail.path))
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    Toasty.showToast(this, R.string.msg_open_file_failed, Toasty.ERROR)
                }
            }
        }
    }

    private fun detailFile(detail: ItemDetail) {
        buildDetailDialog(detail.path)
        showDetail()
    }

    @SuppressLint("SetTextI18n")
    private fun buildDetailDialog(path: String) {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_detail, null, false)
        val file = File(path)
        when (mType) {
            Const.TYPE_IMAGES -> {
                view.tvName.text = file.name.decodeBase64()
                //
                view.tvSize.text = FilePathHelper.getReadableFileSize(file.length().toDouble())
                //
                val bitMapOption: BitmapFactory.Options = BitmapFactory.Options()
                bitMapOption.inJustDecodeBounds = true
                BitmapFactory.decodeFile(path, bitMapOption)
                val imageWidth: Int = bitMapOption.outWidth
                val imageHeight: Int = bitMapOption.outHeight
                view.tvResolution.text = "$imageWidth * $imageHeight"
                //
                view.tvLastModified.text = convertDate(file.lastModified())
            }
            Const.TYPE_VIDEOS -> {
                view.tvName.text = file.name.decodeBase64()
                //
                view.tvSize.text = FilePathHelper.getReadableFileSize(file.length().toDouble())
                //
                try {
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(file.absolutePath)
                    val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    //
                    view.tResolution.setText(R.string.text_duration)
                    view.tvResolution.text = DateUtils.formatElapsedTime(
                        duration?.toLong()
                            ?: (0 / 1000)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                //
                view.tvLastModified.text = convertDate(file.lastModified())
            }
            Const.TYPE_AUDIOS -> {
                view.tvName.text = file.name
                //
                view.tvSize.text = FilePathHelper.getReadableFileSize(file.length().toDouble())
                //
                try {
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(file.absolutePath)
                    val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    view.tResolution.setText(R.string.text_duration)
                    view.tvResolution.text = DateUtils.formatElapsedTime(
                        duration?.toLong()
                            ?: (0 / 1000)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                //
                view.tvLastModified.text = convertDate(file.lastModified())
            }
            Const.TYPE_FILES -> {
                view.tvName.text = file.name
                //
                view.tvSize.text = FilePathHelper.getReadableFileSize(file.length().toDouble())
                view.tResolution.gone()
                view.tvResolution.gone()
                //
                view.tvLastModified.text = convertDate(file.lastModified())

            }
        }
        builder.setView(view)
        mDetailDialog?.dismiss()
        mDetailDialog = builder.create()
        mDetailDialog?.setCancelable(false)
        mDetailDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnOkDetail.setOnClickListener {
            mDetailDialog?.dismiss()
        }
    }

    private fun convertDate(dateInMilliseconds: Long): String {
        return DateFormat.format("hh:mm dd/MM/yyyy", dateInMilliseconds).toString()
    }

    private fun showDetail() {
        mDetailDialog?.show()
        dialogLayout(mDetailDialog)
    }

    private fun moveOutFile() {
        mItemDetail?.let {
            val pathList: MutableList<String> = mutableListOf()
            pathList.add(it.path)
            EncryptionFileManager.decodeBase64(this, pathList, mType, viewModel.appLockHelper, object : EncryptionFileManager.EncodeCallback {
                override fun start() {
                }

                override fun processing(percentage: Int, path: String) {
                }

                override fun complete() {
                    val index = mDetailList.indexOf(it)
                    if (index > -1) {
                        mDetailList.removeAt(index)
                        mDetailListAdapterV2?.notifyDataSetChanged()
                        mExoPlayer?.let { exo ->
                            if (exo.currentTimeline?.windowCount == 1) {
                                releaseExo()
                            } else {
                                if (exo.currentWindowIndex == index) {
                                    if (index == 0) {
                                        exo.seekTo(index, 0)
                                    } else {
                                        exo.seekTo(index - 1, 0)
                                    }
                                }
                                mConcatenatingMediaSource?.removeMediaSource(index)
                            }
                        }
                        setupToolbar()
                        updateRecyclerView()
                    }
                }
            })
        } ?: unlockAll() // unlock all
    }

    private fun unlockAll() {
        val itemDetailList: MutableList<ItemDetail> = mutableListOf()
        for (album in mDetailList) {
            if (album.isSelected) {
                itemDetailList.add(ItemDetail(name = album.name, path = album.path, tvSize = album.tvSize, resIdThumbnail = album.resIdThumbnail, type = album.type))
            }
        }
        val encryptor = EncryptorModel(Const.TYPE_DECODE, itemDetailList)
        val intent = MoveActivity.newIntent(this)
        intent.putExtra(Const.EXTRA_DATA, encryptor)
        intent.putExtra(Const.EXTRA_TYPE, mType)
        startActivityForResult(intent, Const.REQUEST_CODE_MOVE_FILE)
        releaseExo()
    }

    private fun showMoreDialog(view: View?, itemDetail: ItemDetail, position: Int) {
        view?.let {
            val popupView = LayoutInflater.from(this).inflate(R.layout.popup_more_audio, null)
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val popupWindow = PopupWindow(popupView, width, height, true)
            //
            popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val location = IntArray(2)
            it.getLocationOnScreen(location)
            val heightPixels: Int = resources.displayMetrics.heightPixels
            val heightBottom: Int = resources.getDimensionPixelSize(R.dimen.height_bottom)
            if (location[1] + popupView.measuredHeight > heightPixels - heightBottom) {
                val top = popupView.measuredHeight + it.height / 2
                popupWindow.showAsDropDown(it, -popupView.measuredWidth + 50, -top)
            } else {
                popupWindow.showAsDropDown(it, -popupView.measuredWidth + 50, -it.height / 2)
            }
            popupView.setOnTouchListener { _, _ ->
                popupWindow.dismiss()
                true
            }
            popupView.llOpen.setOnClickListener {
                popupWindow.dismiss()
                openFile(itemDetail, position)
            }
            popupView.llDetail.setOnClickListener {
                popupWindow.dismiss()
                detailFile(itemDetail)
            }
            popupView.llMoveOut.setOnClickListener {
                mItemDetail = itemDetail
                popupWindow.dismiss()
                mConfirmOneDialog?.show()
                dialogLayout(mConfirmOneDialog)
            }
            popupView.llDelete.setOnClickListener {
                mItemDetail = itemDetail
                popupWindow.dismiss()
                mConfirmDeleteDialog?.show()
                dialogLayout(mConfirmDeleteDialog)
            }
        }
    }

    private fun deleteFile() {
        mItemDetail?.let{
            val file = File(it.path)
            val success = file.delete()
            if (success) {
                MediaScannerConnection.scanFile(this, arrayOf(it.path), null) { _: String?, _: Uri? -> }
                val index = mDetailList.indexOf(it)
                if (index > -1) {
                    mDetailList.removeAt(index)
                    mDetailListAdapterV2?.notifyDataSetChanged()
                    mExoPlayer?.let { exo ->
                        if (exo.currentTimeline?.windowCount == 1) {
                            releaseExo()
                        } else {
                            if (exo.currentWindowIndex == index) {
                                if (index == 0) {
                                    exo.seekTo(index, 0)
                                } else {
                                    exo.seekTo(index - 1, 0)
                                }
                            }
                            mConcatenatingMediaSource?.removeMediaSource(index)
                        }
                    }
//                    setupToolbar()
                    updateRecyclerView()
                } else {
                    Toasty.showToast(this, R.string.msg_delete_failed, Toasty.ERROR)
                }
            } else {
                Toasty.showToast(this, R.string.msg_delete_failed, Toasty.ERROR)
            }
        } ?: deleteAll()
        toolbar.setShowTvActionExtend(false)

    }

    private fun updateRecyclerView() {
        if (mDetailList.isNullOrEmpty()) {
            onBackPressed()
        }
    }

    private fun deleteAll() {
        val selectedList = mutableListOf<String>()
        for (detail in mDetailList) {
            if (detail.isSelected) {
                selectedList.add(detail.path)
            }
        }
        val successList = mutableListOf<String>()
        for (path in selectedList) {
            val file = File(path)
            val successChild = file.delete()
            MediaScannerConnection.scanFile(this, arrayOf(path), null) { _: String?, _: Uri? -> }
            if (successChild) {
                successList.add(path)
            } else {
                Toasty.showToast(this, R.string.msg_delete_failed, Toasty.ERROR)
            }
        }
        removeData(successList)
    }

    private fun buildConfirmDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_unlock, null, false)
        view.tvMessageUnlock.text = viewModel.getMessage(this, mType)
        builder.setView(view)
        mConfirmDialog?.dismiss()
        mConfirmDialog = builder.create()
        mConfirmDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelUnlock.setOnClickListener { mConfirmDialog?.dismiss() }
        view.btnYesUnlock.setOnClickListener {
            mConfirmDialog?.dismiss()
            moveOutFile()
        }
    }

    private fun buildConfirmOneDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_unlock, null, false)
        view.tvMessageUnlock.text = viewModel.getMessageOne(this, mType)
        builder.setView(view)
        mConfirmOneDialog?.dismiss()
        mConfirmOneDialog = builder.create()
        mConfirmOneDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelUnlock.setOnClickListener { mConfirmOneDialog?.dismiss() }
        view.btnYesUnlock.setOnClickListener {
            mConfirmOneDialog?.dismiss()
            moveOutFile()
        }
    }

    private fun buildConfirmDeleteDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_delete, null, false)
        view.tvMessageDelete.text = viewModel.getMessageDelete(this, mType)
        builder.setView(view)
        mConfirmDeleteDialog?.dismiss()
        mConfirmDeleteDialog = builder.create()
        mConfirmDeleteDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancelDelete.setOnClickListener { mConfirmDeleteDialog?.dismiss() }
        view.btnYesDelete.setOnClickListener {
            mConfirmDeleteDialog?.dismiss()
            deleteFile()
        }
    }

    private fun removeData(pathList: MutableList<String>) {
        mDetailListAdapterV2?.setSelectedAll(false)
        mDetailListAdapterV2?.setShow(false)
        val indexList = mutableListOf<Int>()
        for (path in pathList) {
            for (detail in mDetailList) {
                if (TextUtils.equals(path, detail.path)) {
                    indexList.add(mDetailList.indexOf(detail))
                }
            }
        }
        for (index in indexList.size - 1 downTo 0) {
            mDetailList.removeAt(indexList[index])
            mDetailListAdapterV2?.notifyItemRemoved(indexList[index])
        }
        setupToolbar()
        if (mDetailList.isNullOrEmpty()) {
            onBackPressed()
        }
    }

    private fun loadData() {
        // load data
        showProgressBar()
        doOnBackground {
            viewModel.loadData(mPath, mType, mExtension)
        }
    }

    private fun setupToolbar() {
        mDetailListAdapterV2?.let {
            if (it.isShow()) {
                val number = it.getSelectedNumber()
                updateToolbar("$mName ($number)", false)
                toolbar.setStatusTvActionExtend(number == mDetailList.size && number != 0)
            } else {
                updateToolbar(mName, true)
            }
        } ?: updateToolbar(mName, false)
    }

    private fun updateToolbar(name: String, show: Boolean) {
        toolbar.setTitle(name)
        toolbar.setShowAction(show, false)
    }

    override fun onSelectedDetail(itemDetail: ItemDetail, position: Int) {
        if (mDetailListAdapterV2?.isShow() == true) {
            setupToolbar()
            mDetailListAdapterV2?.let {
                showBottomView(true)
            } ?: showBottomView(show = false)
            return
        }
        openFile(itemDetail, position)
    }

    override fun onMoreDetail(view: View, itemDetail: ItemDetail, position: Int) {
        showMoreDialog(view, itemDetail, position)
    }

    private fun showBottomView(show: Boolean) {
        bottomView.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun getCountWithType(): Int {
        var count = 3
        when (mType) {
            Const.TYPE_IMAGES, Const.TYPE_VIDEOS -> {
                count = 3
            }
            Const.TYPE_AUDIOS, Const.TYPE_FILES -> {
                count = 1
            }
        }
        return count
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            Const.REQUEST_CODE_GO_TO_DETAIL -> {
                data?.let {
                    loadData()
                }
                toolbar.setShowTvActionExtend(false)
                setupToolbar()
                showBottomView(show = false)
            }
            Const.REQUEST_CODE_MOVE_FILE -> {
                mDetailListAdapterV2?.setShow(false)
                data?.let {
                    loadData()
                }
                toolbar.setShowTvActionExtend(false)
                setupToolbar()
                showBottomView(show = false)
            }
        }
    }

    override fun onDestroy() {
        mDetailDialog?.dismiss()
        mConfirmDialog?.dismiss()
        mConfirmOneDialog?.dismiss()
        mConfirmDeleteDialog?.dismiss()
        releaseExo()
        super.onDestroy()
    }

    private fun showProgressBar() {
        rlLoading.visible()
    }

    private fun hideProgressBar() {
        rlLoading.gone()
    }

    override fun onPause() {
        super.onPause()
        playerView.onPause()
        mExoPlayer?.playWhenReady = false
        mAudioFocusManager?.abandonAudioFocus()
    }

    override fun onBackPressed() {
        when {
            mDetailListAdapterV2?.isShow() == true -> {
                mDetailListAdapterV2?.setShow(false)
                mDetailListAdapterV2?.setSelectedAll(false)
                toolbar.setShowTvActionExtend(false)
                setupToolbar()
                showBottomView(show = false)
                return
            }
            mNumber != mDetailList.size -> {
                val intent = Intent()
                intent.putExtra(Const.EXTRA_TYPE, mType)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, DetailListActivity::class.java)
        }
    }
}
