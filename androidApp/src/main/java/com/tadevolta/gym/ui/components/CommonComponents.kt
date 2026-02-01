package com.tadevolta.gym.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.ui.theme.*
import android.net.Uri
import android.widget.VideoView
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect

@Composable
fun WelcomeCard(user: User?) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Olá, ${user?.name ?: "Aluno"}!",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Vamos treinar hoje?",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun CheckInCard(
    stats: CheckInStats?,
    onCheckIn: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Check-in",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            stats?.let {
                Text("${it.checkInsLast365Days}/365 dias")
                Text("Recorde: ${it.currentStreak} dias")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onCheckIn) {
                Text("Fazer Check-in")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanCard(
    plan: TrainingPlan?,
    onViewPlan: (TrainingPlan) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { plan?.let(onViewPlan) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = plan?.name ?: "Nenhum plano ativo",
                style = MaterialTheme.typography.titleLarge
            )
            plan?.description?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamificationCard(
    data: GamificationData?,
    onViewRanking: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onViewRanking
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Nível ${data?.level ?: 1}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("${data?.totalPoints ?: 0} pontos")
            LinearProgressIndicator(
                progress = (data?.xp?.toFloat() ?: 0f) / (data?.xpToNextLevel?.toFloat() ?: 1f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${exercise.sets} séries x ${exercise.reps}",
                style = MaterialTheme.typography.bodySmall
            )
            exercise.weight?.let {
                Text(
                    text = "${it}kg",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ExerciseMedia(
    imageUrl: String?,
    images: List<String>? = null,
    videoUrl: String?
) {
    // Construir lista de URLs de imagens (priorizar images se disponível)
    val imageUrls = images?.mapNotNull { com.tadevolta.gym.utils.ImageUrlBuilder.buildImageUrl(it) }
        ?: listOfNotNull(com.tadevolta.gym.utils.ImageUrlBuilder.buildImageUrl(imageUrl))
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            imageUrls.isNotEmpty() -> {
                // Mostrar primeira imagem (futuramente pode adicionar carrossel para múltiplas imagens)
                AsyncImage(
                    model = imageUrls.first(),
                    contentDescription = "Exercício",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            videoUrl != null -> {
                // Implementar player de vídeo
                Text("Vídeo: $videoUrl")
            }
            else -> {
                Text("Sem mídia disponível")
            }
        }
    }
}

@Composable
fun ExerciseInfo(
    name: String,
    plannedSets: Int,
    plannedReps: String,
    restTime: Int?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("${plannedSets} séries x ${plannedReps}")
            restTime?.let {
                Text("Descanso: ${it}s")
            }
        }
    }
}

@Composable
fun ErrorScreen(
    exception: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Erro: ${exception.message}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Tentar Novamente")
        }
    }
}

// Componentes reutilizáveis para o novo design

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = purpleToPinkGradient(),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                icon?.let {
                    Spacer(modifier = Modifier.width(8.dp))
                    it()
                }
            }
        }
    }
}

@Composable
fun GradientText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge
) {
    Text(
        text = text,
        style = style.copy(
            brush = Brush.linearGradient(
                colors = listOf(PurplePrimary, PinkAccent)
            )
        ),
        modifier = modifier
    )
}

@Composable
fun ProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .width(if (index < currentStep) 32.dp else 24.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index < currentStep) PurplePrimary
                        else MutedDark
                    )
            )
        }
    }
}

@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Buscar...",
    modifier: Modifier = Modifier,
    onLocationClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    isLoading: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = MutedForegroundDark) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = InputDark,
            focusedContainerColor = InputDark,
            unfocusedBorderColor = BorderDark,
            focusedBorderColor = PurplePrimary,
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White
        ),
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Buscar",
                tint = MutedForegroundDark
            )
        },
        trailingIcon = {
            Row {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = PurplePrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    onSearchClick?.let {
                        IconButton(onClick = it) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Buscar endereço",
                                tint = PurplePrimary
                            )
                        }
                    }
                    onLocationClick?.let {
                        IconButton(onClick = it) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Localização",
                                tint = PurplePrimary
                            )
                        }
                    }
                }
            }
        },
        enabled = !isLoading
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (selected) {
                    Modifier.border(
                        width = 2.dp,
                        color = BorderPurple,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = CardDark
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            content()
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(24.dp)
                        .background(
                            brush = purpleToPinkGradient(),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selecionado",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoBox(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = CardDark,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = PurplePrimary.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "i",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = PurplePrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MutedForegroundDark
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SocialLoginButton(
    provider: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CardDark,
            contentColor = Color.White
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = provider,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

@Composable
fun GradientPill(
    text: String,
    icon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = purpleToPinkGradient(),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                it()
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun VideoPlayer(
    videoUri: Uri,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    looping: Boolean = true
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(videoUri)
            }
        },
        modifier = modifier,
        update = { videoView ->
            videoView.setVideoURI(videoUri)
            if (autoPlay) {
                videoView.start()
            }
            if (looping) {
                videoView.setOnCompletionListener {
                    videoView.start()
                }
            }
        }
    )
    
    DisposableEffect(videoUri) {
        onDispose {
            // Cleanup se necessário
        }
    }
}

enum class WarningType {
    WARNING, INFO, ERROR
}

@Composable
fun WarningCard(
    title: String,
    message: String,
    type: WarningType = WarningType.WARNING,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (type) {
        WarningType.WARNING -> Color(0xFFFF9800).copy(alpha = 0.1f) // Laranja claro
        WarningType.INFO -> Color(0xFF2196F3).copy(alpha = 0.1f) // Azul claro
        WarningType.ERROR -> Color(0xFFF44336).copy(alpha = 0.1f) // Vermelho claro
    }
    
    val iconColor = when (type) {
        WarningType.WARNING -> Color(0xFFFF9800) // Laranja
        WarningType.INFO -> Color(0xFF2196F3) // Azul
        WarningType.ERROR -> Color(0xFFF44336) // Vermelho
    }
    
    val icon = when (type) {
        WarningType.WARNING -> Icons.Default.Warning
        WarningType.INFO -> Icons.Default.Info
        WarningType.ERROR -> Icons.Default.Error
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MutedForegroundDark
                    )
                )
            }
        }
    }
}
