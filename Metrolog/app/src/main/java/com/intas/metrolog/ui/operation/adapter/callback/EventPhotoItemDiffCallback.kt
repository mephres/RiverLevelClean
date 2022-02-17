package com.intas.metrolog.ui.operation.adapter.callback

import androidx.recyclerview.widget.DiffUtil
import com.intas.metrolog.pojo.event.event_photo.EventPhotoItem

class EventPhotoItemDiffCallback: DiffUtil.ItemCallback<EventPhotoItem>() {
    override fun areItemsTheSame(oldItem: EventPhotoItem, newItem: EventPhotoItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: EventPhotoItem, newItem: EventPhotoItem): Boolean {
        return oldItem == newItem
    }
}