package com.hajducakmarek.fixit

import androidx.compose.ui.window.ComposeUIViewController
import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.platform.ImagePicker
import com.hajducakmarek.fixit.session.UserSession
import com.hajducakmarek.fixit.utils.PdfExporter
import platform.UIKit.UIViewController

fun createViewController(): UIViewController {
    val databaseDriverFactory = DatabaseDriverFactory()
    val imagePicker = ImagePicker()
    val userSession = UserSession()
    val pdfExporter = PdfExporter()

    return ComposeUIViewController {
        App(
            databaseDriverFactory = databaseDriverFactory,
            imagePicker = imagePicker,
            userSession = userSession,
            pdfExporter = pdfExporter
        )
    }
}