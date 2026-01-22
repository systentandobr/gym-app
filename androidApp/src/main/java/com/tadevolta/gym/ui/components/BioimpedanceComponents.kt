package com.tadevolta.gym.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tadevolta.gym.data.models.BioimpedanceMeasurement
import com.tadevolta.gym.data.models.DataPoint
import com.tadevolta.gym.ui.theme.*
import kotlin.math.max
import kotlin.math.min

@Composable
fun BioimpedanceHeader(
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White
            )
        }
        
        Text(
            text = "Bioimpedância",
            style = MaterialTheme.typography.titleLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
        
        IconButton(onClick = onShareClick) {
            Icon(
                Icons.Default.Share,
                contentDescription = "Compartilhar",
                tint = Color.White
            )
        }
    }
}

@Composable
fun ProgressChartCard(
    period: String = "6 meses",
    title: String = "Progresso Galáctico",
    weightData: List<DataPoint>,
    bodyFatData: List<DataPoint>,
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título secundário
            Text(
                text = "Evolução $period",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MutedForegroundDark
                )
            )
            
            // Título principal
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            
            // Legenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PurplePrimary)
                    )
                    Text(
                        text = "PESO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PinkAccent)
                    )
                    Text(
                        text = "GORDURA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
            
            // Gráfico simplificado (pode ser melhorado com biblioteca de gráficos)
            SimpleLineChart(
                weightData = weightData,
                bodyFatData = bodyFatData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun SimpleLineChart(
    weightData: List<DataPoint>,
    bodyFatData: List<DataPoint>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CardDarker, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // Placeholder para gráfico - pode ser substituído por biblioteca de gráficos
        // Por enquanto, mostra os meses no eixo X
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weightData.forEach { point ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = point.month,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
        }
        
        // Texto placeholder
        Text(
            text = "Gráfico de evolução\n(Implementar com biblioteca de gráficos)",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MutedForegroundDark
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun BioimpedanceMeasurementCard(
    measurement: BioimpedanceMeasurement,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Badge "MELHOR MARCA" se aplicável
            if (measurement.isBestRecord) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            color = Color(0xFF10B981), // Verde
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "MELHOR MARCA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Data
                Text(
                    text = formatDate(measurement.date),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MutedForegroundDark
                    )
                )
                
                // Valores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MeasurementValue(
                        icon = Icons.Default.MonitorWeight,
                        value = "${measurement.weight}",
                        unit = "PESO KG",
                        iconColor = PurplePrimary
                    )
                    
                    MeasurementValue(
                        icon = Icons.Default.ShowChart,
                        value = "${measurement.bodyFat}%",
                        unit = "GORDURA",
                        iconColor = PinkAccent
                    )
                    
                    MeasurementValue(
                        icon = Icons.Default.FitnessCenter,
                        value = "${measurement.muscle}",
                        unit = "MÚSCULO",
                        iconColor = PurplePrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun MeasurementValue(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    unit: String,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MutedForegroundDark
            )
        )
    }
}

@Composable
fun NewMeasurementButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GradientButton(
        text = "Nova Avaliação",
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    )
}

private fun formatDate(dateString: String): String {
    // Formatar data de ISO 8601 para formato brasileiro
    // Exemplo: "2023-10-15" -> "15 Out 2023"
    return try {
        val parts = dateString.split("-")
        if (parts.size == 3) {
            val day = parts[2]
            val month = when (parts[1]) {
                "01" -> "Jan"
                "02" -> "Fev"
                "03" -> "Mar"
                "04" -> "Abr"
                "05" -> "Mai"
                "06" -> "Jun"
                "07" -> "Jul"
                "08" -> "Ago"
                "09" -> "Set"
                "10" -> "Out"
                "11" -> "Nov"
                "12" -> "Dez"
                else -> parts[1]
            }
            val year = parts[0]
            "$day $month $year"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}
