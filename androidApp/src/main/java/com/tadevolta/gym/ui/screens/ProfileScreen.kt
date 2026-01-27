package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onPersonalDataClick: () -> Unit = {},
    onTrainingPlanClick: (String?) -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onAchievementsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val user by viewModel.user.collectAsState()
    val studentId by viewModel.studentId.collectAsState()
    val gamificationData by viewModel.gamificationData.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header do perfil
            item {
                ProfileHeader(
                    user = user,
                    level = gamificationData?.level ?: 1
                )
            }
            
            // Seção de Conquistas
            item {
                AchievementsSection(
                    achievements = gamificationData?.achievements ?: emptyList(),
                    onSeeAll = onAchievementsClick
                )
            }
            
            // Título Configurações
            item {
                Text(
                    text = "Configurações",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
            
            // Seção de Configurações
            item {
                SettingsSection(
                    onPersonalDataClick = onPersonalDataClick,
                    onTrainingPlanClick = { onTrainingPlanClick(studentId) },
                    onPrivacyClick = onPrivacyClick,
                    onLogoutClick = onLogoutClick
                )
            }
        }
    }
}
