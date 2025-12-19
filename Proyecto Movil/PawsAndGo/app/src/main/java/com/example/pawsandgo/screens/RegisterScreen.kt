package com.example.pawsandgo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
// CAMBIO 1: Agregamos el 4to parámetro String para el Rol
    onRegisterSuccess: (String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    // --- ESTADOS ---
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("owner") } // 'owner' o 'walker'

    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Estados de error
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- BOTÓN ATRÁS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Icono flecha correcto
                        contentDescription = "Volver"
                    )
                }
            }

            // --- CABECERA ---
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Crear Cuenta",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- FORMULARIO ---

            // 1. Nombre
            OutlinedTextField(
                value = name,
                onValueChange = {
                    // FILTRO: Solo permite escribir si son 50 caracteres o menos
                    if (it.length <= 50) {
                        name = it
                        nameError = null
                    }
                },
                label = { Text("Nombre Completo") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = nameError != null,
                // Agregamos un contador visual pequeño
                supportingText = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        if (nameError != null) Text(nameError!!) else Spacer(Modifier.width(1.dp))
                        Text("${name.length}/50")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                isError = emailError != null,
                supportingText = { if (emailError != null) Text(emailError!!) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Teléfono
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; phoneError = null },
                label = { Text("Teléfono") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                isError = phoneError != null,
                supportingText = { if (phoneError != null) Text(phoneError!!) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = null },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                isError = passwordError != null,
                supportingText = { if (passwordError != null) Text(passwordError!!) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SELECCIÓN DE ROL ---
            Text(
                text = "Tipo de cuenta",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Chip Dueño
                FilterChip(
                    selected = selectedRole == "owner",
                    onClick = { selectedRole = "owner" },
                    label = {
                        Text(
                            text = "🐕 Dueño",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )

                // Chip Paseador
                FilterChip(
                    selected = selectedRole == "walker",
                    onClick = { selectedRole = "walker" },
                    label = {
                        Text(
                            text = "🚶 Paseador",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BOTÓN REGISTRO ---
            Button(
                onClick = {
                    // Validaciones
                    var isValid = true

                    if (name.isBlank()) { nameError = "Ingresa tu nombre"; isValid = false }

                    if (email.isBlank()) { emailError = "Ingresa tu email"; isValid = false }
                    else if (!email.contains("@")) { emailError = "Email inválido"; isValid = false }

                    if (phone.isBlank()) { phoneError = "Ingresa tu teléfono"; isValid = false }

                    if (password.isBlank()) { passwordError = "Ingresa una contraseña"; isValid = false }
                    else if (password.length < 6) { passwordError = "Mínimo 6 caracteres"; isValid = false }

                    if (isValid) {
                        isLoading = true
                        scope.launch {
                            delay(1500) // Simular API
                            isLoading = false

                            val success = true // Simulación éxito

                            if (success) {
                                // CAMBIO 2: Pasamos los datos reales al MainActivity
                                onRegisterSuccess(name, phone, email, selectedRole)
                            } else {
                                snackbarHostState.showSnackbar(
                                    message = "Error al crear la cuenta"
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Registrarse")
                }
            }
        }
    }
}