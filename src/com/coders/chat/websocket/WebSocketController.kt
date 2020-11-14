package com.coders.chat.websocket

import com.coders.chat.chat.ChatService
import com.coders.chat.message.Message
import com.coders.chat.message.MessagesService
import com.coders.chat.user.UserModel
import com.coders.chat.websocket.event.Event
import com.coders.chat.websocket.event.EventType
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.auth.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.koin.dsl.module
import java.util.*
import kotlin.collections.LinkedHashMap

val webSocketModule = module { single { WebSocketController(get(), get()) } }

class WebSocketController(
    private val messagesService: MessagesService,
    private val chatService: ChatService
) {

    private val clients = Collections.synchronizedMap(LinkedHashMap<UUID, DefaultWebSocketSession>())
    private val objectMapper = ObjectMapper()

    fun addEventRoute(route: Route) {
        route.webSocket("/events") {
            val userId = this.call.principal<UserModel>()?.id ?: return@webSocket
            clients[userId] = this
            readFrames(userId, this)
        }
    }

    private suspend fun readFrames(userId: UUID, session: DefaultWebSocketSession) = coroutineScope {
        try {
            session.incoming
                .consumeAsFlow()
                .mapNotNull { it as? Frame.Text }
                .onEach { handleData(it.readText()) }
                .catch {
                    println("Exception occured")
                    clients.remove(userId)
                }
                .launchIn(this)
        } finally {
            clients.remove(userId)
        }
    }

    private suspend fun handleData(data: String) = coroutineScope {
        val eventType = withContext(Dispatchers.IO) {
            objectMapper.readTree(data).get("type").asText().let { EventType.valueOf(it) }
        }
        when (eventType) {
            EventType.FRIENDSHIP_CREATED -> TODO("DO this ")
            EventType.FRIENDSHIP_UPDATED -> TODO()
            EventType.FRIENDSHIP_DELETED -> TODO()
            EventType.CHAT_CREATED -> TODO()
            EventType.CHAT_UPDATED -> TODO()
            EventType.CHAT_DELETED -> TODO()
            EventType.MESSAGE_CREATED -> handleReceivedMessage(data)
            EventType.MESSAGE_UPDATED -> TODO()
            EventType.MESSAGE_DELETED -> TODO()
        }
    }

    private suspend fun handleReceivedMessage(data: String) = coroutineScope {
        val message = withContext(Dispatchers.IO) {
            objectMapper
                .readTree(data)
                .get("event")
                .asText()
                .let { objectMapper.readValue(it, Message::class.java) }
        }
        val insertedMessage = messagesService.addMessage(message)
        val usersChats = chatService.getChatUsers(insertedMessage.chatId)
        val newEvent = Event(EventType.MESSAGE_CREATED, insertedMessage)
        val frame = withContext(Dispatchers.IO) { objectMapper.writeValueAsString(newEvent) }.let {
            Frame.Text(it)
        }
        usersChats.forEach {
            clients[it]?.send(frame)
        }
    }

}