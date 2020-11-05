package com.coders.chat.message

import java.util.*

data class Message(
    val id: UUID,
    val content: String,
    val contentType: ContentType,
    val chatId: UUID,
    val sendingTime: Long,
    val sendingUser: UUID
)

enum class ContentType {
    TEXT, REFERENCE
}