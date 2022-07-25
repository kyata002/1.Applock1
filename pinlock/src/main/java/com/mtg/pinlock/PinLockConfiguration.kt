package com.mtg.pinlock

import java.io.Serializable

class PinLockConfiguration private constructor(builder: Builder) : Serializable {
    private var mMode: Int = Config.MODE_CREATE
    private var mCodeLength: Int = Config.CODE_LENGTH_DEFAULT
    private var mCodeLengthSuccess: Int = Config.CODE_LENGTH_DEFAULT
    private var mResBackgroundId = 0
    private var mResPinList = mutableListOf<Int>()
    private var mIsDeletePadding = false
    private var mIsNumberPadding = true
    private var mDeletePadding = 0
    private var mResSelectorCheckbox = 0
    private var mColorCheckbox = ""
    private var mResColorMessage = 0
    private var mColorMessage = "#fff"
    private var mResColorButton = 0
    private var mThemeConfig: ThemeConfig? = null
    private var mNextTitle: String = ""

    // online
    private var mBackgroundUrl: String = ""
    private var mPinUrlList = mutableListOf<String>()

    init {
        mMode = builder.getMode()
        mCodeLength = builder.getCodeLength()
        mCodeLengthSuccess = builder.getCodeLengthSuccess()
        mResBackgroundId = builder.getResBackgroundId()
        mResPinList = builder.getResPinList()
        mIsDeletePadding = builder.isDeletePadding()
        mIsNumberPadding = builder.isNumberPadding()
        mDeletePadding = builder.getDeletePadding()
        mResSelectorCheckbox = builder.getResSelectorCheckbox()
        mResColorMessage = builder.getResColorMessage()
        mResColorButton = builder.getResColorButton()
        mThemeConfig = builder.getThemeConfig()
        mNextTitle = builder.getNextTitle()
        mBackgroundUrl = builder.getBackgroundUrl()
        mPinUrlList = builder.getPinUrlList()
        mColorMessage = builder.getColorMessage()
        mColorCheckbox = builder.getColorCheckbox()
    }

    fun getMode(): Int {
        return mMode
    }

    fun isCreate(): Boolean {
        return mMode == Config.MODE_CREATE
    }

    fun getCodeLength(): Int {
        return mCodeLength
    }

    fun getCodeLengthSuccess(): Int {
        return mCodeLengthSuccess
    }

    fun setCodeLengthSuccess(codeLength: Int) {
        mCodeLengthSuccess = codeLength
    }

    fun getResBackgroundId(): Int {
        return mResBackgroundId
    }

    fun getResPinList(): MutableList<Int> {
        return mResPinList
    }

    fun isDeletePadding(): Boolean {
        return mIsDeletePadding
    }

    fun getDeletePadding(): Int {
        return mDeletePadding
    }

    fun isNumberPadding(): Boolean {
        return mIsNumberPadding
    }

    fun getResSelectorCheckbox(): Int {
        return mResSelectorCheckbox
    }

    fun getResColorMessage(): Int {
        return mResColorMessage
    }

    fun getResColorButton(): Int {
        return mResColorButton
    }

    fun getThemeConfig(): ThemeConfig? {
        return mThemeConfig
    }

    fun getNextTitle(): String {
        return mNextTitle
    }

    fun getBackgroundUrl(): String {
        return mBackgroundUrl
    }

    fun getPinUrlList(): MutableList<String> {
        return mPinUrlList
    }

    fun getColorMessage(): String {
        return mColorMessage
    }

    fun getColorCheckbox(): String {
        return mColorCheckbox
    }

    open class Builder {
        private var mMode: Int = Config.MODE_CREATE
        private var mCodeLength: Int = Config.CODE_LENGTH_DEFAULT
        private var mCodeLengthSuccess: Int = Config.CODE_LENGTH_DEFAULT
        private var mResBackgroundId = 0
        private var mResIcon = 0
        private var mResPinList = mutableListOf<Int>()
        private var mIsDeletePadding = false
        private var mIsNumberPadding = true
        private var mDeletePadding = 0
        private var mResSelectorCheckbox = 0
        private var mColorCheckbox = ""
        private var mResColorMessage = 0
        private var mColorMessage = ""
        private var mResColorButton = 0
        private var mThemeConfig: ThemeConfig? = null
        private var mNextTitle: String = ""

        // online
        private var mBackgroundUrl: String = ""
        private var mPinUrlList = mutableListOf<String>()

        fun setMode(mode: Int): Builder {
            mMode = mode
            return this
        }

        fun getMode(): Int {
            return mMode
        }

        fun setCodeLength(codeLength: Int): Builder {
            mCodeLength = if (codeLength >= 4) codeLength else 4
            return this
        }

        fun getCodeLength(): Int {
            return mCodeLength
        }

        fun setCodeLengthSuccess(codeLength: Int): Builder {
            mCodeLengthSuccess = if (codeLength >= 4) codeLength else 4
            return this
        }

        fun getCodeLengthSuccess(): Int {
            return mCodeLengthSuccess
        }

        fun setResBackgroundId(resBackgroundId: Int): Builder {
            mResBackgroundId = resBackgroundId
            return this
        }

        fun getResBackgroundId(): Int {
            return mResBackgroundId
        }

        fun setResPinList(resPinList: MutableList<Int>): Builder {
            mResPinList = resPinList
            return this
        }

        fun getResPinList(): MutableList<Int> {
            return mResPinList
        }

        fun setDeletePadding(isDeletePadding: Boolean): Builder {
            mIsDeletePadding = isDeletePadding
            return this
        }

        fun isDeletePadding(): Boolean {
            return mIsDeletePadding
        }

        fun setNumberPadding(isNumberPadding: Boolean): Builder {
            mIsNumberPadding = isNumberPadding
            return this
        }

        fun isNumberPadding(): Boolean {
            return mIsNumberPadding
        }


        fun setDeletePadding(deletePadding: Int): Builder {
            mDeletePadding = deletePadding
            return this
        }

        fun getDeletePadding(): Int {
            return mDeletePadding
        }

        fun setResSelectorCheckbox(resSelectorCheckbox: Int): Builder {
            mResSelectorCheckbox = resSelectorCheckbox
            return this
        }

        fun getResSelectorCheckbox(): Int {
            return mResSelectorCheckbox
        }

        fun setResColorMessage(resColorMessage: Int): Builder {
            mResColorMessage = resColorMessage
            return this
        }

        fun getResColorMessage(): Int {
            return mResColorMessage
        }

        fun setResColorButton(resColorButton: Int): Builder {
            mResColorButton = resColorButton
            return this
        }

        fun getResColorButton(): Int {
            return mResColorButton
        }

        fun setThemeConfig(themeConfig: ThemeConfig?): Builder {
            mThemeConfig = themeConfig
            return this
        }

        fun getThemeConfig(): ThemeConfig? {
            return mThemeConfig
        }

        fun setNextTitle(nextTitle: String): Builder {
            mNextTitle = nextTitle
            return this
        }

        fun getNextTitle(): String {
            return mNextTitle
        }

        fun setBackgroundUrl(backgroundUrl: String): Builder {
            mBackgroundUrl = backgroundUrl
            return this
        }

        fun getBackgroundUrl(): String {
            return mBackgroundUrl
        }

        fun setPinUrlList(pinUrlList: MutableList<String>): Builder {
            mPinUrlList = pinUrlList
            return this
        }

        fun getPinUrlList(): MutableList<String> {
            return mPinUrlList
        }

        fun setColorMessage(colorMessage: String): Builder {
            mColorMessage = colorMessage
            return this
        }

        fun getColorMessage(): String {
            return mColorMessage
        }

        fun setColorCheckbox(colorCheckbox: String): Builder {
            mColorCheckbox = colorCheckbox
            return this
        }

        fun getColorCheckbox(): String {
            return mColorCheckbox
        }

        fun build(): PinLockConfiguration {
            return PinLockConfiguration(this)
        }
    }

    object Config {
        const val MODE_CREATE = 0
        const val MODE_AUTH = 1
        const val CODE_LENGTH_DEFAULT = 6
        const val TYPE_START = 0
        const val TYPE_CONFIRM = 1
        const val TYPE_ERROR = 2
    }
}