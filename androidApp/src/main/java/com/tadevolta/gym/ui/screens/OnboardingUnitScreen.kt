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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.OnboardingViewModel
import com.tadevolta.gym.utils.LocationHelper

@Composable
fun OnboardingUnitScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNext: (String, String) -> Unit = { _, _ -> },
    onNavigateToSignUp: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredUnits = uiState.filteredUnits
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
            // Buscar localização atual em uma coroutine
            coroutineScope.launch {
                val location = locationHelper.getCurrentLocation()
                location?.let {
                    viewModel.updateLocation(it.latitude, it.longitude)
                } ?: run {
                    // Se não conseguir obter localização, tentar carregar sem coordenadas
                    viewModel.loadNearbyUnits()
                }
            }
        }
    }
    
    // Carregar unidades sem localização se não tiver permissão
    LaunchedEffect(Unit) {
        if (!locationHelper.hasLocationPermission()) {
            // Carregar unidades sem coordenadas
            viewModel.loadNearbyUnits()
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
                ProgressIndicator(currentStep = 1, totalSteps = 3)
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
            
            // Campo de busca
            item {
                Box(
                    modifier = if (uiState.requiresLocation) {
                        Modifier.clickable { showLocationPermissionModal = true }
                    } else {
                        Modifier
                    }
                ) {
                    SearchTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = if (uiState.requiresLocation) {
                            "Clique aqui para permitir localização..."
                        } else {
                            "Buscar academia ou box..."
                        },
                        onLocationClick = {
                            if (!locationHelper.hasLocationPermission()) {
                                showLocationPermissionModal = true
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
                    )
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
            
            // Erro - se requer localização, mostrar card especial
            if (uiState.error != null && !uiState.isLoading) {
                if (uiState.requiresLocation) {
                    // Card especial para erro de localização
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
                                    uiState.error?.let { error ->
                                        Text(
                                            text = error,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MutedForegroundDark
                                            )
                                        )
                                    }
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
                } else {
                    // Erro genérico
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
            if (!uiState.isLoading && uiState.error == null) {
                if (filteredUnits.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhuma academia ou unidade encontrada",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MutedForegroundDark
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
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
                            uiState.selectedUnit?.let { unit ->
                                // Passar unitId e unitName para próxima tela
                                onNext(unit.unitId, unit.name)
                            }
                        },
                        enabled = uiState.selectedUnit != null,
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
                        text = "PASSO 1 DE 3",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MutedForegroundDark
                        )
                    )
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
