package com.intas.metrolog.ui.chat.messages.adapter.callback

import androidx.recyclerview.widget.DiffUtil
import com.intas.metrolog.pojo.chat.MessageItem

class MessageItemDiffCallback : DiffUtil.ItemCallback<MessageItem>() {
    override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
        return oldItem == newItem
    }
}
