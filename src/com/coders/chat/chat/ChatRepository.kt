package com.coders.chat.chat

import com.coders.chat.dbQuery
import io.ktor.features.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.koin.dsl.module
import java.util.*

val chatRepositoryModule = module { single<ChatRepository> { ChatRepositoryImpl() } }

interface ChatRepository {
    suspend fun getChat(id: UUID): ChatModel
    suspend fun createChat(): UUID
    suspend fun addUserToChat(chatId: UUID, userId: UUID)
    suspend fun getChatUsers(chatId: UUID): List<UUID>
    suspend fun getUserChats(userId: UUID): List<UUID>
    suspend fun getChatsById(chatsUserIds: List<UUID>): List<ChatModel>
    suspend fun getChatIdForUsers(usersIds: List<UUID>): UUID?
}

private class ChatRepositoryImpl : ChatRepository {
    override suspend fun getChat(id: UUID): ChatModel {
        return dbQuery {
            Chats
                .select { Chats.id eq id }
                .mapNotNull { it.toChat() }
                .firstOrNull()
        } ?: throw NotFoundException("Chat with id: $id not found")
    }

    override suspend fun createChat(): UUID {
        val newChatId = UUID.randomUUID()
        dbQuery {
            Chats.insert {
                it[id] = newChatId
                it[this.creationTime] = System.currentTimeMillis()
            }
        }
        return newChatId
    }

    override suspend fun addUserToChat(chatId: UUID, userId: UUID) {
        dbQuery {
            ChatsUsers.insert {
                it[this.chatId] = chatId
                it[this.userId] = userId
            }
        }
    }

    override suspend fun getChatIdForUsers(usersIds: List<UUID>): UUID? {
        return dbQuery {
            ChatsUsers
                .select { ChatsUsers.userId inList usersIds }
                .groupBy { it[ChatsUsers.chatId] }
                .map { it.key }

        }.firstOrNull { checkResultIfMatchUsers(it, usersIds) }
    }

    private suspend fun checkResultIfMatchUsers(chatId: UUID, usersIds: List<UUID>): Boolean {
        val usersInChat = getChatUsers(chatId).toHashSet()
        if (usersInChat.size != usersIds.size) return false
        val usersIdsSet = usersIds.toHashSet()
        return usersInChat.containsAll(usersIds) && usersIdsSet.containsAll(usersInChat)
    }

    override suspend fun getChatUsers(chatId: UUID): List<UUID> {
        return dbQuery {
            ChatsUsers.select { ChatsUsers.chatId eq chatId }
                .mapNotNull { it[ChatsUsers.userId] }
        }
    }

    override suspend fun getUserChats(userId: UUID): List<UUID> {
        return dbQuery {
            ChatsUsers
                .select { ChatsUsers.userId eq userId }
                .mapNotNull { it[ChatsUsers.chatId] }
        }
    }

    override suspend fun getChatsById(chatsUserIds: List<UUID>): List<ChatModel> {
        return dbQuery {
            Chats
                .select { Chats.id inList chatsUserIds }
                .mapNotNull { it.toChat() }
        }
    }

    private fun ResultRow.toChat(): ChatModel = ChatModel(
        this[Chats.id],
        this[Chats.creationTime]
    )
}