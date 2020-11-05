package com.coders.chat.user.persistence

import org.jetbrains.exposed.sql.Table

internal object UsersDetails : Table() {
    val id = uuid("id").primaryKey()
    val userId = uuid("user_id").uniqueIndex().references(Users.id)
    val firstName = varchar("first_name", 50).nullable()
    val lastName = varchar("last_name", 50).nullable()
}