package com.intas.metrolog.ui.chat.select_user.adapter.callback

import androidx.recyclerview.widget.DiffUtil
import com.intas.metrolog.pojo.UserItem

class UserItemDiffCallback : DiffUtil.ItemCallback<UserItem>() {
    override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
        return oldItem == newItem
    }
}
