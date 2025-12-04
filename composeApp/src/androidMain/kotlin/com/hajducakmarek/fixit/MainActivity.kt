package com.hajducakmarek.fixit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.platform.ImagePicker
import com.hajducakmarek.fixit.session.UserSession

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val databaseDriverFactory = DatabaseDriverFactory(applicationContext)
        val imagePicker = ImagePicker()
        val userSession = UserSession(applicationContext)

        setContent {
            App(databaseDriverFactory, imagePicker, userSession)
        }
    }
}