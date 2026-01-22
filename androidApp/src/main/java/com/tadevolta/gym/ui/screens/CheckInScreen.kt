package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.CheckInViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = uiState.checkInStats
    val history = uiState.checkInHistory
    val isCheckingIn = uiState.isCheckingIn
    val isValidatingLocation = uiState.isValidatingLocation
    val isOutOfRange = uiState.isOutOfRange
    val locationError = uiState.locationError
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check-in") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Contador 10/365
                CheckInCounter(
                    current = stats?.checkInsLast365Days ?: 0,
                    total = 365,
                    label = "Check-ins nos últimos 365 dias"
                )
            }
            
            item {
                // Streak atual
                StreakCard(
                    current = stats?.currentStreak ?: 0,
                    longest = stats?.longestStreak ?: 0
                )
            }
            
            item {
                Text(
                    text = "Histórico",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            items(history.size) { index ->
                CheckInHistoryItem(checkIn = history[index])
            }
            
            item {
                // Botão de check-in
                GradientButton(
                    text = if (isValidatingLocation) "Validando localização..." else "Fazer Check-in",
                    onClick = { viewModel.performCheckIn() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCheckingIn && !isValidatingLocation
                )
            }
            
            // Mensagem de erro de localização
            if (locationError != null && !isOutOfRange) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Text(
                            text = locationError,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Destructive
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
        
        // Modal de erro "Fora de Alcance"
        if (isOutOfRange) {
            CheckInErrorModal(
                unitName = uiState.selectedUnitName,
                onRetry = { viewModel.retryLocationValidation() },
                onDismiss = { viewModel.clearLocationError() }
            )
        }
    }
}
