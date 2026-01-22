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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.SignUpViewModel

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = hiltViewModel(),
    unitId: String? = null,
    unitName: String? = null,
    goal: String? = null,
    onSignUpSuccess: () -> Unit = {},
    navToLogin: (String?, String?) -> Unit = { _, _ -> }
) {
    // Definir unidade selecionada se fornecida
    androidx.compose.runtime.LaunchedEffect(unitId, unitName) {
        if (!unitId.isNullOrBlank() && !unitName.isNullOrBlank()) {
            viewModel.setSelectedUnit(unitId, unitName)
        }
    }
    
    // Definir goal se fornecida
    androidx.compose.runtime.LaunchedEffect(goal) {
        goal?.let { viewModel.setGoal(it) }
    }
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.isSignUpSuccessful) {
        if (uiState.isSignUpSuccessful) {
            onSignUpSuccess()
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
            Spacer(modifier = Modifier.weight(0.2f))
            
            // Logo e título
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Logo com gradiente (foguete)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(brush = purpleToPinkGradient()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FlightTakeoff,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Criar Conta",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Text(
                    text = "Comece sua jornada galáctica agora!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedForegroundDark
                    )
                )
            }
            
            Spacer(modifier = Modifier.weight(0.2f))
            
            // Campos de cadastro
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Nome Completo
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "NOME COMPLETO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        placeholder = { Text("Ex: Arthur Dent", color = MutedForegroundDark) },
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
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MutedForegroundDark
                            )
                        },
                        singleLine = true
                    )
                }
                
                // E-mail
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "E-MAIL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        placeholder = { Text("seu@email.com", color = MutedForegroundDark) },
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
                    Text(
                        text = "SENHA",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
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
                
                // Confirmar Senha
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CONFIRMAR SENHA",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = { viewModel.updateConfirmPassword(it) },
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
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
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
                
                // Botão Cadastrar
                GradientButton(
                    text = "Cadastrar",
                    onClick = { viewModel.signUp() },
                    enabled = !uiState.isLoading && 
                        uiState.name.isNotBlank() && 
                        uiState.email.isNotBlank() && 
                        uiState.password.isNotBlank() &&
                        uiState.confirmPassword.isNotBlank()
                )
            }
            
            Spacer(modifier = Modifier.weight(0.3f))
            
            // Link de login
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Já tem uma conta? ",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White
                    )
                )
                TextButton(onClick = { 
                    // Passar unitId e unitName ao voltar para Login
                    navToLogin(uiState.unitId, uiState.unitName)
                }) {
                    Text(
                        text = "Entre aqui",
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
