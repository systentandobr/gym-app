package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.BioimpedanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioimpedanceScreen(
    viewModel: BioimpedanceViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNewMeasurementClick: () -> Unit = {}
) {
    val measurements by viewModel.measurements.collectAsState()
    val progressData by viewModel.progressData.collectAsState()
    
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
                BioimpedanceHeader(
                    onBackClick = onBackClick,
                    onShareClick = { /* TODO: Implementar compartilhamento */ }
                )
            }
            
            // Card de progresso com gráfico
            item {
                ProgressChartCard(
                    period = progressData?.period ?: "6 meses",
                    title = progressData?.title ?: "Progresso Galáctico",
                    weightData = progressData?.weightData ?: emptyList(),
                    bodyFatData = progressData?.bodyFatData ?: emptyList()
                )
            }
            
            // Título do histórico
            item {
                Text(
                    text = "Histórico de Avaliações",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
            
            // Lista de avaliações
            items(measurements) { measurement ->
                BioimpedanceMeasurementCard(measurement = measurement)
            }
            
            // Botão nova avaliação
            item {
                NewMeasurementButton(
                    onClick = onNewMeasurementClick
                )
            }
        }
    }
}
