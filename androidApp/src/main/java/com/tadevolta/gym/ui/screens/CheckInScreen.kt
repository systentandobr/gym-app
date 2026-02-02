package com.tadevolta.gym.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.components.CheckInModalType
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.CheckInViewModel
import com.tadevolta.gym.utils.LocationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    viewModel: CheckInViewModel = hiltViewModel(),
    onNavigateToTrainingPlan: (studentId: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = uiState.checkInStats
    val history = uiState.checkInHistory
    val isCheckingIn = uiState.isCheckingIn
    val isValidatingLocation = uiState.isValidatingLocation
    val isOutOfRange = uiState.isOutOfRange
    val locationError = uiState.locationError
    val showModal = uiState.showModal
    val modalType = uiState.modalType
    val modalMessage = uiState.modalMessage
    
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    val coroutineScope = rememberCoroutineScope()
    var showLocationPermissionModal by remember { mutableStateOf(false) }
    
    // Launcher para permissão de localização
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        showLocationPermissionModal = false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Permissão concedida, não precisa fazer nada aqui
            // A validação será feita quando o usuário clicar em "Fazer Check-in"
        }
    }
    
    // Verificar permissão e mostrar modal automaticamente se necessário
    LaunchedEffect(Unit) {
        if (!locationHelper.hasLocationPermission()) {
            // Mostrar modal de permissão após um pequeno delay
            kotlinx.coroutines.delay(1000)
            if (!locationHelper.hasLocationPermission()) {
                showLocationPermissionModal = true
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check-in") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Contador 10/365
                CheckInCounter(
                    current = stats?.checkInsLast365Days ?: 0,
                    total = 365,
                    label = "Check-ins nos últimos 365 dias"
                )
            }
            
            item {
                // Recorde atual
                StreakCard(
                    current = stats?.currentStreak ?: 0,
                    longest = stats?.longestStreak ?: 0
                )
            }
            
            item {
                Text(
                    text = "Histórico",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            items(history.size) { index ->
                CheckInHistoryItem(checkIn = history[index])
            }
            
            item {
                // Botão de check-in
                GradientButton(
                    text = if (isValidatingLocation) "Validando localização..." else "Fazer Check-in",
                    onClick = { viewModel.performCheckIn() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCheckingIn && !isValidatingLocation
                )
            }
            
            // Mensagem de erro de localização
            if (locationError != null && !isOutOfRange) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Text(
                            text = locationError,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Destructive
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
        
        // Modal de erro "Fora de Alcance"
        if (isOutOfRange) {
            CheckInErrorModal(
                unitName = uiState.selectedUnitName,
                onRetry = { viewModel.retryLocationValidation() },
                onDismiss = { viewModel.clearLocationError() }
            )
        }
        
        // Modal de permissão de localização
        if (showLocationPermissionModal) {
            LocationPermissionModal(
                onAllowAccess = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onDismiss = {
                    showLocationPermissionModal = false
                }
            )
        }
        
        // Modal de resultado do check-in
        if (showModal && modalType != null) {
            CheckInResultModal(
                modalType = modalType,
                message = modalMessage,
                onConfirm = {
                    viewModel.dismissModal()
                    if (modalType == CheckInModalType.SUCCESS) {
                        // Obter studentId para navegação - usar método do ViewModel
                        coroutineScope.launch {
                            viewModel.getStudentIdForNavigation { studentId ->
                                onNavigateToTrainingPlan(studentId)
                            }
                        }
                    }
                },
                onDismiss = {
                    viewModel.dismissModal()
                }
            )
        }
    }
}

@Composable
private fun LocationPermissionModal(
    onAllowAccess: () -> Unit,
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
                // Ícone de localização com gradiente
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
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                
                // Título
                Text(
                    text = "Permitir Localização",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                
                // Descrição
                Text(
                    text = "Precisamos da sua localização para validar que você está na unidade da academia e permitir o check-in.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedForegroundDark
                    ),
                    textAlign = TextAlign.Center
                )
                
                // Botão Permitir Acesso
                GradientButton(
                    text = "Permitir Acesso",
                    onClick = onAllowAccess,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Link "Agora não"
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Agora não",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
        }
    }
}
