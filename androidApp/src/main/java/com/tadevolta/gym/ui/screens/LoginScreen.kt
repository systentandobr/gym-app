package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import android.net.Uri
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tadevolta.gym.R
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            onLoginSuccess()
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.3f))
            
            // Logo e título
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Logo PNG ou MP4
                // Para usar o PNG/MP4, coloque-o em res/raw/ com o nome "logo"
                // Suporta: logo.png ou logo.mp4
                val context = LocalContext.current
                val logoUri = remember {
                    try {
                        val resourceId = R.raw.logov
                        Uri.parse("android.resource://${context.packageName}/$resourceId")
                    } catch (e: Exception) {
                        null
                    }
                }
                
                if (logoUri != null) {
                    // Tenta primeiro como MP4 (vídeo), se falhar, tenta como PNG
                    VideoPlayer(
                        videoUri = logoUri,
                        modifier = Modifier
                            .size(200.dp, 80.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        autoPlay = true,
                        looping = true
                    )
                } else {
                    // Fallback: mostra ícone se não encontrar arquivo
                    val logoPng = remember {
                        try {
                            val resourceId = R.raw.logo
                            Uri.parse("android.resource://${context.packageName}/$resourceId")
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (logoPng != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(logoPng)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(200.dp, 80.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(200.dp, 80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(brush = purpleToPinkGradient()),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Em busca da motivação perfeita",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedForegroundDark
                    )
                )
            }
            
            Spacer(modifier = Modifier.weight(0.2f))
            
            // Campos de login
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Email
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "E-mail",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        placeholder = { Text("exemplo@email.com", color = MutedForegroundDark) },
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
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = MutedForegroundDark
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                }
                
                // Senha
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Senha",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White
                            )
                        )
                        TextButton(onClick = onForgotPassword) {
                            Text(
                                text = "Esqueceu a senha?",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PurplePrimary
                                )
                            )
                        }
                    }
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        placeholder = { Text("••••••••", color = MutedForegroundDark) },
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
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MutedForegroundDark
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha",
                                    tint = MutedForegroundDark
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )
                }
                
                // Erro
                uiState.error?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Destructive
                        )
                    )
                }
                
                // Botão Entrar
                GradientButton(
                    text = "Entrar",
                    onClick = { viewModel.login() },
                    enabled = !uiState.isLoading && uiState.email.isNotBlank() && uiState.password.isNotBlank()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Divisor
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = BorderDark
                )
                Text(
                    text = " OU CONTINUE COM ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MutedForegroundDark
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = BorderDark
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botões sociais
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SocialLoginButton(
                    provider = "Google",
                    onClick = { /* TODO: Implementar login Google */ },
                    modifier = Modifier.weight(1f),
                    icon = {
                        // Ícone Google simplificado
                        Box(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                )
                SocialLoginButton(
                    provider = "Facebook",
                    onClick = { /* TODO: Implementar login Facebook */ },
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            Icons.Default.Facebook,
                            contentDescription = null,
                            tint = Color(0xFF1877F2),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.weight(0.3f))
            
            // Link de cadastro
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Não tem uma conta? ",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White
                    )
                )
                TextButton(onClick = onSignUp) {
                    Text(
                        text = "Cadastre-se",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
