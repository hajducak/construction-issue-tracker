package com.hajducakmarek.fixit.platform

expect class ImagePicker {
    fun pickImage(onImagePicked: (String?) -> Unit)
}
