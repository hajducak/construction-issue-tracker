package com.hajducakmarek.fixit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File

@Composable
actual fun IssueImage(
    photoPath: String,
    contentDescription: String,
    modifier: Modifier
) {
    val file = File(photoPath)

    if (file.exists()) {
        AsyncImage(
            model = file,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        // Fallback if file doesn't exist
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("📷", style = MaterialTheme.typography.headlineLarge)
        }
    }
}