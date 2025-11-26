package com.hajducakmarek.fixit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.hajducakmarek.fixit.camera.CameraCapture

@Composable
actual fun CameraOverlay(
    onPhotoCaptured: (String) -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        CameraCapture(
            onPhotoCaptured = onPhotoCaptured,
            onCancel = onCancel
        )
    }
}
