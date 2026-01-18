package com.tadevolta.gym.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tadevolta.gym.data.models.Achievement
import com.tadevolta.gym.data.models.RankingPosition

@Composable
fun UserRankingCard(
    position: Int?,
    points: Int,
    level: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sua Posição",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            position?.let {
                Text(
                    text = "#$it",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Nível $level")
            Text("$points pontos")
        }
    }
}

@Composable
fun RankingItem(
    position: RankingPosition,
    isCurrentUser: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Text(
                    text = "#${position.position}",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = position.unitName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Nível ${position.level}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Text(
                text = "${position.totalPoints} pts",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
