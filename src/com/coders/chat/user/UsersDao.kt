package com.coders.chat.user

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

internal object Users : Table() {
    val id = uuid("id").primaryKey()
    val userName = varchar("user_name", 50).uniqueIndex()
    val password = varchar("password", 100)
}

internal object UsersDetails : Table() {
    val id = uuid("id").primaryKey()
    val userId = reference("user_id", Users.id, onDelete = ReferenceOption.CASCADE)
    val firstName = varchar("first_name", 50).nullable()
    val lastName = varchar("last_name", 50).nullable()
}