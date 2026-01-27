@file:OptIn(ExperimentalMaterial3Api::class)

package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tadevolta.gym.data.models.Gender
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.utils.DateMaskTransformation
import com.tadevolta.gym.ui.utils.formatDateInput
import com.tadevolta.gym.ui.viewmodels.ProfileViewModel

import androidx.compose.foundation.clickable

@Composable
fun PersonalDataScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val user by viewModel.user.collectAsState()
    val student by viewModel.student.collectAsState()
    
    // Estados locais para edição
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<Gender?>(null) }
    var showGenderDropdown by remember { mutableStateOf(false) }
    
    val genderOptions = listOf("Masculino", "Feminino", "Outro")
    val genderMap = mapOf(
        "Masculino" to Gender.MALE,
        "Feminino" to Gender.FEMALE,
        "Outro" to Gender.OTHER
    )
    val selectedGenderText = gender?.let { g ->
        genderMap.entries.find { it.value == g }?.key ?: ""
    } ?: ""
    
    // Inicializar estados com dados do usuário e student quando carregados
    LaunchedEffect(user, student) {
        user?.let { u ->
            if (name.isBlank()) name = u.name
            if (email.isBlank()) email = u.email
            if (phone.isBlank()) phone = u.phone ?: ""
        }
        student?.let { s ->
            if (birthDate.isBlank()) {
                // Converter de ISO (YYYY-MM-DD) para apenas dígitos (DDMMAAAA)
                s.birthDate?.let { isoDate ->
                    if (isoDate.contains("-")) {
                        val parts = isoDate.split("-")
                        if (parts.size == 3) {
                            // Converter de YYYY-MM-DD para DDMMAAAA
                            birthDate = "${parts[2]}${parts[1]}${parts[0]}"
                        } else {
                            // Se não está no formato esperado, extrair apenas dígitos
                            birthDate = isoDate.filter { it.isDigit() }.take(8)
                        }
                    } else {
                        // Se não tem hífen, assumir que já está em formato de dígitos
                        birthDate = isoDate.filter { it.isDigit() }.take(8)
                    }
                }
            }
            if (gender == null) gender = s.gender
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Header com botão voltar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dados Pessoais",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            // Avatar (Visualização apenas por enquanto)
            Box {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            brush = purpleToPinkGradient(),
                            shape = CircleShape
                        )
                        .padding(4.dp)
                        .background(
                            color = CardDark,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.avatar != null) {
                        AsyncImage(
                            model = user?.avatar,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
                
                // Botão de editar avatar (stub)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PurplePrimary)
                        .clickable { /* TODO: Implementar upload de avatar */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Formulário
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Nome Completo
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "NOME COMPLETO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MutedForegroundDark,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
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
                
                // E-mail
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "E-MAIL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MutedForegroundDark,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                }
                
                // Telefone
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TELEFONE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MutedForegroundDark,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }
                
                // Data de Nascimento
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "DATA DE NASCIMENTO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MutedForegroundDark,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { 
                            // Remover formatação e manter apenas dígitos
                            val digitsOnly = it.filter { it.isDigit() }.take(8)
                            birthDate = digitsOnly
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
                
                // Gênero
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "GÊNERO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MutedForegroundDark,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    ExposedDropdownMenuBox(
                        expanded = showGenderDropdown,
                        onExpandedChange = { showGenderDropdown = !showGenderDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedGenderText,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Selecione seu gênero", color = MutedForegroundDark) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
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
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGenderDropdown)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = showGenderDropdown,
                            onDismissRequest = { showGenderDropdown = false }
                        ) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option,
                                            color = Color.White
                                        )
                                    },
                                    onClick = {
                                        genderMap[option]?.let { gender = it }
                                        showGenderDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botão Salvar
            GradientButton(
                text = "Salvar Alterações",
                onClick = {
                    viewModel.updateProfile(
                        name = name,
                        email = email,
                        phone = phone,
                        birthDate = birthDate.takeIf { it.isNotBlank() },
                        gender = gender
                    )
                    onBack() // Voltar após salvar (idealmente esperar sucesso, mas simplificado aqui)
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
