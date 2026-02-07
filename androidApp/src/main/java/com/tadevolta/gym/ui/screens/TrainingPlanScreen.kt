package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.TrainingPlanViewModel

// Helpers moved to TrainingUIHelpers.kt

@Composable
fun TrainingPlanScreen(
    studentId: String,
    viewModel: TrainingPlanViewModel = hiltViewModel(),
    onDayClick: (String, Int) -> Unit
) {
    val plansState by viewModel.trainingPlans.collectAsState()
    
    LaunchedEffect(studentId) {
        viewModel.loadPlansByStudentId(studentId)
    }
   
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        when (val state = plansState) {
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
    }
}

@Composable
private fun TrainingPlansList(
    plans: List<TrainingPlan>,
    onDayClick: (String, Int) -> Unit
) {
    val listState = rememberLazyListState()
    val currentDay = getCurrentDayOfWeek()
    
    // Auto-scroll para o treino de hoje
    LaunchedEffect(plans) {
        if (plans.isNotEmpty()) {
            var todayIndex = -1
            var currentIndex = 2 // Começa em 2 para pular o Header (0) e Search Bar (1)
            
            plans.forEach { plan ->
                plan.weeklySchedule.forEach { daySchedule ->
                    if (daySchedule.exercises.isNotEmpty()) {
                        if (daySchedule.dayOfWeek == currentDay && plan.status == TrainingPlanStatus.ACTIVE) {
                            todayIndex = currentIndex
                        }
                        currentIndex++
                    }
                }
            }
            
            if (todayIndex != -1) {
                listState.animateScrollToItem(todayIndex)
            }
        }
    }
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Planos de ",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Treino",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = PurplePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = "Sugestão Diária",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
                
                // User Avatar placeholder as seen in image
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(CardDark, RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = PurplePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Search Bar Placeholder
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                placeholder = { 
                    Text("Buscar plano...", color = MutedForegroundDark) 
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MutedForegroundDark)
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = CardDark.copy(alpha = 0.5f),
                    focusedContainerColor = CardDark.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = PurplePrimary
                ),
                singleLine = true
            )
        }
        
        // Lista de treinos
        if (plans.isEmpty()) {
            item {
                Text(
                    text = "Nenhum plano de treino disponível",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedForegroundDark
                    ),
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            plans.forEach { plan ->
                // Renderizar um card para cada dia da semana que tenha exercícios
                plan.weeklySchedule.forEach { daySchedule ->
                    if (daySchedule.exercises.isNotEmpty()) {
                        item {
                            val dayName = getDayName(daySchedule.dayOfWeek)
                            val isToday = daySchedule.dayOfWeek == currentDay
                            var planName = plan.name

                            val muscleGroups = daySchedule.exercises
                                .flatMap { it.muscleGroups ?: emptyList() }
                                .distinct()
                                .joinToString(" e ")
                                .let { if (it.isEmpty()) "Geral" else it }

                            if (daySchedule.timeSlots.isNotEmpty()) {
                                planName = daySchedule.timeSlots
                                    .map { it.activity }
                                    .distinct()
                                    .joinToString(" e ")
                                    .let { if (it.isEmpty()) plan.name else it }
                            }

                            val frequency = "${plan.weeklySchedule.count { it.exercises.isNotEmpty() }}x"
                            
                            val difficulty = daySchedule.exercises
                                .mapNotNull { it.difficulty }
                                .maxByOrNull { it.ordinal } ?: ExerciseDifficulty.INTERMEDIATE

                            TrainPlanWorkoutCard(
                                planName = planName,
                                muscleGroups = muscleGroups,
                                exerciseCount = daySchedule.exercises.size,
                                estimatedTime = "${daySchedule.exercises.size * 7}m", // Slightly more realistic estimate
                                frequency = frequency,
                                difficulty = difficulty,
                                isRecommended = isToday && plan.status == TrainingPlanStatus.ACTIVE,
                                dayName = dayName,
                                onClick = {
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

// Helper moved to TrainingUIHelpers.kt
