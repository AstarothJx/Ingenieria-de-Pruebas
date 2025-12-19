package com.example.pawsandgo.models

import com.example.pawsandgo.ChatMessage

data class Walk(
    // --- DATOS PRINCIPALES (Obligatorios al crear) ---
    val id: String,
    val ownerId: String,
    val walkerId: String,
    val petId: String,
    val petName: String,
    val petPhoto: String, // Puede ser "" o una URL
    val routeId: String,
    val status: String,   // "scheduled", "in_progress", "completed", "cancelled"
    val scheduledDate: String,
    val duration: Int,
    val totalPrice: Double,

    // --- TIMESTAMPS ---
    val createdAt: String,
    val updatedAt: String,

    // --- NUEVO: CHAT ---
    // (Gson manejará esta lista automáticamente)
    val chatHistory: List<ChatMessage> = emptyList(),

    // --- SIMULACIÓN Y EVIDENCIA ---
    val startPhoto: String? = null,
    val endPhoto: String? = null,

    // --- CALIFICACIÓN Y PAGOS ---
    val rating: Double = 0.0,
    val review: String? = null,
    val tipAmount: Double = 0.0
)