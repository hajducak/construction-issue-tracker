package com.hajducakmarek.fixit.models

data class Comment(
    val id: String,
    val issueId: String,
    val userId: String,
    val text: String,
    val createdAt: Long
)

data class CommentWithUser(
    val comment: Comment,
    val user: User
)