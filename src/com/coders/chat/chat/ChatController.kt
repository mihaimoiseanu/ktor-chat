package com.coders.chat.chat

import com.coders.chat.chat.service.ChatService
import com.coders.chat.user.model.UserModel
import com.coders.chat.user.service.UserService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*

class ChatController(
    private val chatService: ChatService
) {

    fun addRoute(route: Route) {
        addGetChatRoute(route)
        addGetUserChatsRoute(route)
        addCreateChatRoute(route)
    }

    private fun addGetChatRoute(route: Route) {
        route.get("/chat/{chat_id}") {
            val chatId = call.parameters["chat_id"]?.let { UUID.fromString(it) } ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val chat = chatService.getChatById(chatId)
            call.respond(chat)
        }
    }

    private fun addGetUserChatsRoute(route: Route) {
        route.get("/chat") {
            val principalUserId = call.principal<UserModel>()?.id ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val chats = chatService.getChatsForUser(principalUserId)
            call.respond(chats)
        }
    }

    private fun addCreateChatRoute(route: Route) {
        route.post("/chat") {
            val chat = call.receive<Chat>()
            val principalUserId = call.principal<UserModel>()?.id ?: return@post
            val createdChat = chatService.createChat(chat, principalUserId)
            call.respond(HttpStatusCode.Created, createdChat)
        }
    }
}