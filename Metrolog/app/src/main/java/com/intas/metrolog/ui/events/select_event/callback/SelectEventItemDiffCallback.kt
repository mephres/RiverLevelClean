package com.intas.metrolog.ui.events.select_event.callback

import androidx.recyclerview.widget.DiffUtil
import com.intas.metrolog.pojo.event.EventItem

class SelectEventItemDiffCallback: DiffUtil.ItemCallback<EventItem>() {
    override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
        return oldItem.equipId == newItem.equipId
    }

    override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
        return oldItem == newItem
    }
}
