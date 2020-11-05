package com.coders.chat.chat.service

import com.coders.chat.chat.Chat
import com.coders.chat.chat.repository.ChatRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.dsl.module
import java.util.*

val chatServiceModule = module { single<ChatService> { ChatServiceImpl(get()) } }

interface ChatService {
    suspend fun createChat(chat: Chat, principalUserId: UUID): Chat
    suspend fun getChatById(chatId: UUID): Chat
    suspend fun getChatsForUser(userId: UUID): List<Chat>
}

private class ChatServiceImpl(
    private val chatRepository: ChatRepository
) : ChatService {

    override suspend fun createChat(chat: Chat, principalUserId: UUID): Chat = coroutineScope {
        val users = chat.users.toMutableSet().also { it.add(principalUserId) }
        if (users.size < 2) {
            throw IllegalStateException("Can't create a chat with no user")
        }
        val chatId = chatRepository.createChat()
        users.map { userId ->
            async { chatRepository.addUserToChat(chatId, userId) }
        }.awaitAll()
        getChatById(chatId)
    }

    override suspend fun getChatById(chatId: UUID): Chat {
        val chat = chatRepository.getChat(chatId)
        val chatUsers = chatRepository.getChatUsers(chatId)
        return Chat(chat.id, chat.creationTime, chatUsers)
    }

    override suspend fun getChatsForUser(userId: UUID): List<Chat> = coroutineScope {
        val chatsUserIds = chatRepository.getUsersChat(userId)
        chatRepository.getChatsById(chatsUserIds).map { chat ->
            async {
                val chatUsers = chatRepository.getChatUsers(chat.id!!)
                Chat(chat.id, chat.creationTime, chatUsers)
            }
        }.awaitAll()
    }
}