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
import com.tadevolta.gym.data.models.Exercise
import com.tadevolta.gym.data.models.ExecutedSet
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.viewmodels.ExerciseExecutionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseExecutionScreen(
    exercise: Exercise,
    viewModel: ExerciseExecutionViewModel = hiltViewModel(),
    onFinish: () -> Unit = {}
) {
    val executedSets by viewModel.executedSets.collectAsState()
    val isFinishing by viewModel.isFinishing.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise.name) }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.finishExercise()
                    onFinish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = !isFinishing
            ) {
                if (isFinishing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Finalizar Exercício")
                }
            }
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
                // Imagem/GIF do exercício
                ExerciseMedia(
                    imageUrl = exercise.imageUrl,
                    videoUrl = exercise.videoUrl
                )
            }
            
            item {
                // Informações do exercício
                ExerciseInfo(
                    name = exercise.name,
                    plannedSets = exercise.sets,
                    plannedReps = exercise.reps,
                    restTime = exercise.restTime
                )
            }
            
            item {
                Text(
                    text = "Séries Executadas",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            // Lista de séries executadas
            items(exercise.sets) { index ->
                ExecutedSetCard(
                    setNumber = index + 1,
                    plannedReps = exercise.reps,
                    plannedWeight = exercise.weight,
                    executedSet = executedSets.getOrNull(index),
                    onSetComplete = { set ->
                        viewModel.completeSet(set)
                    },
                    onWeightChange = { set, weight ->
                        viewModel.updateWeight(set, weight)
                    },
                    onRepsChange = { set, reps ->
                        viewModel.updateReps(set, reps)
                    }
                )
            }
        }
    }
}
