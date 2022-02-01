package com.intas.metrolog.pojo.chat

data class ChatItem(
    val id: Int,
    val userName: String,
    val userPosition: String,
    val lastMessage: String,
    val notViewedMessageCount: Int,
    val lastMessageDate: String,
    val companionId: Int,
)