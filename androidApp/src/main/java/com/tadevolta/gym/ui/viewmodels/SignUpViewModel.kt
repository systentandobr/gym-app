package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.data.remote.LeadService
import com.tadevolta.gym.data.repositories.AuthRepository
import com.tadevolta.gym.utils.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val unitId: String? = null,
    val unitName: String? = null,
    val goal: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignUpSuccessful: Boolean = false,
    // Campos para dados do lead quando vem de "Seguir sem Unidade"
    val cameFromWithoutUnit: Boolean = false,
    val gymName: String? = null,
    val gymAddress: String? = null,
    val gymPhone: String? = null,
    val gymCity: String? = null,
    val gymState: String? = null,
    val responsibleName: String? = null,
    val responsiblePhone: String? = null,
    val responsibleEmail: String? = null,
    val manualAddress: String? = null,
    val manualCoordinates: String? = null, // Formato "lat,lng"
    // Campos de endereço do usuário
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val neighborhood: String = "",
    val complement: String = "",
    val localNumber: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    // Sistema de etapas
    val currentStep: Int = 1,
    val totalSteps: Int = 3,
    // Flags de localização
    val isLoadingLocation: Boolean = false,
    val locationError: String? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val leadService: LeadService,
    private val locationHelper: LocationHelper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()
    
    /**
     * Sincroniza dados do OnboardingViewModel para o SignUpViewModel
     * Deve ser chamado quando a tela SignUp é exibida
     */
    fun syncFromOnboardingViewModel(onboardingViewModel: OnboardingViewModel) {
        val unitId = onboardingViewModel.getSelectedUnitId()
        val unitName = onboardingViewModel.getSelectedUnitName()
        val goal = onboardingViewModel.getSelectedGoal()
        
        if (unitId != null && unitName != null) {
            setSelectedUnit(unitId, unitName)
        }
        
        if (goal != null) {
            setGoal(goal)
        }
    }
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }
    
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, error = null)
    }
    
    fun setSelectedUnit(unitId: String, unitName: String) {
        _uiState.value = _uiState.value.copy(
            unitId = unitId,
            unitName = unitName
        )
    }
    
    fun setGoal(goal: String?) {
        _uiState.value = _uiState.value.copy(goal = goal)
    }
    
    fun setLeadData(
        gymName: String?,
        gymAddress: String?,
        gymPhone: String?,
        gymCity: String?,
        gymState: String?,
        responsibleName: String?,
        responsiblePhone: String?,
        responsibleEmail: String?,
        manualAddress: String?,
        manualCoordinates: String?
    ) {
        _uiState.value = _uiState.value.copy(
            cameFromWithoutUnit = true,
            gymName = gymName,
            gymAddress = gymAddress,
            gymPhone = gymPhone,
            gymCity = gymCity,
            gymState = gymState,
            responsibleName = responsibleName,
            responsiblePhone = responsiblePhone,
            responsibleEmail = responsibleEmail,
            manualAddress = manualAddress,
            manualCoordinates = manualCoordinates
        )
    }
    
    fun signUp() {
        val name = _uiState.value.name.trim()
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword
        val unitId = _uiState.value.unitId
        val unitName = _uiState.value.unitName
        
        // Validações
        when {
            name.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Nome é obrigatório")
                return
            }
            email.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "E-mail é obrigatório")
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.value = _uiState.value.copy(error = "E-mail inválido")
                return
            }
            password.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Senha é obrigatória")
                return
            }
            password.length < 6 -> {
                _uiState.value = _uiState.value.copy(error = "Senha deve ter pelo menos 6 caracteres")
                return
            }
            password != confirmPassword -> {
                _uiState.value = _uiState.value.copy(error = "As senhas não coincidem")
                return
            }
            // Validação de campos de endereço obrigatórios
            _uiState.value.address.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Endereço é obrigatório")
                return
            }
            _uiState.value.city.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Cidade é obrigatória")
                return
            }
            _uiState.value.state.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Estado é obrigatório")
                return
            }
            _uiState.value.zipCode.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "CEP é obrigatório")
                return
            }
            _uiState.value.neighborhood.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Bairro é obrigatório")
                return
            }
            _uiState.value.localNumber.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Número local é obrigatório")
                return
            }
            _uiState.value.latitude == null || _uiState.value.longitude == null -> {
                _uiState.value = _uiState.value.copy(error = "Localização é obrigatória. Use o botão 'Usar minha localização'")
                return
            }
            // Removida validação obrigatória de unitId - pode ser null quando vem de "Seguir sem Unidade"
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Determinar unitName: usar gymName se vier de "Seguir sem Unidade" e unitName estiver vazio
            val finalUnitName = when {
                !unitName.isNullOrBlank() -> unitName
                _uiState.value.cameFromWithoutUnit && !_uiState.value.gymName.isNullOrBlank() -> _uiState.value.gymName
                else -> "Sem Unidade" // Valor padrão quando não tem unidade
            }
            
            when (val result = authRepository.signUp(
                name = name,
                email = email,
                password = password,
                unitName = finalUnitName,
                unitId = unitId,
                address = _uiState.value.address,
                city = _uiState.value.city,
                state = _uiState.value.state,
                zipCode = _uiState.value.zipCode,
                neighborhood = _uiState.value.neighborhood,
                complement = _uiState.value.complement,
                localNumber = _uiState.value.localNumber,
                latitude = _uiState.value.latitude,
                longitude = _uiState.value.longitude
            )) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignUpSuccessful = true
                    )
                    
                    // Se veio de "Seguir sem Unidade", enviar lead após cadastro bem-sucedido
                    if (_uiState.value.cameFromWithoutUnit) {
                        sendLeadAfterSignUp(name, email)
                    }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Erro ao criar conta"
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erro desconhecido"
                    )
                }
            }
        }
    }
    
    private suspend fun sendLeadAfterSignUp(studentName: String, studentEmail: String) {
        val state = _uiState.value
        
        // Validar que temos dados mínimos para criar o lead
        if (state.gymName.isNullOrBlank() || state.gymPhone.isNullOrBlank()) {
            return // Não enviar lead se dados essenciais estiverem faltando
        }
        
        // Preparar metadata com todas as informações
        val metadata = buildMap<String, String> {
            state.gymAddress?.let { put("address", it) }
            state.manualAddress?.let { put("manualAddress", it) }
            state.manualCoordinates?.let { put("manualCoordinates", it) }
            state.responsibleName?.let { put("responsibleName", it) }
            state.responsiblePhone?.let { put("responsiblePhone", it) }
            state.responsibleEmail?.let { put("responsibleEmail", it) }
            state.goal?.let { put("goal", it) }
            put("source", "app-signup")
            put("studentName", studentName)
            put("studentEmail", studentEmail)
        }
        
        // Criar LeadRequest
        val leadRequest = LeadRequest(
            name = state.gymName ?: "",
            email = state.responsibleEmail ?: state.gymPhone, // Usar email do responsável ou telefone como fallback
            phone = state.gymPhone ?: "",
            city = state.gymCity,
            state = state.gymState,
            unitId = null, // Não tem unitId quando vem de "Seguir sem Unidade"
            marketSegment = "gym",
            userType = "student", // Aluno interessado em se matricular
            objectives = state.goal?.let {
                LeadObjectives(
                    primary = it,
                    secondary = emptyList(),
                    interestedInFranchise = false
                )
            },
            metadata = metadata
        )
        
        // Enviar lead (não bloquear se falhar)
        leadService.postLead(leadRequest)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    // Métodos de atualização de endereço
    fun updateAddress(address: String) {
        _uiState.value = _uiState.value.copy(address = address, error = null)
    }
    
    fun updateCity(city: String) {
        _uiState.value = _uiState.value.copy(city = city, error = null)
    }
    
    fun updateState(state: String) {
        _uiState.value = _uiState.value.copy(state = state, error = null)
    }
    
    fun updateZipCode(zipCode: String) {
        _uiState.value = _uiState.value.copy(zipCode = zipCode, error = null)
    }
    
    fun updateNeighborhood(neighborhood: String) {
        _uiState.value = _uiState.value.copy(neighborhood = neighborhood, error = null)
    }
    
    fun updateComplement(complement: String) {
        _uiState.value = _uiState.value.copy(complement = complement, error = null)
    }
    
    fun updateLocalNumber(localNumber: String) {
        _uiState.value = _uiState.value.copy(localNumber = localNumber, error = null)
    }
    
    // Métodos de navegação entre etapas
    fun nextStep() {
        val current = _uiState.value.currentStep
        if (current < _uiState.value.totalSteps && canProceedToNextStep()) {
            _uiState.value = _uiState.value.copy(
                currentStep = current + 1,
                error = null
            )
        }
    }
    
    fun previousStep() {
        val current = _uiState.value.currentStep
        if (current > 1) {
            _uiState.value = _uiState.value.copy(
                currentStep = current - 1,
                error = null
            )
        }
    }
    
    fun canProceedToNextStep(): Boolean {
        return validateStep(_uiState.value.currentStep)
    }
    
    fun validateStep(step: Int): Boolean {
        val state = _uiState.value
        return when (step) {
            1 -> {
                // Validar Etapa 1: Dados Pessoais
                state.name.isNotBlank() &&
                state.email.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches() &&
                state.password.isNotBlank() &&
                state.password.length >= 6 &&
                state.password == state.confirmPassword
            }
            2 -> {
                // Validar Etapa 2: Endereço
                state.address.isNotBlank() &&
                state.city.isNotBlank() &&
                state.state.isNotBlank() &&
                state.zipCode.isNotBlank() &&
                state.neighborhood.isNotBlank() &&
                state.localNumber.isNotBlank() &&
                state.latitude != null &&
                state.longitude != null
            }
            else -> true
        }
    }
    
    /**
     * Obtém localização atual e preenche automaticamente os campos de endereço
     */
    fun loadLocationAndFillAddress() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingLocation = true,
                locationError = null
            )
            
            // Verificar permissão de localização
            if (!locationHelper.hasLocationPermission()) {
                _uiState.value = _uiState.value.copy(
                    isLoadingLocation = false,
                    locationError = "Permissão de localização necessária. Por favor, permita o acesso à localização nas configurações do aplicativo."
                )
                return@launch
            }
            
            // Obter localização atual
            val location = locationHelper.getCurrentLocation()
            if (location == null) {
                _uiState.value = _uiState.value.copy(
                    isLoadingLocation = false,
                    locationError = "Não foi possível obter sua localização. Por favor, preencha o endereço manualmente."
                )
                return@launch
            }
            
            // Fazer reverse geocoding
            val addressResult = locationHelper.reverseGeocode(
                location.latitude,
                location.longitude
            )
            
            if (addressResult == null) {
                _uiState.value = _uiState.value.copy(
                    isLoadingLocation = false,
                    locationError = "Não foi possível obter o endereço da sua localização. Por favor, preencha manualmente."
                )
                return@launch
            }
            
            // Preencher campos automaticamente
            _uiState.value = _uiState.value.copy(
                address = addressResult.address,
                city = addressResult.city,
                state = addressResult.state,
                zipCode = addressResult.zipCode,
                neighborhood = addressResult.neighborhood,
                complement = addressResult.complement,
                latitude = location.latitude,
                longitude = location.longitude,
                isLoadingLocation = false,
                locationError = null
            )
        }
    }
}
