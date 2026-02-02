package com.tadevolta.gym.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tadevolta.gym.data.models.CheckIn
import com.tadevolta.gym.data.models.CheckInStats
import com.tadevolta.gym.ui.theme.*
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.FitnessCenter

@Composable
fun CheckInCounter(
    current: Int,
    total: Int,
    label: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$current / $total",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = current.toFloat() / total.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun StreakCard(
    current: Int,
    longest: Int
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Recorde Atual",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "$current dias",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Column {
                Text(
                    text = "Maior Recorde",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "$longest dias",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

@Composable
fun CheckInHistoryItem(checkIn: CheckIn) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Check-in realizado",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = checkIn.date,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun CheckInErrorModal(
    unitName: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .border(
                    width = 2.dp,
                    brush = purpleToPinkGradient(),
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Ícone de localização bloqueada com gradiente
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = CardDarker,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                brush = purpleToPinkGradient(),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Ícone de localização com X sobreposto
                        Box {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFFF0000), // Vermelho
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
                
                // Título
                Text(
                    text = "Ops! Fora de Alcance",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                
                // Mensagem
                Text(
                    text = "Lamentamos, mas não foi possível fazer check-in. Parece que você não está na sua Academia (${unitName ?: "Academia"}).",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedForegroundDark
                    ),
                    textAlign = TextAlign.Center
                )
                
                // Botão Tentar Novamente
                GradientButton(
                    text = "Tentar Novamente",
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Botão Fechar
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Fechar",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
        }
    }
}

/**
 * Tipo de modal de resultado do check-in
 */
enum class CheckInModalType {
    SUCCESS,
    ERROR_LOCATION,
    ERROR_TRAINING_IN_PROGRESS,
    ERROR_ALREADY_DONE,
    ERROR_GENERIC
}

/**
 * Mensagens motivacionais de sucesso (rotacionadas)
 */
private val successMessages = listOf(
    "Parabéns! Seu check-in foi realizado continue o seu treino de hoje",
    "Que maravilha te ver por aqui, está preparado para começar o treino de hoje?",
    "Excelente! Check-in realizado com sucesso. Vamos treinar?",
    "Ótimo! Você está aqui. Hora de dar o seu melhor no treino!",
    "Perfeito! Check-in confirmado. Bora treinar?"
)

/**
 * Modal de resultado do check-in com mensagens customizadas
 */
@Composable
fun CheckInResultModal(
    modalType: CheckInModalType,
    message: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .border(
                    width = 2.dp,
                    brush = when (modalType) {
                        CheckInModalType.SUCCESS -> purpleToPinkGradient()
                        else -> androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF6B6B),
                                Color(0xFFFF8E8E)
                            )
                        )
                    },
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Ícone baseado no tipo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = CardDarker,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                brush = when (modalType) {
                                    CheckInModalType.SUCCESS -> purpleToPinkGradient()
                                    else -> androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFF6B6B),
                                            Color(0xFFFF8E8E)
                                        )
                                    )
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when (modalType) {
                            CheckInModalType.SUCCESS -> {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            CheckInModalType.ERROR_TRAINING_IN_PROGRESS -> {
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
                
                // Título
                Text(
                    text = when (modalType) {
                        CheckInModalType.SUCCESS -> "Check-in Realizado!"
                        CheckInModalType.ERROR_LOCATION -> "Ops! Fora de Alcance"
                        CheckInModalType.ERROR_TRAINING_IN_PROGRESS -> "Treino em Execução"
                        CheckInModalType.ERROR_ALREADY_DONE -> "Check-in Já Realizado"
                        CheckInModalType.ERROR_GENERIC -> "Erro ao Fazer Check-in"
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                
                // Mensagem
                Text(
                    text = message ?: when (modalType) {
                        CheckInModalType.SUCCESS -> successMessages.random()
                        CheckInModalType.ERROR_LOCATION -> "Um check-in só pode ser feito dentro da academia"
                        CheckInModalType.ERROR_TRAINING_IN_PROGRESS -> "Ao fazer o check-in precisa executar e encerrar o treino"
                        CheckInModalType.ERROR_ALREADY_DONE -> "Você já realizou check-in hoje. Tente novamente amanhã!"
                        CheckInModalType.ERROR_GENERIC -> "Não foi possível realizar o check-in. Tente novamente mais tarde."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedForegroundDark
                    ),
                    textAlign = TextAlign.Center
                )
                
                // Botão de confirmação
                GradientButton(
                    text = when (modalType) {
                        CheckInModalType.SUCCESS -> "Começar Treino"
                        else -> "Entendi"
                    },
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Botão Fechar (apenas para erros)
                if (modalType != CheckInModalType.SUCCESS) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Fechar",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MutedForegroundDark
                            )
                        )
                    }
                }
            }
        }
    }
}
