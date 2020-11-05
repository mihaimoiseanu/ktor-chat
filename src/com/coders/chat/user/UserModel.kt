package com.coders.chat.user

import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.auth.*
import java.util.*

data class UserModel(
    val id: UUID? = null,
    val userName: String? = null,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val password: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
) : Principal