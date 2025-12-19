package com.example.pawsandgo.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pawsandgo.DataRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    ownerId: String,
    petIdToEdit: String? = null,
    onBack: () -> Unit,
    onPetAdded: () -> Unit
) {
    val context = LocalContext.current
    val existingPet = remember { if (petIdToEdit != null) DataRepository.getPet(petIdToEdit) else null }

    var name by remember { mutableStateOf(existingPet?.name ?: "") }
    var breed by remember { mutableStateOf(existingPet?.breed ?: "") }
    var age by remember { mutableStateOf(existingPet?.age?.toString() ?: "") }
    var weight by remember { mutableStateOf(existingPet?.weight?.toString() ?: "") }
    var size by remember { mutableStateOf(existingPet?.size ?: "Mediano") }
    var specialNeeds by remember { mutableStateOf(existingPet?.specialNeeds ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(existingPet?.photo?.let { Uri.parse(it) }) }

    // --- SOLO UNA DECLARACIÓN DEL LAUNCHER ---
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                val flag = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flag)
                selectedImageUri = uri
            }
        }
    )

    var expanded by remember { mutableStateOf(false) }
    val sizes = listOf("Pequeño", "Mediano", "Grande")
    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var nameError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }
    var weightError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (existingPet != null) "Editar Mascota" else "Agregar Mascota") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
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
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { photoPickerLauncher.launch(arrayOf("image/*")) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(selectedImageUri).crossfade(true).build(),
                        contentDescription = "Foto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.AddAPhoto, "Agregar Foto", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.Start) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it; nameError = null },
                    label = { Text("Nombre") }, isError = nameError != null,
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = breed, onValueChange = { breed = it },
                    label = { Text("Raza") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = age, onValueChange = { age = it },
                        label = { Text("Edad") }, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = weight, onValueChange = { weight = it },
                        label = { Text("Kg") }, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = size, onValueChange = {}, readOnly = true, label = { Text("Tamaño") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        sizes.forEach { s -> DropdownMenuItem(text = { Text(s) }, onClick = { size = s; expanded = false }) }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = specialNeeds, onValueChange = { specialNeeds = it },
                    label = { Text("Necesidades especiales") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val ageInt = age.toIntOrNull() ?: 0
                        val weightDouble = weight.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank() && ageInt >= 0 && weightDouble > 0) {
                            scope.launch {
                                val newPet = com.example.pawsandgo.models.Pet(
                                    id = existingPet?.id ?: System.currentTimeMillis().toString(),
                                    ownerId = ownerId, name = name, breed = breed, age = ageInt,
                                    weight = weightDouble, size = size, specialNeeds = specialNeeds,
                                    photo = selectedImageUri?.toString(), createdAt = "", updatedAt = ""
                                )
                                if (existingPet != null) DataRepository.updatePet(newPet) else DataRepository.addPet(newPet)
                                onPetAdded()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text("Guardar") }
            }
        }
    }
}