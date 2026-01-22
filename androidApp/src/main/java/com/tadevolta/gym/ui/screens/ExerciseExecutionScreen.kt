package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.data.models.Exercise
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.ExerciseExecutionViewModel

@Composable
fun ExerciseExecutionScreen(
    workoutTitle: String = "TREINO A - INFERIORES",
    exercise: Exercise,
    viewModel: ExerciseExecutionViewModel = hiltViewModel(),
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val executedSets by viewModel.executedSets.collectAsState()
    var currentSetIndex by remember { mutableStateOf(0) }
    var restTime by remember { mutableStateOf("01:24") }
    var totalTime by remember { mutableStateOf("24:08") }
    
    val progressPercentage = if (exercise.sets > 0) {
        (executedSets.size * 100) / exercise.sets
    } else 0
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                ExerciseExecutionHeader(
                    workoutTitle = workoutTitle,
                    recordText = "RECORDE: 12 DIAS",
                    progressPercentage = progressPercentage,
                    onClose = onClose
                )
            }
            
            // Mídia do exercício
            item {
                ExerciseMediaCard(
                    imageUrl = exercise.imageUrl,
                    focusMuscle = "QUADRÍCEPS"
                )
            }
            
            // Nome e descrição
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Mantenha as costas retas, desça o quadril até que as coxas estejam paralelas ao chão e suba controladamente.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
            
            // Cards de tempo
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimeCard(
                        label = "DESCANSO",
                        time = restTime,
                        icon = {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        iconColor = PurplePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    TimeCard(
                        label = "TEMPO TOTAL",
                        time = totalTime,
                        icon = {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = PinkAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        iconColor = PinkAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Cabeçalho da tabela
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SÉRIE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MutedForegroundDark,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.width(40.dp)
                    )
                    Text(
                        text = "ANTERIOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MutedForegroundDark,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.width(100.dp)
                    )
                    Text(
                        text = "PESO / REPS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MutedForegroundDark,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MutedForegroundDark,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.width(40.dp)
                    )
                }
            }
            
            // Séries
            items(exercise.sets) { index ->
                val set = executedSets.getOrNull(index)
                var weight by remember { mutableStateOf(set?.executedWeight?.toString() ?: "") }
                var reps by remember { mutableStateOf(set?.executedReps?.toString() ?: "") }
                
                SetRow(
                    setNumber = index + 1,
                    previousWeight = "80kg",
                    previousReps = if (index == 0) "12" else "10",
                    currentWeight = weight,
                    currentReps = reps,
                    isCompleted = set != null,
                    isCurrent = index == currentSetIndex && set == null,
                    onWeightChange = { weight = it },
                    onRepsChange = { reps = it },
                    onComplete = {
                        viewModel.completeSet(
                            com.tadevolta.gym.data.models.ExecutedSet(
                                setNumber = index + 1,
                                plannedReps = exercise.reps,
                                executedReps = reps.toIntOrNull(),
                                plannedWeight = exercise.weight,
                                executedWeight = weight.toDoubleOrNull(),
                                completed = true
                            )
                        )
                        if (index < exercise.sets - 1) {
                            currentSetIndex = index + 1
                        }
                    }
                )
            }
            
            // Botão adicionar série
            item {
                OutlinedButton(
                    onClick = { /* TODO: Adicionar série */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Adicionar Série",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
        
        // Botões de navegação
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardDark,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Anterior",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            GradientButton(
                text = "Próximo Exercício",
                onClick = onNext,
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}
