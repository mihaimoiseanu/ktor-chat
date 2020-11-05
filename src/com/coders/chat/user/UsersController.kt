package com.coders.chat.user

import com.coders.chat.user.model.UserModel
import com.coders.chat.user.service.UserService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*

class UsersController(private val userService: UserService) {

    fun addRoutes(route: Route) {
        addPrincipalRoute(route)
        addGetUsersRoute(route)
        addGetUserRoute(route)
        addUpdateUserRoute(route)
    }

    private fun addPrincipalRoute(route: Route) {
        route.get("/principal") {
            val principal = call.principal<UserModel>() ?: run {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }
            call.respond(principal)
        }
    }

    private fun addGetUsersRoute(route: Route) {
        route.get("/users") {
            val users = userService.getUsers()
            call.respond(users)
        }
    }

    private fun addGetUserRoute(route: Route) {
        route.get("/users/{userId}") {
            val userId = call.parameters["userId"] ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val user = userService.getUserById(UUID.fromString(userId)) ?: run {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@get
            }
            call.respond(user)
        }
    }

    private fun addUpdateUserRoute(route: Route) {
        route.put("/users") {
            val user = call.receive<UserModel>()
            val principalId = call.principal<UserModel>()?.id ?: run {
                call.respond(HttpStatusCode.Forbidden)
                return@put
            }
            val updatedUser = userService.updateUser(user, principalId)
            call.respond(updatedUser)
        }
    }
}