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
import com.tadevolta.gym.data.models.TrainingPlan
import com.tadevolta.gym.data.models.TrainingPlanStatus
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.TrainingPlanViewModel

@Composable
fun TrainingPlanScreen(
    planId: String? = null,
    studentId: String? = null,
    viewModel: TrainingPlanViewModel = hiltViewModel(),
    onExerciseClick: (Exercise) -> Unit = {},
    onPlanClick: (String) -> Unit = {}
) {
    val plan by viewModel.trainingPlan.collectAsState()
    val plans by viewModel.trainingPlans.collectAsState()
    
    LaunchedEffect(planId, studentId) {
        when {
            studentId != null -> {
                viewModel.loadPlansByStudentId(studentId)
            }
            planId != null -> {
                viewModel.loadPlan(planId)
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Se tiver studentId, mostrar lista de treinos
        if (studentId != null) {
            when (val state = plans) {
                is Result.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PurplePrimary)
                    }
                }
                is Result.Success -> {
                    TrainingPlansList(
                        plans = state.data,
                        onPlanClick = onPlanClick,
                        onExerciseClick = onExerciseClick
                    )
                }
                is Result.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Erro ao carregar treinos: ${state.exception.message}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White
                            )
                        )
                    }
                }
            }
        } else if (planId != null) {
            // Se tiver planId, mostrar plano específico (comportamento antigo)
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
                    TrainingPlanDetail(
                        plan = state.data,
                        onExerciseClick = onExerciseClick
                    )
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
}

@Composable
private fun TrainingPlansList(
    plans: List<TrainingPlan>,
    onPlanClick: (String) -> Unit,
    onExerciseClick: (Exercise) -> Unit
) {
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
        if (plans.isEmpty()) {
            item {
                Text(
                    text = "Nenhum treino disponível",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedForegroundDark
                    ),
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            plans.forEach { plan ->
                item {
                    WorkoutCard(
                        title = plan.name,
                        exercises = plan.exercises.size,
                        duration = "${plan.exercises.size * 5} min", // Estimativa
                        description = plan.description ?: "Treino personalizado",
                        iconColor = Color(0xFF3B82F6),
                        isRecommended = plan.status == TrainingPlanStatus.ACTIVE,
                        onClick = {
                            onPlanClick(plan.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrainingPlanDetail(
    plan: TrainingPlan,
    onExerciseClick: (Exercise) -> Unit
) {
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
                        text = plan.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = plan.description ?: "Treino personalizado",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
        }
        
        // Lista de exercícios
        plan.exercises.forEach { exercise ->
            item {
                WorkoutCard(
                    title = exercise.name,
                    exercises = exercise.sets,
                    duration = "${exercise.restTime ?: 60}s de descanso",
                    description = "${exercise.reps} repetições",
                    iconColor = Color(0xFF3B82F6),
                    onClick = {
                        onExerciseClick(exercise)
                    }
                )
            }
        }
    }
}
