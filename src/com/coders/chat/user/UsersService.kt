package com.coders.chat.user

import org.koin.dsl.module
import org.mindrot.jbcrypt.BCrypt
import java.util.*

val userServiceModule = module { single<UsersService> { UsersServiceImpl(get()) } }

interface UsersService {
    suspend fun getUserById(userId: UUID): UserModel?
    suspend fun getUserByUserName(userName: String): UserModel
    suspend fun getUsers(): List<UserModel>
    suspend fun saveUser(user: UserModel): UserModel
    suspend fun updateUser(user: UserModel, principalId: UUID): UserModel
}

private class UsersServiceImpl(
    val usersRepository: UsersRepository
) : UsersService {
    override suspend fun getUserById(userId: UUID): UserModel {
        return usersRepository.getUserById(userId)
    }

    override suspend fun getUserByUserName(userName: String): UserModel {
        return usersRepository.getUserByUserName(userName)
    }

    override suspend fun getUsers(): List<UserModel> {
        return usersRepository.getUsers()
    }

    override suspend fun saveUser(user: UserModel): UserModel {
        val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
        val id = UUID.randomUUID()
        usersRepository.saveUser(
            UserModel(id, user.userName, hashedPassword, null)
        )
        return getUserById(id)
    }

    override suspend fun updateUser(user: UserModel, principalId: UUID): UserModel {
        usersRepository.updateUser(principalId, user)
        return getUserById(principalId)
    }
}