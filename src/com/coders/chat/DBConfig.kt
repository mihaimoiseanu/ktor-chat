package com.coders.chat

import com.coders.chat.chat.persistence.Chats
import com.coders.chat.chat.persistence.ChatsMessages
import com.coders.chat.chat.persistence.ChatsUsers
import com.coders.chat.message.Messages
import com.coders.chat.user.persistence.Users
import com.coders.chat.user.persistence.UsersDetails
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("unused") // Referenced in application.conf
fun Application.dbModule() {
    initDb()
}

internal fun initDb() {
    val config = HikariConfig("/hikari.properties")
    config.schema = "chat-ktor"
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)
    val tables = arrayOf(Users, UsersDetails, Messages, Chats, ChatsMessages, ChatsUsers)
    transaction {
//        SchemaUtils.drop(*tables, inBatch = true)
        SchemaUtils.create(*tables)
    }
}

suspend fun <T> dbQuery(block: () -> T): T {
    return withContext(Dispatchers.IO) {
        transaction { block() }
    }
}
