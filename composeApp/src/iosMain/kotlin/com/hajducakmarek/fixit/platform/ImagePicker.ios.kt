package com.hajducakmarek.fixit.platform

import kotlinx.datetime.Clock

actual class ImagePicker {
    actual fun pickImage(onImagePicked: (String?) -> Unit) {
        // For now, just simulate a photo path
        onImagePicked("/var/mobile/photo_${Clock.System.now().toEpochMilliseconds()}.jpg")
    }
}
