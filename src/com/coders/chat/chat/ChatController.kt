package com.coders.chat.chat

import com.coders.chat.message.MessagesService
import com.coders.chat.user.UserModel
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.dsl.module
import java.util.*

val chatModule = module { single { ChatController(get(), get()) } } + chatServiceModule + chatRepositoryModule

class ChatController(
    private val chatService: ChatService,
    private val messagesService: MessagesService
) {

    fun addChatRoutes(route: Route) {
        route.apply {
            addGetChatRoute()
            addGetUserChatsRoute()
            addCreateChatRoute()
            addGetMessagesRoute()
            addGetChatForUsers()
        }
    }

    private fun Route.addGetChatRoute() {
        get("/chat/{chat_id}") {
            val chatId = call.parameters["chat_id"]?.let { UUID.fromString(it) } ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val chat = chatService.getChatById(chatId)
            call.respond(chat)
        }
    }

    private fun Route.addGetUserChatsRoute() {
        get("/chat") {
            val principalUserId = call.principal<UserModel>()?.id ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val chats = chatService.getChatsForUser(principalUserId)
            call.respond(chats)
        }
    }

    private fun Route.addCreateChatRoute() {
        post("/chat") {
            val chat = call.receive<ChatModel>()
            val principalUserId = call.principal<UserModel>()?.id ?: return@post
            val createdChat = chatService.createChat(chat, principalUserId)
            call.respond(HttpStatusCode.Created, createdChat)
        }
    }

    private fun Route.addGetChatForUsers() {
        get("/chat/for") {
            val users = call.receive<List<String>>().map { UUID.fromString(it) }.toMutableSet().also {
                val principalUserId = call.principal<UserModel>()?.id!!
                it.add(principalUserId)
            }.toList()
            val chatId = chatService.getChatForUsers(users)
            call.respond(chatId)
        }
    }

    private fun Route.addGetMessagesRoute() {
        get("/{chat_id}/messages") {
            val chatId = call.parameters["chat_id"]?.let { UUID.fromString(it) } ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val principalId = call.principal<UserModel>()?.id ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            chatService.checkIfUserIsInChat(chatId, principalId)
            val messages = messagesService.getMessagesForChat(chatId)
            call.respond(messages)
        }
    }
}