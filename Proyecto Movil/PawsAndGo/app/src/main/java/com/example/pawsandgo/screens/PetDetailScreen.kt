package com.example.pawsandgo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.* // Importante para remember, mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pawsandgo.DataRepository

// IMPORTANTE: Estos imports son necesarios para usar "by remember"
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    petId: String,
    onBack: () -> Unit,
    onEditClick: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Buscamos la mascota. Puede ser null si no se encuentra.
    val pet = DataRepository.pets.find { it.id == petId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(pet?.name ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // BOTÓN EDITAR
                    IconButton(onClick = { if (pet != null) onEditClick(pet.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    // BOTÓN ELIMINAR
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->

        // VERIFICAMOS SI LA MASCOTA EXISTE ANTES DE PINTAR NADA
        if (pet == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Mascota no encontrada")
            }
        } else {
            // SI EXISTE, PINTAMOS EL CONTENIDO
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // FOTO DE CABECERA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (pet.photo != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(pet.photo)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    // TÍTULO
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${pet.breed} • ${pet.age} años",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // TARJETAS DE DATOS
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DetailItem(Icons.Default.MonitorWeight, "Peso", "${pet.weight} kg", Modifier.weight(1f))
                        DetailItem(Icons.Default.Straighten, "Tamaño", pet.size, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // NECESIDADES ESPECIALES
                    if (!pet.specialNeeds.isNullOrBlank()) {
                        Text("Necesidades Especiales", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = pet.specialNeeds,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // --- DIÁLOGO DE CONFIRMACIÓN (SE PINTA ENCIMA DEL CONTENIDO) ---
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Confirmar Eliminación") },
                    text = { Text("¿Estás seguro de que quieres eliminar a ${pet.name}?\nEsta acción no se puede deshacer.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                DataRepository.deletePet(pet.id)
                                showDeleteDialog = false
                                onBack() // Volver al Home/Mascotas
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Eliminar", color = Color.White)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}