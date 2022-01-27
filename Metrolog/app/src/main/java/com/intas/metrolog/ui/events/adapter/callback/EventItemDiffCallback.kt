package com.intas.metrolog.ui.events.adapter.callback

import androidx.recyclerview.widget.DiffUtil
import com.intas.metrolog.pojo.event.EventItem

class EventItemDiffCallback : DiffUtil.ItemCallback<EventItem>() {
    override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
        return oldItem.opId == newItem.opId
    }

    override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
        return oldItem == newItem
    }
}