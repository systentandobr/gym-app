@file:OptIn(ExperimentalMaterial3Api::class)

package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.LeadType
import com.tadevolta.gym.ui.viewmodels.OnboardingLeadViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingLeadDetailsScreen(
    viewModel: OnboardingLeadViewModel = hiltViewModel(),
    unitId: String? = null,
    unitName: String? = null,
    goal: String? = null,
    onStudentSelected: (String?, String?, String?) -> Unit = { _, _, _ -> },
    onGymLeadSubmitted: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    val averageStudentsOptions = listOf("0-50", "51-100", "101-200", "201-500", "500+")
    var showAverageStudentsDropdown by remember { 
        mutableStateOf(false) 
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
                ProgressIndicator(currentStep = 3, totalSteps = 3)
            }
            
            // Título
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Você é um",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    GradientText(
                        text = "Aluno ou Nova Academia?",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Selecione sua categoria para continuar com o cadastro.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
            }
            
            // Cards de seleção de tipo
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card "Sou Aluno"
                    LeadTypeCard(
                        title = "Sou Aluno",
                        description = "Quero me matricular",
                        icon = Icons.Default.Person,
                        selected = uiState.leadType == LeadType.STUDENT,
                        onClick = { viewModel.selectLeadType(LeadType.STUDENT) },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Card "Sou uma Academia"
                    LeadTypeCard(
                        title = "Sou uma Academia",
                        description = "Quero me cadastrar",
                        icon = Icons.Default.Business,
                        selected = uiState.leadType == LeadType.GYM,
                        onClick = { viewModel.selectLeadType(LeadType.GYM) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Campos condicionais para Academia
            if (uiState.leadType == LeadType.GYM) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Nome da Academia
                        OutlinedTextField(
                            value = uiState.gymName,
                            onValueChange = { viewModel.updateGymName(it) },
                            label = { Text("Nome da Academia") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = MutedForegroundDark,
                                focusedLabelColor = PurplePrimary,
                                unfocusedLabelColor = MutedForegroundDark
                            ),
                            singleLine = true
                        )
                        
                        // Email
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.updateEmail(it) },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Email
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = MutedForegroundDark,
                                focusedLabelColor = PurplePrimary,
                                unfocusedLabelColor = MutedForegroundDark
                            ),
                            singleLine = true
                        )
                        
                        // Telefone
                        OutlinedTextField(
                            value = uiState.phone,
                            onValueChange = { viewModel.updatePhone(it) },
                            label = { Text("Telefone") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Phone
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = MutedForegroundDark,
                                focusedLabelColor = PurplePrimary,
                                unfocusedLabelColor = MutedForegroundDark
                            ),
                            singleLine = true
                        )
                        
                        // Endereço
                        OutlinedTextField(
                            value = uiState.address,
                            onValueChange = { viewModel.updateAddress(it) },
                            label = { Text("Endereço") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = MutedForegroundDark,
                                focusedLabelColor = PurplePrimary,
                                unfocusedLabelColor = MutedForegroundDark
                            ),
                            singleLine = true
                        )
                        
                        // Cidade e Estado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.city,
                                onValueChange = { viewModel.updateCity(it) },
                                label = { Text("Cidade") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = MutedForegroundDark,
                                    focusedLabelColor = PurplePrimary,
                                    unfocusedLabelColor = MutedForegroundDark
                                ),
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = uiState.state,
                                onValueChange = { viewModel.updateState(it) },
                                label = { Text("Estado (UF)") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = MutedForegroundDark,
                                    focusedLabelColor = PurplePrimary,
                                    unfocusedLabelColor = MutedForegroundDark
                                ),
                                singleLine = true
                            )
                        }
                        
                        // Média de Alunos Matriculados (Dropdown)
                        ExposedDropdownMenuBox(
                            expanded = showAverageStudentsDropdown,
                            onExpandedChange = { showAverageStudentsDropdown = !showAverageStudentsDropdown }
                        ) {
                            OutlinedTextField(
                                value = uiState.averageStudents ?: "",
                                onValueChange = { },
                                label = { Text("Média de Alunos Matriculados") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = showAverageStudentsDropdown
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = MutedForegroundDark,
                                    focusedLabelColor = PurplePrimary,
                                    unfocusedLabelColor = MutedForegroundDark
                                )
                            )
                            
                            ExposedDropdownMenu(
                                expanded = showAverageStudentsDropdown,
                                onDismissRequest = { showAverageStudentsDropdown = false },
                                modifier = Modifier.background(CardDark)
                            ) {
                                averageStudentsOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                color = Color.White
                                            )
                                        },
                                        onClick = {
                                            viewModel.updateAverageStudents(option)
                                            showAverageStudentsDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Mensagem de erro
            uiState.error?.let { error ->
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
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Destructive
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Botão Finalizar Cadastro
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GradientButton(
                        text = "Finalizar Cadastro",
                        onClick = {
                            coroutineScope.launch {
                                if (uiState.leadType == LeadType.STUDENT) {
                                    // Se for aluno, apenas navegar para SignUp
                                    onStudentSelected(unitId, unitName, goal)
                                } else if (uiState.leadType == LeadType.GYM) {
                                    // Se for academia, enviar lead
                                    when (val result = viewModel.submitLead(unitId, unitName, goal)) {
                                        is com.tadevolta.gym.data.models.Result.Success -> {
                                            onGymLeadSubmitted()
                                        }
                                        is com.tadevolta.gym.data.models.Result.Error -> {
                                            // Erro já está no estado
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        },
                        enabled = uiState.leadType != null && !uiState.isLoading,
                        icon = {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )
                    Text(
                        text = "PASSO 3 DE 3",
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
fun LeadTypeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .then(
                if (selected) {
                    Modifier.border(
                        width = 2.dp,
                        color = BorderPurple,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = CardDark
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = PurplePrimary.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MutedForegroundDark
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            // Checkmark quando selecionado
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            brush = purpleToPinkGradient(),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selecionado",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
