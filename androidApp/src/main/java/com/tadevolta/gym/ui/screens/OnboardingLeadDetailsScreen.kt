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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.LeadType
import com.tadevolta.gym.ui.viewmodels.OnboardingSharedViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingLeadDetailsScreen(
    viewModel: OnboardingSharedViewModel = hiltViewModel(),
    onStudentSelected: () -> Unit = {},
    onGymLeadSubmitted: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    val averageStudentsOptions = listOf("0-50", "51-100", "101-200", "201-500", "500+")
    var showAverageStudentsDropdown by remember { 
        mutableStateOf(false) 
    }
    
    // Se veio de "Indique sua Academia", marcar flag e sincronizar endereço manual
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (uiState.showContinueWithoutUnit && uiState.manualAddress.isNotBlank()) {
            viewModel.setLeadManualAddress(uiState.manualAddress)
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
                ProgressIndicator(currentStep = 4, totalSteps = 4)
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
                        description = "Quero usar o aplicativo",
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

            // Aviso quando não há unidade selecionada
            if (uiState.selectedUnitId == null && uiState.leadType == LeadType.STUDENT) {
                item {
                    WarningCard(
                        title = "Atenção",
                        message = "Sem uma academia selecionada, você não terá acompanhamento profissional de treinadores. Recomendamos selecionar uma unidade próxima para melhor experiência.",
                        type = WarningType.WARNING,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Campos condicionais para Academia (ou quando veio de "Indique sua Academia" e escolheu "Sou Aluno")
            if (uiState.leadType == LeadType.GYM || (uiState.leadType == LeadType.STUDENT && uiState.selectedUnit == null)) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Mensagem explicativa quando é aluno mas precisa preencher dados da academia
                        if (uiState.leadType == LeadType.STUDENT && uiState.selectedUnitId == null) {
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
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = PurplePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Informações da Academia",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "Preencha os dados da academia onde você deseja se matricular para que possamos entrar em contato.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MutedForegroundDark
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Nome da Academia
                        OutlinedTextField(
                            value = uiState.selectedUnit?.name ?: uiState.gymName,
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

                        // Se veio de indicação e é aluno, mostrar apenas campos essenciais
                        if (uiState.leadType == LeadType.STUDENT && uiState.cameFromWithoutUnit) {
                            // Responsável da Academia
                            OutlinedTextField(
                                value = uiState.responsibleName,
                                onValueChange = { viewModel.updateResponsibleName(it) },
                                label = { Text("Responsável da Academia") },
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

                            // Endereço
                            OutlinedTextField(
                                value = uiState.selectedUnit?.address ?: uiState.leadAddress,
                                onValueChange = { viewModel.updateLeadAddress(it) },
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

                            // CEP e Bairro
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.leadZipCode,
                                    onValueChange = { viewModel.updateLeadZipCode(it) },
                                    label = { Text("CEP") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = KeyboardType.Number
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

                                OutlinedTextField(
                                    value = uiState.leadNeighborhood,
                                    onValueChange = { viewModel.updateLeadNeighborhood(it) },
                                    label = { Text("Bairro") },
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

                            // Estado e Cidade (Dropdowns)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Estado Dropdown
                                var showStateDropdown by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = showStateDropdown,
                                    onExpandedChange = { showStateDropdown = !showStateDropdown },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.leadState,
                                        onValueChange = { },
                                        label = { Text("Estado") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = showStateDropdown
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
                                        expanded = showStateDropdown,
                                        onDismissRequest = { showStateDropdown = false },
                                        modifier = Modifier.background(CardDark)
                                    ) {
                                        uiState.states.forEach { state ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = "${state.name} (${state.uf})",
                                                        color = Color.White
                                                    )
                                                },
                                                onClick = {
                                                    viewModel.updateSelectedState(state.id)
                                                    showStateDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Cidade Dropdown
                                var showCityDropdown by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = showCityDropdown,
                                    onExpandedChange = { showCityDropdown = !showCityDropdown },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.leadCity,
                                        onValueChange = { },
                                        label = { Text("Cidade") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        readOnly = true,
                                        enabled = uiState.selectedStateId != null,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = showCityDropdown
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
                                        expanded = showCityDropdown,
                                        onDismissRequest = { showCityDropdown = false },
                                        modifier = Modifier.background(CardDark)
                                    ) {
                                        if (uiState.cities.isEmpty()) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = "Selecione um estado primeiro",
                                                        color = MutedForegroundDark
                                                    )
                                                },
                                                onClick = { showCityDropdown = false }
                                            )
                                        } else {
                                            uiState.cities.forEach { city ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = city.name,
                                                            color = Color.White
                                                        )
                                                    },
                                                    onClick = {
                                                        viewModel.updateSelectedCity(city.id)
                                                        showCityDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Formulário completo para Academia ou quando não veio de indicação
                            // Email
                            OutlinedTextField(
                                value = uiState.leadEmail,
                                onValueChange = { viewModel.updateLeadEmail(it) },
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
                                value = uiState.selectedUnit?.let { it.owner.phone }
                                    ?: uiState.leadPhone,
                                onValueChange = { viewModel.updateLeadPhone(it) },
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
                                value = uiState.selectedUnit?.address ?: uiState.leadAddress,
                                onValueChange = { viewModel.updateLeadAddress(it) },
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
                                    value = uiState.selectedUnit?.city ?: uiState.leadCity,
                                    onValueChange = { viewModel.updateLeadCity(it) },
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
                                    value = uiState.selectedUnit?.state ?: uiState.leadState,
                                    onValueChange = { viewModel.updateLeadState(it) },
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

                            // Média de Alunos Matriculados (Dropdown) - apenas para Academia
                            if (uiState.leadType == LeadType.GYM) {
                                ExposedDropdownMenuBox(
                                    expanded = showAverageStudentsDropdown,
                                    onExpandedChange = {
                                        showAverageStudentsDropdown = !showAverageStudentsDropdown
                                    }
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
                }
            }

            // Campos adicionais para aluno (apenas se não veio de indicação)
            if (uiState.leadType == LeadType.STUDENT && !uiState.cameFromWithoutUnit) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Divisor
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MutedForegroundDark.copy(alpha = 0.3f)
                        )

                        Text(
                            text = "Informações de Condicionamento Físico",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )

                        // Nível de Condicionamento Físico
                        var showFitnessLevelDropdown by remember { mutableStateOf(false) }
                        val fitnessLevelOptions =
                            listOf("Iniciante", "Intermediário", "Avançado")
                        val fitnessLevelMap = mapOf(
                            "Iniciante" to com.tadevolta.gym.data.models.FitnessLevel.BEGINNER,
                            "Intermediário" to com.tadevolta.gym.data.models.FitnessLevel.INTERMEDIATE,
                            "Avançado" to com.tadevolta.gym.data.models.FitnessLevel.ADVANCED
                        )
                        val selectedFitnessLevelText =
                            uiState.fitnessLevel?.let { level ->
                                fitnessLevelMap.entries.find { it.value == level }?.key
                                    ?: ""
                            } ?: ""

                        ExposedDropdownMenuBox(
                            expanded = showFitnessLevelDropdown,
                            onExpandedChange = {
                                showFitnessLevelDropdown = !showFitnessLevelDropdown
                            }
                        ) {
                            OutlinedTextField(
                                value = selectedFitnessLevelText,
                                onValueChange = { },
                                label = { Text("Nível de Condicionamento Físico") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = showFitnessLevelDropdown
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
                                expanded = showFitnessLevelDropdown,
                                onDismissRequest = { showFitnessLevelDropdown = false },
                                modifier = Modifier.background(CardDark)
                            ) {
                                fitnessLevelOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                color = Color.White
                                            )
                                        },
                                        onClick = {
                                            fitnessLevelMap[option]?.let {
                                                viewModel.updateFitnessLevel(
                                                    it
                                                )
                                            }
                                            showFitnessLevelDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Contato de Emergência (Opcional)
                        Text(
                            text = "Contato de Emergência (Opcional)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = uiState.emergencyContactName ?: "",
                            onValueChange = {
                                viewModel.updateEmergencyContact(
                                    name = it,
                                    phone = uiState.emergencyContactPhone ?: "",
                                    relationship = uiState.emergencyContactRelationship
                                        ?: ""
                                )
                            },
                            label = { Text("Nome do Contato") },
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

                        OutlinedTextField(
                            value = uiState.emergencyContactPhone ?: "",
                            onValueChange = {
                                viewModel.updateEmergencyContact(
                                    name = uiState.emergencyContactName ?: "",
                                    phone = it,
                                    relationship = uiState.emergencyContactRelationship
                                        ?: ""
                                )
                            },
                            label = { Text("Telefone do Contato") },
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

                        OutlinedTextField(
                            value = uiState.emergencyContactRelationship ?: "",
                            onValueChange = {
                                viewModel.updateEmergencyContact(
                                    name = uiState.emergencyContactName ?: "",
                                    phone = uiState.emergencyContactPhone ?: "",
                                    relationship = it
                                )
                            },
                            label = { Text("Relação (Ex: Pai, Mãe, Cônjuge)") },
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
            
            // Botão Avançar para Cadastro
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GradientButton(
                        text = "Avançar para Cadastro",
                        onClick = {
                            coroutineScope.launch {
                                if (uiState.leadType == LeadType.STUDENT) {
                                   
                                    // Para alunos, não enviar lead agora.
                                    // O lead será enviado após o cadastro do usuário (SignUp)
                                    // para garantir que tenhamos os dados completos e o usuário criado.
                                    onStudentSelected()

                                    
                                } else if (uiState.leadType == LeadType.GYM) {
                                    // Se for academia, enviar lead
                                    when (val result = viewModel.submitLead()) {
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
                        text = "PASSO 4 DE 4",
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
