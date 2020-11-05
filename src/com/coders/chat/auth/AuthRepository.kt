package com.coders.chat.auth

import com.coders.chat.dbQuery
import com.coders.chat.user.UserModel
import com.coders.chat.user.Users
import org.jetbrains.exposed.sql.select
import org.koin.dsl.module

val authRepoModule = module { single<AuthRepository> { AuthRepositoryImpl() } }

interface AuthRepository {
    suspend fun getUserByUsername(userName: String): UserModel?
}

private class AuthRepositoryImpl : AuthRepository {
    override suspend fun getUserByUsername(userName: String): UserModel? {
        return dbQuery {
            Users.select {
                Users.userName eq userName
            }.mapNotNull {
                UserModel(
                    id = it[Users.id],
                    userName = it[Users.userName],
                    password = it[Users.password]
                )
            }.firstOrNull()
        }
    }
}