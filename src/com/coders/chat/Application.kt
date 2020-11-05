package com.coders.chat

import com.coders.chat.auth.AuthController
import com.coders.chat.auth.AuthService
import com.coders.chat.auth.authModule
import com.coders.chat.chat.ChatController
import com.coders.chat.chat.chatModule
import com.coders.chat.user.UsersController
import com.coders.chat.user.userModule
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import java.sql.SQLException

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val authService: AuthService by inject()
    val authController: AuthController by inject()
    val usersController: UsersController by inject()
    val chatController: ChatController by inject()

    install(DefaultHeaders)

    install(Koin) {
        modules(authModule + userModule + chatModule)
        this.fileProperties("/jwt.properties")
    }
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        }
    }

    install(Authentication) {
        jwt {
            verifier(authService.makeJwtVerifier())
            this.realm = authService.realm
            validate {
                authService.verifyPayloadAndReturnUser(it.payload)
            }
        }
    }

    install(StatusPages) {
        exception<SQLException> {
            call.respond(HttpStatusCode.Conflict, it.message ?: "")
        }
        exception<NotFoundException> {
            call.respond(HttpStatusCode.NotFound, it.message ?: "")
        }
        exception<IllegalStateException> {
            call.respond(HttpStatusCode.Forbidden, it.message ?: "")
        }
        exception<IllegalArgumentException> {
            call.respond(HttpStatusCode.BadRequest, it.message ?: "")
        }
    }

    routing {
        authController.addRoutes(this)
        authenticate {
            usersController.addRoutes(this)
            chatController.addRoute(this)
        }
    }
}