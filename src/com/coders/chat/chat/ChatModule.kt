package com.coders.chat.chat

import com.coders.chat.chat.repository.chatRepositoryModule
import com.coders.chat.chat.service.chatServiceModule
import org.koin.dsl.module

val chatModule = module {
    single { ChatController(get()) }
} + chatServiceModule + chatRepositoryModule