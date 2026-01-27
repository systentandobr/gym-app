package com.tadevolta.gym.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.RankingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    viewModel: RankingViewModel = hiltViewModel()
) {
    val ranking by viewModel.ranking.collectAsState()
    val userData by viewModel.userGamificationData.collectAsState()
    val checkInStats by viewModel.checkInStats.collectAsState()
    val shareableText by viewModel.shareableText.collectAsState()
    val context = LocalContext.current
    
    val top3 = ranking.take(3)
    val weeklyRanking = ranking.take(10) // Top 10 para ranking semanal
    
    val currentMonth = SimpleDateFormat("MMMM", Locale("pt", "BR")).format(Date())
    
    // Compartilhar quando o texto estiver disponível
    LaunchedEffect(shareableText) {
        shareableText?.let { text ->
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Progresso"))
            viewModel.clearShareableText()
        }
    }
    
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
            // Cabeçalho
            item {
                RankingHeader(
                    currentMonth = currentMonth
                )
            }
            
            // Top 3
            if (top3.isNotEmpty()) {
                item {
                    GlobalRankingTop3(
                        top3 = top3,
                        currentUserId = userData?.userId
                    )
                }
            }
            
            // Card de desempenho do usuário
            item {
                UserPerformanceCard(
                    daysCompleted = checkInStats?.checkInsLast365Days ?: 10,
                    totalDays = 365,
                    globalPosition = userData?.ranking?.position,
                    onShareClick = { viewModel.shareProgress() }
                )
            }
            
            // Ranking Semanal
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ranking Semanal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Atualizado agora",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
            
            // Lista do ranking semanal
            item {
                WeeklyRankingList(
                    ranking = weeklyRanking,
                    currentUserId = userData?.userId,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun RankingHeader(
    currentMonth: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            GradientText(
                text = "Ranking Global",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Tema Galáxia • $currentMonth",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MutedForegroundDark
                )
            )
        }
        
        // Avatar/ícone do usuário
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = CardDark,
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
