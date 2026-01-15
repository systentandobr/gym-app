package com.tadevolta.gym.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tadevolta.gym.data.models.*

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
                Text("Streak: ${it.currentStreak} dias")
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
    videoUrl: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            imageUrl != null -> {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Exercício",
                    modifier = Modifier.fillMaxSize()
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
