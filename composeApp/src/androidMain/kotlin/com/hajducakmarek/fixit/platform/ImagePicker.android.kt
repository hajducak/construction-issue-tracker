package com.hajducakmarek.fixit.platform

actual class ImagePicker {
    actual fun pickImage(onImagePicked: (String?) -> Unit) {
        // For now, just simulate a photo path
        // We'll add real camera in next session
        onImagePicked("/storage/emulated/0/photo_${System.currentTimeMillis()}.jpg")
    }
}
