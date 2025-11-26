package com.hajducakmarek.fixit.platform

import androidx.compose.runtime.MutableState

expect class ImagePicker {
    fun pickImage(onImagePicked: (String?) -> Unit)
    var showCamera: Boolean
        private set
    fun onPhotoCaptured(path: String?)
    fun onCameraCancel()
}
