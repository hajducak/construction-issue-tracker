package com.hajducakmarek.fixit.session

import com.hajducakmarek.fixit.models.User

expect class UserSession {
    fun saveUser(user: User)
    fun getCurrentUser(): User?
    fun clearUser()
    fun isLoggedIn(): Boolean
}