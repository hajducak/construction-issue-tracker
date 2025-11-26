package com.hajducakmarek.fixit.database

import app.cash.sqldelight.db.SqlDriver

// expect class = "I expect Android and iOS to provide this"
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
