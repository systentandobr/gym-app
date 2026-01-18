package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.data.models.Exercise
import com.tadevolta.gym.data.models.Result
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.viewmodels.TrainingPlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plano de Treino") }
            )
        }
    ) { padding ->
        when (val state = plan) {
            is Result.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Result.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = state.data.name,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    state.data.description?.let {
                        item {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    state.data.exercises.forEach { exercise ->
                        item {
                            ExerciseCard(
                                exercise = exercise,
                                onClick = { onExerciseClick(exercise) }
                            )
                        }
                    }
                }
            }
            is Result.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Erro ao carregar plano: ${state.exception.message}")
                }
            }
        }
    }
}
