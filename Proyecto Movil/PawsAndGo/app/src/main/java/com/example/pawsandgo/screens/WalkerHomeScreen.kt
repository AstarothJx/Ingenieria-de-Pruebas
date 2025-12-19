package com.example.pawsandgo.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pawsandgo.DataRepository
import com.example.pawsandgo.UserProfile
import com.example.pawsandgo.WalkRoute
import com.example.pawsandgo.components.WalkCard
import com.example.pawsandgo.models.Walk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkerHomeScreen(
    walkerId: String,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onWalkClick: (Walk) -> Unit
) {
    // 1. OBTENER DATOS Y FILTRAR
    val myWalks = DataRepository.walks.filter { it.walkerId == walkerId }
    val scheduledWalks = myWalks.filter { it.status == "scheduled" }
    val completedWalks = myWalks.filter { it.status == "completed" }

    val userProfile = DataRepository.getUserProfile()

    // 2. RUTAS ACTIVAS (Reactivo)
    // Leemos directamente del mapa observable del repositorio
    val myActiveRoutes = DataRepository.walkerRoutes[walkerId] ?: setOf("r_parque", "r_urbana")
    val allSystemRoutes = DataRepository.allRoutes

    // 3. CÁLCULOS FINANCIEROS
    val totalBase = completedWalks.sumOf { it.totalPrice }
    val totalTips = completedWalks.sumOf { it.tipAmount }
    val totalEarnings = totalBase + totalTips
    val totalWalksCount = completedWalks.size

    val myRating = if (totalWalksCount > 0) {
        completedWalks.sumOf { it.rating } / totalWalksCount
    } else {
        5.0
    }

    var currentTab by remember { mutableIntStateOf(0) }
    var isOnline by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isOnline) Color(0xFF4CAF50) else Color.Gray,
                            modifier = Modifier.size(12.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(userProfile.name.ifEmpty { "Walker" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                if (isOnline) "En línea" else "Desconectado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Switch(
                        checked = isOnline,
                        onCheckedChange = { isOnline = it },
                        thumbContent = { if (isOnline) Icon(Icons.Default.Check, null, modifier = Modifier.size(12.dp)) }
                    )
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Salir", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(icon = { Icon(Icons.Default.Dashboard, null) }, label = { Text("Panel") }, selected = currentTab == 0, onClick = { currentTab = 0 })
                NavigationBarItem(icon = { Icon(Icons.Default.ReceiptLong, null) }, label = { Text("Historial") }, selected = currentTab == 1, onClick = { currentTab = 1 })
                NavigationBarItem(icon = { Icon(Icons.Default.Person, null) }, label = { Text("Perfil") }, selected = currentTab == 2, onClick = { currentTab = 2 })
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))

            when (currentTab) {
                0 -> { // TAB 1: DASHBOARD
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // 1. Resumen Financiero
                        item {
                            EarningsCard(totalEarnings, totalBase, totalTips)
                        }

                        // 2. Rating Rápido
                        item {
                            RatingSummaryCard(myRating, totalWalksCount)
                        }

                        // 3. SELECTOR DE RUTAS (NUEVO)
                        item {
                            Text("Rutas Disponibles", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Toca para activar o desactivar", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(allSystemRoutes) { route ->
                                    val isSelected = myActiveRoutes.contains(route.id)
                                    RouteToggleCard(
                                        route = route,
                                        isSelected = isSelected,
                                        onToggle = { DataRepository.toggleRouteForWalker(walkerId, route.id) }
                                    )
                                }
                            }
                        }

                        // 4. Agenda
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Agenda de Hoy 📅", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }

                        if (scheduledWalks.isEmpty()) {
                            item {
                                EmptyStateMessage("No tienes paseos pendientes.\n¡Relájate un poco! ☕", Icons.Outlined.Weekend)
                            }
                        } else {
                            items(scheduledWalks) { walk ->
                                WalkCard(walk = walk, onClick = { onWalkClick(walk) })
                            }
                        }
                        // Espacio al final para scroll
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }

                1 -> { // TAB 2: HISTORIAL
                    Text("Historial de Ganancias 💰", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (completedWalks.isEmpty()) {
                        EmptyStateMessage("Completa paseos para ver tu historial.", Icons.Outlined.History)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(completedWalks.reversed()) { walk -> CompletedWalkCardDetails(walk) }
                        }
                    }
                }

                2 -> { // TAB 3: PERFIL
                    ProfileTabWalker(userProfile, myRating, totalWalksCount, onEditProfile)
                }
            }
        }
    }
}

// --- COMPONENTES VISUALES ---

@Composable
fun RouteToggleCard(route: WalkRoute, isSelected: Boolean, onToggle: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier
            .width(160.dp)
            .height(110.dp)
            .clickable { onToggle() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(Icons.Default.Map, null, tint = contentColor)
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                }
            }

            Column {
                Text(
                    route.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1
                )
                Text(
                    "${route.startHour}:00 - ${route.endHour}:00",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun EarningsCard(total: Double, base: Double, tips: Double) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(8.dp)) {
        Box(modifier = Modifier.background(Brush.horizontalGradient(colors = listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2)))).padding(24.dp)) {
            Column {
                Text("Ganancias Totales", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                Text("$${String.format("%.2f", total)}", color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { Text("Tarifas Base", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp); Text("$${String.format("%.2f", base)}", color = Color.White, fontWeight = FontWeight.SemiBold) }
                    Column(horizontalAlignment = Alignment.End) { Text("Propinas Extra 💚", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp); Text("$${String.format("%.2f", tips)}", color = Color(0xFF69F0AE), fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun RatingSummaryCard(rating: Double, count: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("${String.format("%.1f", rating)} / 5.0", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Basado en $count paseos completados", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun CompletedWalkCardDetails(walk: Walk) {
    Card(elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(32.dp)) { Box(contentAlignment = Alignment.Center) { Text(walk.petName.take(1), fontWeight = FontWeight.Bold) } }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column { Text(walk.petName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("Finalizado", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50)) }
                }
                Text(walk.scheduledDate, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(12.dp)); Divider(color = MaterialTheme.colorScheme.outlineVariant); Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row { repeat(5) { i -> Icon(if (i < walk.rating) Icons.Default.Star else Icons.Default.StarBorder, null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp)) } }
                Column(horizontalAlignment = Alignment.End) { if (walk.tipAmount > 0) Text("+ $${walk.tipAmount} propina", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32)); Text("$${walk.totalPrice + walk.tipAmount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) }
            }
        }
    }
}

@Composable
fun ProfileTabWalker(user: UserProfile, rating: Double, walks: Int, onEdit: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
        Surface(modifier = Modifier.size(100.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary, shadowElevation = 8.dp) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = Color.White) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(user.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(user.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onEdit, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
            Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Editar Mis Datos")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ProfileStatBadge("Nivel", "Oro 🥇")
            ProfileStatBadge("Rating", String.format("%.1f", rating))
            ProfileStatBadge("Paseos", "$walks")
        }
    }
}

@Composable
fun ProfileStatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun EmptyStateMessage(msg: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Text(msg, color = Color.Gray, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}