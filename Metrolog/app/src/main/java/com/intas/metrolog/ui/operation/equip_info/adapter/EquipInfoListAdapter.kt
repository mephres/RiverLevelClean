package com.intas.metrolog.ui.operation.equip_info.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.intas.metrolog.R
import com.intas.metrolog.pojo.equip.EquipInfo
import com.intas.metrolog.ui.operation.equip_info.adapter.callback.EquipInfoItemDiffCallback
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.Util

class EquipInfoListAdapter :
    ListAdapter<EquipInfo, EquipInfoItemViewHolder>(EquipInfoItemDiffCallback()) {

    lateinit var context: Context
    var onEquipInfoItemCheckedListener: ((EquipInfo) -> Unit)? = null
    var onEquipInfoItemUncheckedListener: ((EquipInfo) -> Unit)? = null

    companion object {
        const val MAX_POOL_SIZE = 15
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipInfoItemViewHolder {
        context = parent.context
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.equip_info_item, parent, false)
        return EquipInfoItemViewHolder(view)
    }


    override fun onBindViewHolder(holder: EquipInfoItemViewHolder, position: Int) {
        val equipInfoItem = getItem(position)

        holder.equipInfoTextView.text = equipInfoItem.text

        holder.itemView.setOnClickListener {
            holder.equipInfoCheckBox.isChecked = !holder.equipInfoCheckBox.isChecked
        }

        holder.equipInfoCheckBox.setOnCheckedChangeListener { _, isChecked ->

            Util.authUser?.userId?.let {
                val equipInfo = equipInfoItem.copy(
                    checked = isChecked,
                    checkedDateTime = DateTimeUtil.getUnixDateTimeNow(),
                    checkedUserId = it
                )
                if (isChecked) onEquipInfoItemCheckedListener?.invoke(equipInfo)
                else onEquipInfoItemUncheckedListener?.invoke(equipInfo)
            }
        }
    }

    override fun onViewRecycled(holder: EquipInfoItemViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }
}