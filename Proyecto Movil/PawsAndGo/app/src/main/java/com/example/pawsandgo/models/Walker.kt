package com.example.pawsandgo.models

data class Walker(
    val id: String,
    val userId: String,
    val name: String,
    val isDummy: Boolean = false,
    val bio: String,
    val maxDogs: Int,
    val rating: Double,
    var totalRatings: Int = 0,
    val totalWalks: Int,
    val availableRoutes: List<String>,
    val isAvailable: Boolean,
    val createdAt: String,
    val updatedAt: String,
    var completedWalks: Int = 0,
    var activeWalks: Int = 0,
    var totalTips: Double = 0.0
)