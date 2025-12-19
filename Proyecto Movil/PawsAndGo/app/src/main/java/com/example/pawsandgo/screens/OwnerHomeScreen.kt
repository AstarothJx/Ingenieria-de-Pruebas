package com.example.pawsandgo.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.pawsandgo.DataRepository
import com.example.pawsandgo.components.PetCard
import com.example.pawsandgo.components.WalkCard
import com.example.pawsandgo.components.WalkerCard
import com.example.pawsandgo.models.Pet
import com.example.pawsandgo.models.Walk
import com.example.pawsandgo.models.Walker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    userId: String,
    onLogout: () -> Unit,
    onAddPetClick: () -> Unit,
    onPetClick: (String) -> Unit,
    onBookWalkClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onHistoryClick: () -> Unit,
    currentTab: Int,
    onTabChange: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    // 1. Datos del usuario actual
    val myPets = DataRepository.pets.filter { it.ownerId == userId }
    val myWalks = DataRepository.walks.filter { it.ownerId == userId }
    val userProfile = DataRepository.getUserProfile()

    // 2. Lista de paseadores DUMMY (Estáticos)
    val defaultWalkers = listOf(
        Walker(
            id = "w_def1", userId = "u_def1", name = "Ana la Rápida",
            bio = "Experta en perros grandes.", maxDogs = 5,
            rating = 4.9, totalRatings = 15, totalWalks = 120, completedWalks = 120,
            activeWalks = 0, totalTips = 500.0, availableRoutes = listOf(),
            isAvailable = true, isDummy = true, createdAt = "", updatedAt = ""
        ),
        Walker(
            id = "w_def2", userId = "u_def2", name = "Beto el Amable",
            bio = "Paciencia infinita.", maxDogs = 3,
            rating = 4.7, totalRatings = 8, totalWalks = 85, completedWalks = 85,
            activeWalks = 0, totalTips = 300.0, availableRoutes = listOf(),
            isAvailable = true, isDummy = true, createdAt = "", updatedAt = ""
        ),
        Walker(
            id = "w_def3", userId = "u_def3", name = "Carlos Runner",
            bio = "Full deporte.", maxDogs = 4,
            rating = 4.8, totalRatings = 20, totalWalks = 200, completedWalks = 200,
            activeWalks = 0, totalTips = 800.0, availableRoutes = listOf(),
            isAvailable = true, isDummy = true, createdAt = "", updatedAt = ""
        )
    )

    // 3. Lista de paseadores REALES
    val registeredProfiles = DataRepository.getAllWalkersProfiles()

    val realWalkers = registeredProfiles.map { profile ->
        val stats = DataRepository.getWalkerRating(profile.id)
        val completedCount = DataRepository.walks.count { it.walkerId == profile.id && it.status == "completed" }
        val formattedRating = String.format("%.1f", stats.first).toDouble()

        Walker(
            id = profile.id,
            userId = profile.id,
            name = profile.name,
            bio = "Paseador verificado ✅",
            maxDogs = 5,
            rating = formattedRating,
            totalRatings = stats.second,
            totalWalks = completedCount,
            completedWalks = completedCount,
            activeWalks = 0,
            totalTips = 0.0,
            availableRoutes = listOf("r_parque", "r_urbana"),
            isAvailable = true,
            isDummy = false,
            createdAt = "",
            updatedAt = ""
        )
    }

    val allWalkers = (defaultWalkers + realWalkers).distinctBy { it.id }

    // --- ESTADOS ---
    var selectedWalk by remember { mutableStateOf<Walk?>(null) }
    var isWalkingLive by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showCancelConfirmation by remember { mutableStateOf(false) }

    // --- NAVEGACIÓN ---
    if (isWalkingLive && selectedWalk != null) {
        WalkLiveScreen(
            walk = selectedWalk!!,
            onFinishWalk = {
                isWalkingLive = false
                showRatingDialog = true
            }
        )
    } else {
        Scaffold(
            containerColor = Color(0xFFF8F9FA), // Fondo gris muy suave
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Pets, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PawsGo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                    actions = { IconButton(onClick = { }) { Icon(Icons.Outlined.Notifications, null, tint = MaterialTheme.colorScheme.primary) } }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                    val items = listOf(
                        Triple("Inicio", Icons.Filled.Home, 0),
                        Triple("Mascotas", Icons.Filled.Pets, 1),
                        Triple("Paseadores", Icons.Filled.PersonSearch, 2),
                        Triple("Perfil", Icons.Filled.Person, 3)
                    )
                    items.forEach { (label, icon, index) ->
                        NavigationBarItem(
                            icon = { Icon(icon, null) },
                            label = { Text(label) },
                            selected = currentTab == index,
                            onClick = { onTabChange(index) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp) // Un poco menos de padding lateral para aprovechar espacio
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                when (currentTab) {
                    0 -> HomeTab(myWalks, onBookWalkClick) { selectedWalk = it }
                    1 -> PetsTab(myPets, onAddPetClick, onPetClick)
                    2 -> WalkersTab(allWalkers)
                    3 -> ProfileTab(userProfile.name, onEditProfileClick, onHistoryClick, onLogout)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // --- DIALOGOS ---
            if (selectedWalk != null && !showRatingDialog && !showCancelConfirmation) {
                WalkDetailDialog(selectedWalk!!, allWalkers, { selectedWalk = null }, { showCancelConfirmation = true }, { isWalkingLive = true })
            }
            if (showCancelConfirmation) {
                AlertDialog(
                    onDismissRequest = { showCancelConfirmation = false },
                    title = { Text("Cancelar Paseo") },
                    text = { Text("¿Seguro que deseas cancelar? No se puede deshacer.") },
                    confirmButton = {
                        Button(onClick = { if (selectedWalk != null) { DataRepository.cancelWalk(selectedWalk!!.id); selectedWalk = null }; showCancelConfirmation = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Sí, Cancelar") }
                    },
                    dismissButton = { TextButton(onClick = { showCancelConfirmation = false }) { Text("Volver") } }
                )
            }
            if (showRatingDialog && selectedWalk != null) {
                RatingDialog(
                    walkerName = allWalkers.find { it.id == selectedWalk!!.walkerId }?.name ?: "Paseador",
                    onDismiss = { showRatingDialog = false; selectedWalk = null },
                    onSubmit = { rating, tip -> DataRepository.rateWalker(selectedWalk!!.id, selectedWalk!!.walkerId, rating, tip); DataRepository.completeWalk(selectedWalk!!.id); showRatingDialog = false; selectedWalk = null }
                )
            }
        }
    }
}

// --- TAB INICIO (MODERNIZADO) ---
@Composable
fun HomeTab(walks: List<Walk>, onBookWalkClick: () -> Unit, onWalkClick: (Walk) -> Unit) {
    Column {
        Text("Hola, Dueño! 👋", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Text("Panel Principal", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(24.dp))

        val activeWalks = walks.filter { it.status != "cancelled" }.sortedBy { it.status == "completed" }

        // Stats Row Moderno
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                InfoCard(Icons.Default.CalendarToday, "Próximos", "${activeWalks.count { it.status == "scheduled" }}", Color(0xFF2196F3), Color(0xFF64B5F6))
            }
            Box(modifier = Modifier.weight(1f)) {
                InfoCard(Icons.Default.CheckCircle, "Completados", "${activeWalks.count { it.status == "completed" }}", Color(0xFF4CAF50), Color(0xFF81C784))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de Acción Principal Mejorado
        Button(
            onClick = onBookWalkClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Reservar Nuevo Paseo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Mis Paseos Recientes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (activeWalks.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Pets, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Text("No tienes paseos activos.", color = Color.Gray)
                }
            }
        } else {
            activeWalks.forEach { walk ->
                WalkCard(walk = walk, onClick = { onWalkClick(walk) })
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

// --- PETS TAB ---
@Composable
fun PetsTab(pets: List<Pet>, onAddPetClick: () -> Unit, onPetClick: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Mis Mascotas 🐕", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        FilledIconButton(onClick = onAddPetClick, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Add, "Agregar") }
    }
    Spacer(modifier = Modifier.height(16.dp))
    if(pets.isEmpty()) Text("Agrega a tu primera mascota para empezar.", color = Color.Gray)
    else pets.forEach { pet -> PetCard(pet = pet, onClick = { onPetClick(pet.id) }); Spacer(modifier = Modifier.height(12.dp)) }
}

// --- WALKERS TAB ---
@Composable
fun WalkersTab(walkers: List<Walker>) {
    Text("Paseadores Top 🌟", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))
    if (walkers.isEmpty()) Text("No hay paseadores disponibles.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    else walkers.forEach { walker -> WalkerCard(walker = walker); Spacer(modifier = Modifier.height(12.dp)) }
}

// --- PROFILE TAB (MODERNIZADO) ---
@Composable
fun ProfileTab(userName: String, onEdit: () -> Unit, onHistory: () -> Unit, onLogout: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        // Avatar con sombra
        Surface(
            modifier = Modifier.size(110.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 10.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(userName.take(1).uppercase(), style = MaterialTheme.typography.displayMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(userName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Dueño Responsable", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Spacer(modifier = Modifier.height(40.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileOption(Icons.Default.Edit, "Editar Perfil", onEdit)
                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                ProfileOption(Icons.Default.History, "Historial de Paseos", onHistory)
                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                ProfileOption(Icons.Outlined.Help, "Ayuda y Soporte", {})
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(
            onClick = onLogout,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión")
        }
    }
}

// --- INFO CARD (AHORA CON DEGRADADO) ---
@Composable
fun InfoCard(icon: ImageVector, title: String, value: String, colorStart: Color, colorEnd: Color) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth().height(110.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(colorStart, colorEnd)))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                Icon(icon, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(28.dp))
                Column {
                    Text(value, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(title, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.9f))
                }
            }
        }
    }
}

// --- PROFILE OPTION (MEJORADO) ---
@Composable
fun ProfileOption(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
    }
}

// --- COMPONENTES AUXILIARES SIN CAMBIOS DE LÓGICA ---
@Composable
fun WalkDetailDialog(walk: Walk, allWalkers: List<Walker>, onDismiss: () -> Unit, onCancel: () -> Unit, onSimulateComplete: () -> Unit) {
    var isSimulating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val walkerName = allWalkers.find { it.id == walk.walkerId }?.name ?: "Paseador"
    val route = DataRepository.allRoutes.find { it.id == walk.routeId }
    val routeName = route?.name ?: "Ruta Personalizada"

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(if (walk.status == "completed") Icons.Default.CheckCircle else Icons.Default.Map, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        if (isSimulating) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(if (walk.status == "scheduled") "Paseo Programado" else "Paseo Finalizado", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(Icons.Default.Place, "Ruta", routeName)
                    DetailRow(Icons.Default.Person, "Paseador", walkerName)
                    DetailRow(Icons.Default.CalendarToday, "Fecha", walk.scheduledDate)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", style = MaterialTheme.typography.bodyMedium)
                    Text("$${walk.totalPrice}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(24.dp))
                if (isSimulating) {
                    Text("Conectando...", color = MaterialTheme.colorScheme.primary)
                } else {
                    if (walk.status == "scheduled") {
                        Button(onClick = { isSimulating = true; scope.launch { delay(1500); onSimulateComplete() } }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Iniciar Simulación") }
                        TextButton(onClick = onCancel, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Cancelar Paseo") }
                    } else {
                        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cerrar") }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun RatingDialog(walkerName: String, onDismiss: () -> Unit, onSubmit: (Double, Double) -> Unit) {
    var rating by remember { mutableDoubleStateOf(0.0) }
    var selectedTip by remember { mutableDoubleStateOf(0.0) }
    val tips = listOf(10.0, 20.0, 50.0)
    AlertDialog(
        onDismissRequest = {}, title = { Text("¡Paseo Finalizado!") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("¿Qué tal $walkerName?")
                Spacer(modifier = Modifier.height(16.dp))
                Row { (1..5).forEach { i -> Icon(if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp).clickable { rating = i.toDouble() }) } }
                Spacer(modifier = Modifier.height(16.dp)); Divider(); Spacer(modifier = Modifier.height(16.dp))
                Text("Propina")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { tips.forEach { t -> FilterChip(selected = selectedTip == t, onClick = { selectedTip = if (selectedTip == t) 0.0 else t }, label = { Text("$${t.toInt()}") }) } }
            }
        },
        confirmButton = { Button(onClick = { onSubmit(rating, selectedTip) }, enabled = rating > 0, modifier = Modifier.fillMaxWidth()) { Text("Enviar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Omitir") } }
    )
}