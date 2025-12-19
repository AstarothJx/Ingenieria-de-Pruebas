package com.example.pawsandgo.screens

import androidx.compose.runtime.*
import com.example.pawsandgo.DataRepository

@Composable
fun HomeScreen(
    userId: String,
    userRole: String,
    onLogout: () -> Unit,
    onAddPetClick: () -> Unit,
    onPetClick: (String) -> Unit,
    onBookWalkClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    // Control simple de pestañas
    var currentTab by remember { mutableIntStateOf(0) }

    if (userRole == "walker") {
        WalkerHomeScreen(
            walkerId = userId,
            onLogout = onLogout,
            onEditProfile = onEditProfileClick,
            onWalkClick = { /* ... */ }
        )
    } else {
        OwnerHomeScreen(
            userId = userId,
            onLogout = onLogout,
            onAddPetClick = onAddPetClick,
            onPetClick = onPetClick,
            onBookWalkClick = onBookWalkClick,
            onEditProfileClick = onEditProfileClick,
            onHistoryClick = onHistoryClick,
            currentTab = currentTab,
            onTabChange = { currentTab = it }
        )
    }
}