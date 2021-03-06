package com.coders.chat.chat

import com.coders.chat.message.MessagesDao
import com.coders.chat.user.Users
import org.jetbrains.exposed.sql.Table

internal object Chats : Table() {
    val id = uuid("id").primaryKey()
    val creationTime = long("creation_time")
}

internal object ChatsUsers : Table() {
    val chatId = reference("chat_id", Chats.id).primaryKey()
    val userId = reference("user_id", Users.id).primaryKey()
}

internal object ChatsMessages : Table() {
    val chatId = reference("chat_id", Chats.id).primaryKey()
    val chatMessage = reference("chat_message", MessagesDao.id).primaryKey()
}