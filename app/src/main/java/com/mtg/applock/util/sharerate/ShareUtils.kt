package com.mtg.applock.util.sharerate

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.mtg.applock.BuildConfig
import com.mtg.applock.R
import java.io.File

object ShareUtils {
    fun shareOther(context: Context, path: String) {
        try {
            val share = Intent("android.intent.action.SEND")
            share.type = "image/*"
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                share.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", File(path))
            } else {
                Uri.fromFile(File(path))
            }
            share.putExtra("android.intent.extra.STREAM", uri)
            val intent = Intent.createChooser(share, context.getString(R.string.text_share_with))
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}