package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.data.models.Achievement
import com.tadevolta.gym.data.models.AchievementRarity
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.ProfileViewModel

@Composable
fun AchievementsScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val gamificationData by viewModel.gamificationData.collectAsState()
    val achievements = gamificationData?.achievements ?: emptyList()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Header com botão voltar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Conquistas",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (achievements.isEmpty()) {
                // Estado vazio
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MutedForegroundDark,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Nenhuma conquista ainda",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MutedForegroundDark
                            )
                        )
                        Text(
                            text = "Continue treinando para desbloquear conquistas!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MutedForegroundDark
                            )
                        )
                    }
                }
            } else {
                // Grid de conquistas
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(count = achievements.size) { index ->
                        AchievementDetailCard(achievement = achievements[index])
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementDetailCard(
    achievement: Achievement,
    modifier: Modifier = Modifier
) {
    val isUnlocked = achievement.unlockedAt != null
    val rarityColor = getRarityColor(achievement.rarity)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .alpha(if (isUnlocked) 1f else 0.5f),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Ícone da conquista com borda colorida baseada na raridade
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        color = if (isUnlocked) rarityColor.copy(alpha = 0.2f) else CardDarker,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val icon = getAchievementIcon(achievement.name)
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isUnlocked) rarityColor else MutedForegroundDark,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            // Nome da conquista
            Text(
                text = achievement.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = if (isUnlocked) Color.White else MutedForegroundDark,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Descrição
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MutedForegroundDark
                ),
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Badge de raridade
            if (isUnlocked) {
                Text(
                    text = getRarityLabel(achievement.rarity),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = rarityColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            } else {
                Text(
                    text = "Bloqueada",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MutedForegroundDark,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun getRarityColor(rarity: AchievementRarity): Color {
    return when (rarity) {
        AchievementRarity.COMMON -> Color(0xFF94A3B8) // Cinza
        AchievementRarity.RARE -> Color(0xFF3B82F6) // Azul
        AchievementRarity.EPIC -> Color(0xFF8B5CF6) // Roxo
        AchievementRarity.LEGENDARY -> Color(0xFFF59E0B) // Dourado
    }
}

private fun getRarityLabel(rarity: AchievementRarity): String {
    return when (rarity) {
        AchievementRarity.COMMON -> "COMUM"
        AchievementRarity.RARE -> "RARA"
        AchievementRarity.EPIC -> "ÉPICA"
        AchievementRarity.LEGENDARY -> "LENDÁRIA"
    }
}

private fun getAchievementIcon(name: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        name.contains("ÓRBITA", ignoreCase = true) -> Icons.Default.Star
        name.contains("NOVA", ignoreCase = true) -> Icons.Default.LocalFireDepartment
        name.contains("CONSTELAÇÃO", ignoreCase = true) -> Icons.Default.Star
        name.contains("VELOCIDADE", ignoreCase = true) -> Icons.Default.Bolt
        name.contains("FORÇA", ignoreCase = true) -> Icons.Default.FitnessCenter
        name.contains("RESISTÊNCIA", ignoreCase = true) -> Icons.Default.DirectionsRun
        name.contains("CHECK-IN", ignoreCase = true) -> Icons.Default.CheckCircle
        name.contains("STREAK", ignoreCase = true) -> Icons.Default.Whatshot
        else -> Icons.Default.EmojiEvents
    }
}
