package com.fbchat.model

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val isSent: Boolean
)
