package com.intas.metrolog.ui.operation.adapter.callback

import androidx.recyclerview.widget.DiffUtil
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem

class EventOperationItemDiffCallback : DiffUtil.ItemCallback<EventOperationItem>() {
    override fun areItemsTheSame(oldItem: EventOperationItem, newItem: EventOperationItem): Boolean {
        return oldItem.subId == newItem.subId
    }

    override fun areContentsTheSame(oldItem: EventOperationItem, newItem: EventOperationItem): Boolean {
        return oldItem == newItem
    }
}