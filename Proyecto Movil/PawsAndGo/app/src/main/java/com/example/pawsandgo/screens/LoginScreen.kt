package com.example.pawsandgo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.example.pawsandgo.DataRepository
import com.example.pawsandgo.UserProfile // Asegúrate de importar esto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (UserProfile) -> Unit, // CAMBIO: Devolvemos el perfil encontrado
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Pets, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))
        Text("Bienvenido a PawsGo", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it; errorMessage = null },
            label = { Text("Email") }, leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Contraseña") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if(passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } },
            visualTransformation = if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if(email.isBlank()) {
                    errorMessage = "Ingresa tu correo"
                } else {
                    isLoading = true
                    scope.launch {
                        delay(1000)
                        val foundProfile = DataRepository.performLogin(email)
                        isLoading = false

                        if (foundProfile != null) {
                            onLoginSuccess(foundProfile) // ¡ÉXITO! Encontramos al usuario
                        } else {
                            errorMessage = "Usuario no encontrado. Regístrate primero."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading
        ) {
            if(isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary) else Text("Iniciar Sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToRegister) { Text("¿No tienes cuenta? Regístrate") }
    }
}