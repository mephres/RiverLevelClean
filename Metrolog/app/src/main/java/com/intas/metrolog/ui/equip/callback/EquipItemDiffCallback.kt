package com.intas.metrolog.ui.equip.callback

import androidx.recyclerview.widget.DiffUtil
import com.intas.metrolog.pojo.equip.EquipItem

class EquipItemDiffCallback: DiffUtil.ItemCallback<EquipItem>() {
    override fun areItemsTheSame(oldItem: EquipItem, newItem: EquipItem): Boolean {
        return oldItem.equipId == newItem.equipId
    }

    override fun areContentsTheSame(oldItem: EquipItem, newItem: EquipItem): Boolean {
        return oldItem == newItem
    }
}
