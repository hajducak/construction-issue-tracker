package com.hajducakmarek.fixit.ui

import androidx.compose.runtime.Composable

@Composable
expect fun FullScreenPhotoDialog(
    photoPath: String,
    onDismiss: () -> Unit
)
