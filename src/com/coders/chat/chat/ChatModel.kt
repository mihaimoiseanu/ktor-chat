package com.coders.chat.chat

import java.util.*

data class ChatModel(
    val id: UUID? = null,
    val creationTime: Long = System.currentTimeMillis(),
    val users: List<UUID> = emptyList()
)