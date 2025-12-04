package com.hajducakmarek.fixit.session

import android.content.Context
import android.content.SharedPreferences
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.UserRole
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

actual class UserSession(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "user_session",
        Context.MODE_PRIVATE
    )
    private val json = Json { ignoreUnknownKeys = true }

    actual fun saveUser(user: User) {
        val userJson = json.encodeToString(user)
        prefs.edit()
            .putString(KEY_USER, userJson)
            .apply()
    }

    actual fun getCurrentUser(): User? {
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            json.decodeFromString<User>(userJson)
        } catch (e: Exception) {
            null
        }
    }

    actual fun clearUser() {
        prefs.edit()
            .remove(KEY_USER)
            .apply()
    }

    actual fun isLoggedIn(): Boolean {
        return prefs.contains(KEY_USER)
    }

    companion object {
        private const val KEY_USER = "current_user"
    }
}