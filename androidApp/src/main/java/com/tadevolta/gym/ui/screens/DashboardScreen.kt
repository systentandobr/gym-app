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
import com.tadevolta.gym.ui.viewmodels.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToTrainingPlan: (String) -> Unit = {},
    onNavigateToCheckIn: () -> Unit = {},
    onNavigateToRanking: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tadevolta Gym") }
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
                WelcomeCard(user = uiState.user)
            }
            item {
                CheckInCard(
                    stats = uiState.checkInStats,
                    onCheckIn = onNavigateToCheckIn
                )
            }
            item {
                TrainingPlanCard(
                    plan = uiState.currentTrainingPlan,
                    onViewPlan = { plan ->
                        onNavigateToTrainingPlan(plan.id)
                    }
                )
            }
            item {
                GamificationCard(
                    data = uiState.gamificationData,
                    onViewRanking = onNavigateToRanking
                )
            }
        }
    }
}
