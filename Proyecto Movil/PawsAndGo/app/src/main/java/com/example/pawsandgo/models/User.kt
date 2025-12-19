package com.example.pawsandgo.models

data class User(
    val id: String,
    val email: String,
    val name: String,
    val phone: String,
    val role: String,
    val profileImage: String? = null,
    val createdAt: String,
    val updatedAt: String
) {


    fun toJson(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "email" to email,
            "name" to name,
            "phone" to phone,
            "role" to role,
            "profileImage" to profileImage,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromJson(json: Map<String, Any>): User {
            return User(
                id = json["id"] as String,
                email = json["email"] as String,
                name = json["name"] as String,
                phone = json["phone"] as String,
                role = json["role"] as String,
                profileImage = json["profileImage"] as? String,
                createdAt = json["createdAt"] as String,
                updatedAt = json["updatedAt"] as String
            )
        }
    }
}