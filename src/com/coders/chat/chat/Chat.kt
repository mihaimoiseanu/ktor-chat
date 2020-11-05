package com.coders.chat.chat

import java.util.*

data class Chat(
    val id: UUID? = null,
    val creationTime: Long = System.currentTimeMillis(),
    val users: List<UUID> = emptyList()
)