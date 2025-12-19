package com.example.pawsandgo.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pawsandgo.ChatMessage
import com.example.pawsandgo.DataRepository
import com.example.pawsandgo.models.Walk
import kotlinx.coroutines.delay
import kotlin.math.atan2

@Composable
fun WalkLiveScreen(
    walk: Walk,
    onFinishWalk: () -> Unit
) {
    val liveWalk = DataRepository.walks.find { it.id == walk.id } ?: walk
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // TIEMPO
    var simulatedMinutes by remember { mutableIntStateOf(0) }
    val totalMinutes = liveWalk.duration

    // Progreso suave (0.0 a 1.0)
    val targetProgress = (simulatedMinutes.toFloat() / totalMinutes.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000)
    )

    // FRASES (Sin fotos)
    val frasesInicio = listOf("¡Arrancamos! 🐕 Energía a tope.", "El clima está perfecto.", "¡Listo! Empezamos la aventura.")
    val frasesMedio = listOf("Parada técnica para agua 💧", "Olfateando un árbol 🌳", "Vamos a muy buen ritmo.")
    val frasesFinal = listOf("Ya vamos de regreso a casa 🏠", "Últimas cuadras, va feliz.", "Casi llegamos.")
    val respuestasWalker = listOf("¡Entendido!", "👍", "Claro, sin problema.", "¡Hecho!")

    // LÓGICA DE SIMULACIÓN
    LaunchedEffect(Unit) {
        if (liveWalk.chatHistory.isEmpty()) {
            addSimulatedMessage(liveWalk.id, "system", "📍 GPS Conectado. Ruta cargada.")
            delay(1000)
            addSimulatedMessage(liveWalk.id, "walker", "¡Hola! Ya tengo a ${liveWalk.petName}, ¡vamonos! 🐕")
        }

        while (simulatedMinutes < totalMinutes) {
            delay(1000) // 1 seg real = 2 min simulados
            simulatedMinutes += 2

            // Eventos (Sin fotos)
            if (simulatedMinutes == 4) addSimulatedMessage(liveWalk.id, "walker", frasesInicio.random())
            if (simulatedMinutes == (totalMinutes / 2)) addSimulatedMessage(liveWalk.id, "walker", frasesMedio.random())
            if (simulatedMinutes == (totalMinutes - 4)) addSimulatedMessage(liveWalk.id, "walker", frasesFinal.random())
        }

        addSimulatedMessage(liveWalk.id, "system", "🏁 Has llegado a tu destino.")
        delay(3000)
        onFinishWalk()
    }

    // RESPUESTA AUTOMÁTICA
    LaunchedEffect(liveWalk.chatHistory.size) {
        if (liveWalk.chatHistory.isNotEmpty()) {
            val lastMsg = liveWalk.chatHistory.last()
            listState.animateScrollToItem(liveWalk.chatHistory.size - 1)
            if (lastMsg.senderId == "owner") {
                delay(2000)
                addSimulatedMessage(liveWalk.id, "walker", respuestasWalker.random())
            }
        }
    }

    // UI
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F2F5))) {
        // 1. MAPA AVANZADO
        AdvancedVectorMap(
            petName = liveWalk.petName,
            progress = animatedProgress,
            timeStr = "$simulatedMinutes / $totalMinutes min"
        )

        // 2. CHAT
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            state = listState,
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(liveWalk.chatHistory) { msg -> ChatBubbleSimple(msg) }
        }

        // 3. INPUT
        InputBar(
            text = messageText,
            onTextChange = { messageText = it },
            onSend = {
                if (messageText.isNotBlank()) {
                    val newMsg = ChatMessage(senderId = "owner", senderName = "Yo", message = messageText)
                    DataRepository.addMessageToWalk(liveWalk.id, newMsg)
                    messageText = ""
                }
            }
        )
    }
}

// --- MAPA VECTORIAL AVANZADO (PathMeasure CORREGIDO) ---
@Composable
fun AdvancedVectorMap(petName: String, progress: Float, timeStr: String) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.fillMaxWidth().height(280.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE5E5E5))) {

            // VARIABLES PARA POSICIÓN DEL PERRO
            var dogRotation by remember { mutableStateOf(0f) }

            // Variables para controlar la posición relativa en el Box (BiasAlignment)
            // Bias va de -1 (izquierda/arriba) a 1 (derecha/abajo)
            var biasX by remember { mutableStateOf(0f) }
            var biasY by remember { mutableStateOf(0f) }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. DIBUJAR CALLES Y MANZANAS (Fondo)
                drawRect(color = Color(0xFFF5F5F5), topLeft = Offset(0f, 0f), size = size)
                drawRect(color = Color(0xFFC8E6C9), topLeft = Offset(w * 0.4f, h * 0.2f), size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.6f))

                val roadColor = Color.White
                val strokeRoad = 40.dp.toPx()
                drawLine(roadColor, start = Offset(w * 0.2f, 0f), end = Offset(w * 0.2f, h), strokeWidth = strokeRoad)
                drawLine(roadColor, start = Offset(0f, h * 0.8f), end = Offset(w, h * 0.8f), strokeWidth = strokeRoad)
                drawLine(roadColor, start = Offset(0f, h * 0.3f), end = Offset(w, h * 0.3f), strokeWidth = strokeRoad)

                // 2. DEFINIR LA RUTA EXACTA (Bezier Curve)
                val routePath = Path().apply {
                    moveTo(w * 0.2f, h * 0.9f) // Inicio (Abajo Izquierda)
                    lineTo(w * 0.2f, h * 0.3f) // Sube por la calle
                    quadraticBezierTo(w * 0.2f, h * 0.1f, w * 0.4f, h * 0.2f) // Curva
                    cubicTo(w * 0.6f, h * 0.3f, w * 0.7f, h * 0.5f, w * 0.5f, h * 0.6f) // Vuelta
                    lineTo(w * 0.4f, h * 0.8f) // Baja
                }

                // Dibujar la ruta planeada (Gris)
                drawPath(routePath, Color(0xFFB0BEC5), style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))

                // 3. CÁLCULOS MATEMÁTICOS (Corrección getPosTan)
                // Convertimos el Path de Compose a Android nativo para usar PathMeasure completo
                val androidPath = routePath.asAndroidPath()
                val pathMeasure = android.graphics.PathMeasure(androidPath, false)
                val length = pathMeasure.length

                // Dibujar el segmento recorrido (Azul)
                val segmentPath = Path()
                // Usamos la versión de Compose para getSegment que es más fácil de dibujar
                val composeMeasure = androidx.compose.ui.graphics.PathMeasure()
                composeMeasure.setPath(routePath, false)
                composeMeasure.getSegment(0f, length * progress, segmentPath, true)
                drawPath(segmentPath, Color(0xFF4285F4), style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))

                // CALCULAR POSICIÓN Y ROTACIÓN
                val pos = FloatArray(2)
                val tan = FloatArray(2)
                pathMeasure.getPosTan(length * progress, pos, tan)

                // Convertir coordenadas de píxeles (0..width) a Bias (-1..1) para el Alignment
                // Formula: bias = (value / max) * 2 - 1
                biasX = (pos[0] / w) * 2 - 1
                biasY = (pos[1] / h) * 2 - 1

                // Calcular rotación en grados
                dogRotation = (atan2(tan[1], tan[0]) * (180 / Math.PI)).toFloat()
            }

            // --- UI SUPERPUESTA ---

            // Info Header
            Surface(
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RadioButtonChecked, null, tint = Color.Red, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EN VIVO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Red)
                    }
                    Text(petName, fontWeight = FontWeight.Bold)
                }
            }

            // EL PERRITO (Se mueve con BiasAlignment calculado)
            Box(
                modifier = Modifier
                    .align(BiasAlignment(biasX, biasY)) // <--- CORRECCIÓN AQUÍ: Usamos BiasAlignment
            ) {
                // Pin de Ubicación con Rotación
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 6.dp,
                        border = BorderStroke(2.dp, Color(0xFF4285F4)),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.rotate(dogRotation + 90f) // Ajuste para que mire al frente
                        ) {
                            Text("🐕", fontSize = 20.sp)
                        }
                    }
                    // Punta del pin
                    Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF4285F4), modifier = Modifier.size(32.dp).offset(y = (-8).dp))
                }
            }

            // Tiempo
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Text(
                    text = timeStr,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

// --- CHAT SIMPLE ---
@Composable
fun ChatBubbleSimple(msg: ChatMessage) {
    val isMe = msg.senderId == "owner"
    val isSystem = msg.senderId == "system"

    if (isSystem) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.Black.copy(alpha = 0.06f)) {
                Text(text = msg.message, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp, topEnd = 18.dp,
                    bottomStart = if (isMe) 18.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 18.dp
                ),
                color = if (isMe) MaterialTheme.colorScheme.primary else Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = msg.message,
                    modifier = Modifier.padding(12.dp),
                    color = if (isMe) Color.White else Color.Black,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun InputBar(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(color = Color.White, tonalElevation = 8.dp) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = text, onValueChange = onTextChange, placeholder = { Text("Escribe un mensaje...", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent, focusedContainerColor = Color(0xFFF2F3F5), unfocusedContainerColor = Color(0xFFF2F3F5)),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onSend, modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, CircleShape), enabled = text.isNotBlank()) {
                Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
            }
        }
    }
}

fun addSimulatedMessage(walkId: String, sender: String, text: String, type: String = "text") {
    val name = if (sender == "walker") "Paseador" else "Sistema"
    val msg = ChatMessage(senderId = sender, senderName = name, message = text, type = type)
    DataRepository.addMessageToWalk(walkId, msg)
}