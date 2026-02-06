package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    viewModel: ExerciseExecutionViewModel = hiltViewModel(),
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val exerciseDetails by viewModel.exerciseDetails.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val executedSets = uiState.executedSets
    val exercise = exerciseDetails ?: uiState.exercise // Usar detalhes completos se disponível, senão usar exercício inicial
    val workoutTitle = uiState.workoutTitle
    val workoutSubtitle = uiState.workoutSubtitle
    val currentExerciseIndex = uiState.currentExerciseIndex
    val totalExercises = uiState.totalExercises
    val nextExercises = uiState.nextExercises
    val isExecutionRunning = uiState.isExecutionRunning
    val totalExecutionTimeSeconds = uiState.totalExecutionTimeSeconds
    val restTimeRemaining = uiState.restTimeRemaining
    
    var currentSetIndex by remember { mutableStateOf(0) }
    var showEditDialog by remember { mutableStateOf<Int?>(null) }
    val listState = rememberLazyListState()
    var shouldScroll by remember { mutableStateOf(false) }
    var shouldScrollToTop by remember { mutableStateOf(false) }
    
    // Carregar detalhes do exercício de forma lazy quando a tela abrir
    LaunchedEffect(exercise?.exerciseId) {
        exercise?.exerciseId?.let { exerciseId ->
            viewModel.loadExerciseDetails(exerciseId)
        }
    }
    
    // Formatar tempo total
    val totalTime = viewModel.formatTime(totalExecutionTimeSeconds)
    
    // Formatar tempo de descanso
    val restTimeText = restTimeRemaining?.let { "${it}s" } ?: "${exercise?.restTime ?: 60}s"
    
    // Scroll automático quando solicitado
    LaunchedEffect(shouldScroll) {
        if (shouldScroll) {
            // Rolar para o item das séries (aproximadamente item 4-5)
            listState.animateScrollToItem(4)
            shouldScroll = false
        }
    }
    
    // Scroll para cima quando solicitado (mostrar botão Descansar)
    LaunchedEffect(shouldScrollToTop) {
        if (shouldScrollToTop) {
            // O botão "Descansar" está no item 1 (ExerciseCardWithSets)
            // Header (0), Mídia (1), Card com botão (2)
            listState.animateScrollToItem(1)
            shouldScrollToTop = false
        }
    }
    
    // Função para scroll para cima (mostrar botão Descansar)
    val scrollToTop = {
        shouldScrollToTop = true
    }
    
    // Verificar se todas as séries estão completas
    val allSetsCompleted = exercise?.let { ex ->
        (0 until ex.sets).all { index ->
            executedSets.getOrNull(index)?.completed == true
        }
    } ?: false
    
    // Avançar automaticamente para o próximo exercício quando todas as séries estiverem completas
    // e não houver tempo de descanso remanescente (descanso finalizado)
    var hasAdvanced by remember { mutableStateOf(false) }
    LaunchedEffect(allSetsCompleted, restTimeRemaining) {
        if (allSetsCompleted && restTimeRemaining == null && !hasAdvanced) {
            // Verificar se acabou de finalizar o descanso (não estava em descanso antes)
            // Pequeno delay para garantir que o estado foi atualizado
            kotlinx.coroutines.delay(500)
            // Finalizar exercício
            viewModel.finishExercise()
            // Aguardar um pouco antes de navegar
            kotlinx.coroutines.delay(300)
            hasAdvanced = true
            onNext()
        } else if (restTimeRemaining != null) {
            // Resetar flag quando iniciar novo descanso
            hasAdvanced = false
        }
    }
    
    // Calcular progresso do exercício atual (séries completadas)
    val exerciseProgressPercentage = if (exercise?.sets ?: 0 > 0) {
        (executedSets.count { it.completed } * 100) / (exercise?.sets ?: 1)
    } else 0
    
    // Calcular progresso do treino (exercícios completados)
    val workoutProgress = if (totalExercises > 0 && currentExerciseIndex >= 0) {
        ((currentExerciseIndex + 1) * 100) / totalExercises
    } else 0
    
    // Mostrar loading ou erro se necessário
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PurplePrimary)
        }
        return
    }
    
    if (uiState.error != null || exercise == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = uiState.error ?: "Exercício não encontrado",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Destructive)
                )
                TextButton(onClick = onClose) {
                    Text("Voltar")
                }
            }
        }
        return
    }
    
    Scaffold() { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.refreshExerciseData() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    ExerciseExecutionHeader(
                        workoutTitle = workoutTitle,
                        workoutSubtitle = workoutSubtitle,
                        recordText = if (totalExercises > 0 && currentExerciseIndex >= 0) {
                            "${currentExerciseIndex + 1} DE $totalExercises EXERCÍCIOS"
                        } else {
                            "RECORDE: 12 DIAS"
                        },
                        progressPercentage = workoutProgress,
                        onClose = onClose
                    )
                }
                
                // Mídia do exercício
                item {
                    ExerciseMediaCard(
                        videoUrl = exercise.videoUrl,
                        imageUrl = exercise.imageUrl,
                        images = exercise.images,
                        focusMuscle = exercise.name
                    )
                }
                
                // Card do exercício atual com botões de série
                item {
                    ExerciseCardWithSets(
                        exercise = exercise,
                        executedSets = executedSets,
                        currentSetIndex = currentSetIndex,
                        isExecutionRunning = isExecutionRunning,
                        restTimeRemaining = restTimeRemaining,
                        onSetClick = { setIndex ->
                            currentSetIndex = setIndex
                        },
                        onRestClick = {
                            viewModel.startRestTimer()
                            shouldScroll = true
                        },
                        onToggleExecution = { isRunning ->
                            if (isRunning) {
                                viewModel.startExecutionTimer()
                                shouldScroll = true
                            } else {
                                viewModel.pauseExecutionTimer()
                            }
                        }
                    )
                }
                
                // Descrição do exercício (se disponível)
                if (exercise.notes != null) {
                    item {
                        Text(
                            text = exercise.notes ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MutedForegroundDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
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
                            label = "PRÓXIMO DESCANSO",
                            time = restTimeText,
                            icon = {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            iconColor = PurplePrimary,
                            modifier = Modifier.weight(1f),
                            onAddTime = {
                                // TODO: Adicionar +30s ao descanso
                            }
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
                
                // Séries (planejadas + extras)
                items(executedSets.size) { index ->
                    val set = executedSets.getOrNull(index)
                    var weight by remember { mutableStateOf(set?.executedWeight?.toString() ?: "") }
                    var reps by remember { mutableStateOf(set?.executedReps?.toString() ?: "") }
                    
                    val isExtraSet = index >= (exercise?.sets ?: 0)
                    val setNumberDisplay = if (isExtraSet) {
                        "S${index + 1}+"
                    } else {
                        "${index + 1}"
                    }
                    
                    SetRow(
                        setNumber = index + 1,
                        previousWeight = "80kg",
                        previousReps = if (index == 0) "12" else "10",
                        currentWeight = weight,
                        currentReps = reps,
                        isCompleted = set?.completed == true,
                        isCurrent = index == currentSetIndex && set?.completed != true,
                        restTimeRemaining = restTimeRemaining,
                        onWeightChange = { weight = it },
                        onRepsChange = { reps = it },
                        onComplete = {
                            viewModel.completeSet(
                                com.tadevolta.gym.data.models.ExecutedSet(
                                    setNumber = index + 1,
                                    plannedReps = exercise?.reps ?: "",
                                    executedReps = reps.toIntOrNull(),
                                    plannedWeight = exercise?.weight,
                                    executedWeight = weight.toDoubleOrNull(),
                                    completed = true
                                )
                            )
                            
                            if (index < executedSets.size - 1) {
                                currentSetIndex = index + 1
                            }
                        },
                        onLongPress = {
                            if (set?.completed != true) {
                                showEditDialog = index
                            }
                        },
                        onScrollRequest = scrollToTop
                    )
                }
                
                // Botão adicionar série
                item {
                    OutlinedButton(
                        onClick = { viewModel.addExtraSet() },
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
                
                // Seção de próximos exercícios
                if (nextExercises.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "PRÓXIMOS NA LISTA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MutedForegroundDark,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            nextExercises.forEach { nextExercise ->
                                NextExerciseCard(
                                    exercise = nextExercise,
                                    isLocked = true
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Diálogo de edição de série
        showEditDialog?.let { index ->
            val set = executedSets.getOrNull(index)
            EditSetDialog(
                setNumber = index + 1,
                currentWeight = set?.executedWeight?.toString() ?: "",
                currentReps = set?.executedReps?.toString() ?: "",
                onDismiss = { showEditDialog = null },
                onSave = { newWeight, newReps ->
                    viewModel.updateWeight(
                        set ?: com.tadevolta.gym.data.models.ExecutedSet(
                            setNumber = index + 1,
                            plannedReps = exercise?.reps ?: "",
                            executedReps = null,
                            plannedWeight = exercise?.weight,
                            executedWeight = null,
                            completed = false
                        ),
                        newWeight.toDoubleOrNull() ?: 0.0
                    )
                    viewModel.updateReps(
                        set ?: com.tadevolta.gym.data.models.ExecutedSet(
                            setNumber = index + 1,
                            plannedReps = exercise?.reps ?: "",
                            executedReps = null,
                            plannedWeight = exercise?.weight,
                            executedWeight = null,
                            completed = false
                        ),
                        newReps.toIntOrNull() ?: 0
                    )
                }
            )
        }
    }
}
