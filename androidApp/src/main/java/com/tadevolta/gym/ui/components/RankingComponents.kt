package com.tadevolta.gym.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tadevolta.gym.data.models.Achievement
import com.tadevolta.gym.data.models.RankingPosition
import com.tadevolta.gym.ui.theme.*

@Composable
fun GlobalRankingTop3(
    top3: List<RankingPosition>,
    currentUserId: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2º lugar
        if (top3.size > 1) {
            Top3PodiumItem(
                position = top3[1],
                rank = 2,
                isCurrentUser = top3[1].userId == currentUserId,
                modifier = Modifier.weight(1f)
            )
        }
        
        // 1º lugar (maior)
        if (top3.isNotEmpty()) {
            Top3PodiumItem(
                position = top3[0],
                rank = 1,
                isCurrentUser = top3[0].userId == currentUserId,
                modifier = Modifier.weight(1.2f)
            )
        }
        
        // 3º lugar
        if (top3.size > 2) {
            Top3PodiumItem(
                position = top3[2],
                rank = 3,
                isCurrentUser = top3[2].userId == currentUserId,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Top3PodiumItem(
    position: RankingPosition,
    rank: Int,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Estrela para 1º lugar
        if (rank == 1) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // Círculo do avatar
        val borderColor = when (rank) {
            1 -> PurplePrimary
            2 -> Color(0xFFD4A574) // Bege
            3 -> Color(0xFFCD853F) // Laranja-esverdeado
            else -> Color.Gray
        }
        
        Box(
            modifier = Modifier
                .size(if (rank == 1) 100.dp else 80.dp)
                .clip(CircleShape)
                .background(CardDarker)
                .border(
                    width = 3.dp,
                    color = borderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Avatar placeholder - pode ser substituído por AsyncImage
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(if (rank == 1) 60.dp else 48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Posição e nome
        Text(
            text = "${rank}º",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = position.userName ?: "Usuário",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MutedForegroundDark
            ),
            maxLines = 1
        )
    }
}

@Composable
fun UserPerformanceCard(
    daysCompleted: Int,
    totalDays: Int = 365,
    globalPosition: Int?,
    onShareClick: () -> Unit
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SEU DESEMPENHO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = PinkAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$daysCompleted de $totalDays dias",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Posição Global",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    globalPosition?.let {
                        Text(
                            text = "#$it",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = PurplePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
            
            // Botão compartilhar
            GradientButton(
                text = "Compartilhar Progresso",
                onClick = onShareClick,
                icon = {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun WeeklyRankingList(
    ranking: List<RankingPosition>,
    currentUserId: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ranking.forEach { position ->
            WeeklyRankingItem(
                position = position,
                isCurrentUser = position.userId == currentUserId
            )
        }
    }
}

@Composable
private fun WeeklyRankingItem(
    position: RankingPosition,
    isCurrentUser: Boolean
) {
    Card(
        modifier = Modifier
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
            // Número da posição
            Text(
                text = "${position.position}",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = if (isCurrentUser) PurplePrimary else MutedForegroundDark,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.width(32.dp)
            )
            
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardDarker),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Nome e métricas
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = position.userName ?: "Usuário",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${position.totalPoints / 100} treinos • ${position.totalPoints / 10}k pts",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MutedForegroundDark
                    )
                )
            }
            
            // Ícone de medalha
            if (position.position <= 3) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = when (position.position) {
                        1 -> Gold
                        2 -> Color(0xFFC0C0C0) // Silver
                        3 -> Bronze
                        else -> Color.Transparent
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ShareProgressButton(
    onClick: () -> Unit
) {
    GradientButton(
        text = "Compartilhar Progresso",
        onClick = onClick,
        icon = {
            Icon(
                Icons.Default.Share,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    )
}

// Componentes legados mantidos para compatibilidade
@Composable
fun UserRankingCard(
    position: Int?,
    points: Int,
    level: Int
) {
    UserPerformanceCard(
        daysCompleted = 10,
        globalPosition = position,
        onShareClick = {}
    )
}

@Composable
fun RankingItem(
    position: RankingPosition,
    isCurrentUser: Boolean
) {
    WeeklyRankingItem(
        position = position,
        isCurrentUser = isCurrentUser
    )
}

@Composable
fun AchievementCard(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Ícone da conquista
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White
                    )
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MutedForegroundDark
                    )
                )
            }
        }
    }
}
