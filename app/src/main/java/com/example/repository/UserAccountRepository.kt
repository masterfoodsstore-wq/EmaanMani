package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.model.AuthResult
import com.example.model.UserAccount
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

/**
 * Secure User Account Repository.
 * Handles online Firebase Authentication, real-time Cloud Firestore synchronization,
 * SHA-256 password hashing, persistent sessions, and entertainment balance.
 */
class UserAccountRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("game_user_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val usersMap = mutableMapOf<String, UserAccount>() // username/email lowercase -> UserAccount
    private val passwordHashes = mutableMapOf<Long, String>() // userId -> SHA-256 hash
    private var firestoreUserRegistration: ListenerRegistration? = null
    private val repoScope = CoroutineScope(Dispatchers.IO)

    init {
        repoScope.launch {
            FirebaseManager.initialize(context)
            loadUsersFromStorage()
            restoreSession()
        }
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

        // Remove any legacy demo accounts from storage
        usersMap.remove("hunter_99")
        usersMap.remove("hunter99@game.com")

        // Seed development admin account if not already present
        val adminKey = "admin@system.com"
        if (!usersMap.containsKey(adminKey)) {
            val adminUser = UserAccount(
                id = 1001L,
                username = "admin@system.com",
                email = "admin@system.com",
                avatar = "👑",
                gamePoints = 50000L,
                isAdmin = true,
                status = "ACTIVE"
            )

            usersMap[adminUser.username.lowercase()] = adminUser
            usersMap[adminUser.email.lowercase()] = adminUser
            passwordHashes[adminUser.id] = hashPassword("Admin@786")

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
        // No active session — do not automatically log in any user. Requires user authentication.
        _currentUser.value = null
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
            sessionToken = sessionToken,
            status = "ACTIVE"
        )

        usersMap[cleanName.lowercase()] = newUser
        usersMap[cleanEmail] = newUser
        passwordHashes[newId] = hashPassword(password)
        saveUsersToStorage()

        prefs.edit().putString("active_session_token", sessionToken).apply()
        _currentUser.value = newUser

        // Live Cloud Firestore and Firebase Auth registration
        repoScope.launch {
            try {
                val auth = FirebaseManager.auth
                val db = FirebaseManager.firestore
                if (auth != null && db != null) {
                    auth.createUserWithEmailAndPassword(cleanEmail, password)
                        .addOnSuccessListener { result ->
                            val uid = result.user?.uid
                            if (uid != null) {
                                val userDoc = hashMapOf(
                                    "uid" to uid,
                                    "id" to newId,
                                    "username" to cleanName,
                                    "email" to cleanEmail,
                                    "avatar" to avatar,
                                    "balance" to NEW_ACCOUNT_BONUS_RS,
                                    "status" to "ACTIVE",
                                    "isAdmin" to cleanEmail.startsWith("admin@"),
                                    "role" to if (cleanEmail.startsWith("admin@")) "admin" else "user",
                                    "createdAt" to System.currentTimeMillis(),
                                    "lastLoginAt" to System.currentTimeMillis()
                                )
                                db.collection("users").document(uid).set(userDoc)
                                startFirebaseUserListener(uid)
                            }
                        }
                }
            } catch (e: Exception) {
                Log.w("UserAccountRepository", "Firebase registration notice: ${e.message}")
            }
        }

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

        // Live Cloud Firestore and Firebase Auth login & real-time sync
        repoScope.launch {
            try {
                val auth = FirebaseManager.auth
                if (auth != null) {
                    val loginEmail = if (query.contains("@")) query else "${query}@game.com"
                    auth.signInWithEmailAndPassword(loginEmail, password)
                        .addOnSuccessListener { result ->
                            result.user?.uid?.let { uid ->
                                startFirebaseUserListener(uid)
                            }
                        }
                        .addOnFailureListener {
                            // If user exists locally but not yet in Firebase Auth, auto-register
                            auth.createUserWithEmailAndPassword(loginEmail, password)
                                .addOnSuccessListener { regResult ->
                                    regResult.user?.uid?.let { uid ->
                                        val db = FirebaseManager.firestore
                                        val userDoc = hashMapOf(
                                            "uid" to uid,
                                            "id" to user.id,
                                            "username" to user.username,
                                            "email" to user.email,
                                            "avatar" to user.avatar,
                                            "balance" to user.gamePoints,
                                            "status" to user.status,
                                            "isAdmin" to user.isAdmin,
                                            "createdAt" to user.createdAt,
                                            "lastLoginAt" to System.currentTimeMillis()
                                        )
                                        db?.collection("users")?.document(uid)?.set(userDoc)
                                        startFirebaseUserListener(uid)
                                    }
                                }
                        }
                }
            } catch (e: Exception) {
                Log.w("UserAccountRepository", "Firebase login notice: ${e.message}")
            }
        }

        return AuthResult.Success(updatedUser)
    }

    private fun startFirebaseUserListener(uid: String) {
        firestoreUserRegistration?.remove()
        val db = FirebaseManager.firestore ?: return
        try {
            firestoreUserRegistration = db.collection("users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("UserAccountRepository", "Firestore user listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val serverBalance = snapshot.getDouble("balance")?.toLong()
                            ?: snapshot.getLong("balance")
                            ?: _currentUser.value?.gamePoints
                            ?: 20L
                        val status = snapshot.getString("status") ?: "ACTIVE"
                        val isAdmin = snapshot.getBoolean("isAdmin") ?: false
                        val avatar = snapshot.getString("avatar") ?: (_currentUser.value?.avatar ?: "🦁")
                        val username = snapshot.getString("username") ?: (_currentUser.value?.username ?: "Player")

                        _currentUser.value?.let { current ->
                            val updated = current.copy(
                                gamePoints = serverBalance,
                                status = status,
                                isAdmin = isAdmin,
                                avatar = avatar,
                                username = username,
                                firebaseUid = uid
                            )
                            _currentUser.value = updated
                            updateUserInternal(updated)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w("UserAccountRepository", "Failed to attach user snapshot listener: ${e.message}")
        }
    }

    fun logout() {
        firestoreUserRegistration?.remove()
        firestoreUserRegistration = null
        try {
            FirebaseManager.auth?.signOut()
        } catch (_: Exception) {}

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

        try {
            FirebaseManager.auth?.sendPasswordResetEmail(cleanEmail)
        } catch (_: Exception) {}

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

    fun updateBalance(newPoints: Long) {
        val user = _currentUser.value ?: return
        val updated = user.copy(gamePoints = newPoints.coerceAtLeast(0L))
        updateUserInternal(updated)
        _currentUser.value = updated
    }

    private fun updateUserInternal(user: UserAccount) {
        usersMap[user.username.lowercase()] = user
        usersMap[user.email.lowercase()] = user
        saveUsersToStorage()
    }
}
