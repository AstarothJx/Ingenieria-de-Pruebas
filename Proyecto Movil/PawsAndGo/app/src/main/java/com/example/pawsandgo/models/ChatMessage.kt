package com.example.pawsandgo.models
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String, // "walker", "owner", or "system"
    val senderName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "text" // "text", "photo", "location"
)