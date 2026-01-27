package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.GradientButton
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.ForgotPasswordViewModel

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onSendLinkSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Navegar de volta quando sucesso
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            // Aguardar um pouco para mostrar a mensagem de sucesso
            kotlinx.coroutines.delay(2000)
            onSendLinkSuccess()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header com botão voltar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "RECUPERAÇÃO",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(48.dp)) // Balance
            }
            
            Spacer(modifier = Modifier.weight(0.2f))
            
            // Ícone de cadeado
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        spotColor = PurplePrimary.copy(alpha = 0.5f)
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PurplePrimary.copy(alpha = 0.3f),
                                PurplePrimary.copy(alpha = 0.1f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Título
            Text(
                text = "Esqueceu sua senha?",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Descrição
            Text(
                text = "Não se preocupe! Insira seu e-mail cadastrado para receber as instruções de recuperação.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MutedForegroundDark
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Campo de e-mail
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "E-mail de cadastro",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White
                    )
                )
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.updateEmail(it) },
                    placeholder = { 
                        Text(
                            "Ex: astronauta@gym.com",
                            color = MutedForegroundDark
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = InputDark,
                        focusedContainerColor = InputDark,
                        unfocusedBorderColor = BorderDark,
                        focusedBorderColor = PurplePrimary,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    trailingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    singleLine = true,
                    enabled = !uiState.isLoading && !uiState.isSuccess
                )
            }
            
            // Mensagem de erro
            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Destructive
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Mensagem de sucesso
            uiState.successMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF4CAF50) // Verde para sucesso
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Botão Enviar Link
            GradientButton(
                text = if (uiState.isLoading) "Enviando..." else "Enviar Link",
                onClick = { 
                    viewModel.sendRecoveryLink()
                },
                enabled = !uiState.isLoading && !uiState.isSuccess && uiState.email.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = PinkAccent.copy(alpha = 0.5f)
                    )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Link voltar para login
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onBack) {
                    Text(
                        text = "Voltar para o Login",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
