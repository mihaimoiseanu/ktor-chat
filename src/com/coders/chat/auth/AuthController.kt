package com.coders.chat.auth

import com.coders.chat.user.model.UserModel
import com.coders.chat.user.service.UserService
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

class AuthController(
    private val userService: UserService,
    private val authService: AuthService
) {
    fun addRoutes(route: Route) {
        addLoginRoute(route)
        addRegisterRoute(route)
    }

    private fun addLoginRoute(route: Route) {
        route.get("/login") {
            val credentials = call.request.headers["Authorization"] ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val token = authService.checkCredentialsAndGetToken(credentials) ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid credentials")
            }
            call.response.headers.append("Authorization", "Bearer $token")
            call.respond(HttpStatusCode.Accepted)
        }
    }

    private fun addRegisterRoute(route: Route) {
        route.post("/register") {
            val user = call.receive<UserModel>()
            userService.saveUser(user)
            call.respond(HttpStatusCode.Created)
        }
    }
}