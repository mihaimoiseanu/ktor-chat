package com.coders.chat.auth

import org.koin.dsl.module

val authModule = module {
    single { AuthController(get(), get()) }
} + authRepoModule + authServiceModule