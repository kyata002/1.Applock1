package com.mtg.library.customview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.appbar.AppBarLayout
import com.mtg.library.R
import com.mtg.library.callback.OnSingleClickListener
import java.util.*


open class CustomToolbar : AppBarLayout {
    private var mTvTitle: TextView? = null
    private var mTvcAction: TextView? = null
    private var mTvcActionExtend: ImageView? = null
    private var mImageBack: ImageView? = null
    private var mImageAction: ImageView? = null
    private var mImageActionExtend: ImageView? = null
    private var mEditSearchView: CustomEditText? = null
    private var mEditName: CustomEditText? = null
    private var mTitle: String = ""
    private var mTvAction: String = ""
    private var mTvActionExtend: String = ""
    private var mHintSearchView: String = ""
    private var mHintName: String = ""
    private var mResBack = 0
    private var mResAction = 0
    private var mResActionExtend = 0
    private var mRootView: View? = null
    private var mOnActionToolbarBack: OnActionToolbarBack? = null
    private var mOnActionToolbar: OnActionToolbar? = null
    private var mOnActionToolbarFull: OnActionToolbarFull? = null
    private var mOnActionExtendToolbar: OnActionExtendToolbar? = null
    private var mOnTvActionToolbar: OnTvActionToolbar? = null
    private var mOnTvActionExtendToolbar: OnTvActionExtendToolbar? = null
    private var mOnSearchToolbar: OnSearchToolbar? = null
    private var mOnEditNameToolbar: OnEditNameToolbar? = null
    private var mOnHideKeyboardListener: CustomEditText.OnHideKeyboardListener? = null
    private var mTextColorTitle = 0
    private var mTextColorAction = 0
    private var mIsShowBack = true
    private var mIsInvisibleShowBack = false
    private var mIsShowTitle = true
    private var mIsShowAction = true
    private var mIsShowActionExtend = false
    private var mIsShowTvActionExtend = false
    private var mIsShowTvAction = false
    private var mIsShowEditSearchView = false
    private var mIsShowEditName = false
    private var mResBackgroundColor = 0
    private var mIsTvTextAllCaps = false

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomToolbar)
            mTitle = typedArray.getString(R.styleable.CustomToolbar_title) ?: ""
            mTvAction = typedArray.getString(R.styleable.CustomToolbar_tv_action) ?: ""
            mTvActionExtend = typedArray.getString(R.styleable.CustomToolbar_tv_action_extend) ?: ""
            mHintSearchView = typedArray.getString(R.styleable.CustomToolbar_hint_search_view) ?: ""
            mHintName = typedArray.getString(R.styleable.CustomToolbar_hint_name) ?: ""
            //
            mResBack = typedArray.getResourceId(R.styleable.CustomToolbar_ic_back, R.drawable.baseline_arrow_back_24)
            mResAction = typedArray.getResourceId(R.styleable.CustomToolbar_ic_action, R.drawable.baseline_arrow_back_24)
            mResActionExtend = typedArray.getResourceId(R.styleable.CustomToolbar_ic_action_extend, R.drawable.baseline_arrow_back_24)
            //
            mResBackgroundColor = typedArray.getResourceId(R.styleable.CustomToolbar_background_color, Color.WHITE)
            mTextColorTitle = typedArray.getColor(R.styleable.CustomToolbar_text_color_title, Color.WHITE)
            mTextColorAction = typedArray.getColor(R.styleable.CustomToolbar_text_color_action, Color.WHITE)
            //
            mIsShowBack = typedArray.getBoolean(R.styleable.CustomToolbar_show_back, true)
            mIsInvisibleShowBack = typedArray.getBoolean(R.styleable.CustomToolbar_invisible_back, false)
            mIsShowTitle = typedArray.getBoolean(R.styleable.CustomToolbar_show_title, true)
            mIsShowAction = typedArray.getBoolean(R.styleable.CustomToolbar_show_action, true)
            mIsShowActionExtend = typedArray.getBoolean(R.styleable.CustomToolbar_show_action_extend, false)
            mIsShowTvActionExtend = typedArray.getBoolean(R.styleable.CustomToolbar_show_tv_action_extend, false)
            mIsShowTvAction = typedArray.getBoolean(R.styleable.CustomToolbar_show_tv_action, false)
            mIsShowEditSearchView = typedArray.getBoolean(R.styleable.CustomToolbar_show_edit_search_view, false)
            mIsShowEditName = typedArray.getBoolean(R.styleable.CustomToolbar_show_edit_name, false)
            mIsTvTextAllCaps = typedArray.getBoolean(R.styleable.CustomToolbar_tv_action_text_all_caps, false)
            typedArray.recycle()
        }
        mRootView = LayoutInflater.from(context).inflate(R.layout.toolbar_custom_view, this, true)
        mTvTitle = mRootView?.findViewById(R.id.tv_title)
        mImageBack = mRootView?.findViewById(R.id.image_back)
        mImageAction = mRootView?.findViewById(R.id.image_action)
        mImageActionExtend = mRootView?.findViewById(R.id.image_action_extend)
        mTvcAction = mRootView?.findViewById(R.id.tvc_action)
        mTvcActionExtend = mRootView?.findViewById(R.id.tvc_action_extend)
        mEditSearchView = mRootView?.findViewById(R.id.edit_search_view)
        mEditName = mRootView?.findViewById(R.id.edit_name)
        //
        mEditSearchView?.setOnHideKeyboardListener {
            mOnHideKeyboardListener?.onHideKeyboard()
        }
        mEditSearchView?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // todo
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // todo
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mOnSearchToolbar?.onSearch(s.toString())
            }
        })
        mEditSearchView?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mOnSearchToolbar?.searchDone()
                true
            }
            false
        }
        mEditName?.setOnHideKeyboardListener {
            // todo
        }
        mEditName?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // todo
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // todo
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // todo
            }
        })
        mEditName?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mOnEditNameToolbar?.editNameDone()
                true
            }
            false
        }
        updateView()
    }

    private fun updateView() {
        mTvTitle?.text = mTitle
        mTvcAction?.text = mTvAction
    //    mTvcActionExtend?.text = mTvActionExtend
        mEditSearchView?.hint = mHintSearchView
        mEditName?.hint = mHintName
        setResImageBack()
        mImageAction?.setImageResource(mResAction)
        mImageActionExtend?.setImageResource(mResActionExtend)
        //
        mTvTitle?.setTextColor(mTextColorTitle)
        mTvcAction?.setTextColor(mTextColorAction)
        //
        mImageBack?.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                mOnActionToolbarBack?.onBack()
                mOnActionToolbarFull?.onBack()
            }
        })
        mImageAction?.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                mOnActionToolbar?.onAction()
                mOnActionToolbarFull?.onAction()
            }
        })
        mImageActionExtend?.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                mOnActionExtendToolbar?.onActionExtend()
            }
        })
        mTvcAction?.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                mOnTvActionToolbar?.onTvAction()
            }
        })
        //
        setShowBack(mIsShowBack)
        setShowTitle(mIsShowTitle)
        setShowAction(mIsShowAction)
        setShowActionExtend(mIsShowActionExtend)
        setShowTvAction(mIsShowTvAction)
        setShowEditSearchView(mIsShowEditSearchView)
        setShowEditName(mIsShowEditName)
        setTvActionTextAllCaps(mIsTvTextAllCaps)
        mRootView?.setBackgroundColor(mResBackgroundColor)

        mTvcActionExtend?.setOnClickListener {
            setStatusTvActionExtend(mTvcActionExtend?.isSelected != true)
            mOnTvActionExtendToolbar?.onTvActionExtend(mTvcActionExtend?.isSelected == true)
        }
        invalidate()
    }

    fun setTvActionTextAllCaps(isTextAllCaps: Boolean) {
        if (isTextAllCaps) {
            mTvcAction?.text = mTvAction.toUpperCase(Locale.getDefault())
        } else {
            mTvcAction?.text = mTvAction
        }
        invalidate()
    }

    fun setStatusTvActionExtend(isSelected: Boolean) {
        mTvcActionExtend?.isSelected = isSelected
        if (mTvcActionExtend?.isSelected == true) {
            mTvcActionExtend?.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_select_all_active))
            //val topDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_select_all_active)
           // mTvcActionExtend?.setCompoundDrawablesWithIntrinsicBounds(null, topDrawable, null, null)
        } else {
            mTvcActionExtend?.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_select_all_inactive))
            //val topDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_select_all_inactive)
            //mTvcActionExtend?.setCompoundDrawablesWithIntrinsicBounds(null, topDrawable, null, null)
        }
    }

    fun setResImageBack() {
        mImageBack?.setImageResource(mResBack)
        invalidate()
    }

    fun setResImageBack(resBackId: Int) {
        mResBack = resBackId
        setResImageBack()
    }

    open fun setAllCaps(isAllCaps: Boolean) {
        mTvTitle?.isAllCaps = isAllCaps
    }

    fun setShowBack(showBack: Boolean) {
        mIsShowBack = showBack
        mImageBack?.visibility = when {
            showBack -> View.VISIBLE
            mIsInvisibleShowBack -> View.INVISIBLE
            else -> View.GONE
        }
    }

    fun setShowTitle(showTitle: Boolean) {
        mIsShowTitle = showTitle
        mTvTitle?.visibility = when {
            showTitle -> View.VISIBLE
            else -> View.GONE
        }
    }

    fun setShowAction(showAction: Boolean) {
        setShowAction(showAction, true)
    }

    fun setShowAction(showAction: Boolean, useAnimation: Boolean) {
        mIsShowAction = showAction
        if (showAction) {
            if (!useAnimation) {
                mImageAction?.visibility = View.VISIBLE
                return
            }
            val alphaAnimation: ObjectAnimator = ObjectAnimator.ofFloat(mImageAction, ALPHA, 0f, 1f)
            alphaAnimation.apply {
                duration = 1000
                mImageAction?.alpha = 0f
                mImageAction?.visibility = View.VISIBLE
                alphaAnimation.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                        mImageAction?.isEnabled = true
                        mImageAction?.requestFocus()
                    }
                })
                start()
            }
        } else {
            if (!useAnimation) {
                mImageAction?.visibility = View.GONE
                return
            }
            if (mImageAction?.isVisible == true) {
                val alphaAnimation: ObjectAnimator = ObjectAnimator.ofFloat(mImageAction, ALPHA, 1f, 0f)
                alphaAnimation.apply {
                    duration = 1000
                    alphaAnimation.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                            mImageAction?.isEnabled = false
                            mImageAction?.clearFocus()
                        }
                    })
                    start()
                }
            } else {
                mImageAction?.visibility = View.GONE
            }
        }
    }

    open fun isShowAction(): Boolean {
        return mIsShowAction
    }

    open fun isShowEditSearchView(): Boolean {
        return mIsShowEditSearchView
    }

    open fun setShowActionExtend(showActionExtend: Boolean) {
        mIsShowActionExtend = showActionExtend
        mImageActionExtend?.visibility = if (showActionExtend) View.VISIBLE else View.GONE
    }

    open fun setShowTvAction(showTvAction: Boolean) {
        mIsShowTvAction = showTvAction
        mTvcAction?.visibility = if (showTvAction) View.VISIBLE else View.GONE
    }

    open fun setShowTvActionExtend(showTvActionExtend: Boolean) {
        mIsShowTvActionExtend = showTvActionExtend
        mTvcActionExtend?.visibility = if (showTvActionExtend) View.VISIBLE else View.GONE
    }

    open fun setShowEditSearchView(showEditSearchView: Boolean) {
        mIsShowEditSearchView = showEditSearchView
        if (showEditSearchView) {
            val alphaAnimation: ObjectAnimator = ObjectAnimator.ofFloat(mEditSearchView, ALPHA, 0f, 1f)
            alphaAnimation.apply {
                duration = 1000
                mEditSearchView?.alpha = 0f
                mEditSearchView?.visibility = View.VISIBLE
                alphaAnimation.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                        mEditSearchView?.isEnabled = true
                        mEditSearchView?.requestFocus()
                    }
                })
                start()
            }
        } else {
            if (mEditSearchView?.isVisible == true) {
                val alphaAnimation: ObjectAnimator = ObjectAnimator.ofFloat(mEditSearchView, ALPHA, 1f, 0f)
                alphaAnimation.apply {
                    duration = 1000
                    alphaAnimation.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                            mEditSearchView?.isEnabled = false
                            mEditSearchView?.clearFocus()
                        }
                    })
                    start()
                }
            } else {
                mEditSearchView?.visibility = View.GONE
            }
        }
    }

    open fun setShowEditName(showEditName: Boolean) {
        mIsShowEditName = showEditName
        mEditName?.visibility = if (showEditName) View.VISIBLE else View.GONE
    }

    open fun setOnActionToolbarBack(onActionToolbarBack: OnActionToolbarBack?) {
        mOnActionToolbarBack = onActionToolbarBack
    }

    open fun setOnActionToolbar(onActionToolbar: OnActionToolbar?) {
        mOnActionToolbar = onActionToolbar
    }

    open fun setOnActionToolbarFull(onActionToolbarFull: OnActionToolbarFull) {
        mOnActionToolbarFull = onActionToolbarFull
    }

    open fun setOnActionExtendToolbar(onActionExtendToolbar: OnActionExtendToolbar?) {
        mOnActionExtendToolbar = onActionExtendToolbar
    }

    open fun setOnTvActionToolbar(onTvActionToolbar: OnTvActionToolbar?) {
        mOnTvActionToolbar = onTvActionToolbar
    }

    open fun setOnTvActionExtendToolbar(onTvActionExtendToolbar: OnTvActionExtendToolbar) {
        mOnTvActionExtendToolbar = onTvActionExtendToolbar
    }

    fun setOnSearchToolbar(onSearchToolbar: OnSearchToolbar?) {
        mOnSearchToolbar = onSearchToolbar
    }

    fun setOnEditNameToolbar(editNameToolbar: OnEditNameToolbar) {
        mOnEditNameToolbar = editNameToolbar
    }

    fun setOnHideKeyboardListener(onHideKeyboardListener: CustomEditText.OnHideKeyboardListener) {
        mOnHideKeyboardListener = onHideKeyboardListener
    }

    open fun setTitle(title: String) {
        mTitle = title
        mTvTitle?.text = mTitle
    }

    open fun setTitle(resId: Int) {
        mTitle = context.getString(resId)
        mTvTitle?.text = mTitle
    }

    fun getTitle(): String {
        return mTitle
    }

    fun setTvAction(tvAction: String) {
        mTvAction = tvAction
        mTvcAction?.text = mTvAction
    }

    fun setTvActionExtend(tvActionExtend: String) {
        mTvActionExtend = tvActionExtend
       // mTvcActionExtend?.text = mTvActionExtend
    }

    fun getNameText(): String {
        return mEditName?.text.toString()
    }

    fun setNameText(name: String) {
        mEditName?.setText(name)
    }

    fun getEditSearchView(): CustomEditText? {
        return mEditSearchView
    }

    fun getEditName(): CustomEditText? {
        return mEditName
    }


    interface OnActionToolbarBack {
        fun onBack()
    }

    interface OnActionToolbar {
        fun onAction()
    }

    interface OnActionToolbarFull {
        fun onBack()
        fun onAction()
    }

    interface OnActionExtendToolbar {
        fun onActionExtend()
    }

    interface OnTvActionToolbar {
        fun onTvAction()
    }

    interface OnSearchToolbar {
        fun onSearch(searchData: String)
        fun searchDone()
    }

    interface OnEditNameToolbar {
        fun editNameDone()
    }

    interface OnTvActionExtendToolbar {
        fun onTvActionExtend(selectedAll: Boolean)
    }
}