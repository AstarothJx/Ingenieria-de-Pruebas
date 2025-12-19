package com.example.pawsandgo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.pawsandgo.screens.*
import com.example.pawsandgo.ui.theme.PawsAndGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DataRepository.init(applicationContext)

        setContent {
            PawsAndGoTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val savedUser = DataRepository.getCurrentUser()

    var currentScreen by remember { mutableStateOf(if (savedUser != null) "HOME" else "LOGIN") }
    var currentUserRole by remember { mutableStateOf(savedUser?.role ?: "owner") }
    var currentUserId by remember { mutableStateOf(savedUser?.id ?: "") }
    var homeTabSelected by remember { mutableIntStateOf(0) }
    var selectedPetId by remember { mutableStateOf<String?>(null) }

    when (currentScreen) {
        "LOGIN" -> {
            LoginScreen(
                onLoginSuccess = { userProfile ->
                    // LOGIN REAL: Usamos los datos recuperados de la BD
                    currentUserId = userProfile.id
                    currentUserRole = userProfile.role // ¡Aquí recuperamos si es Walker o Owner!

                    // Nota: DataRepository.performLogin ya guardó la sesión internamente
                    homeTabSelected = 0
                    currentScreen = "HOME"
                },
                onNavigateToRegister = { currentScreen = "REGISTER" }
            )
        }
        "REGISTER" -> {
            RegisterScreen(
                onRegisterSuccess = { name, phone, email, role ->
                    val prefix = if (role == "walker") "paseador" else "dueño"
                    val userId = "${prefix}_${System.currentTimeMillis()}"

                    // Creamos el perfil completo con ID y Rol
                    val newProfile = com.example.pawsandgo.UserProfile(userId, name, phone, email, role)

                    DataRepository.saveSession(userId, role)
                    DataRepository.saveUserProfile(newProfile) // Esto lo guarda en la BD de usuarios

                    currentUserId = userId
                    currentUserRole = role
                    homeTabSelected = 0
                    currentScreen = "HOME"
                },
                onBack = { currentScreen = "LOGIN" }
            )
        }

        "HOME" -> {
            com.example.pawsandgo.screens.HomeScreen(
                userId = currentUserId,
                userRole = currentUserRole,
                onLogout = {
                    DataRepository.clearSession()
                    currentUserId = ""
                    currentUserRole = ""
                    currentScreen = "LOGIN"
                },
                onAddPetClick = {
                    selectedPetId = "" // Reset para nueva mascota
                    currentScreen = "ADD_PET"
                },
                onPetClick = { petId ->
                    selectedPetId = petId
                    currentScreen = "PET_DETAIL"
                },
                onBookWalkClick = { currentScreen = "BOOK_WALK" },
                onEditProfileClick = { currentScreen = "EDIT_PROFILE" },
                onHistoryClick = { currentScreen = "HISTORY" }
            )
        }
        "BOOK_WALK" -> {
            com.example.pawsandgo.screens.BookWalkScreen(
                ownerId = currentUserId,
                onBack = { currentScreen = "HOME" },
                onWalkBooked = {
                    homeTabSelected = 0
                    currentScreen = "HOME"
                }
            )
        }
        "ADD_PET" -> {
            AddPetScreen(
                ownerId = currentUserId,
                petIdToEdit = selectedPetId,
                onBack = {
                    selectedPetId = null
                    currentScreen = "HOME"
                },
                onPetAdded = {
                    selectedPetId = null
                    homeTabSelected = 1
                    currentScreen = "HOME"
                }
            )
        }
        "PET_DETAIL" -> {
            if (selectedPetId != null) {
                PetDetailScreen(
                    petId = selectedPetId!!,
                    onBack = {
                        selectedPetId = null
                        currentScreen = "HOME"
                    },
                    onEditClick = { id ->
                        selectedPetId = id
                        currentScreen = "ADD_PET"
                    }
                )
            } else {
                currentScreen = "HOME"
            }
        }
        // NUEVAS PANTALLAS
        "EDIT_PROFILE" -> {
            EditProfileScreen(onBack = { currentScreen = "HOME"; homeTabSelected = 3 })
        }
        "HISTORY" -> {
            HistoryScreen(userId = currentUserId, onBack = { currentScreen = "HOME"; homeTabSelected = 3 })
        }
    }
}