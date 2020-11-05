package com.coders.chat.user

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.dsl.module
import java.util.*

val userModule = module { single { UsersController(get()) } } + userServiceModule + userRepoModule

class UsersController(private val usersService: UsersService) {

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
            val users = usersService.getUsers()
            call.respond(users)
        }
    }

    private fun addGetUserRoute(route: Route) {
        route.get("/users/{userId}") {
            val userId = call.parameters["userId"] ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val user = usersService.getUserById(UUID.fromString(userId)) ?: run {
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
            val updatedUser = usersService.updateUser(user, principalId)
            call.respond(updatedUser)
        }
    }
}