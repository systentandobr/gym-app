package com.tadevolta.gym.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import com.tadevolta.gym.data.models.Gender
import com.tadevolta.gym.data.models.FitnessLevel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.theme.purpleToPinkGradient
import com.tadevolta.gym.ui.utils.DateMaskTransformation
import com.tadevolta.gym.ui.utils.formatDateInput
import com.tadevolta.gym.ui.utils.CpfMaskTransformation
import com.tadevolta.gym.ui.viewmodels.OnboardingSharedViewModel
import com.tadevolta.gym.utils.LocationHelper
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    viewModel: OnboardingSharedViewModel = hiltViewModel(),
    onSignUpSuccess: () -> Unit = {},
    navToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Os dados já estão no OnboardingSharedViewModel, não precisa sincronizar
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showThankYouModal by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Mostrar modal de agradecimento quando signup completar e veio de indicação
    LaunchedEffect(uiState.signUpStep, uiState.cameFromWithoutUnit) {
        if (uiState.signUpStep == com.tadevolta.gym.ui.viewmodels.SignUpStep.COMPLETED && 
            uiState.cameFromWithoutUnit) {
            showThankYouModal = true
        }
    }
    
    // Launcher para permissão de localização
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.loadLocationAndFillAddress(locationHelper)
        }
    }
    
    // Navegar apenas quando todas as requisições completarem e modal não estiver aberto
    LaunchedEffect(uiState.signUpStep, showThankYouModal) {
        if (uiState.signUpStep == com.tadevolta.gym.ui.viewmodels.SignUpStep.COMPLETED && 
            !uiState.cameFromWithoutUnit && !showThankYouModal) {
            // Pequeno delay para garantir que todas as requisições foram processadas
            kotlinx.coroutines.delay(500)
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
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
                    text = "Etapa ${uiState.currentStep} de ${uiState.totalSteps}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedForegroundDark
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Indicador de progresso
            ProgressIndicator(
                currentStep = uiState.currentStep,
                totalSteps = uiState.totalSteps,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Conteúdo da etapa atual (scrollável)
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (uiState.currentStep) {
                    1 -> PersonalDataStep(
                        uiState = uiState,
                        viewModel = viewModel,
                        passwordVisible = passwordVisible,
                        confirmPasswordVisible = confirmPasswordVisible,
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        onConfirmPasswordVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible }
                    )
                    2 -> AddressStep(
                        uiState = uiState,
                        viewModel = viewModel,
                        locationHelper = locationHelper,
                        locationPermissionLauncher = locationPermissionLauncher,
                        coroutineScope = coroutineScope,
                        scrollState = scrollState
                    )
                    3 -> ReviewStep(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
                
                // Loading detalhado durante criação
                if (uiState.isLoading || uiState.isCreatingStudent) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = PurplePrimary
                            )
                            Text(
                                text = when (uiState.signUpStep) {
                                    com.tadevolta.gym.ui.viewmodels.SignUpStep.CREATING_USER -> "Criando sua conta..."
                                    com.tadevolta.gym.ui.viewmodels.SignUpStep.CREATING_STUDENT -> "Criando perfil de aluno..."
                                    com.tadevolta.gym.ui.viewmodels.SignUpStep.SENDING_LEAD -> "Enviando informações..."
                                    else -> "Processando..."
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
                
                // Campos obrigatórios não preenchidos
                if (uiState.missingRequiredFields.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Destructive,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Campos obrigatórios não preenchidos",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Destructive,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                uiState.missingRequiredFields.forEach { field ->
                                    Text(
                                        text = "• $field",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Erro geral
                uiState.error?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Erro",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Destructive,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
                
                // Erro específico de criação de aluno
                uiState.studentCreationError?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Erro ao criar perfil de aluno",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Destructive,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White
                                )
                            )
                            // Botão de retry apenas para criação de aluno
                            if (uiState.signUpStep == com.tadevolta.gym.ui.viewmodels.SignUpStep.ERROR && 
                                uiState.isSignUpSuccessful) {
                                GradientButton(
                                    text = "Tentar Novamente",
                                    onClick = { viewModel.retryStudentCreation() },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botões de navegação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botão Voltar (se não estiver na primeira etapa)
                if (uiState.currentStep > 1) {
                    OutlinedButton(
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp
                        )
                    ) {
                        Text("Voltar")
                    }
                }
                
                // Botão Próximo ou Cadastrar
                GradientButton(
                    text = when (uiState.currentStep) {
                        uiState.totalSteps -> when (uiState.signUpStep) {
                            com.tadevolta.gym.ui.viewmodels.SignUpStep.CREATING_USER -> "Criando conta..."
                            com.tadevolta.gym.ui.viewmodels.SignUpStep.CREATING_STUDENT -> "Criando perfil..."
                            com.tadevolta.gym.ui.viewmodels.SignUpStep.SENDING_LEAD -> "Finalizando..."
                            com.tadevolta.gym.ui.viewmodels.SignUpStep.COMPLETED -> "Concluído!"
                            else -> if (uiState.isLoading || uiState.isCreatingStudent) "Processando..." else "Cadastrar"
                        }
                        else -> "Próximo"
                    },
                    onClick = {
                        if (uiState.currentStep < uiState.totalSteps) {
                            if (viewModel.canProceedToNextStep()) {
                                viewModel.nextStep()
                            } else {
                                // Mostrar erro de validação
                                val stepName = when (uiState.currentStep) {
                                    1 -> "Dados Pessoais"
                                    2 -> "Endereço"
                                    else -> "Etapa $uiState.currentStep"
                                }
                                viewModel.clearError()
                                // O erro será mostrado pela validação
                            }
                        } else {
                            viewModel.signUp()
                        }
                    },
                    enabled = !uiState.isLoading && !uiState.isCreatingStudent && 
                             uiState.signUpStep != com.tadevolta.gym.ui.viewmodels.SignUpStep.COMPLETED &&
                             when (uiState.currentStep) {
                                1 -> {
                                    val nameWords = uiState.name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
                                    uiState.name.isNotBlank() && 
                                    nameWords.size >= 2 &&
                                    uiState.email.isNotBlank() && 
                                    uiState.password.isNotBlank() &&
                                    uiState.confirmPassword.isNotBlank()
                                }
                                2 -> uiState.address.isNotBlank() &&
                                     uiState.city.isNotBlank() &&
                                     uiState.state.isNotBlank() &&
                                     uiState.zipCode.isNotBlank() &&
                                     uiState.neighborhood.isNotBlank() &&
                                     uiState.localNumber.isNotBlank()
                                else -> true
                            },
                    modifier = Modifier.weight(if (uiState.currentStep > 1) 1f else 1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
                    navToLogin()
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
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Modal de Agradecimento
        if (showThankYouModal) {
            ThankYouModal(
                onDismiss = {
                    showThankYouModal = false
                    // Navegar após fechar modal
                    onSignUpSuccess()
                }
            )
        }
    }
}

@Composable
private fun ThankYouModal(
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
                // Ícone de agradecimento
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
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                
                // Título
                Text(
                    text = "Agradecemos pela Indicação!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                // Mensagem
                Text(
                    text = "Os dados da sua academia serão analisados e logo entraremos em contato para liberar o acesso ao aplicativo.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedForegroundDark
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                // Botão OK
                GradientButton(
                    text = "Entendi",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Composable para Etapa 1: Dados Pessoais
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PersonalDataStep(
    uiState: com.tadevolta.gym.ui.viewmodels.OnboardingSharedUiState,
    viewModel: OnboardingSharedViewModel,
    passwordVisible: Boolean,
    confirmPasswordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Dados Pessoais",
            style = MaterialTheme.typography.titleLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
        
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
                    unfocusedBorderColor = if (uiState.fieldErrors.containsKey("name")) Destructive else BorderDark,
                    focusedBorderColor = if (uiState.fieldErrors.containsKey("name")) Destructive else PurplePrimary,
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
                isError = uiState.fieldErrors.containsKey("name"),
                supportingText = uiState.fieldErrors["name"]?.let { error ->
                    {
                        Text(
                            text = error,
                            color = Destructive,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
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
        
        // Data de Nascimento
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "DATA DE NASCIMENTO",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            OutlinedTextField(
                value = uiState.birthDate ?: "",
                onValueChange = { 
                    // Remover formatação e manter apenas dígitos
                    val digitsOnly = it.filter { it.isDigit() }.take(8)
                    viewModel.updateBirthDate(digitsOnly)
                },
                placeholder = { Text("DD/MM/AAAA", color = MutedForegroundDark) },
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
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MutedForegroundDark
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                visualTransformation = DateMaskTransformation()
            )
        }
        
        // CPF
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "CPF (Opcional)",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            OutlinedTextField(
                value = uiState.cpf ?: "",
                onValueChange = { 
                    // Remover formatação e manter apenas dígitos
                    val digitsOnly = it.filter { it.isDigit() }.take(11)
                    viewModel.updateCpf(digitsOnly)
                },
                placeholder = { Text("000.000.000-00", color = MutedForegroundDark) },
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
                        Icons.Default.Badge,
                        contentDescription = null,
                        tint = MutedForegroundDark
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                visualTransformation = CpfMaskTransformation()
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
                    IconButton(onClick = onPasswordVisibilityToggle) {
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
                trailingIcon = {
                    IconButton(onClick = onConfirmPasswordVisibilityToggle) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Ocultar senha" else "Mostrar senha",
                            tint = MutedForegroundDark
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
        }
    }
}

// Composable para Etapa 2: Endereço
@Composable
private fun AddressStep(
    uiState: com.tadevolta.gym.ui.viewmodels.OnboardingSharedUiState,
    viewModel: OnboardingSharedViewModel,
    locationHelper: LocationHelper,
    locationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    scrollState: ScrollState
) {
    // Animação discreta para o botão de localização
    val infiniteTransition = rememberInfiniteTransition(label = "button_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Scroll automático para o topo quando entrar no step 2
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300) // Pequeno delay para garantir que a UI foi renderizada
        // Animação suave de scroll para o topo
        val target = 0
        val current = scrollState.value
        val distance = target - current
        val steps = 20
        val stepSize = distance / steps
        repeat(steps) {
            scrollState.scrollTo((current + stepSize * (it + 1)).toInt())
            kotlinx.coroutines.delay(10)
        }
        scrollState.scrollTo(0)
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Endereço",
            style = MaterialTheme.typography.titleLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
        
        // Botão para usar localização com animação discreta
        OutlinedButton(
            onClick = {
                if (locationHelper.hasLocationPermission()) {
                    viewModel.loadLocationAndFillAddress(locationHelper)
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            enabled = !uiState.isLoadingLocation,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PurplePrimary
            )
        ) {
            if (uiState.isLoadingLocation) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = PurplePrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Obtendo localização...")
            } else {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Usar minha localização")
            }
        }
        
        // Erro de localização
        uiState.locationError?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Destructive
                )
            )
        }
        
        // Endereço
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "ENDEREÇO",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            OutlinedTextField(
                value = uiState.address,
                onValueChange = { viewModel.updateAddress(it) },
                placeholder = { Text("Rua, número", color = MutedForegroundDark) },
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
                        Icons.Default.Home,
                        contentDescription = null,
                        tint = MutedForegroundDark
                    )
                },
                singleLine = true
            )
        }
        
        // Complemento
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "COMPLEMENTO",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            OutlinedTextField(
                value = uiState.complement,
                onValueChange = { viewModel.updateComplement(it) },
                placeholder = { Text("Apto, bloco, etc. (opcional)", color = MutedForegroundDark) },
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
                singleLine = true
            )
        }
        
        // Bairro
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "BAIRRO",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            OutlinedTextField(
                value = uiState.neighborhood,
                onValueChange = { viewModel.updateNeighborhood(it) },
                placeholder = { Text("Nome do bairro", color = MutedForegroundDark) },
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
                singleLine = true
            )
        }
        
        // Cidade e Estado em linha
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cidade
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CIDADE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = uiState.city,
                    onValueChange = { viewModel.updateCity(it) },
                    placeholder = { Text("Cidade", color = MutedForegroundDark) },
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
                    singleLine = true
                )
            }
            
            // Estado (UF)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ESTADO (UF)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = uiState.state,
                    onValueChange = { viewModel.updateState(it) },
                    placeholder = { Text("UF", color = MutedForegroundDark) },
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
                    singleLine = true
                )
            }
        }
        
        // CEP e Número Local em linha
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // CEP
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CEP",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = uiState.zipCode,
                    onValueChange = { viewModel.updateZipCode(it) },
                    placeholder = { Text("00000-000", color = MutedForegroundDark) },
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            
            // Número Local
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NÚMERO LOCAL",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = uiState.localNumber,
                    onValueChange = { viewModel.updateLocalNumber(it) },
                    placeholder = { Text("Número", color = MutedForegroundDark) },
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
    }
}

// Composable para Etapa 3: Revisão
@Composable
private fun ReviewStep(
    uiState: com.tadevolta.gym.ui.viewmodels.OnboardingSharedUiState,
    viewModel: OnboardingSharedViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Revisão dos Dados",
            style = MaterialTheme.typography.titleLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
        
        Text(
            text = "Revise suas informações antes de criar a conta",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MutedForegroundDark
            )
        )
        
        // Card com dados pessoais
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Dados Pessoais",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                ReviewItem("Nome", uiState.name)
                ReviewItem("E-mail", uiState.email)
            }
        }
        
        // Card com endereço
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Endereço",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                ReviewItem("Endereço", uiState.address)
                if (uiState.complement.isNotBlank()) {
                    ReviewItem("Complemento", uiState.complement)
                }
                ReviewItem("Bairro", uiState.neighborhood)
                ReviewItem("Cidade", uiState.city)
                ReviewItem("Estado", uiState.state)
                ReviewItem("CEP", uiState.zipCode)
                ReviewItem("Número Local", uiState.localNumber)
            }
        }
    }
}

@Composable
private fun ReviewItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MutedForegroundDark
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

