package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToTrainingPlan: (String) -> Unit = {},
    onNavigateToCheckIn: () -> Unit = {},
    onNavigateToRanking: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTrainingPlans: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            item {
                DashboardHeader(
                    userName = uiState.user?.name ?: "Aluno",
                    hasNotification = true,
                    onAvatarClick = onNavigateToProfile
                )
            }
            
            // Notificação de treino
            item {
                WorkoutNotificationCard(
                    onStart = {
                        uiState.currentTrainingPlan?.let {
                            onNavigateToTrainingPlan(it.id)
                        }
                    },
                    onCardClick = {
                        uiState.studentId?.let { studentId ->
                            onNavigateToTrainingPlans(studentId)
                        } ?: run {
                            // Fallback: se não tiver studentId, usar o planId se existir
                            uiState.currentTrainingPlan?.let {
                                onNavigateToTrainingPlan(it.id)
                            }
                        }
                    }
                )
            }
            
            // Progresso Anual
            item {
                AnnualProgressCard(
                    currentDays = uiState.checkInStats?.checkInsLast365Days ?: 0,
                    percentage = 3
                )
            }
            
            // Botão Check-in
            item {
                CheckInButton(onClick = onNavigateToCheckIn)
            }
            
            // Cards de resumo
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        title = "Treinos",
                        value = "${uiState.weeklyActivity?.summary?.totalWorkouts ?: 0}",
                        icon = {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        iconColor = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Check-ins",
                        value = "${uiState.checkInStats?.checkInsLast365Days ?: 10}",
                        icon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        iconColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Atividade Semanal
            item {
                val weeklyActivity = uiState.weeklyActivity
                if (weeklyActivity != null) {
                    // Mapear DailyActivity para formato do componente
                    val daysMap = mapOf(
                        "DOM" to "D",
                        "SEG" to "S",
                        "TER" to "T",
                        "QUA" to "Q",
                        "QUI" to "Q",
                        "SEX" to "S",
                        "SAB" to "S"
                    )
                    val daysList = weeklyActivity.dailyActivity.map { activity ->
                        val dayLabel = daysMap[activity.dayOfWeek] ?: activity.dayOfWeek.take(1)
                        dayLabel to (activity.checkIns > 0 || activity.workoutsCompleted > 0 || activity.exercisesCompleted > 0)
                    }
                    WeeklyActivityChart(days = daysList)
                } else if (!uiState.isLoading) {
                    // Mostrar gráfico vazio se não houver dados
                    WeeklyActivityChart(
                        days = listOf("D" to false, "S" to false, "T" to false, "Q" to false, "Q" to false, "S" to false, "S" to false)
                    )
                }
            }
            
            // Ranking Semanal
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ranking Semanal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        TextButton(onClick = onNavigateToRanking) {
                            Text(
                                text = "Ver todos",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PurplePrimary
                                )
                            )
                        }
                    }
                    
                    // Exibir ranking real do ViewModel
                    if (uiState.rankingPreview.isNotEmpty()) {
                        uiState.rankingPreview.take(3).forEachIndexed { index, ranking ->
                            val isCurrentUser = ranking.userId == uiState.user?.id
                            RankingCard(
                                position = ranking.position,
                                name = if (isCurrentUser) "${ranking.userName ?: "Você"} (Você)" else (ranking.userName ?: "Usuário"),
                                workouts = ranking.totalPoints / 50, // Aproximação: assumir 50 pontos por treino
                                isCurrentUser = isCurrentUser
                            )
                        }
                    } else if (!uiState.isLoading) {
                        // Placeholder quando não há dados
                        Text(
                            text = "Nenhum ranking disponível",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MutedForegroundDark
                            )
                        )
                    }
                }
            }
            
            // Loading indicator
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PurplePrimary)
                    }
                }
            }
            
            // Error message
            uiState.error?.let { error ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Destructive
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
