package com.example.repository

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.functions.FirebaseFunctions
import org.json.JSONObject
import java.io.InputStream

/**
 * Enterprise Firebase Service Manager for Royal X.
 * Handles graceful initialization of Firebase Authentication, Cloud Firestore,
 * and Cloud Functions with offline persistence and automatic fallback.
 */
object FirebaseManager {

    private const val TAG = "FirebaseManager"

    private var isInitialized = false
    private var firebaseApp: FirebaseApp? = null

    val auth: FirebaseAuth?
        get() = try {
            if (isInitialized) FirebaseAuth.getInstance() else null
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth not ready: ${e.message}")
            null
        }

    val firestore: FirebaseFirestore?
        get() = try {
            if (isInitialized) {
                val db = FirebaseFirestore.getInstance()
                // Configure offline persistence cache
                val cacheSettings = PersistentCacheSettings.newBuilder().build()
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(cacheSettings)
                    .build()
                db.firestoreSettings = settings
                db
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore not ready: ${e.message}")
            null
        }

    val functions: FirebaseFunctions?
        get() = try {
            if (isInitialized) FirebaseFunctions.getInstance() else null
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFunctions not ready: ${e.message}")
            null
        }

    fun isOnlineAvailable(): Boolean = isInitialized && auth != null && firestore != null

    /**
     * Initializes Firebase safely. If google-services.json is packaged or options exist,
     * it binds automatically; otherwise it loads from app assets/resources or custom options.
     */
    fun initialize(context: Context) {
        if (isInitialized) return

        try {
            val existingApps = FirebaseApp.getApps(context)
            if (existingApps.isNotEmpty()) {
                firebaseApp = FirebaseApp.getInstance()
                isInitialized = true
                Log.i(TAG, "Firebase successfully bound from existing app context")
                return
            }

            // Attempt default initialization
            try {
                firebaseApp = FirebaseApp.initializeApp(context)
                if (firebaseApp != null) {
                    isInitialized = true
                    Log.i(TAG, "Firebase default initializeApp succeeded")
                    return
                }
            } catch (e: Exception) {
                Log.d(TAG, "Default initializeApp skipped: ${e.message}")
            }

            // Fallback: Read google-services.json from assets if available or provide builder
            val options = loadOptionsFromAssets(context) ?: FirebaseOptions.Builder()
                .setApplicationId("1:102938475610:android:a1b2c3d4e5f60718293a4b")
                .setApiKey("AIzaSy_ROYAL_X_ANDROID_SAMPLE_CLIENT_KEY")
                .setProjectId("royal-x-casino")
                .setStorageBucket("royal-x-casino.appspot.com")
                .build()

            firebaseApp = FirebaseApp.initializeApp(context, options)
            isInitialized = true
            Log.i(TAG, "Firebase initialized with Royal X options: ${options.projectId}")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization warning: ${e.message}. Using offline mode.")
            isInitialized = false
        }
    }

    private fun loadOptionsFromAssets(context: Context): FirebaseOptions? {
        return try {
            context.assets.open("google-services.json").use { stream ->
                parseJsonToOptions(stream)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseJsonToOptions(stream: InputStream): FirebaseOptions? {
        return try {
            val jsonStr = stream.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonStr)
            val projectInfo = root.getJSONObject("project_info")
            val projectId = projectInfo.getString("project_id")
            val storageBucket = projectInfo.optString("storage_bucket", "")

            val clientArray = root.getJSONArray("client")
            if (clientArray.length() == 0) return null

            val client0 = clientArray.getJSONObject(0)
            val appId = client0.getJSONObject("client_info").getString("mobilesdk_app_id")
            val apiKey = client0.getJSONArray("api_key").getJSONObject(0).getString("current_key")

            FirebaseOptions.Builder()
                .setApplicationId(appId)
                .setApiKey(apiKey)
                .setProjectId(projectId)
                .setStorageBucket(storageBucket)
                .build()
        } catch (e: Exception) {
            null
        }
    }
}
