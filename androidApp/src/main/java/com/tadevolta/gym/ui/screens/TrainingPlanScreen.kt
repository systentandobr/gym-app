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

// Função helper para obter o dia atual da semana (0-6, onde 0=Domingo)
fun getCurrentDayOfWeek(): Int {
    // Calendar.DAY_OF_WEEK retorna 1-7 (1=Domingo, 2=Segunda, ..., 7=Sábado)
    // Converter para 0-6 (0=Domingo, 1=Segunda, ..., 6=Sábado)
    val calendar = java.util.Calendar.getInstance()
    val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    return (dayOfWeek - 1) % 7
}

// Função helper para obter o nome do dia da semana
fun getDayName(dayOfWeek: Int): String {
    val dayNames = listOf("Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")
    return dayNames.getOrElse(dayOfWeek) { "Dia $dayOfWeek" }
}

// Função helper para contar total de exercícios de todos os dias
fun getTotalExercisesCount(plan: TrainingPlan): Int {
    return plan.weeklySchedule.sumOf { it.exercises.size }
}

// Função helper para calcular duração estimada do treino
fun getEstimatedDuration(plan: TrainingPlan): String {
    val totalExercises = getTotalExercisesCount(plan)
    val estimatedMinutes = totalExercises * 5 // Estimativa de 5 minutos por exercício
    return "$estimatedMinutes min"
}

@Composable
fun TrainingPlanScreen(
    planId: String? = null,
    studentId: String? = null,
    dayOfWeek: Int? = null,
    viewModel: TrainingPlanViewModel = hiltViewModel(),
    onExerciseClick: (Exercise) -> Unit = {},
    onPlanClick: (String) -> Unit = {},
    onDayClick: (String, Int) -> Unit = { planId, dayOfWeek -> onPlanClick(planId) }
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
                        onExerciseClick = onExerciseClick,
                        onDayClick = onDayClick
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
                        dayOfWeek = dayOfWeek,
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
    onExerciseClick: (Exercise) -> Unit,
    onDayClick: (String, Int) -> Unit
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
            val currentDay = getCurrentDayOfWeek()
            
            plans.forEach { plan ->
                // Renderizar um card para cada dia da semana que tenha exercícios
                plan.weeklySchedule.forEach { daySchedule ->
                    if (daySchedule.exercises.isNotEmpty()) {
                        item {
                            val dayName = getDayName(daySchedule.dayOfWeek)
                            val isToday = daySchedule.dayOfWeek == currentDay
                            
                            WorkoutCard(
                                title = "$dayName - ${plan.name}",
                                subsession = "exercícios",
                                exercises = daySchedule.exercises.size,
                                duration = "${daySchedule.exercises.size * 5} min", // Estimativa
                                description = plan.description ?: "Treino personalizado",
                                iconColor = Color(0xFF3B82F6),
                                isRecommended = isToday && plan.status == TrainingPlanStatus.ACTIVE,
                                dayOfWeek = daySchedule.dayOfWeek,
                                dayName = dayName,
                                onClick = {
                                    // Navegar para o plano com o dayOfWeek específico
                                    onDayClick(plan.id, daySchedule.dayOfWeek)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainingPlanDetail(
    plan: TrainingPlan,
    dayOfWeek: Int? = null,
    onExerciseClick: (Exercise) -> Unit
) {
    // Se dayOfWeek foi fornecido, mostrar apenas exercícios daquele dia
    val exercisesToShow = if (dayOfWeek != null) {
        plan.weeklySchedule.find { it.dayOfWeek == dayOfWeek }?.exercises ?: emptyList()
    } else {
        // Fallback: usar exercises se weeklySchedule estiver vazio (compatibilidade)
        plan.exercises.ifEmpty {
            plan.weeklySchedule.flatMap { it.exercises }
        }
    }
    
    val dayName = dayOfWeek?.let { getDayName(it) }
    val displayTitle = if (dayName != null) {
        "$dayName - ${plan.name}"
    } else {
        plan.name
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Lista de exercícios
        if (exercisesToShow.isEmpty()) {
            item {
                Text(
                    text = "Nenhum exercício disponível para este dia",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedForegroundDark
                    ),
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            exercisesToShow.forEach { exercise ->
                item {
                    WorkoutCard(
                        title = exercise.name,
                        subsession = "séries",
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
}
