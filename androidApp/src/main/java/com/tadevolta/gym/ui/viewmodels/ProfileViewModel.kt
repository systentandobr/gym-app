package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.GamificationData
import com.tadevolta.gym.data.models.Student
import com.tadevolta.gym.data.models.User
import com.tadevolta.gym.data.remote.GamificationService
import com.tadevolta.gym.data.remote.StudentService
import com.tadevolta.gym.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gamificationService: GamificationService,
    private val studentService: StudentService
) : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _student = MutableStateFlow<Student?>(null)
    val student: StateFlow<Student?> = _student.asStateFlow()
    
    // studentId derivado do user ou student
    val studentId: StateFlow<String?> = _user.map { user -> 
        user?.id // Usar id do usuário como studentId
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    private val _gamificationData = MutableStateFlow<GamificationData?>(null)
    val gamificationData: StateFlow<GamificationData?> = _gamificationData.asStateFlow()
    
    init {
        loadProfileData()
    }
    
    private fun loadProfileData() {
        viewModelScope.launch {
            // Carregar usuário atual
            when (val result = authRepository.getCurrentUser()) {
                is com.tadevolta.gym.data.models.Result.Success -> {
                    _user.value = result.data
                    
                    // Carregar dados do Student
                    when (val studentResult = studentService.getStudentByUserId(result.data.id)) {
                        is com.tadevolta.gym.data.models.Result.Success -> {
                            _student.value = studentResult.data
                        }
                        else -> {
                            // Se não encontrou student, não é erro crítico
                            _student.value = null
                        }
                    }
                    
                    // Carregar dados de gamificação
                    when (val gamificationResult = gamificationService.getGamificationData(result.data.id)) {
                        is com.tadevolta.gym.data.models.Result.Success -> {
                            _gamificationData.value = gamificationResult.data
                        }
                        else -> {}
                    }
                }
                else -> {}
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _user.value = null
            _student.value = null
            _gamificationData.value = null
        }
    }

    fun updateProfile(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        birthDate: String? = null,
        gender: com.tadevolta.gym.data.models.Gender? = null
    ) {
        viewModelScope.launch {
            // Atualizar dados do User
            val updateData = com.tadevolta.gym.data.models.UpdateUserData(
                // name não existe, mapear para firstName/lastName se possível ou usar username?
                // Backend usa username e email. Phone também.
                // Se name for passado, vamos assumir que é para atualizar firstName (simplificação)
                firstName = name?.split(" ")?.firstOrNull(),
                lastName = name?.split(" ")?.drop(1)?.joinToString(" "),
                email = email,
                phone = phone
            )
            
            when (val result = authRepository.updateProfile(updateData)) {
                is com.tadevolta.gym.data.models.Result.Success -> {
                    _user.value = result.data
                }
                else -> {
                    // TODO: Handle error
                }
            }
            
            // Atualizar dados do Student se necessário
            val student = _student.value
            if (student != null && (birthDate != null || gender != null || phone != null)) {
                // Converter birthDate para ISO 8601 se necessário
                val formattedBirthDate = birthDate?.takeIf { it.isNotBlank() }?.let { date ->
                    // Se já está em formato ISO, usar como está
                    if (date.contains("-")) {
                        date
                    } else {
                        // Extrair apenas dígitos e converter para AAAA-MM-DD
                        val digits = date.filter { it.isDigit() }
                        if (digits.length == 8) {
                            val day = digits.substring(0, 2)
                            val month = digits.substring(2, 4)
                            val year = digits.substring(4, 8)
                            "$year-$month-$day"
                        } else {
                            // Tentar formato DD/MM/AAAA como fallback
                            val parts = date.split("/")
                            if (parts.size == 3) {
                                "${parts[2]}-${parts[1]}-${parts[0]}"
                            } else {
                                date
                            }
                        }
                    }
                }
                
                when (val studentResult = studentService.updateStudent(
                    studentId = student.id,
                    birthDate = formattedBirthDate,
                    gender = gender,
                    phone = phone
                )) {
                    is com.tadevolta.gym.data.models.Result.Success -> {
                        _student.value = studentResult.data
                    }
                    else -> {
                        // TODO: Handle error
                    }
                }
            }
        }
    }
}
