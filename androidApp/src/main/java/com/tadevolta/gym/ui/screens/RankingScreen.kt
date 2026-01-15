package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.viewmodels.RankingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    viewModel: RankingViewModel = hiltViewModel()
) {
    val ranking by viewModel.ranking.collectAsState()
    val userData by viewModel.userGamificationData.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ranking") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Card do usuário
                UserRankingCard(
                    position = userData?.ranking?.position,
                    points = userData?.totalPoints ?: 0,
                    level = userData?.level ?: 1
                )
            }
            
            item {
                Text(
                    text = "Ranking Geral",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            // Lista de ranking
            items(ranking.size) { index ->
                RankingItem(
                    position = ranking[index],
                    isCurrentUser = ranking[index].userId == userData?.userId
                )
            }
            
            item {
                Text(
                    text = "Conquistas",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            // Conquistas
            items(userData?.achievements?.size ?: 0) { index ->
                userData?.achievements?.getOrNull(index)?.let { achievement ->
                    AchievementCard(achievement = achievement)
                }
            }
        }
    }
}
