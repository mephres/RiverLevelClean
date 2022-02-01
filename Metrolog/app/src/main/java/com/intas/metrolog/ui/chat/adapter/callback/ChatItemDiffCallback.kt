package com.intas.metrolog.ui.chat.adapter.callback

import androidx.recyclerview.widget.DiffUtil
import com.intas.metrolog.pojo.chat.ChatItem

class ChatItemDiffCallback: DiffUtil.ItemCallback<ChatItem>() {
    override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
        return oldItem.userName == newItem.userName
    }

    override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
        return oldItem == newItem
    }
}
