package com.example.pawsandgo.models

data class WalkRoute(
    val id: String,
    val name: String,
    val description: String,
    val distanceKm: Double,
    val durationMinutes: Int,
    val difficulty: String,
    val createdAt: String,
    val updatedAt: String
) {
    fun toJson(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "description" to description,
            "distanceKm" to distanceKm,
            "durationMinutes" to durationMinutes,
            "difficulty" to difficulty,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromJson(json: Map<String, Any>): WalkRoute {
            return WalkRoute(
                id = json["id"] as String,
                name = json["name"] as String,
                description = json["description"] as String,
                distanceKm = (json["distanceKm"] as Number).toDouble(),
                durationMinutes = (json["durationMinutes"] as Number).toInt(),
                difficulty = json["difficulty"] as String,
                createdAt = json["createdAt"] as String,
                updatedAt = json["updatedAt"] as String
            )
        }
    }
}