package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
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
import com.tadevolta.gym.ui.components.ProgressGamificationCard
import com.tadevolta.gym.ui.components.WorkoutCard
import com.tadevolta.gym.ui.theme.BackgroundDark
import com.tadevolta.gym.ui.theme.CardDark
import com.tadevolta.gym.ui.theme.MutedForegroundDark
import com.tadevolta.gym.ui.theme.PurplePrimary
import com.tadevolta.gym.ui.viewmodels.TrainingPlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(
    planId: String,
    dayOfWeek: Int? = null,
    viewModel: TrainingPlanViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onExerciseClick: (Exercise) -> Unit = {}
) {
    val planResult by viewModel.trainingPlan.collectAsState()
    
    LaunchedEffect(planId) {
        viewModel.loadPlan(planId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Meus Treinos",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Escolha seu treino de hoje",
                                style = MaterialTheme.typography.bodyMedium.copy(
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
                                Icons.Default.CalendarToday,
                                contentDescription = "Calendário",
                                tint = PurplePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
    
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = planResult) {
                is Result.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PurplePrimary
                    )
                }
                is Result.Success -> {
                    TrainingExercisesList(
                        plan = state.data,
                        dayOfWeek = dayOfWeek,
                        onExerciseClick = onExerciseClick
                    )
                }
                is Result.Error -> {
                    Text(
                        text = "Erro: ${state.exception.message}",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrainingExercisesList(
    plan: TrainingPlan,
    dayOfWeek: Int? = null,
    onExerciseClick: (Exercise) -> Unit
) {
    val listState = rememberLazyListState()

    val exercisesToShow = if (dayOfWeek != null) {
        plan.weeklySchedule.find { it.dayOfWeek == dayOfWeek }?.exercises ?: emptyList()
    } else {
        plan.exercises.ifEmpty {
            plan.weeklySchedule.flatMap { it.exercises }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Gamification Card
        item {
            ProgressGamificationCard(
                currentStreak = 0,
                checkInsThisMonth = 0,
                totalCheckIns = 365
            )
        }

        if (exercisesToShow.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Nenhum exercício disponível",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MutedForegroundDark)
                    )
                }
            }
        } else {
            exercisesToShow.forEach { exercise ->
                item {
                    WorkoutCard(
                        title = exercise.name,
                        sets = exercise.sets,
                        reps = exercise.reps,
                        restTime = "${exercise.restTime ?: 60}s Descanso",
                        imageUrl = exercise.primaryImageUrl,
                        notes = exercise.notes,
                        onClick = { onExerciseClick(exercise) }
                    )
                }
            }
        }
    }
}
