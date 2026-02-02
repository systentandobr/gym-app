package com.tadevolta.gym.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import com.tadevolta.gym.data.models.UnitOccupancyResponse
import com.tadevolta.gym.data.models.UnitOccupancyStatus
import com.tadevolta.gym.ui.theme.*

@Composable
fun DashboardHeader(
    userName: String,
    subtitle: String = "Painel do Atleta",
    avatarUrl: String? = null,
    hasNotification: Boolean = false,
    onAvatarClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Olá, ",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${userName ?: "Atleta"}!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MutedForegroundDark
                )
            )
        }
        
        Box {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable(onClick = onAvatarClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Indicador de notificação/status
            if (hasNotification) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
            }
        }
    }
}

@Composable
fun WorkoutNotificationCard(
    onDismiss: () -> Unit = {},
    onStart: () -> Unit = {},
    onCardClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(
            containerColor = PurplePrimary.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(brush = purpleToPinkGradient()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Seu treino de hoje está pronto!",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Inicie agora e mantenha o foco.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onStart) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Iniciar",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AnnualProgressCard(
    currentDays: Int,
    totalDays: Int = 365,
    percentage: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "PROGRESSO ANUAL",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = PinkAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$currentDays de $totalDays dias",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "$percentage% Concluído",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MutedForegroundDark
                    )
                )
            }
            
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PurplePrimary,
                trackColor = CardDarker
            )
        }
    }
}

@Composable
fun CheckInButton(
    onClick: () -> Unit
) {
    GradientButton(
        text = "Fazer Check-in",
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        icon = {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    )
}

@Composable
fun StatsCard(
    title: String,
    value: String,
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MutedForegroundDark
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun WeeklyActivityChart(
    days: List<Pair<String, Boolean>> // (Day, HasActivity)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Atividade Semanal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = "Últimos 7 dias",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MutedForegroundDark
                    )
                )
            }
            
            // Gráfico de barras
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { (day, hasActivity) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(if (hasActivity) 60.dp else 16.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (hasActivity) PurplePrimary
                                    else CardDarker
                                )
                        )
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MutedForegroundDark
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RankingCard(
    position: Int,
    name: String,
    workouts: Int,
    isCurrentUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isCurrentUser) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$position",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = if (isCurrentUser) PurplePrimary else Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.width(32.dp)
            )
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "$workouts treinos",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MutedForegroundDark
                    )
                )
            }
            
            // Ícone de medalha/troféu
            when (position) {
                1 -> Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700), // Gold
                    modifier = Modifier.size(24.dp)
                )
                2 -> Icon(
                    Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = Color(0xFFC0C0C0), // Silver
                    modifier = Modifier.size(24.dp)
                )
                3 -> Icon(
                    Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = Color(0xFFCD7F32), // Bronze
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun PeakHoursCard(
    occupancyData: UnitOccupancyResponse?,
    selectedDayOfWeek: Int?,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header com título e ícone de ajuda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Horários de pico",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Informações",
                        tint = MutedForegroundDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Seletor de dias da semana
            val daysOfWeek = listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB")
            val currentDay = selectedDayOfWeek ?: occupancyData?.dayOfWeek ?: 0
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                daysOfWeek.forEachIndexed { index, day ->
                    TextButton(
                        onClick = { onDaySelected(index) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (index == currentDay) PurplePrimary else MutedForegroundDark
                        )
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (index == currentDay) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
            
            // Gráfico de barras
            occupancyData?.let { data ->
                val peakHours = data.peakHours.filter { it.hour in listOf(5,7,9,11,13,15,17,19,21) }
                val maxCheckIns = peakHours.maxOfOrNull { it.checkInCount } ?: 1
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Barras do gráfico
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        peakHours.forEach { peakHour ->
                            val barHeight = if (maxCheckIns > 0) {
                                (peakHour.checkInCount.toFloat() / maxCheckIns.toFloat() * 100.dp.value).dp.coerceAtLeast(8.dp)
                            } else {
                                8.dp
                            }
                            
                            val isCurrentHour = peakHour.hour == data.currentHour
                            val statusColor = when (data.currentStatus) {
                                UnitOccupancyStatus.VERY_BUSY -> Color(0xFFFF4444) // Vermelho
                                UnitOccupancyStatus.BUSY -> Color(0xFFFF8800) // Laranja
                                UnitOccupancyStatus.MODERATELY_BUSY -> Color(0xFFFFAA00) // Amarelo
                                UnitOccupancyStatus.NOT_BUSY -> Color(0xFF10B981) // Verde
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(32.dp)
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (isCurrentHour) statusColor.copy(alpha = 0.8f)
                                            else CardDarker
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${peakHour.hour}h",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MutedForegroundDark
                                    )
                                )
                            }
                        }
                    }
                    
                    // Indicador "Ao vivo" se for o horário atual
                    if (data.currentHour in listOf(5,7,9,11,13,15,17,19,21,23)) {
                        val currentIndex = peakHours.indexOfFirst { it.hour == data.currentHour }
                        if (currentIndex >= 0) {
                            val statusText = when (data.currentStatus) {
                                UnitOccupancyStatus.VERY_BUSY -> "Muito movimentado"
                                UnitOccupancyStatus.BUSY -> "Movimentado"
                                UnitOccupancyStatus.MODERATELY_BUSY -> "Moderadamente movimentado"
                                UnitOccupancyStatus.NOT_BUSY -> "Não muito movimentado"
                            }
                            
                            val statusColor = when (data.currentStatus) {
                                UnitOccupancyStatus.VERY_BUSY -> Color(0xFFFF4444)
                                UnitOccupancyStatus.BUSY -> Color(0xFFFF8800)
                                UnitOccupancyStatus.MODERATELY_BUSY -> Color(0xFFFFAA00)
                                UnitOccupancyStatus.NOT_BUSY -> Color(0xFF10B981)
                            }
                            
                            val offsetX = (currentIndex * (100f / peakHours.size.coerceAtLeast(1))).dp.value.toInt()
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset { IntOffset(offsetX, 0) }
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Text(
                                        text = "Ao vivo: $statusText",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                // Linha pontilhada vertical
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(120.dp)
                                        .background(statusColor.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
                
                // Informação de tempo médio de permanência
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MutedForegroundDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "As pessoas costumam ficar ${data.averageStayMinutes} min aqui",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            } ?: run {
                // Estado vazio
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Dados não disponíveis",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
        }
    }
}
