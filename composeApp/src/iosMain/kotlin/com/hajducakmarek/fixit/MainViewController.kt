package com.hajducakmarek.fixit

import androidx.compose.ui.window.ComposeUIViewController
import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.platform.ImagePicker

fun MainViewController(
    databaseDriverFactory: DatabaseDriverFactory,
    imagePicker: ImagePicker
) = ComposeUIViewController {
    App(databaseDriverFactory, imagePicker)
}