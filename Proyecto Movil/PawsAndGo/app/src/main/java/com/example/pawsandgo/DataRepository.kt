package com.example.pawsandgo

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.example.pawsandgo.models.Pet
import com.example.pawsandgo.models.Walk
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

// --- MODELOS DE DATOS ---

data class UserProfile(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: String
)

data class UserSession(val id: String, val role: String)

data class WalkRoute(
    val id: String,
    val name: String,
    val description: String,
    val startHour: Int,
    val endHour: Int
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String, // "walker", "owner", or "system"
    val senderName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "text" // "text", "photo", "location"
)

// --- REPOSITORIO GLOBAL ---

object DataRepository {

    private const val PREFS_NAME = "paws_prefs"
    private const val KEY_PETS = "pets_data"
    private const val KEY_WALKS = "walks_data"
    private const val KEY_WALKERS_STATS = "walkers_stats"
    private const val KEY_USER_PROFILE = "user_profile_data"
    private const val KEY_ALL_USERS = "all_users_db"
    private const val KEY_WALKER_ROUTES = "walker_routes_map"
    private const val KEY_USER_ID = "curr_user_id"
    private const val KEY_USER_ROLE = "curr_user_role"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    // Estados Observables para UI
    val pets = mutableStateListOf<Pet>()
    val walks = mutableStateListOf<Walk>()
    val walkerRoutes = mutableStateMapOf<String, Set<String>>()

    // Mapas locales
    private val usersMap = mutableMapOf<String, UserProfile>()
    // Mapa: ID del Walker -> Pair(PromedioRating, TotalVotos)
    private val walkerStats = mutableMapOf<String, Pair<Double, Int>>()

    val allRoutes = listOf(
        WalkRoute("r_parque", "Parque Hundido", "Caminata relajada con zonas de pasto.", 7, 11),
        WalkRoute("r_urbana", "Ruta Urbana Centro", "Caminata activa por calles principales.", 16, 20),
        WalkRoute("r_bosque", "Bosque de Chapultepec", "Senderismo ligero y aire fresco.", 6, 10),
        WalkRoute("r_nocturna", "Vigilancia Nocturna", "Paseo seguro en zona residencial.", 19, 23)
    )

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadPetsFromDisk()
        loadWalksFromDisk()
        loadUsersDB()
        loadWalkerRoutes()
        loadWalkerStats()
    }

    // ----------------------------------------------------------------
    // SECCIÓN 1: PASEOS, CHAT Y CALIFICACIÓN
    // ----------------------------------------------------------------

    fun addWalk(walk: Walk) {
        walks.add(walk)
        saveWalksToDisk()
    }

    fun cancelWalk(walkId: String) {
        val index = walks.indexOfFirst { it.id == walkId }
        if (index != -1) {
            walks[index] = walks[index].copy(status = "cancelled")
            saveWalksToDisk()
        }
    }

    fun completeWalk(walkId: String) {
        val index = walks.indexOfFirst { it.id == walkId }
        if (index != -1) {
            walks[index] = walks[index].copy(status = "completed")
            saveWalksToDisk()
        }
    }

    // Agregar mensaje al chat de un paseo
    fun addMessageToWalk(walkId: String, msg: ChatMessage) {
        val index = walks.indexOfFirst { it.id == walkId }
        if (index != -1) {
            val currentMsgs = walks[index].chatHistory.toMutableList()
            currentMsgs.add(msg)
            walks[index] = walks[index].copy(chatHistory = currentMsgs)
            saveWalksToDisk()
        }
    }

    // Función para Finalizar Rating y Propina
    fun rateWalker(walkId: String, walkerId: String, rating: Double, tipAmount: Double) {
        val index = walks.indexOfFirst { it.id == walkId }
        if (index != -1) {
            // ACTUALIZADO: Usamos los nombres correctos del modelo Walk
            walks[index] = walks[index].copy(
                rating = rating, // Antes era ratingGiven
                tipAmount = tipAmount        // Antes era tipGiven
            )
            saveWalksToDisk()

            // 2. Actualizar el promedio del Walker
            updateWalkerStats(walkerId, rating)
        }
    }

    // Lógica matemática para actualizar estrellas
    private fun updateWalkerStats(walkerId: String, newRating: Double) {
        val current = walkerStats[walkerId] ?: Pair(5.0, 1) // Default 5.0 con 1 voto inicial
        val currentTotal = current.first * current.second
        val newCount = current.second + 1
        val newAverage = (currentTotal + newRating) / newCount

        walkerStats[walkerId] = Pair(newAverage, newCount)

        val json = gson.toJson(walkerStats)
        prefs.edit().putString(KEY_WALKERS_STATS, json).apply()
    }

    // Obtener rating real (o default)
    fun getWalkerRating(walkerId: String): Pair<Double, Int> {
        return walkerStats[walkerId] ?: Pair(5.0, 0)
    }

    private fun loadWalkerStats() {
        val json = prefs.getString(KEY_WALKERS_STATS, "{}")
        val type = object : TypeToken<Map<String, Pair<Double, Int>>>() {}.type
        val loaded: Map<String, Pair<Double, Int>> = gson.fromJson(json, type) ?: emptyMap()
        walkerStats.putAll(loaded)
    }

    private fun saveWalksToDisk() {
        val json = gson.toJson(walks)
        prefs.edit().putString(KEY_WALKS, json).apply()
    }

    private fun loadWalksFromDisk() {
        val json = prefs.getString(KEY_WALKS, null)
        if (json != null) {
            val type = object : TypeToken<List<Walk>>() {}.type
            val savedList: List<Walk> = gson.fromJson(json, type)
            walks.clear()
            walks.addAll(savedList)
        }
    }

    // ----------------------------------------------------------------
    // SECCIÓN 2: RUTAS DE PASEADORES
    // ----------------------------------------------------------------

    fun getRoutesByIds(ids: List<String>): List<WalkRoute> {
        return allRoutes.filter { ids.contains(it.id) }
    }

    fun toggleRouteForWalker(walkerId: String, routeId: String) {
        val currentRoutes = walkerRoutes[walkerId] ?: setOf("r_parque", "r_urbana")
        val newRoutes = if (currentRoutes.contains(routeId)) currentRoutes - routeId else currentRoutes + routeId

        walkerRoutes[walkerId] = newRoutes
        val json = gson.toJson(walkerRoutes)
        prefs.edit().putString(KEY_WALKER_ROUTES, json).apply()
    }

    fun getRoutesForWalker(walkerId: String): List<String> = walkerRoutes[walkerId]?.toList() ?: listOf("r_parque", "r_urbana")

    private fun loadWalkerRoutes() {
        val json = prefs.getString(KEY_WALKER_ROUTES, "{}")
        val type = object : TypeToken<Map<String, Set<String>>>() {}.type
        val loadedMap: Map<String, Set<String>> = gson.fromJson(json, type) ?: emptyMap()
        walkerRoutes.clear()
        walkerRoutes.putAll(loadedMap)
    }

    // ----------------------------------------------------------------
    // SECCIÓN 3: USUARIOS Y SESIÓN
    // ----------------------------------------------------------------

    fun performLogin(email: String): UserProfile? {
        val existingProfile = usersMap[email]
        if (existingProfile != null) {
            saveUserProfile(existingProfile)
            saveSession(existingProfile.id, existingProfile.role)
            return existingProfile
        }
        return null
    }

    fun saveSession(userId: String, role: String) {
        prefs.edit().putString(KEY_USER_ID, userId).putString(KEY_USER_ROLE, role).apply()
    }

    fun getCurrentUser(): UserSession? {
        val id = prefs.getString(KEY_USER_ID, null)
        val role = prefs.getString(KEY_USER_ROLE, null)
        return if (id != null && role != null) UserSession(id, role) else null
    }

    fun clearSession() {
        prefs.edit().remove(KEY_USER_ID).remove(KEY_USER_ROLE).remove(KEY_USER_PROFILE).apply()
    }

    fun saveUserProfile(profile: UserProfile) {
        val jsonProfile = gson.toJson(profile)
        prefs.edit().putString(KEY_USER_PROFILE, jsonProfile).apply()

        if (profile.email.isNotEmpty()) {
            usersMap[profile.email] = profile
            val jsonMap = gson.toJson(usersMap)
            prefs.edit().putString(KEY_ALL_USERS, jsonMap).apply()
        }
    }

    fun getUserProfile(): UserProfile {
        val json = prefs.getString(KEY_USER_PROFILE, null)
        return if (json != null) {
            gson.fromJson(json, UserProfile::class.java)
        } else {
            UserProfile("demo", "Usuario Demo", "000", "demo@email.com", "owner")
        }
    }

    fun getAllWalkersProfiles(): List<UserProfile> {
        return usersMap.values.filter { it.role == "walker" }.toList()
    }

    private fun loadUsersDB() {
        val json = prefs.getString(KEY_ALL_USERS, "{}")
        val type = object : TypeToken<Map<String, UserProfile>>() {}.type
        val loadedMap: Map<String, UserProfile> = gson.fromJson(json, type) ?: emptyMap()
        usersMap.clear()
        usersMap.putAll(loadedMap)
    }

    // ----------------------------------------------------------------
    // SECCIÓN 4: MASCOTAS
    // ----------------------------------------------------------------

    fun addPet(pet: Pet) {
        pets.add(pet)
        savePetsToDisk()
    }

    fun getPet(id: String): Pet? = pets.find { it.id == id }

    fun updatePet(updatedPet: Pet) {
        val index = pets.indexOfFirst { it.id == updatedPet.id }
        if (index != -1) { pets[index] = updatedPet; savePetsToDisk() }
    }

    fun deletePet(id: String) {
        pets.removeAll { it.id == id }
        savePetsToDisk()
    }

    private fun savePetsToDisk() {
        val json = gson.toJson(pets)
        prefs.edit().putString(KEY_PETS, json).apply()
    }

    private fun loadPetsFromDisk() {
        val json = prefs.getString(KEY_PETS, null)
        if (json != null) {
            val type = object : TypeToken<List<Pet>>() {}.type
            val savedList: List<Pet> = gson.fromJson(json, type)
            pets.clear()
            pets.addAll(savedList)
        }
    }
}