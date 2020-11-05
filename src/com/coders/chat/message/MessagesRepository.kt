package com.coders.chat.message

import com.coders.chat.dbQuery
import io.ktor.features.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.koin.dsl.module
import java.util.*

val messagesRepositoryModule = module { single<MessagesRepository> { MessagesRepositoryImpl() } }

interface MessagesRepository {
    suspend fun getMessagesForChat(chatId: UUID): List<Message>
    suspend fun addMessage(message: Message): Message
    suspend fun getMessageById(messageId: UUID): Message

}

private class MessagesRepositoryImpl : MessagesRepository {

    override suspend fun getMessagesForChat(chatId: UUID): List<Message> {
        return dbQuery {
            MessagesDao
                .select { MessagesDao.chatId eq chatId }
                .mapNotNull { it.toMessage() }
        }
    }

    override suspend fun addMessage(message: Message): Message {
        val messageId = UUID.randomUUID()
        dbQuery {
            MessagesDao.insert {
                it[id] = messageId
                it[content] = message.content
                it[contentType] = message.contentType.name
                it[chatId] = message.chatId
                it[sendingTime] = message.sendingTime
                it[senderId] = message.sendingUser
            }
        }
        return getMessageById(messageId)
    }

    override suspend fun getMessageById(messageId: UUID): Message {
        return dbQuery {
            MessagesDao.select { MessagesDao.id eq messageId }
                .firstOrNull()
                ?.toMessage() ?: throw NotFoundException("Message with id: $messageId not found")
        }
    }

    private fun ResultRow.toMessage() = Message(
        this[MessagesDao.id],
        this[MessagesDao.content],
        ContentType.valueOf(this[MessagesDao.contentType]),
        this[MessagesDao.chatId],
        this[MessagesDao.sendingTime],
        this[MessagesDao.senderId]
    )
}