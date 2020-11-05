package com.coders.chat.auth

import com.coders.chat.user.UserModel
import com.coders.chat.user.UsersService
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.dsl.module

val authModule = module { single { AuthController(get(), get()) } } + authRepoModule + authServiceModule

class AuthController(
    private val usersService: UsersService,
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
            usersService.saveUser(user)
            call.respond(HttpStatusCode.Created)
        }
    }
}