package com.coders.chat.message

import org.koin.dsl.module
import java.util.*

val messagesServiceModule = module { single<MessagesService> { MessagesServiceImpl(get()) } }

interface MessagesService {
    suspend fun getMessagesForChat(chatId: UUID): List<Message>
    suspend fun addMessage(message: Message): Message
}

private class MessagesServiceImpl(private val messagesRepository: MessagesRepository) : MessagesService {

    override suspend fun getMessagesForChat(chatId: UUID): List<Message> {
        return messagesRepository.getMessagesForChat(chatId)
    }

    override suspend fun addMessage(message: Message): Message {
        return messagesRepository.addMessage(message)
    }
}