package com.hajducakmarek.fixit.utils

object Validation {

    // Flat number validation (format: A-101, B-205, etc.)
    fun isValidFlatNumber(flatNumber: String): Boolean {
        if (flatNumber.isBlank()) return false
        // Format: Letter-Number (e.g., A-101, B-205)
        val regex = Regex("^[A-Z]-\\d{3}$")
        return flatNumber.matches(regex)
    }

    fun getFlatNumberError(flatNumber: String): String? {
        return when {
            flatNumber.isBlank() -> "Flat number is required"
            !isValidFlatNumber(flatNumber) -> "Format: A-101 (Letter-Number)"
            else -> null
        }
    }

    // Description validation
    fun isValidDescription(description: String): Boolean {
        return description.isNotBlank() && description.length >= 10
    }

    fun getDescriptionError(description: String): String? {
        return when {
            description.isBlank() -> "Description is required"
            description.length < 10 -> "Description must be at least 10 characters"
            description.length > 500 -> "Description too long (max 500 characters)"
            else -> null
        }
    }

    // Worker name validation
    fun isValidWorkerName(name: String): Boolean {
        return name.isNotBlank() && name.length >= 2
    }

    fun getWorkerNameError(name: String): String? {
        return when {
            name.isBlank() -> "Name is required"
            name.length < 2 -> "Name must be at least 2 characters"
            name.length > 50 -> "Name too long (max 50 characters)"
            else -> null
        }
    }
}