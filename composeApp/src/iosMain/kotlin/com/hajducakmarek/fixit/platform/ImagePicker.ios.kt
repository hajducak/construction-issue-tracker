package com.hajducakmarek.fixit.platform

import androidx.compose.runtime.*
import platform.UIKit.*
import platform.Foundation.*
import kotlinx.cinterop.*
import platform.darwin.NSObject

actual class ImagePicker {
    private var onImagePickedCallback: ((String?) -> Unit)? = null
    actual var showCamera by mutableStateOf(false)
        private set

    actual fun pickImage(onImagePicked: (String?) -> Unit) {
        this.onImagePickedCallback = onImagePicked
        // For now, simulate photo capture
        // Real UIImagePickerController requires more setup
        val timestamp = platform.Foundation.NSDate().timeIntervalSince1970.toLong()
        val photoPath = "/tmp/photo_$timestamp.jpg"
        onImagePicked(photoPath)
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