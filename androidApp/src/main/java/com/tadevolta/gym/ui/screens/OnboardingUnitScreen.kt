package com.tadevolta.gym.ui.screens

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.OnboardingSharedViewModel
import com.tadevolta.gym.utils.LocationHelper

@Composable
fun OnboardingUnitScreen(
    viewModel: OnboardingSharedViewModel = hiltViewModel(),
    onNext: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredUnits = uiState.filteredUnits
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    val coroutineScope = rememberCoroutineScope()
    var showLocationPermissionModal by remember { mutableStateOf(false) }
    
    // Não mostrar campo manual automaticamente - só após tentar buscar por localização
    
    // Launcher para permissão de localização
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        showLocationPermissionModal = false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Buscar localização atual em uma coroutine
            coroutineScope.launch {
                val location = locationHelper.getCurrentLocation()
                location?.let {
                    viewModel.updateLocation(it.latitude, it.longitude)
                }
                // Não carregar unidades sem coordenadas - a API requer lat/lng
            }
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
            return@LaunchedEffect
        } else {
            // Se já tiver permissão, buscar localização
            coroutineScope.launch {
                val location = locationHelper.getCurrentLocation()
                location?.let {
                    viewModel.updateLocation(it.latitude, it.longitude)
                }
            }
        }
    }
    
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
                ProgressIndicator(currentStep = 1, totalSteps = 4)
            }
            
            // Título
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Selecione sua",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    GradientText(
                        text = "Unidade",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Escolha sua academia para acompanhar seu progresso e seguir o Plano de treino recomendado.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
            
            // Campo de busca consolidado
            item {
                val placeholder = when {
                    !locationHelper.hasLocationPermission() && uiState.currentLatitude == null -> 
                        "Clique para permitir localização..."
                    uiState.units.isEmpty() && !uiState.isLoading -> 
                        "Digite um endereço para buscar unidades..."
                    else -> 
                        "Buscar academia ou box..."
                }
                
                SearchTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = placeholder,
                    isLoading = uiState.isGeocoding,
                    onLocationClick = {
                        if (!locationHelper.hasLocationPermission()) {
                            showLocationPermissionModal = true
                        } else {
                            coroutineScope.launch {
                                val location = locationHelper.getCurrentLocation()
                                location?.let {
                                    viewModel.updateLocation(it.latitude, it.longitude)
                                }
                            }
                        }
                    },
                    onSearchClick = {
                        if (uiState.searchQuery.isNotBlank()) {
                            coroutineScope.launch {
                                viewModel.searchAddressOrFilter(
                                    uiState.searchQuery,
                                    locationHelper
                                )
                            }
                        }
                    }
                )
            }
            
            // Card de Localização Necessária - mostrar logo no início se não tiver coordenadas
            if (!uiState.isLoading && 
                (uiState.currentLatitude == null || uiState.currentLongitude == null) &&
                (!locationHelper.hasLocationPermission() || uiState.requiresLocation)) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLocationPermissionModal = true },
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Localização Necessária",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Precisamos da sua localização para encontrar unidades próximas. Toque aqui para permitir o acesso.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MutedForegroundDark
                                    )
                                )
                            }
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Permitir acesso",
                                tint = PurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Loading
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PurplePrimary)
                    }
                }
            }
            
            // Erro genérico (não relacionado a localização)
            if (uiState.error != null && !uiState.isLoading && !uiState.requiresLocation) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Destructive,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Erro ao carregar unidades",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color.White
                                        )
                                    )
                                    uiState.error?.let { error ->
                                        Text(
                                            text = error,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MutedForegroundDark
                                            )
                                        )
                                    }
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(onClick = { 
                                    viewModel.loadNearbyUnits() 
                                }) {
                                    Text("Tentar novamente", color = PurplePrimary)
                                }
                            }
                        }
                    }
                }
            }
            
            // Opção de continuar sem unidade
            if (!uiState.isLoading && uiState.units.isEmpty() && uiState.showContinueWithoutUnit && uiState.hasTriedLocationSearch) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Não encontramos unidades próximas",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Você pode continuar sem selecionar uma unidade e fornecer os dados da sua academia para captura de lead.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MutedForegroundDark
                                        )
                                    )
                                }
                            }
                            GradientButton(
                                text = "Seguir sem Unidade",
                                onClick = {
                                    // Navegar para a próxima etapa do onboarding (Goal)
                                    onNext()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            
            // Card de sugestão de localização (apenas se não tiver permissão)
            if (!uiState.locationPermissionGranted && !uiState.isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "SUGESTÕES PRÓXIMAS",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Ative a localização para melhores resultados",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MutedForegroundDark
                                        )
                                    )
                                }
                            }
                            GradientButton(
                                text = "Permitir Acesso",
                                onClick = {
                                    showLocationPermissionModal = true
                                },
                                modifier = Modifier.width(140.dp)
                            )
                        }
                    }
                }
            }
            
            // Lista de unidades
            if (!uiState.isLoading && uiState.error == null && uiState.hasTriedLocationSearch) {
                if (!filteredUnits.isEmpty()) {
                    items(filteredUnits) { unit ->
                    SelectableCard(
                        selected = uiState.selectedUnit?.id == unit.id,
                        onClick = { viewModel.selectUnit(unit) }
                    ) {
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
                                    .size(48.dp)
                                    .background(
                                        color = PurplePrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            // Informações
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = unit.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${unit.address}, ${unit.city} - ${unit.state}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MutedForegroundDark
                                    )
                                )
                                Text(
                                    text = "a ${unit.distance} de distância",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = PurplePrimary
                                    )
                                )
                                if (unit.tags.isNotEmpty()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        unit.tags.forEach { tag ->
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = CardDarker,
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = tag,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color.White
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
            
            // Botão Confirmar
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GradientButton(
                        text = "Próxima Etapa",
                        onClick = {
                            // Dados já estão no ViewModel, apenas navegar
                            onNext()
                        },
                        enabled = uiState.selectedUnit != null || uiState.showContinueWithoutUnit,
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
                        text = "PASSO 1 DE 4",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                    
                    // Link para usuários que já têm login
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Já tenho uma conta",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PurplePrimary
                            )
                        )
                    }
                }
            }
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                // Descrição
                Text(
                    text = "Precisamos da sua localização para encontrar a unidade da academia mais próxima de você e personalizar sua experiência.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedForegroundDark
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
