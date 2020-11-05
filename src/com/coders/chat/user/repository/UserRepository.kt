package com.coders.chat.user.repository

import com.coders.chat.dbQuery
import com.coders.chat.user.model.UserModel
import com.coders.chat.user.persistence.Users
import com.coders.chat.user.persistence.UsersDetails
import io.ktor.features.*
import org.jetbrains.exposed.sql.*
import org.koin.dsl.module
import java.util.*

val userRepoModule = module { single<UserRepository> { UserRepositoryImpl() } }

interface UserRepository {
    suspend fun getUsers(): List<UserModel>
    suspend fun getUserById(userId: UUID): UserModel
    suspend fun saveUser(userToSave: UserModel)
    suspend fun getUserByUserName(userName: String): UserModel
    suspend fun updateUser(userId: UUID, user: UserModel)
}

private class UserRepositoryImpl : UserRepository {

    override suspend fun getUsers(): List<UserModel> {
        return dbQuery {
            (Users leftJoin UsersDetails)
                .selectAll()
                .mapNotNull { it.toUser() }
        }
    }

    override suspend fun getUserById(userId: UUID): UserModel {
        return dbQuery {
            (Users leftJoin UsersDetails)
                .select { Users.id eq userId }
                .mapNotNull { it.toUser() }
                .firstOrNull()
        } ?: throw NotFoundException("User not found")
    }

    override suspend fun saveUser(userToSave: UserModel) {
        dbQuery {
            Users.insert {
                it[id] = userToSave.id ?: UUID.randomUUID()
                it[userName] = userToSave.userName ?: throw IllegalArgumentException("Missing field user_name")
                it[password] = userToSave.password ?: throw IllegalArgumentException("Missing field password")
            }
        }
    }

    override suspend fun getUserByUserName(userName: String): UserModel {
        return dbQuery {
            (Users leftJoin UsersDetails)
                .select { Users.userName eq userName }
                .mapNotNull { it.toUser() }
                .firstOrNull()
        } ?: throw NotFoundException("User not found")
    }

    override suspend fun updateUser(userId: UUID, user: UserModel) {
        val userDetails = dbQuery {
            UsersDetails
                .select { UsersDetails.userId eq userId }
                .firstOrNull()
        }
        if (userDetails == null)
            createUserDetails(userId, user)
        else
            updateUserDetails(userId, user)
    }

    private suspend fun createUserDetails(userId: UUID, user: UserModel) {
        dbQuery {
            UsersDetails.insert {
                it[id] = UUID.randomUUID()
                it[this.userId] = userId
                it[this.firstName] = user.firstName ?: ""
                it[this.lastName] = user.lastName ?: ""
            }
        }
    }

    private suspend fun updateUserDetails(userId: UUID, user: UserModel) {
        dbQuery {
            UsersDetails.update({ UsersDetails.userId eq userId }) {
                user.firstName?.let { firstName -> it[this.firstName] = firstName }
                user.lastName?.let { lastName -> it[this.lastName] = lastName }
            }
        }
    }

    private fun ResultRow.toUser(): UserModel = UserModel(
        this[Users.id],
        this[Users.userName],
        this[Users.password],
        this[UsersDetails.firstName],
        this[UsersDetails.lastName],
    )
}