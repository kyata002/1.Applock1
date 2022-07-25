package com.mtg.library.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.mtg.library.R
import com.mtg.library.adapter.TextAdapter
import kotlinx.android.synthetic.main.long_text_view.view.*

class LongTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val mTextList = mutableListOf<String>()
    private var mAdapter: TextAdapter

    init {
        LayoutInflater.from(context).inflate(R.layout.long_text_view, this, true)
        mAdapter = TextAdapter(context, mTextList)
        recyclerText.adapter = mAdapter
    }

    fun setText(text: String) {
        val listText = text.split("\n")
        mTextList.clear()
        mTextList.addAll(listText)
        mAdapter.notifyDataSetChanged()
    }

    fun addText(text: String) {
        mTextList.add(text)
        mAdapter.notifyItemInserted(mTextList.size)
    }
}