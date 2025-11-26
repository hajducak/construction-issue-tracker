package com.hajducakmarek.fixit

import androidx.compose.runtime.Composable

@Composable
actual fun CameraOverlay(
    onPhotoCaptured: (String) -> Unit,
    onCancel: () -> Unit
) {
    // iOS implementation later
}
