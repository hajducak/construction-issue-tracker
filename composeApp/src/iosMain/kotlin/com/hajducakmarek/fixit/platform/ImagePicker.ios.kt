package com.hajducakmarek.fixit.platform

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

actual class ImagePicker {
    private var onImagePickedCallback: ((String?) -> Unit)? = null

    actual var showCamera by mutableStateOf(false)
        private set

    actual fun pickImage(onImagePicked: (String?) -> Unit) {
        this.onImagePickedCallback = onImagePicked
        // iOS implementation later - for now just simulate
        onImagePickedCallback?.invoke("/ios/photo_${System.currentTimeMillis()}.jpg")
    }

    actual fun onPhotoCaptured(path: String?) {
        showCamera = false
        onImagePickedCallback?.invoke(path)
        onImagePickedCallback = null
    }

    actual fun onCameraCancel() {
        showCamera = false
        onImagePickedCallback?.invoke(null)
        onImagePickedCallback = null
    }
}
