package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.OnboardingViewModel

data class GoalOption(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun OnboardingGoalScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    unitId: String? = null,
    unitName: String? = null,
    onNext: (String?, String?, String?) -> Unit = { _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val goals = listOf(
        GoalOption(
            id = "mass_gain",
            title = "Ganho de Massa",
            description = "Foco em hipertrofia e força bruta",
            icon = Icons.Default.Bolt
        ),
        GoalOption(
            id = "weight_loss",
            title = "Perda de Peso",
            description = "Queime calorias e alcance seu peso ideal",
            icon = Icons.Default.Speed
        ),
        GoalOption(
            id = "performance",
            title = "Performance",
            description = "Melhore sua resistência e capacidade física",
            icon = Icons.Default.EmojiEvents
        ),
        GoalOption(
            id = "health_wellness",
            title = "Saúde & Bem-estar",
            description = "Mantenha-se ativo e saudável no dia a dia",
            icon = Icons.Default.SelfImprovement
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Progress Indicator
            item {
                ProgressIndicator(currentStep = 2, totalSteps = 3)
            }
            
            // Título
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Defina seu",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    GradientText(
                        text = "Objetivo Principal",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Personalizaremos sua experiência e desafios com base no que você deseja alcançar.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
            
            // Cards de objetivos
            goals.forEach { goal ->
                item {
                    GoalCard(
                        goal = goal,
                        selected = uiState.selectedGoal == goal.id,
                        onClick = { viewModel.selectGoal(goal.id) }
                    )
                }
            }
            
            // InfoBox
            item {
                InfoBox(
                    text = "Você pode alterar seu objetivo a qualquer momento nas configurações."
                )
            }
            
            // Botão Próxima Etapa
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GradientButton(
                        text = "Próxima Etapa",
                        onClick = {
                            if (uiState.selectedGoal != null) {
                                onNext(unitId, unitName, uiState.selectedGoal)
                            }
                        },
                        enabled = uiState.selectedGoal != null,
                        icon = {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    Text(
                        text = "PASSO 2 DE 3",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCard(
    goal: GoalOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (selected) {
                    Modifier.border(
                        width = 2.dp,
                        color = BorderPurple,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = CardDark
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícone
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = PurplePrimary.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        goal.icon,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Informações
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = goal.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
            
            // Checkmark quando selecionado
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(24.dp)
                        .background(
                            brush = purpleToPinkGradient(),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selecionado",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
