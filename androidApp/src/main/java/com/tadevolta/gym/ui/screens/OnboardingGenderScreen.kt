package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.tadevolta.gym.R
import com.tadevolta.gym.data.models.Gender
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.OnboardingSharedViewModel

@Composable
fun OnboardingGenderScreen(
    viewModel: OnboardingSharedViewModel = hiltViewModel(),
    onNext: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
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
                ProgressIndicator(currentStep = 3, totalSteps = 4)
            }
            
            // Título
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Escolha seu",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    GradientText(
                        text = "Perfil",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Isso nos ajuda a personalizar seu plano de treino e métricas corporais.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
            
            // Card Masculino
            item {
                GenderCard(
                    title = "MASCULINO",
                    description = "Treinos otimizados para fisiologia masculina",
                    selected = uiState.gender == Gender.MALE,
                    onClick = { viewModel.updateGender(Gender.MALE) },
                    imageResId = R.drawable.athlete_male,
                    contentDescription = "Atleta masculino"
                )
            }
            
            // Card Feminino
            item {
                GenderCard(
                    title = "FEMININO",
                    description = "Treinos otimizados para fisiologia feminina",
                    selected = uiState.gender == Gender.FEMALE,
                    onClick = { viewModel.updateGender(Gender.FEMALE) },
                    imageResId = R.drawable.athlete_female,
                    contentDescription = "Atleta feminino"
                )
            }
            
            // InfoBox
            item {
                InfoBox(
                    text = "Suas recomendações de volume e intensidade de treino serão adaptadas com base nesta escolha."
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
                            if (uiState.gender != null) {
                                onNext()
                            }
                        },
                        enabled = uiState.gender != null,
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
                        text = "PASSO 3 DE 4",
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
private fun GenderCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    imageResId: Int,
    contentDescription: String
) {
    val context = LocalContext.current
    
    SelectableCard(
        selected = selected,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Informações
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
                
                // Ícone do atleta posicionado à direita, parcialmente fora do card
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = 16.dp) // Parcialmente fora do card
                ) {
                    // Usar SubcomposeAsyncImage para permitir composables customizados em error e loading
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageResId)
                            .crossfade(true)
                            .build(),
                        contentDescription = contentDescription,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {
                            // Placeholder enquanto carrega
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = PurplePrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = contentDescription,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        },
                        error = {
                            // Fallback se a imagem não existir
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = PurplePrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = contentDescription,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        },
                        success = {
                            SubcomposeAsyncImageContent()
                        }
                    )
                }
            }
        }
    }
}
