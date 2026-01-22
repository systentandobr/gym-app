package com.tadevolta.gym.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import coil.compose.AsyncImage
import com.tadevolta.gym.data.models.Achievement
import com.tadevolta.gym.data.models.User
import com.tadevolta.gym.ui.theme.*

@Composable
fun ProfileHeader(
    user: User?,
    level: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar com borda gradiente e badge PRO
        Box {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        brush = purpleToPinkGradient(),
                        shape = CircleShape
                    )
                    .padding(4.dp)
                    .background(
                        color = CardDark,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (user?.avatar != null) {
                    AsyncImage(
                        model = user.avatar,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            
            // Badge PRO
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush = purpleToPinkGradient()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PRO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        
        // Nome do usuário
        Text(
            text = user?.name ?: "Usuário",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
        
        // Nível
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🚀",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "NÍVEL $level ASTRONAUTA",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = PinkAccent,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun AchievementsSection(
    achievements: List<Achievement>,
    onSeeAll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Conquistas",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            TextButton(onClick = onSeeAll) {
                Text(
                    text = "Ver todas",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MutedForegroundDark
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(achievements) { achievement ->
                AchievementCard(achievement = achievement)
            }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .height(140.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Ícone da conquista (pode ser customizado baseado no tipo)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = purpleToPinkGradient(),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Usar ícone baseado no nome ou tipo da conquista
                val icon = when {
                    achievement.name.contains("ÓRBITA", ignoreCase = true) -> Icons.Default.Star
                    achievement.name.contains("NOVA", ignoreCase = true) -> Icons.Default.LocalFireDepartment
                    achievement.name.contains("CONSTELAÇÃO", ignoreCase = true) -> Icons.Default.Star
                    achievement.name.contains("VELOCIDADE", ignoreCase = true) -> Icons.Default.Bolt
                    else -> Icons.Default.EmojiEvents
                }
                
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Text(
                text = achievement.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SettingsSection(
    onPersonalDataClick: () -> Unit = {},
    onTrainingPlanClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
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
                .padding(vertical = 8.dp)
        ) {
            SettingItem(
                icon = Icons.Default.Person,
                iconColor = PurplePrimary,
                text = "Dados Pessoais",
                onClick = onPersonalDataClick
            )
            
            Divider(
                color = BorderDark,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            SettingItem(
                icon = Icons.Default.FitnessCenter,
                iconColor = PurplePrimary,
                text = "Meu Plano de Treino",
                onClick = onTrainingPlanClick
            )
            
            Divider(
                color = BorderDark,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            SettingItem(
                icon = Icons.Default.Lock,
                iconColor = PurplePrimary,
                text = "Privacidade",
                onClick = onPrivacyClick
            )
            
            Divider(
                color = BorderDark,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            SettingItem(
                icon = Icons.Default.ExitToApp,
                iconColor = Destructive,
                text = "Sair",
                onClick = onLogoutClick
            )
        }
    }
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MutedForegroundDark,
            modifier = Modifier.size(20.dp)
        )
    }
}
