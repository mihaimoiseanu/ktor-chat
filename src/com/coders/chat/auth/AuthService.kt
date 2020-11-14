package com.coders.chat.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import com.coders.chat.user.UserModel
import org.koin.dsl.module
import org.mindrot.jbcrypt.BCrypt
import java.util.*

val authServiceModule = module {
    single {
        JwtProperties(
            getProperty("domain"),
            getProperty("audience"),
            getProperty("realm"),
            getProperty("validityInMs")
        )
    }
    single<AuthService> { AuthServiceImpl(get(), get()) }
}

interface AuthService {
    val realm: String
    suspend fun checkCredentialsAndGetToken(credentials: String): String?
    val jwtVerifier: JWTVerifier
    suspend fun verifyPayloadAndReturnUser(payload: Payload): UserModel
}

private class AuthServiceImpl(
    private val authRepository: AuthRepository,
    private val jwtProperties: JwtProperties
) : AuthService {

    private val algorithm = Algorithm.HMAC256("my_awesome_secret")
    override val realm: String
        get() = jwtProperties.realm

    override suspend fun checkCredentialsAndGetToken(credentials: String): String? {
        val (userName, password) = getUserFromAuthorization(credentials)
        val userFromDb = authRepository.getUserByUsername(userName) ?: return null
        if (!BCrypt.checkpw(password, userFromDb.password)) return null
        return generateTokenForUser(userFromDb)
    }

    private fun getUserFromAuthorization(basicAuth: String): Pair<String, String> {
        val basic = basicAuth.removePrefix("Basic").trim()
        val credentials = Base64.getDecoder().decode(basic).let { String(it) }.split(":")
        val userName = credentials[0]
        val password = credentials[1]
        return Pair(userName, password)
    }

    private fun generateTokenForUser(authUser: UserModel): String = JWT.create()
        .withSubject("Authentication")
        .withAudience(jwtProperties.audience)
        .withIssuer(jwtProperties.domain)
        .withClaim("username", authUser.userName)
        .withClaim("password", authUser.password)
        .withExpiresAt(getExpiration())  // optional
        .sign(algorithm)

    private fun getExpiration() = Date(System.currentTimeMillis() + jwtProperties.validityInMs)

    override val jwtVerifier: JWTVerifier =
        JWT.require(algorithm)
            .withAudience(jwtProperties.audience)
            .withIssuer(jwtProperties.domain)
            .withSubject("Authentication")
            .build()

    override suspend fun verifyPayloadAndReturnUser(payload: Payload): UserModel {
        val username = payload.getClaim("username").asString()
        val pass = payload.getClaim("password").asString()
        val user = authRepository.getUserByUsername(username) ?: throw IllegalStateException("Invalid token")
        if (user.password != pass) throw IllegalStateException("Invalid token")
        return user
    }
}
