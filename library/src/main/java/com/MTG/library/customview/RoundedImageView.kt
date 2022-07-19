package com.MTG.library.customview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory

class RoundedImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ImageView(context, attrs, defStyleAttr) {
    override fun setImageDrawable(drawable: Drawable?) {
        if (drawable == null) return
        val radius = 25f
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            val rid = RoundedBitmapDrawableFactory.create(resources, bitmap)
            rid.cornerRadius = radius
            super.setImageDrawable(rid)
        } else {
            super.setImageDrawable(drawable)
        }
    }

    override fun setImageBitmap(bitmap: Bitmap) {
        val radius = 25f
        val rid = RoundedBitmapDrawableFactory.create(resources, bitmap)
        rid.cornerRadius = radius
        super.setImageBitmap(bitmap)
    }
}