package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AuthResult
import com.example.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

/**
 * Secure User Account Repository.
 * Handles account creation, SHA-256 password hashing, persistent sessions,
 * profile management, and non-monetary entertainment points.
 */
class UserAccountRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("game_user_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val usersMap = mutableMapOf<String, UserAccount>() // username/email lowercase -> UserAccount
    private val passwordHashes = mutableMapOf<Long, String>() // userId -> SHA-256 hash

    init {
        loadUsersFromStorage()
        restoreSession()
    }

    private fun hashPassword(password: String): String {
        val salt = "ZOO_3D_SECURE_SALT_2026"
        val bytes = MessageDigest.getInstance("SHA-256").digest((password + salt).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun saveUsersToStorage() {
        val array = JSONArray()
        val hashObj = JSONObject()
        usersMap.values.distinctBy { it.id }.forEach { user ->
            val uObj = JSONObject().apply {
                put("id", user.id)
                put("username", user.username)
                put("email", user.email)
                put("avatar", user.avatar)
                put("gamePoints", user.gamePoints)
                put("createdAt", user.createdAt)
                put("lastLoginAt", user.lastLoginAt)
                put("isAdmin", user.isAdmin)
                put("sessionToken", user.sessionToken ?: "")
            }
            array.put(uObj)
            hashObj.put(user.id.toString(), passwordHashes[user.id] ?: "")
        }
        prefs.edit()
            .putString("saved_users_json", array.toString())
            .putString("saved_hashes_json", hashObj.toString())
            .apply()
    }

    private fun loadUsersFromStorage() {
        val usersJson = prefs.getString("saved_users_json", null)
        val hashesJson = prefs.getString("saved_hashes_json", null)

        if (!usersJson.isNullOrBlank()) {
            try {
                val array = JSONArray(usersJson)
                val hashObj = if (!hashesJson.isNullOrBlank()) JSONObject(hashesJson) else JSONObject()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.getLong("id")
                    val user = UserAccount(
                        id = id,
                        username = obj.getString("username"),
                        email = obj.getString("email"),
                        avatar = obj.optString("avatar", "🦁"),
                        gamePoints = obj.optLong("gamePoints", NEW_ACCOUNT_BONUS_RS),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        lastLoginAt = obj.optLong("lastLoginAt", System.currentTimeMillis()),
                        isAdmin = obj.optBoolean("isAdmin", false),
                        sessionToken = obj.optString("sessionToken").takeIf { it.isNotBlank() }
                    )
                    usersMap[user.username.lowercase()] = user
                    usersMap[user.email.lowercase()] = user
                    passwordHashes[id] = hashObj.optString(id.toString(), "")
                }
            } catch (_: Exception) {}
        }

        // Ensure default demo player conforms to 20 RS bonus
        usersMap["hunter_99"]?.let { hunter ->
            if (hunter.gamePoints > NEW_ACCOUNT_BONUS_RS && !hunter.isAdmin) {
                val updated = hunter.copy(gamePoints = NEW_ACCOUNT_BONUS_RS)
                updateUserInternal(updated)
            }
        }

        // Seed default player and admin account if none exists
        if (usersMap.isEmpty()) {
            val adminUser = UserAccount(
                id = 1001L,
                username = "Admin",
                email = "admin@zoo3d.game",
                avatar = "👑",
                gamePoints = 50000L,
                isAdmin = true
            )
            val demoPlayer = UserAccount(
                id = 1002L,
                username = "Hunter_99",
                email = "hunter99@game.com",
                avatar = "🦁",
                gamePoints = NEW_ACCOUNT_BONUS_RS,
                isAdmin = false
            )

            usersMap[adminUser.username.lowercase()] = adminUser
            usersMap[adminUser.email.lowercase()] = adminUser
            passwordHashes[adminUser.id] = hashPassword("Admin@2026")

            usersMap[demoPlayer.username.lowercase()] = demoPlayer
            usersMap[demoPlayer.email.lowercase()] = demoPlayer
            passwordHashes[demoPlayer.id] = hashPassword("hunter123")

            saveUsersToStorage()
        }
    }

    companion object {
        const val NEW_ACCOUNT_BONUS_RS = 20L
    }

    private fun restoreSession() {
        val savedToken = prefs.getString("active_session_token", null)
        if (!savedToken.isNullOrBlank()) {
            val user = usersMap.values.firstOrNull { it.sessionToken == savedToken }
            if (user != null) {
                _currentUser.value = user
                return
            }
        }
        // If no active session, auto-login default demo user for seamless start
        val defaultUser = usersMap["hunter_99"] ?: usersMap.values.firstOrNull()
        if (defaultUser != null) {
            val token = UUID.randomUUID().toString()
            val sessionUser = defaultUser.copy(sessionToken = token, lastLoginAt = System.currentTimeMillis())
            updateUserInternal(sessionUser)
            prefs.edit().putString("active_session_token", token).apply()
            _currentUser.value = sessionUser
        }
    }

    fun register(username: String, email: String, password: String, avatar: String): AuthResult {
        val cleanName = username.trim()
        val cleanEmail = email.trim().lowercase()

        if (cleanName.length < 3) {
            return AuthResult.Error("Username must be at least 3 characters")
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return AuthResult.Error("Please enter a valid email address")
        }
        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters")
        }
        if (usersMap.containsKey(cleanName.lowercase())) {
            return AuthResult.Error("Username is already taken")
        }
        if (usersMap.containsKey(cleanEmail)) {
            return AuthResult.Error("An account with this email already exists")
        }

        val newId = System.currentTimeMillis()
        val sessionToken = UUID.randomUUID().toString()
        val newUser = UserAccount(
            id = newId,
            username = cleanName,
            email = cleanEmail,
            avatar = avatar,
            gamePoints = NEW_ACCOUNT_BONUS_RS, // 20 RS welcome bonus for every new account
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis(),
            isAdmin = cleanEmail.startsWith("admin@"),
            sessionToken = sessionToken
        )

        usersMap[cleanName.lowercase()] = newUser
        usersMap[cleanEmail] = newUser
        passwordHashes[newId] = hashPassword(password)
        saveUsersToStorage()

        prefs.edit().putString("active_session_token", sessionToken).apply()
        _currentUser.value = newUser
        return AuthResult.Success(newUser)
    }

    fun login(usernameOrEmail: String, password: String, rememberMe: Boolean = true): AuthResult {
        val query = usernameOrEmail.trim().lowercase()
        val user = usersMap[query] ?: return AuthResult.Error("Account not found. Please check your credentials.")

        val storedHash = passwordHashes[user.id]
        val inputHash = hashPassword(password)

        if (storedHash != inputHash) {
            return AuthResult.Error("Incorrect password. Please try again.")
        }

        val sessionToken = if (rememberMe) UUID.randomUUID().toString() else null
        val updatedUser = user.copy(
            lastLoginAt = System.currentTimeMillis(),
            sessionToken = sessionToken
        )
        updateUserInternal(updatedUser)

        if (rememberMe && sessionToken != null) {
            prefs.edit().putString("active_session_token", sessionToken).apply()
        } else {
            prefs.edit().remove("active_session_token").apply()
        }

        _currentUser.value = updatedUser
        return AuthResult.Success(updatedUser)
    }

    fun logout() {
        val user = _currentUser.value
        if (user != null) {
            val cleared = user.copy(sessionToken = null)
            updateUserInternal(cleared)
        }
        prefs.edit().remove("active_session_token").apply()
        _currentUser.value = null
    }

    fun forgotPassword(email: String, newPassword: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        val user = usersMap[cleanEmail] ?: return AuthResult.Error("No account found with this email.")

        if (newPassword.length < 6) {
            return AuthResult.Error("New password must be at least 6 characters")
        }

        passwordHashes[user.id] = hashPassword(newPassword)
        saveUsersToStorage()
        return AuthResult.Success(user)
    }

    fun updateAvatar(newAvatar: String) {
        val user = _currentUser.value ?: return
        val updated = user.copy(avatar = newAvatar)
        updateUserInternal(updated)
        _currentUser.value = updated
    }

    fun adjustPoints(delta: Long) {
        val user = _currentUser.value ?: return
        val newPoints = (user.gamePoints + delta).coerceAtLeast(0L)
        val updated = user.copy(gamePoints = newPoints)
        updateUserInternal(updated)
        _currentUser.value = updated
    }

    private fun updateUserInternal(user: UserAccount) {
        usersMap[user.username.lowercase()] = user
        usersMap[user.email.lowercase()] = user
        saveUsersToStorage()
    }
}
