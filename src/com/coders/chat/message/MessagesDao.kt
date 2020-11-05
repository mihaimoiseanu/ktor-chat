package com.coders.chat.message

import com.coders.chat.chat.Chats
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

internal object MessagesDao : Table() {
    val id = uuid("id").primaryKey()
    val content = text("content")
    val contentType = text("content_type")
    val chatId = reference("chat_id", Chats.id, onDelete = ReferenceOption.CASCADE)
    val senderId = uuid("sender_id")
    val sendingTime = long("sending_time")
}