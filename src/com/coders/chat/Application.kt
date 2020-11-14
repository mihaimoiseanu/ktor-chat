package com.coders.chat

import com.coders.chat.auth.AuthController
import com.coders.chat.auth.AuthService
import com.coders.chat.auth.authModule
import com.coders.chat.chat.ChatController
import com.coders.chat.chat.chatModule
import com.coders.chat.message.messagesModule
import com.coders.chat.user.UsersController
import com.coders.chat.user.userModule
import com.coders.chat.websocket.WebSocketController
import com.coders.chat.websocket.webSocketModule
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import java.sql.SQLException
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val authService: AuthService by inject()
    val authController: AuthController by inject()
    val usersController: UsersController by inject()
    val chatController: ChatController by inject()
    val webSocketController: WebSocketController by inject()

    install(DefaultHeaders)

    install(Koin) {
        modules(authModule + userModule + chatModule + messagesModule + webSocketModule)
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
            verifier(authService.jwtVerifier)
            this.realm = authService.realm
            validate {
                authService.verifyPayloadAndReturnUser(it.payload)
            }
        }
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(60) // Disabled (null) by default
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE // Disabled (max value). The connection will be closed if surpassed this length.
        masking = false
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
            chatController.addChatRoutes(this)
            webSocketController.addEventRoute(this)
        }
    }
}