package com.intas.metrolog.ui.operation.equip_info.adapter.callback

import androidx.recyclerview.widget.DiffUtil
import com.intas.metrolog.pojo.equip.EquipInfo
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem

class EquipInfoItemDiffCallback : DiffUtil.ItemCallback<EquipInfo>() {
    override fun areItemsTheSame(oldItem: EquipInfo, newItem: EquipInfo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: EquipInfo, newItem: EquipInfo): Boolean {
        return oldItem == newItem
    }
}