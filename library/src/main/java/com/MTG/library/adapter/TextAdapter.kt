package com.MTG.library.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.MTG.library.R
import kotlinx.android.synthetic.main.item_recycler_text.view.*

class TextAdapter(private val mContext: Context, private val mTextList: List<String>) : RecyclerView.Adapter<TextAdapter.TextHolder>() {
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextAdapter.TextHolder {
        val view = mLayoutInflater.inflate(R.layout.item_recycler_text, parent, false)
        return TextHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TextAdapter.TextHolder, position: Int) {
        val text = mTextList[position]
        holder.tvLine.text = text
    }

    override fun getItemCount(): Int {
        return mTextList.size
    }

    inner class TextHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLine: TextView = view.tvLine
    }
}
