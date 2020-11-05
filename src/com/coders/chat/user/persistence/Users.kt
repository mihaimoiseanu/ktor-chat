package com.coders.chat.user.persistence

import org.jetbrains.exposed.sql.Table

internal object Users : Table() {
    val id = uuid("id").primaryKey()
    val userName = varchar("user_name", 50).uniqueIndex()
    val password = varchar("password", 100)
}