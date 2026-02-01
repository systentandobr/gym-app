package com.tadevolta.gym.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.tadevolta.gym.ui.theme.*

@Composable
fun ExerciseExecutionHeader(
    workoutTitle: String,
    workoutSubtitle: String = "",
    recordText: String,
    progressPercentage: Int,
    onClose: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Fechar",
                    tint = Color.White
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = workoutTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (workoutSubtitle.isNotBlank()) {
                    Text(
                        text = workoutSubtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
        }
        
        // Pílula de recorde
        GradientPill(
            text = recordText,
            icon = {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        // Barra de progresso
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.width(0.dp))
                Text(
                    text = "$progressPercentage% CONCLUÍDO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MutedForegroundDark
                    )
                )
            }
            LinearProgressIndicator(
                progress = progressPercentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PurplePrimary,
                trackColor = CardDarker
            )
        }
    }
}

@Composable
fun ExerciseMediaCard(
    imageUrl: String?,
    images: List<String>? = null,
    focusMuscle: String
) {
    // Construir lista de URLs de imagens (priorizar images se disponível)
    val imageUrls = images?.mapNotNull { com.tadevolta.gym.utils.ImageUrlBuilder.buildImageUrl(it) }
        ?: listOfNotNull(com.tadevolta.gym.utils.ImageUrlBuilder.buildImageUrl(imageUrl))
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
    ) {
        // Mostrar imagem do exercício se disponível
        if (imageUrls.isNotEmpty()) {
            AsyncImage(
                model = imageUrls.first(),
                contentDescription = "Exercício",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            // Fallback: ícone quando não há imagem
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = "Exercício",
                    tint = MutedForegroundDark,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
        
        // Tag de foco
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PinkAccent)
                )
                Text(
                    text = "FOCO: $focusMuscle",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun TimeCard(
    label: String,
    time: String,
    icon: @Composable () -> Unit,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onAddTime: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MutedForegroundDark,
                    letterSpacing = 1.sp
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (onAddTime != null && label.contains("DESCANSO")) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = onAddTime,
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Text(
                            text = "+30s",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PurplePrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SetRow(
    setNumber: Int,
    previousWeight: String?,
    previousReps: String?,
    currentWeight: String,
    currentReps: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    restTimeRemaining: Int? = null,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onComplete: () -> Unit,
    onLongPress: (() -> Unit)? = null,
    onScrollRequest: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100), label = ""
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .then(
                if (isCurrent) {
                    Modifier.background(
                        color = PurplePrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            )
            .then(
                if (onLongPress != null && !isCurrent && !isCompleted) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                isPressed = true
                                onLongPress()
                                isPressed = false
                            },
                            onPress = {
                                isPressed = true
                            }
                        )
                    }
                } else Modifier
            )
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Série
        Text(
            text = "$setNumber",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.width(40.dp)
        )
        
        // Anterior
        Text(
            text = previousWeight?.let { "$it x ${previousReps ?: ""}" } ?: "— / —",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MutedForegroundDark
            ),
            modifier = Modifier.width(100.dp)
        )
        
        // Peso / Reps
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isCurrent && !isCompleted) {
                OutlinedTextField(
                    value = currentWeight,
                    onValueChange = onWeightChange,
                    placeholder = { Text("Peso", color = MutedForegroundDark) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CardDarker,
                        focusedContainerColor = CardDarker,
                        unfocusedBorderColor = BorderDark,
                        focusedBorderColor = PurplePrimary,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Text(
                    text = "/",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White
                    ),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                OutlinedTextField(
                    value = currentReps,
                    onValueChange = onRepsChange,
                    placeholder = { Text("Reps", color = MutedForegroundDark) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CardDarker,
                        focusedContainerColor = CardDarker,
                        unfocusedBorderColor = BorderDark,
                        focusedBorderColor = PurplePrimary,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            } else if (isCompleted) {
                Text(
                    text = "$currentWeight / $currentReps",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = "— / —",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedForegroundDark
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // OK
        val isButtonEnabled = !isCompleted && restTimeRemaining == null
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .then(
                    if (isCompleted) {
                        Modifier.background(brush = purpleToPinkGradient())
                    } else {
                        Modifier
                            .border(2.dp, BorderDark, CircleShape)
                            .background(Color.Transparent)
                    }
                )
                .clickable(enabled = isButtonEnabled) { 
                    onComplete()
                    // Fazer scroll para cima para mostrar o botão "Descansar"
                    onScrollRequest?.invoke()
                },
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Concluído",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun NextExerciseCard(
    exercise: com.tadevolta.gym.data.models.Exercise,
    isLocked: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícone do exercício
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isLocked) MutedForegroundDark.copy(alpha = 0.2f)
                            else PurplePrimary.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isLocked) Icons.Default.Lock else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isLocked) MutedForegroundDark else PurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isLocked) MutedForegroundDark else Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "${exercise.sets} séries • ${exercise.reps} reps",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
            
            // Ícone de cadeado se bloqueado
            if (isLocked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Bloqueado",
                    tint = MutedForegroundDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ExerciseCardWithSets(
    exercise: com.tadevolta.gym.data.models.Exercise,
    executedSets: List<com.tadevolta.gym.data.models.ExecutedSet>,
    currentSetIndex: Int,
    isExecutionRunning: Boolean = false,
    restTimeRemaining: Int? = null,
    onSetClick: (Int) -> Unit,
    onRestClick: () -> Unit,
    onToggleExecution: (Boolean) -> Unit = {}
) {
    // Verificar se todas as séries planejadas estão completas
    val allSetsCompleted = (0 until exercise.sets).all { index ->
        executedSets.getOrNull(index)?.completed == true
    }
    
    // Verificar se há pelo menos uma série completa (para permitir descanso entre séries)
    val hasCompletedSet = executedSets.any { it.completed == true }
    
    val restButtonText = restTimeRemaining?.let { "${it}s" } ?: "Descansar"
    val restButtonEnabled = hasCompletedSet && restTimeRemaining == null
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tag "EM EXECUÇÃO"
                Box(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .background(
                            color = PurplePrimary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "EM EXECUÇÃO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
                
                // Nome do exercício
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                // Informações do exercício
                Text(
                    text = "${exercise.sets} séries • ${exercise.reps} repetições • ${exercise.restTime ?: 60}s de descanso",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedForegroundDark
                    )
                )
                
                // Botões de série (S1, S2, S3, S4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(exercise.sets) { index ->
                        val setNumber = index + 1
                        val set = executedSets.getOrNull(index)
                        val isCompleted = set?.completed == true
                        val repsCompleted = set?.executedReps?.toString() ?: "--"
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    color = when {
                                        isCompleted -> PurplePrimary.copy(alpha = 0.3f)
                                        index == currentSetIndex -> PurplePrimary.copy(alpha = 0.2f)
                                        else -> CardDarker
                                    }
                                )
                                .border(
                                    width = if (index == currentSetIndex) 2.dp else 0.dp,
                                    color = PurplePrimary,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSetClick(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "S$setNumber",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isCompleted || index == currentSetIndex) Color.White else MutedForegroundDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = repsCompleted,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isCompleted) Color.White else MutedForegroundDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
                
                // Botão Iniciar/Descansar (com lógica combinada)
                val buttonText = when {
                    restTimeRemaining != null -> "${restTimeRemaining}s"
                    !isExecutionRunning -> "Iniciar"
                    hasCompletedSet && isExecutionRunning -> "Descansar"
                    else -> "Executando..."
                }
                
                val buttonEnabled = when {
                    restTimeRemaining != null -> false // Durante descanso, botão desabilitado
                    !isExecutionRunning -> true // Pode iniciar
                    hasCompletedSet && isExecutionRunning -> true // Pode descansar após série completa
                    else -> false // Executando mas nenhuma série completa ainda
                }
                
                val buttonOnClick: () -> Unit = {
                    if (!isExecutionRunning) {
                        // Iniciar execução
                        onToggleExecution(true)
                    } 
                    else if (hasCompletedSet && isExecutionRunning) {
                        // Iniciar descanso (após pelo menos uma série completa)
                        onRestClick()
                    }
                }
                
                GradientButton(
                    text = buttonText,
                    onClick = buttonOnClick,
                    enabled = buttonEnabled,
                    icon = {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun EditSetDialog(
    setNumber: Int,
    currentWeight: String,
    currentReps: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var weight by remember { mutableStateOf(currentWeight) }
    var reps by remember { mutableStateOf(currentReps) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Editar Série $setNumber",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Peso (kg)", color = MutedForegroundDark) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CardDarker,
                        focusedContainerColor = CardDarker,
                        unfocusedBorderColor = BorderDark,
                        focusedBorderColor = PurplePrimary,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedLabelColor = MutedForegroundDark,
                        focusedLabelColor = PurplePrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Repetições", color = MutedForegroundDark) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CardDarker,
                        focusedContainerColor = CardDarker,
                        unfocusedBorderColor = BorderDark,
                        focusedBorderColor = PurplePrimary,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedLabelColor = MutedForegroundDark,
                        focusedLabelColor = PurplePrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancelar")
                    }
                    GradientButton(
                        text = "Salvar",
                        onClick = {
                            onSave(weight, reps)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
