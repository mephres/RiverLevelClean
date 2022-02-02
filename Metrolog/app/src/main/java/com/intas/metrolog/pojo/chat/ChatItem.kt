package com.intas.metrolog.pojo.chat

import com.intas.metrolog.pojo.UserItem

data class ChatItem(
    val id: Int,
    val lastMessage: String,
    var notViewedMessageCount: Int = 0,
    val lastMessageDate: Long,
    val companion: UserItem,
)