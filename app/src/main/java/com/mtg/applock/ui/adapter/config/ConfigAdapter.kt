package com.mtg.applock.ui.adapter.config


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mtg.applock.R
import com.mtg.applock.data.sqllite.model.ConfigurationModel
import com.mtg.applock.util.extensions.gone
import com.mtg.applock.util.extensions.visible
import com.mtg.library.customview.TagImageView
import kotlinx.android.synthetic.main.item_configuration.view.*

class ConfigAdapter(mContext: Context, private val mConfigurationModelList: MutableList<Any?>, private val mOnSelectedConfigListener: OnSelectedConfigListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            VIEW_ADS -> {
                view = mLayoutInflater.inflate(R.layout.item_native_ads, parent, false)
                AdsHolder(view)
            }
            else ->{
                view = mLayoutInflater.inflate(R.layout.item_configuration, parent, false)
                ConfigHolder(view)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_ITEM -> {
                val holderItem = holder as ConfigHolder
                val configurationModel = mConfigurationModelList[position] as ConfigurationModel
                holderItem.tvName.text = configurationModel.name
                holderItem.tagImageView.setPackageNameList(configurationModel.getPackageAppLockList())
                holderItem.tagImageView.visible()
                holderItem.imageMore.setOnClickListener {
                    mOnSelectedConfigListener.onSelectedMoreConfig(it, configurationModel)
                }
                holderItem.rlRoot.setOnClickListener {
                    mOnSelectedConfigListener.onSelectedConfig(it, configurationModel)
                }
                holderItem.tvTime.text = configurationModel.getTime()
                holderItem.tvDate.text = configurationModel.getDate()
                if (configurationModel.isDefaultSetting) {
                    holderItem.viewLine.gone()
                    holderItem.tvTime.gone()
                    holderItem.tvDate.gone()
                } else {
                    holderItem.viewLine.visible()
                    holderItem.tvTime.visible()
                    holderItem.tvDate.visible()
                }
                if (configurationModel.isActive) {
                    holderItem.imageActive.visible()
                } else {
                    holderItem.imageActive.gone()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(mConfigurationModelList.get(position)==null){
            return VIEW_ADS
        }else
        return VIEW_ITEM
    }

    override fun getItemCount(): Int {
        return mConfigurationModelList.size
    }

    inner class ConfigHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rlRoot: RelativeLayout = view.rlRoot
        val tvName: TextView = view.tvName
        val imageMore: ImageView = view.imageMore
        val tagImageView: TagImageView = view.tagImageView
        val viewLine: View = view.viewLine
        val tvTime: TextView = view.tvTime
        val tvDate: TextView = view.tvDate
        val imageActive: ImageView = view.imageActive
    }

    interface OnSelectedConfigListener {
        fun onSelectedMoreConfig(view: View?, configurationModel: ConfigurationModel)
        fun onSelectedConfig(view: View?, configurationModel: ConfigurationModel)
    }

    companion object {
        /**
         * Integer value for Ads item view type
         */
        const val VIEW_ADS = 1

        /**
         * Integer value for Config item view type
         */
        const val VIEW_ITEM = 2
        private const val MIN_CLICK_INTERVAL: Long = 800
    }
}
