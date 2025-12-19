package com.example.pawsandgo.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pawsandgo.DataRepository
import com.example.pawsandgo.components.WalkerCard
import com.example.pawsandgo.models.Walk
import com.example.pawsandgo.models.Walker
import java.util.Calendar

// SOLUCIÓN AL ERROR: Agregamos ExperimentalLayoutApi aquí
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookWalkScreen(
    ownerId: String,
    onBack: () -> Unit,
    onWalkBooked: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // --- CARGAR DATOS ---
    val myPets = remember { DataRepository.pets.filter { it.ownerId == ownerId } }
    val registeredProfiles = remember { DataRepository.getAllWalkersProfiles() }
    val allSystemRoutes = remember { DataRepository.allRoutes }

    // Construimos paseadores con rutas simuladas
    val allWalkers = remember(registeredProfiles) {
        val dummies = listOf(
            Walker(
                id = "w_def1",
                userId = "u_def1",
                name = "Ana la Rápida",
                bio = "Experta en perros grandes.",
                maxDogs = 5,
                rating = 4.9,
                totalRatings = 50,
                totalWalks = 120,
                completedWalks = 120,
                activeWalks = 0,
                totalTips = 0.0,
                availableRoutes = listOf("r_parque", "r_urbana"),
                isAvailable = true,
                isDummy = true,
                createdAt = "",
                updatedAt = ""
            ),
            Walker(
                id = "w_def2",
                userId = "u_def2",
                name = "Beto el Amable",
                bio = "Paciencia con cachorros.",
                maxDogs = 3,
                rating = 4.7,
                totalRatings = 30,
                totalWalks = 85,
                completedWalks = 85,
                activeWalks = 0,
                totalTips = 0.0,
                availableRoutes = listOf("r_bosque"),
                isAvailable = true,
                isDummy = true,
                createdAt = "",
                updatedAt = ""
            ),
            Walker(
                id = "w_def3",
                userId = "u_def3",
                name = "Carlos Runner",
                bio = "Running con tu perro.",
                maxDogs = 4,
                rating = 4.8,
                totalRatings = 45,
                totalWalks = 200,
                completedWalks = 200,
                activeWalks = 0,
                totalTips = 0.0,
                availableRoutes = listOf("r_nocturna", "r_urbana"),
                isAvailable = true,
                isDummy = true,
                createdAt = "",
                updatedAt = ""
            )
        )

        // AQUÍ ESTÁ EL CAMBIO CLAVE:
        val realOnes = registeredProfiles.map { profile ->
            // Leemos las rutas que este paseador guardó en su perfil
            val savedRoutes = DataRepository.getRoutesForWalker(profile.id)

            Walker(
                id = profile.id,
                userId = profile.id,
                name = profile.name,
                bio = "Paseador verificado ✅",
                maxDogs = 3,
                rating = 5.0,
                totalWalks = 0,
                availableRoutes = savedRoutes,
                isAvailable = true,
                createdAt = "",
                updatedAt = ""
            )
        }
        dummies + realOnes
    }

    // --- ESTADOS ---
    var selectedPetId by remember { mutableStateOf(myPets.firstOrNull()?.id ?: "") }
    var selectedWalker by remember { mutableStateOf<Walker?>(null) }

    // Ruta seleccionada
    var selectedRouteId by remember { mutableStateOf("") }
    val selectedRouteData = allSystemRoutes.find { it.id == selectedRouteId }

    // Fecha
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf("") }

    // Hora (Ahora seleccionada de chips)
    var selectedHourInt by remember { mutableStateOf<Int?>(null) }

    // Duración
    var durationMinutes by remember { mutableIntStateOf(60) }
    var notes by remember { mutableStateOf("") }

    // UI Dropdowns
    var expandedWalker by remember { mutableStateOf(false) }
    var expandedPet by remember { mutableStateOf(false) }
    var expandedRoute by remember { mutableStateOf(false) }

    val pricePerMinute = 0.8
    val totalPrice = (durationMinutes * pricePerMinute).toInt()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reservar Paseo") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 16.dp, tonalElevation = 8.dp) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Total a pagar", style = MaterialTheme.typography.bodyMedium)
                            Text("$$totalPrice MXN", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                if (selectedPetId.isEmpty() || selectedWalker == null || selectedRouteData == null || selectedDate.isEmpty() || selectedHourInt == null) {
                                    Toast.makeText(context, "Faltan datos por seleccionar", Toast.LENGTH_SHORT).show()
                                } else {
                                    val petName = myPets.find { it.id == selectedPetId }?.name ?: "Mascota"
                                    val finalTime = String.format("%02d:00", selectedHourInt)

                                    val newWalk = Walk(
                                        id = "walk_${System.currentTimeMillis()}",
                                        ownerId = ownerId,
                                        walkerId = selectedWalker!!.id,
                                        petId = selectedPetId,
                                        petName = petName,
                                        petPhoto = "",
                                        routeId = selectedRouteId,
                                        scheduledDate = "$selectedDate $finalTime",
                                        duration = durationMinutes,
                                        totalPrice = totalPrice.toDouble(),
                                        status = "scheduled",
                                        createdAt = System.currentTimeMillis().toString(),
                                        updatedAt = System.currentTimeMillis().toString()
                                    )
                                    DataRepository.addWalk(newWalk)
                                    Toast.makeText(context, "¡Reserva Exitosa!", Toast.LENGTH_SHORT).show()
                                    onWalkBooked()
                                }
                            },
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirmar")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 24.dp).verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. MASCOTA
            Text("¿A quién vamos a pasear?", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(expanded = expandedPet, onExpandedChange = { expandedPet = !expandedPet }) {
                OutlinedTextField(
                    value = myPets.find { it.id == selectedPetId }?.name ?: "Selecciona mascota",
                    onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPet) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Pets, null) }
                )
                ExposedDropdownMenu(expanded = expandedPet, onDismissRequest = { expandedPet = false }) {
                    myPets.forEach { pet -> DropdownMenuItem(text = { Text(pet.name) }, onClick = { selectedPetId = pet.id; expandedPet = false }) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. PASEADOR
            Text("Elige a tu paseador", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(expanded = expandedWalker, onExpandedChange = { expandedWalker = !expandedWalker }) {
                OutlinedTextField(
                    value = selectedWalker?.name ?: "Selecciona un paseador",
                    onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWalker) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.PersonSearch, null) }
                )
                ExposedDropdownMenu(expanded = expandedWalker, onDismissRequest = { expandedWalker = false }) {
                    allWalkers.forEach { walker ->
                        DropdownMenuItem(
                            text = { Text("${walker.name} (⭐ ${walker.rating})") },
                            onClick = {
                                selectedWalker = walker
                                selectedRouteId = ""
                                selectedHourInt = null
                                expandedWalker = false
                            }
                        )
                    }
                }
            }

            if (selectedWalker != null) {
                Spacer(modifier = Modifier.height(8.dp))
                WalkerCard(walker = selectedWalker!!)

                Spacer(modifier = Modifier.height(24.dp))

                // 3. RUTA
                val walkerRoutes = allSystemRoutes.filter { selectedWalker!!.availableRoutes.contains(it.id) }

                Text("Selecciona la Ruta", style = MaterialTheme.typography.titleMedium)
                if (walkerRoutes.isEmpty()) {
                    Text("Este paseador no tiene rutas configuradas.", color = MaterialTheme.colorScheme.error)
                } else {
                    ExposedDropdownMenuBox(expanded = expandedRoute, onExpandedChange = { expandedRoute = !expandedRoute }) {
                        OutlinedTextField(
                            value = selectedRouteData?.name ?: "Selecciona una ruta",
                            onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoute) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Map, null) }
                        )
                        ExposedDropdownMenu(expanded = expandedRoute, onDismissRequest = { expandedRoute = false }) {
                            walkerRoutes.forEach { route ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(route.name, fontWeight = FontWeight.Bold)
                                            Text(route.description, style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    onClick = {
                                        selectedRouteId = route.id
                                        selectedHourInt = null
                                        expandedRoute = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 4. FECHA Y HORA (Con FlowRow corregido)
            if (selectedRouteData != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Fecha y Hora", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedCard(
                    onClick = {
                        val datePicker = DatePickerDialog(context, { _, year, month, day ->
                            selectedDate = "$day/${month + 1}/$year"
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
                        datePicker.show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(if (selectedDate.isEmpty()) "Seleccionar Fecha" else selectedDate, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Horarios disponibles para ${selectedRouteData.name}:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                val availableHours = (selectedRouteData.startHour until selectedRouteData.endHour).toList()

                // Aquí usamos FlowRow, gracias al @OptIn de arriba ya no dará error
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableHours.forEach { hour ->
                        FilterChip(
                            selected = selectedHourInt == hour,
                            onClick = { selectedHourInt = hour },
                            label = { Text("$hour:00") },
                            leadingIcon = if (selectedHourInt == hour) { { Icon(Icons.Default.Check, null) } } else null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. DURACIÓN
            Text("Duración", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(30, 60, 90).forEach { mins ->
                    FilterChip(
                        selected = durationMinutes == mins,
                        onClick = { durationMinutes = mins },
                        label = { Text("$mins min") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notas") }, modifier = Modifier.fillMaxWidth(), minLines = 3
            )
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}