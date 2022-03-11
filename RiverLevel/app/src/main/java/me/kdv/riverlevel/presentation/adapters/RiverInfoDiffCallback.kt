package me.kdv.riverlevel.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import me.kdv.riverlevel.domain.RiverInfo

class RiverInfoDiffCallback: DiffUtil.ItemCallback<RiverInfo>() {
    override fun areItemsTheSame(oldItem: RiverInfo, newItem: RiverInfo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RiverInfo, newItem: RiverInfo): Boolean {
        return oldItem == newItem
    }
}