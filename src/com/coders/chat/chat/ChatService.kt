package com.coders.chat.chat

import io.ktor.features.*
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
    suspend fun getChatForUsers(usersId: List<UUID>): ChatModel
    suspend fun getChatUsers(chatId: UUID): List<UUID>
}

private class ChatServiceImpl(
    private val chatRepository: ChatRepository
) : ChatService {

    override suspend fun createChat(chat: ChatModel, principalUserId: UUID): ChatModel = coroutineScope {
        val users = chat.users.toMutableSet().also { it.add(principalUserId) }
        if (users.size < 2) {
            throw IllegalStateException("Can't create a chat with no user")
        }
        var chatId = chatRepository.getChatIdForUsers(users.toList())
        if (chatId == null) {
            chatId = chatRepository.createChat()
            users.map { userId ->
                async { chatRepository.addUserToChat(chatId, userId) }
            }.awaitAll()
        }
        getChatById(chatId)
    }

    override suspend fun getChatById(chatId: UUID): ChatModel {
        val chat = chatRepository.getChat(chatId)
        val chatUsers = chatRepository.getChatUsers(chatId)
        return ChatModel(chat.id, chat.creationTime, chatUsers)
    }

    override suspend fun getChatsForUser(userId: UUID): List<ChatModel> = coroutineScope {
        val chatsUserIds = chatRepository.getUserChats(userId)
        chatRepository.getChatsById(chatsUserIds).map { chat ->
            async {
                val chatUsers = chatRepository.getChatUsers(chat.id!!)
                ChatModel(chat.id, chat.creationTime, chatUsers)
            }
        }.awaitAll()
    }

    override suspend fun getChatForUsers(usersId: List<UUID>): ChatModel {
        val chatId = chatRepository.getChatIdForUsers(usersId) ?: throw NotFoundException("Chat not found")
        return getChatById(chatId)
    }

    override suspend fun checkIfUserIsInChat(chatId: UUID, principalId: UUID) {
        val chatUsers = chatRepository.getChatUsers(chatId)
        if (chatUsers.contains(principalId)) return
        throw IllegalStateException("User not in chat")
    }

    override suspend fun getChatUsers(chatId: UUID): List<UUID> {
        return chatRepository.getChatUsers(chatId)
    }
}