package com.hajducakmarek.fixit.models

data class Photo(
    val id: String,
    val issueId: String,
    val photoPath: String,
    val createdAt: Long,
    val uploadedBy: String
)