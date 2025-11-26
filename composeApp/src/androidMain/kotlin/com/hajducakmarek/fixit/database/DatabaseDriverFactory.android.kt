package com.hajducakmarek.fixit.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

// actual = "This is Android's fulfillment of the expect"
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = FixItDatabase.Schema,
            context = context,
            name = "fixit.db"
        )
    }
}
