package com.hajducakmarek.fixit.session

import com.hajducakmarek.fixit.models.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import platform.Foundation.NSUserDefaults

actual class UserSession {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val json = Json { ignoreUnknownKeys = true }

    actual fun saveUser(user: User) {
        val userJson = json.encodeToString(user)
        userDefaults.setObject(userJson, KEY_USER)
        userDefaults.synchronize()
    }

    actual fun getCurrentUser(): User? {
        val userJson = userDefaults.stringForKey(KEY_USER) ?: return null
        return try {
            json.decodeFromString<User>(userJson)
        } catch (e: Exception) {
            null
        }
    }

    actual fun clearUser() {
        userDefaults.removeObjectForKey(KEY_USER)
        userDefaults.synchronize()
    }

    actual fun isLoggedIn(): Boolean {
        return userDefaults.stringForKey(KEY_USER) != null
    }

    companion object {
        private const val KEY_USER = "current_user"
    }
}