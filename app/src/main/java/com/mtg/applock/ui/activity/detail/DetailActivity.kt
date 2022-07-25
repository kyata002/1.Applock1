package com.mtg.applock.ui.activity.detail

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
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
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.mtg.applock.BuildConfig
import com.mtg.applock.R
import com.mtg.applock.ui.base.BaseActivity
import com.mtg.applock.ui.view.BottomView
import com.mtg.applock.ui.view.BottomViewType
import com.mtg.applock.ui.view.ProgressDialog
import com.mtg.applock.util.Const
import com.mtg.applock.util.audio.AudioFocusManager
import com.mtg.applock.util.extensions.dialogLayout
import com.mtg.applock.util.extensions.gone
import com.mtg.applock.util.extensions.visible
import com.mtg.applock.util.file.EncryptionFileManager
import com.mtg.applock.util.file.FilePathHelper
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.mtg.library.customview.imagezoom.ImageViewTouchBase
import com.mtg.pinlock.extension.decodeBase64
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_detail.toolbar
import kotlinx.android.synthetic.main.dialog_delete.view.*
import kotlinx.android.synthetic.main.dialog_detail.view.*
import kotlinx.android.synthetic.main.dialog_unlock.view.*
import java.io.File

class DetailActivity : BaseActivity<DetailViewModel>() {
    private lateinit var mPath: String
    private var mType: Int = Const.TYPE_IMAGES
    private var mConfirmDialog: AlertDialog? = null
    private var mConfirmDeleteDialog: AlertDialog? = null
    private var mDetailDialog: AlertDialog? = null
    private var mExoPlayer: SimpleExoPlayer? = null
    private var mThread: Thread? = null
    private var mAudioFocusManager: AudioFocusManager? = null
    private var mProgressBar: Dialog? = null

    override fun getViewModel(): Class<DetailViewModel> {
        return DetailViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_detail
    }

    override fun initViews() {
        mPath = intent.getStringExtra(Const.EXTRA_PATH) ?: ""
        mType = intent.getIntExtra(Const.EXTRA_TYPE, Const.TYPE_IMAGES)
        val file = File(mPath)
        val name = when (mType) {
            Const.TYPE_IMAGES, Const.TYPE_VIDEOS -> {
                file.name.decodeBase64()
            }
            else -> {
                file.name
            }
        }

        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(name)

        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
        Glide.with(this).load(mPath).into(imageDetail)
        bottomView.setOnSelectedItemListener(object : BottomView.OnSelectedItemListener {
            override fun onSelectedItem(v: View?, type: Int) {
                when (type) {
                    BottomView.TYPE_UNLOCK -> {
                        mConfirmDialog?.show()
                        dialogLayout(mConfirmDialog)
                    }
                    BottomView.TYPE_DELETE -> {
                        mConfirmDeleteDialog?.show()
                        dialogLayout(mConfirmDeleteDialog)
                    }
                    BottomView.TYPE_DETAIL -> {
                        showDetail()
                    }
                }
            }
        })
        buildConfirmDialog()
        buildConfirmDeleteDialog()
        buildDetailDialog()
        imageDetail.displayType = ImageViewTouchBase.DisplayType.FIT_TO_SCREEN
        imageDetail.setHasMove(false)
        if (mType == Const.TYPE_VIDEOS) {
            imageDetail.gone()
            playerView.visible()
            bottomView.setModeView(BottomViewType.DELETE_UNLOCK_DETAIL)
            val mTrackSelector = DefaultTrackSelector()
            val mLoadControl = DefaultLoadControl()
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, mTrackSelector, mLoadControl)
            mExoPlayer?.addListener(object : Player.EventListener {})
            mExoPlayer?.playWhenReady = true
            playerView.player = mExoPlayer
            val factory = getDataSourceFactory(this)
            if (factory != null) {
                val videoSource = ProgressiveMediaSource.Factory(factory).createMediaSource(Uri.parse(mPath))
                mExoPlayer?.prepare(videoSource)
            }
            mExoPlayer?.let {
                val audioManager: AudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                mAudioFocusManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val audioAttributes: AudioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
                    AudioFocusManager(audioManager, audioAttributes, it)
                } else {
                    AudioFocusManager(audioManager, null, it)
                }
                mAudioFocusManager?.requestAudioFocus()
            }
        } else if (mType == Const.TYPE_FILES) {
            nsView.visible()
            imageDetail.gone()
            playerView.gone()
            bottomView.gone()
            showProgressBar()
            mThread = object : Thread() {
                override fun run() {
                    viewModel.loadData(mPath)
                }
            }
            mThread?.start()
        }
        viewModel.getTextDataLiveData().observe(this, {
            hideProgressBar()
            tvLongDetail.addText(it)
        })
    }

    private fun getDataSourceFactory(context: Context?): DataSource.Factory? {
        return if (context == null) null else DefaultDataSourceFactory(context, Util.getUserAgent(context, BuildConfig.APPLICATION_ID), null)
    }

    private fun unlock() {
        val pathList: MutableList<String> = mutableListOf()
        pathList.add(mPath)
        EncryptionFileManager.decodeBase64(this@DetailActivity, pathList, mType, viewModel.appLockHelper, object : EncryptionFileManager.EncodeCallback {
            override fun start() {
            }

            override fun processing(percentage: Int, path: String) {
            }

            override fun complete() {
                val intent = Intent()
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        })
    }

    private fun showDetail() {
        mDetailDialog?.show()
        dialogLayout(mDetailDialog)
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
            unlock()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun buildDetailDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_detail, null, false)
        val file = File(mPath)
        when (mType) {
            Const.TYPE_IMAGES -> {
                view.tvName.text = file.name.decodeBase64()
                //
                view.tvSize.text = FilePathHelper.getReadableFileSize(file.length().toDouble())
                //
                val bitMapOption: BitmapFactory.Options = BitmapFactory.Options()
                bitMapOption.inJustDecodeBounds = true
                BitmapFactory.decodeFile(mPath, bitMapOption)
                val imageWidth: Int = bitMapOption.outWidth
                val imageHeight: Int = bitMapOption.outHeight
                view.tvResolution.text = "$imageWidth * $imageHeight"
                //
                view.tvLastModified.text = convertDate(file.lastModified())
            }
            Const.TYPE_VIDEOS -> {
                view.tvName.text = file.name.decodeBase64()
                //
                view.tvSize.text = FilePathHelper.getReadableFileSize(
                        file.length().toDouble()
                )
                //
                try {
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(file.absolutePath)
                    val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    //
                    view.tResolution.setText(R.string.text_duration)
                    view.tvResolution.text = DateUtils.formatElapsedTime(duration?.toLong()
                            ?: 0 / 1000)
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
                    view.tvResolution.text = DateUtils.formatElapsedTime(duration?.toLong()
                            ?: 0 / 1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                //
                view.tvLastModified.text = convertDate(file.lastModified())
            }
            Const.TYPE_FILES -> {
                view.tvName.text = file.name
                //
                view.tvSize.text = FilePathHelper.getReadableFileSize(
                        file.length().toDouble()
                )
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
            delete()
        }
    }

    private fun delete() {
        val file = File(mPath)
        val success = file.delete()
        MediaScannerConnection.scanFile(this, arrayOf(mPath), null) { _: String?, _: Uri? -> }
        if (success) {
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            Toasty.showToast(this, R.string.msg_delete_failed, Toasty.ERROR)
        }
    }

    private fun showProgressBar() {
        mProgressBar?.dismiss()
        mProgressBar = ProgressDialog.progressDialogV2(this)
        mProgressBar?.show()
    }

    private fun hideProgressBar() {
        mProgressBar?.dismiss()
    }

    override fun onPause() {
        mExoPlayer?.playWhenReady = false
        mAudioFocusManager?.abandonAudioFocus()
        super.onPause()
    }

    override fun onDestroy() {
        mConfirmDialog?.dismiss()
        mConfirmDeleteDialog?.dismiss()
        mDetailDialog?.dismiss()
        releaseExo()
        mThread?.interrupt()
        mThread = null
        mProgressBar?.dismiss()
        super.onDestroy()
    }

    private fun releaseExo() {
        mExoPlayer?.stop()
        mExoPlayer?.release()
        mExoPlayer = null
        mAudioFocusManager?.abandonAudioFocus()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, DetailActivity::class.java)
        }
    }
}
