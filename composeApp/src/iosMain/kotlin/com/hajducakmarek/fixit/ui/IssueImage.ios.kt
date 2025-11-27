package com.hajducakmarek.fixit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun IssueImage(
    photoPath: String,
    contentDescription: String,
    modifier: Modifier
) {
    // Placeholder for iOS
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text("\ud83d\udcf7", style = MaterialTheme.typography.headlineLarge)
    }
}
