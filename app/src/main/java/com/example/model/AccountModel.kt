package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * User Account model for non-monetary game platform.
 * Passwords are never stored as plain text.
 */
data class UserAccount(
    val id: Long,
    val username: String,
    val email: String,
    val avatar: String = "🦁",
    val gamePoints: Long = 20L, // 20 RS Welcome bonus for every new account
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val isAdmin: Boolean = false,
    val sessionToken: String? = null
) {
    val formattedCreatedAt: String
        get() = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(createdAt))

    val formattedLastLogin: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(lastLoginAt))
}

sealed class AuthResult {
    data class Success(val user: UserAccount) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

val AVATAR_OPTIONS = listOf("🦁", "🐯", "🦅", "🦈", "🐼", "🦚", "👑", "👩", "🥷", "⚡")
