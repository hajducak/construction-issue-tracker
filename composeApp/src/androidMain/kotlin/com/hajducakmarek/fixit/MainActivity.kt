package com.hajducakmarek.fixit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.platform.ImagePicker
import com.hajducakmarek.fixit.session.UserSession
import com.hajducakmarek.fixit.utils.PdfExporter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val databaseDriverFactory = DatabaseDriverFactory(applicationContext)
        val imagePicker = ImagePicker()
        val userSession = UserSession(applicationContext)
        val pdfExporter = PdfExporter(this)

        setContent {
            App(
                databaseDriverFactory = databaseDriverFactory,
                imagePicker = imagePicker,
                userSession = userSession,
                pdfExporter = pdfExporter
            )
        }
    }
}