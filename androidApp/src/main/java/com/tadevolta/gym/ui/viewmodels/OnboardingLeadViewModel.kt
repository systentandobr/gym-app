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
    val error: String? = null,
    // Campos adicionais para "Seguir sem Unidade"
    val responsibleName: String = "",
    val responsiblePhone: String = "",
    val responsibleEmail: String = "",
    val manualAddress: String = "",
    val manualCoordinates: Pair<Double, Double>? = null,
    val cameFromWithoutUnit: Boolean = false // Flag para indicar que veio de "Seguir sem Unidade"
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
    
    fun updateResponsibleName(name: String) {
        _uiState.value = _uiState.value.copy(responsibleName = name)
    }
    
    fun updateResponsiblePhone(phone: String) {
        _uiState.value = _uiState.value.copy(responsiblePhone = phone)
    }
    
    fun updateResponsibleEmail(email: String) {
        _uiState.value = _uiState.value.copy(responsibleEmail = email)
    }
    
    fun setManualAddress(address: String, coordinates: Pair<Double, Double>? = null) {
        _uiState.value = _uiState.value.copy(
            manualAddress = address,
            manualCoordinates = coordinates,
            cameFromWithoutUnit = true,
            address = address // Pré-preencher endereço também
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Sincroniza dados do OnboardingViewModel
     * Deve ser chamado quando a tela OnboardingLeadDetailsScreen é exibida
     */
    fun syncFromOnboardingViewModel(onboardingViewModel: OnboardingViewModel) {
        val manualAddress = onboardingViewModel.uiState.value.manualAddress
        if (manualAddress.isNotBlank()) {
            setManualAddress(manualAddress)
        }
    }
    
    suspend fun submitLead(
        onboardingViewModel: OnboardingViewModel
    ): Result<LeadResponse> {
        val unitId = onboardingViewModel.getSelectedUnitId()
        val unitName = onboardingViewModel.getSelectedUnitName()
        val goal = onboardingViewModel.getSelectedGoal()
        
        return submitLead(unitId, unitName, goal)
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
        
        // Preparar metadata
        val metadata = buildMap<String, String> {
            unitName?.let { put("selectedUnitName", it) }
            put("address", state.address)
            state.averageStudents?.let { put("averageStudents", it) }
            goal?.let { put("goal", it) }
            
            // Campos adicionais quando veio de "Seguir sem Unidade"
            if (state.cameFromWithoutUnit) {
                if (state.responsibleName.isNotBlank()) {
                    put("responsibleName", state.responsibleName)
                }
                if (state.responsiblePhone.isNotBlank()) {
                    put("responsiblePhone", state.responsiblePhone)
                }
                if (state.responsibleEmail.isNotBlank()) {
                    put("responsibleEmail", state.responsibleEmail)
                }
                if (state.manualAddress.isNotBlank()) {
                    put("manualAddress", state.manualAddress)
                }
                state.manualCoordinates?.let { (lat, lng) ->
                    put("manualCoordinates", "$lat,$lng")
                }
            }
        }
        
        // Criar LeadRequest baseado no tipo de lead
        val leadRequest = when (state.leadType) {
            LeadType.GYM -> {
                // Lead de academia - usar dados da academia
                LeadRequest(
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
                            interestedInFranchise = false
                        )
                    },
                    metadata = metadata
                )
            }
            LeadType.STUDENT -> {
                // Lead de aluno - este método não deveria ser usado para alunos
                // Os dados do aluno devem ser enviados via OnboardingSharedViewModel.sendLeadAfterSignUp()
                // Mas mantendo compatibilidade: usar dados disponíveis
                LeadRequest(
                    name = state.responsibleName.takeIf { it.isNotBlank() } ?: "",
                    email = state.email,
                    phone = state.phone,
                    city = state.city,
                    state = state.state,
                    unitId = unitId,
                    marketSegment = "gym",
                    userType = "student",
                    objectives = goal?.let {
                        LeadObjectives(
                            primary = it,
                            secondary = emptyList(),
                            interestedInFranchise = false
                        )
                    },
                    metadata = metadata
                )
            }
            null -> {
                // Fallback: tratar como academia
                LeadRequest(
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
                            interestedInFranchise = false
                        )
                    },
                    metadata = metadata
                )
            }
        }
        
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
