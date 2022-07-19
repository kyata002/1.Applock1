package com.MTG.library.customview

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import com.MTG.library.R
import java.util.*

class TagImageView : LinearLayout {
    private var mRootView: View? = null

    constructor(context: Context) : super(context) {
        initViews(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initViews(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initViews(context, attrs)
    }

    private fun initViews(context: Context, attrs: AttributeSet?) {
        mRootView = LayoutInflater.from(context).inflate(R.layout.tag_image_view, this, true)
    }

    fun setPackageNameList(packageAppLockList: List<String>) {
        var number = 0
        val root = getChildAt(0) as LinearLayout
        val count = root.childCount
        val maxSize: Int = (2f /3 * Resources.getSystem().displayMetrics.widthPixels / (resources.getDimensionPixelSize(R.dimen.width_tag) + 2 * resources.getDimensionPixelSize(R.dimen.margin_tag)) - 1).toInt()
        var sizeReality = count.coerceAtMost(maxSize)
        val size = packageAppLockList.size
        var more = size - sizeReality
        // hide view
        for (x in 0 until count) {
            val view = root.getChildAt(x)
            if (view is ImageView) {
                view.visibility = View.GONE
            } else if (view is TextView) {
                view.visibility = View.GONE
            }
        }
        for (x in size - 1 downTo 0 step 1) {
            val drawable = getAppPackageNameIcon(packageAppLockList[x])
            if (drawable != null) {
                if (number < sizeReality) {
                    val view = root.getChildAt(number)
                    if (view is ImageView) {
                        view.setImageDrawable(drawable)
                        view.visibility = View.VISIBLE
                    }
                    number++
                }
            }
        }
        if (more > 0) {
            if (more > 99) {
                more = 99
            }
            val view = root.getChildAt(count - 1)
            if (view is TextView) {
                view.text = String.format(Locale.getDefault(), "+%d", more)
                view.visibility = VISIBLE
            }
        }
    }

    private fun getAppPackageNameIcon(appPackageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(appPackageName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }
}