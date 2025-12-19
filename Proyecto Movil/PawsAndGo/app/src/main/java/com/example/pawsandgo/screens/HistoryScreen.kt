package com.example.pawsandgo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pawsandgo.DataRepository
import com.example.pawsandgo.components.WalkCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(userId: String, onBack: () -> Unit) {
    // Aquí SÍ mostramos todos (completados y cancelados)
    val historyWalks = DataRepository.walks.filter {
        it.ownerId == userId && (it.status == "completed" || it.status == "cancelled")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Historial") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(24.dp).verticalScroll(rememberScrollState())) {
            if (historyWalks.isEmpty()) {
                Text("No hay historial de paseos.")
            } else {
                historyWalks.forEach { walk ->
                    WalkCard(walk = walk, onClick = {}) // Sin acción al click
                }
            }
        }
    }
}