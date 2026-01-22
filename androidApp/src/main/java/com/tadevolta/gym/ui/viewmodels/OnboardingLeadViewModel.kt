package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.data.remote.LeadService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LeadType {
    STUDENT, GYM
}

data class OnboardingLeadUiState(
    val leadType: LeadType? = null,
    val gymName: String = "",
    val email: String = "",
    val address: String = "",
    val phone: String = "",
    val city: String = "",
    val state: String = "",
    val averageStudents: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OnboardingLeadViewModel @Inject constructor(
    private val leadService: LeadService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OnboardingLeadUiState())
    val uiState: StateFlow<OnboardingLeadUiState> = _uiState.asStateFlow()
    
    fun selectLeadType(type: LeadType) {
        _uiState.value = _uiState.value.copy(leadType = type)
    }
    
    fun updateGymName(name: String) {
        _uiState.value = _uiState.value.copy(gymName = name)
    }
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }
    
    fun updateAddress(address: String) {
        _uiState.value = _uiState.value.copy(address = address)
    }
    
    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone)
    }
    
    fun updateCity(city: String) {
        _uiState.value = _uiState.value.copy(city = city)
    }
    
    fun updateState(state: String) {
        _uiState.value = _uiState.value.copy(state = state)
    }
    
    fun updateAverageStudents(range: String) {
        _uiState.value = _uiState.value.copy(averageStudents = range)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    suspend fun submitLead(
        unitId: String?,
        unitName: String?,
        goal: String?
    ): Result<LeadResponse> {
        val state = _uiState.value
        
        // Validar campos obrigatórios para academia
        if (state.leadType == LeadType.GYM) {
            if (state.gymName.isBlank()) {
                _uiState.value = state.copy(error = "Nome da academia é obrigatório")
                return Result.Error(Exception("Nome da academia é obrigatório"))
            }
            if (state.email.isBlank()) {
                _uiState.value = state.copy(error = "Email é obrigatório")
                return Result.Error(Exception("Email é obrigatório"))
            }
            if (state.phone.isBlank()) {
                _uiState.value = state.copy(error = "Telefone é obrigatório")
                return Result.Error(Exception("Telefone é obrigatório"))
            }
            if (state.address.isBlank()) {
                _uiState.value = state.copy(error = "Endereço é obrigatório")
                return Result.Error(Exception("Endereço é obrigatório"))
            }
            if (state.city.isBlank()) {
                _uiState.value = state.copy(error = "Cidade é obrigatória")
                return Result.Error(Exception("Cidade é obrigatória"))
            }
            if (state.state.isBlank()) {
                _uiState.value = state.copy(error = "Estado é obrigatório")
                return Result.Error(Exception("Estado é obrigatório"))
            }
            if (state.averageStudents == null) {
                _uiState.value = state.copy(error = "Média de alunos é obrigatória")
                return Result.Error(Exception("Média de alunos é obrigatória"))
            }
        }
        
        // Se for aluno, não enviar lead (apenas navegar para SignUp)
        if (state.leadType == LeadType.STUDENT) {
            return Result.Success(LeadResponse(
                id = "",
                unitId = unitId,
                name = "",
                email = "",
                phone = "",
                city = null,
                state = null,
                userType = "student",
                marketSegment = "gym",
                status = "new",
                createdAt = ""
            ))
        }
        
        // Preparar metadata
        val metadata = buildMap<String, String> {
            unitName?.let { put("selectedUnitName", it) }
            put("address", state.address)
            state.averageStudents?.let { put("averageStudents", it) }
            goal?.let { put("goal", it) }
        }
        
        // Criar LeadRequest
        val leadRequest = LeadRequest(
            name = state.gymName,
            email = state.email,
            phone = state.phone,
            city = state.city,
            state = state.state,
            unitId = unitId,
            marketSegment = "gym",
            userType = "franchise",
            objectives = goal?.let {
                LeadObjectives(
                    primary = it,
                    secondary = emptyList(),
                    interestedInFranchise = true
                )
            },
            metadata = metadata
        )
        
        _uiState.value = state.copy(isLoading = true, error = null)
        
        return when (val result = leadService.postLead(leadRequest)) {
            is Result.Success -> {
                _uiState.value = state.copy(isLoading = false)
                result
            }
            is Result.Error -> {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = result.exception.message ?: "Erro ao enviar lead"
                )
                result
            }
            else -> {
                _uiState.value = state.copy(isLoading = false)
                Result.Error(Exception("Erro desconhecido"))
            }
        }
    }
}
