package com.coders.chat.auth

data class JwtProperties(
    val domain: String,
    val audience: String,
    val realm: String,
    val validityInMs: Long
)