package com.coders.chat.user

import com.coders.chat.user.repository.userRepoModule
import com.coders.chat.user.service.userServiceModule
import org.koin.dsl.module

val userModule = module {
    single { UsersController(get()) }
} + userServiceModule + userRepoModule