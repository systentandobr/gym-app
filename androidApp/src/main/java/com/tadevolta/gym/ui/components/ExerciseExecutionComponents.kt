package com.tadevolta.gym.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tadevolta.gym.ui.theme.*

@Composable
fun ExerciseExecutionHeader(
    workoutTitle: String,
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
            Text(
                text = workoutTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
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
    focusMuscle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
    ) {
        // Aqui seria a imagem/vídeo do exercício
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
    modifier: Modifier = Modifier
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
            Text(
                text = time,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
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
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) {
                    Modifier.background(
                        color = PurplePrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
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
                .clickable(enabled = !isCompleted) { onComplete() },
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
