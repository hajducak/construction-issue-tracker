package com.hajducakmarek.fixit

import androidx.compose.ui.window.ComposeUIViewController
import com.hajducakmarek.fixit.database.DatabaseDriverFactory

fun MainViewController(databaseDriverFactory: DatabaseDriverFactory) = ComposeUIViewController {
    App(databaseDriverFactory)
}