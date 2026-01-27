package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.GradientButton
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.PrivacyViewModel

@Composable
fun PrivacyScreen(
    viewModel: PrivacyViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val privacySettings by viewModel.privacySettings.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
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
                    text = "Privacidade",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            // Card de Configurações de Privacidade
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PrivacyToggleItem(
                        title = "Perfil Público",
                        description = "Permitir que outros vejam seu nível",
                        checked = privacySettings.publicProfile,
                        onCheckedChange = { viewModel.updatePublicProfile(it) }
                    )
                    
                    Divider(color = BorderDark)
                    
                    PrivacyToggleItem(
                        title = "Mostrar conquistas no Ranking",
                        description = "Exibir medalhas para a comunidade",
                        checked = privacySettings.showAchievementsInRanking,
                        onCheckedChange = { viewModel.updateShowAchievementsInRanking(it) }
                    )
                    
                    Divider(color = BorderDark)
                    
                    PrivacyToggleItem(
                        title = "Compartilhar treinos",
                        description = "Amigos podem ver sua rotina diária",
                        checked = privacySettings.shareWorkouts,
                        onCheckedChange = { viewModel.updateShareWorkouts(it) }
                    )
                    
                    Divider(color = BorderDark)
                    
                    PrivacyToggleItem(
                        title = "Marketing",
                        description = "Receber notificações de promoções",
                        checked = privacySettings.marketing,
                        onCheckedChange = { viewModel.updateMarketing(it) }
                    )
                }
            }
            
            // Card de Proteção de Dados
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Seus dados estão protegidos por criptografia de ponta a ponta. A Galáxia não compartilha suas informações pessoais com terceiros sem seu consentimento.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MutedForegroundDark
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botão Excluir Conta
            Button(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Destructive
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Excluir minha conta",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
            
            // Aviso de exclusão
            Text(
                text = "ESTA AÇÃO É IRREVERSÍVEL E TODOS OS SEUS DADOS DE TREINO E CONQUISTAS SERÃO PERDIDOS PARA SEMPRE.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MutedForegroundDark,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Dialog de confirmação de exclusão
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = "Excluir Conta",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "Tem certeza que deseja excluir sua conta? Esta ação é irreversível e todos os seus dados serão perdidos permanentemente.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedForegroundDark
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount()
                        showDeleteConfirmation = false
                        // TODO: Navegar para tela de login após exclusão
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Destructive
                    )
                ) {
                    Text("Excluir", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancelar", color = MutedForegroundDark)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun PrivacyToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MutedForegroundDark
                )
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PurplePrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = CardDarker
            )
        )
    }
}
