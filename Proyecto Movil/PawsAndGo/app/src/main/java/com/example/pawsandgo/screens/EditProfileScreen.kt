package com.example.pawsandgo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pawsandgo.DataRepository
import com.example.pawsandgo.UserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val currentProfile = remember { DataRepository.getUserProfile() }
    var name by remember { mutableStateOf(currentProfile.name) }
    var phone by remember { mutableStateOf(currentProfile.phone) }
    var email by remember { mutableStateOf(currentProfile.email) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(24.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // MANTENEMOS ID Y ROL
                    val currentId = currentProfile.id
                    val currentRole = currentProfile.role

                    val updatedProfile = com.example.pawsandgo.UserProfile(
                        id = currentId,
                        name = name,
                        phone = phone,
                        email = email,
                        role = currentRole
                    )

                    DataRepository.saveUserProfile(updatedProfile)
                    scope.launch {
                        snackbarHostState.showSnackbar("Perfil actualizado")
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Guardar Cambios") }
        }
    }
}