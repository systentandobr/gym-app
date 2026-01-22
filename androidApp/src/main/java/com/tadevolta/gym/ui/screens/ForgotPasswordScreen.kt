package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.tadevolta.gym.ui.components.GradientButton
import com.tadevolta.gym.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit = {},
    onSendLink: (String) -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    
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
                    value = email,
                    onValueChange = { email = it },
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
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Botão Enviar Link
            GradientButton(
                text = "Enviar Link",
                onClick = { 
                    if (email.isNotBlank()) {
                        onSendLink(email)
                    }
                },
                enabled = email.isNotBlank(),
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
