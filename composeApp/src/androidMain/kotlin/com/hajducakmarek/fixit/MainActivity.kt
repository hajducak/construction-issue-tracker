package com.hajducakmarek.fixit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.platform.ImagePicker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val databaseDriverFactory = DatabaseDriverFactory(applicationContext)
        val imagePicker = ImagePicker()

        setContent {
            App(databaseDriverFactory, imagePicker)
        }
    }
}
