package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.data.models.Exercise
import com.tadevolta.gym.data.models.Result
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.TrainingPlanViewModel

@Composable
fun TrainingPlanScreen(
    planId: String,
    viewModel: TrainingPlanViewModel = hiltViewModel(),
    onExerciseClick: (Exercise) -> Unit = {}
) {
    val plan by viewModel.trainingPlan.collectAsState()
    
    LaunchedEffect(planId) {
        viewModel.loadPlan(planId)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        when (val state = plan) {
            is Result.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PurplePrimary)
                }
            }
            is Result.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Meus Treinos",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Escolha seu treino de hoje",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MutedForegroundDark
                                    )
                                )
                            }
                            IconButton(onClick = { /* TODO: Abrir calendário */ }) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Calendário",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    
                    // Card de progresso/gamificação
                    item {
                        ProgressGamificationCard(
                            currentStreak = 12,
                            checkInsThisMonth = 10
                        )
                    }
                    
                    // Lista de treinos
                    item {
                        WorkoutCard(
                            title = "Pernas",
                            exercises = 12,
                            duration = "45 min",
                            description = "Treino focado em desenvolvimento de força e hipertrofia dos membros inferiores.",
                            iconColor = Color(0xFF3B82F6),
                            isRecommended = true,
                            onClick = {
                                state.data.exercises.firstOrNull()?.let {
                                    onExerciseClick(it)
                                }
                            }
                        )
                    }
                    
                    item {
                        WorkoutCard(
                            title = "Peito e Tríceps",
                            exercises = 10,
                            duration = "40 min",
                            description = "Desenvolva força e massa muscular no peitoral e tríceps.",
                            iconColor = PinkAccent,
                            onClick = {
                                state.data.exercises.firstOrNull()?.let {
                                    onExerciseClick(it)
                                }
                            }
                        )
                    }
                    
                    item {
                        WorkoutCard(
                            title = "Cardio",
                            exercises = 8,
                            duration = "30 min",
                            description = "Melhore sua capacidade cardiovascular e queime calorias.",
                            iconColor = Color(0xFF3B82F6),
                            onClick = {
                                state.data.exercises.firstOrNull()?.let {
                                    onExerciseClick(it)
                                }
                            }
                        )
                    }
                }
            }
            is Result.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Erro ao carregar plano: ${state.exception.message}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}
