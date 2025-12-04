package com.hajducakmarek.fixit

import androidx.compose.ui.window.ComposeUIViewController
import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.platform.ImagePicker
import com.hajducakmarek.fixit.session.UserSession
import platform.UIKit.UIViewController

fun createViewController(): UIViewController {
    val databaseDriverFactory = DatabaseDriverFactory()
    val imagePicker = ImagePicker()
    val userSession = UserSession()

    return ComposeUIViewController {
        App(databaseDriverFactory, imagePicker, userSession)
    }
}