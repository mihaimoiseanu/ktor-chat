package com.coders.chat.chat

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.dsl.module
import java.util.*

val chatServiceModule = module { single<ChatService> { ChatServiceImpl(get()) } }

interface ChatService {
    suspend fun createChat(chat: ChatModel, principalUserId: UUID): ChatModel
    suspend fun getChatById(chatId: UUID): ChatModel
    suspend fun getChatsForUser(userId: UUID): List<ChatModel>
    suspend fun checkIfUserIsInChat(chatId: UUID, principalId: UUID)
}

private class ChatServiceImpl(
    private val chatRepository: ChatRepository
) : ChatService {

    override suspend fun createChat(chat: ChatModel, principalUserId: UUID): ChatModel = coroutineScope {
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

    override suspend fun getChatById(chatId: UUID): ChatModel {
        val chat = chatRepository.getChat(chatId)
        val chatUsers = chatRepository.getChatUsers(chatId)
        return ChatModel(chat.id, chat.creationTime, chatUsers)
    }

    override suspend fun getChatsForUser(userId: UUID): List<ChatModel> = coroutineScope {
        val chatsUserIds = chatRepository.getUsersChat(userId)
        chatRepository.getChatsById(chatsUserIds).map { chat ->
            async {
                val chatUsers = chatRepository.getChatUsers(chat.id!!)
                ChatModel(chat.id, chat.creationTime, chatUsers)
            }
        }.awaitAll()
    }

    override suspend fun checkIfUserIsInChat(chatId: UUID, principalId: UUID) {
        val chatUsers = chatRepository.getChatUsers(chatId)
        if (chatUsers.contains(principalId)) return
        throw IllegalStateException("User not in chat")
    }
}