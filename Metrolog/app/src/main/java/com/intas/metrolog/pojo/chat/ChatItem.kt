package com.intas.metrolog.pojo.chat

import com.intas.metrolog.pojo.UserItem

data class ChatItem(
    val id: Int,
    val lastMessage: String,
    val notViewedMessageCount: Int,
    val lastMessageDate: Long,
    val companion: UserItem,
)