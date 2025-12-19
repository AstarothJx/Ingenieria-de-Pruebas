package com.example.pawsandgo.models

data class Pet(
    val id: String,
    val ownerId: String,
    val name: String,
    val breed: String,
    val age: Int,
    val weight: Double,
    val size: String,
    val specialNeeds: String? = null,
    val photo: String? = null,
    val createdAt: String,
    val updatedAt: String
) {
    fun toJson(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "ownerId" to ownerId,
            "name" to name,
            "breed" to breed,
            "age" to age,
            "weight" to weight,
            "size" to size,
            "specialNeeds" to specialNeeds,
            "photo" to photo,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromJson(json: Map<String, Any>): Pet {
            return Pet(
                id = json["id"] as String,
                ownerId = json["ownerId"] as String,
                name = json["name"] as String,
                breed = json["breed"] as String,
                age = (json["age"] as Number).toInt(),
                weight = (json["weight"] as Number).toDouble(),
                size = json["size"] as String,
                specialNeeds = json["specialNeeds"] as? String,
                photo = json["photo"] as? String,
                createdAt = json["createdAt"] as String,
                updatedAt = json["updatedAt"] as String
            )
        }
    }
}