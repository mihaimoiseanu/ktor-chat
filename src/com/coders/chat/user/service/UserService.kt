package com.coders.chat.user.service

import com.coders.chat.user.model.UserModel
import com.coders.chat.user.repository.UserRepository
import org.koin.dsl.module
import org.mindrot.jbcrypt.BCrypt
import java.util.*

val userServiceModule = module { single<UserService> { UserServiceImpl(get()) } }

interface UserService {
    suspend fun getUserById(userId: UUID): UserModel?
    suspend fun getUserByUserName(userName: String): UserModel
    suspend fun getUsers(): List<UserModel>
    suspend fun saveUser(user: UserModel): UserModel
    suspend fun updateUser(user: UserModel, principalId: UUID): UserModel
}

private class UserServiceImpl(
    val userRepository: UserRepository
) : UserService {
    override suspend fun getUserById(userId: UUID): UserModel {
        return userRepository.getUserById(userId)
    }

    override suspend fun getUserByUserName(userName: String): UserModel {
        return userRepository.getUserByUserName(userName)
    }

    override suspend fun getUsers(): List<UserModel> {
        return userRepository.getUsers()
    }

    override suspend fun saveUser(user: UserModel): UserModel {
        val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
        val id = UUID.randomUUID()
        userRepository.saveUser(
            UserModel(id, user.userName, hashedPassword, null)
        )
        return getUserById(id)
    }

    override suspend fun updateUser(user: UserModel, principalId: UUID): UserModel {
        userRepository.updateUser(principalId, user)
        return getUserById(principalId)
    }
}