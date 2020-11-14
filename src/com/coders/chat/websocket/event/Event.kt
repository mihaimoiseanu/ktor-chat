package com.coders.chat.websocket.event

data class Event(
    val type: EventType,
    val event: Any
)

enum class EventType {

    FRIENDSHIP_CREATED,
    FRIENDSHIP_UPDATED,
    FRIENDSHIP_DELETED,

    CHAT_CREATED,
    CHAT_UPDATED,
    CHAT_DELETED,

    MESSAGE_CREATED,
    MESSAGE_UPDATED,
    MESSAGE_DELETED
}