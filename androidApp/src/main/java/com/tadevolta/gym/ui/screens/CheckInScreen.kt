package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.viewmodels.CheckInViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val stats by viewModel.checkInStats.collectAsState()
    val history by viewModel.checkInHistory.collectAsState()
    val isCheckingIn by viewModel.isCheckingIn.collectAsState()
    
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
                Button(
                    onClick = { viewModel.performCheckIn() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCheckingIn
                ) {
                    if (isCheckingIn) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Fazer Check-in")
                    }
                }
            }
        }
    }
}
