package com.coders.chat.message

import org.jetbrains.exposed.sql.Table

internal object Messages : Table() {
    val id = uuid("id").primaryKey()
    val content = text("content")
    val contentType = integer("content_type")
    val chatId = uuid("chat_id")
    val senderId = uuid("sender_id")
    val sendingTime = long("sending_time")
}