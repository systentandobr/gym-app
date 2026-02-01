package com.tadevolta.gym.ui.viewmodels

import android.app.Application
import android.icu.text.StringSearch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.data.remote.FranchiseService
import com.tadevolta.gym.data.remote.LeadService
import com.tadevolta.gym.data.remote.LocationRequiredException
import com.tadevolta.gym.data.remote.StudentService
import com.tadevolta.gym.data.repositories.AuthRepository
import com.tadevolta.gym.data.repositories.UserSessionStorage
import com.tadevolta.gym.utils.LocationHelper
import com.tadevolta.gym.utils.config.DEFAULT_UNIT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SignUpStep {
    IDLE,
    CREATING_USER,
    USER_CREATED,
    CREATING_STUDENT,
    SENDING_LEAD,
    COMPLETED,
    ERROR
}

data class OnboardingSharedUiState(
    // Dados da unidade (OnboardingViewModel)
    val units: List<UnitItem> = emptyList(),
    val filteredUnits: List<UnitItem> = emptyList(),
    val searchQuery: String = "",
    val selectedUnit: UnitItem? = null,
    val selectedUnitId: String? = null,
    val selectedUnitName: String? = null,
    
    // Dados do objetivo
    val selectedGoal: String? = null,
    
    // Estados de localização e busca
    val requiresLocation: Boolean = false,
    val locationPermissionGranted: Boolean = false,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val manualAddress: String = "",
    val isGeocoding: Boolean = false,
    val showManualAddressInput: Boolean = false,
    val showContinueWithoutUnit: Boolean = false,
    val hasTriedLocationSearch: Boolean = false,
    val hasTriedManualSearch: Boolean = false,
    
    // Dados do lead (OnboardingLeadViewModel)
    val leadType: LeadType? = null,
    val gymName: String = "",
    val gymEmail: String = "",
    val gymPhone: String = "",
    val gymAddress: String = "",
    val gymCity: String = "",
    val leadEmail: String = "",
    val leadName: String = "",
    val leadAddress: String = "",
    val leadPhone: String = "",
    val leadCity: String = "",
    val leadState: String = "",
    val averageStudents: String? = null,
    val responsibleName: String = "",
    val responsiblePhone: String = "",
    val responsibleEmail: String = "",
    val leadManualAddress: String = "",
    val leadManualCoordinates: Pair<Double, Double>? = null,
    val cameFromWithoutUnit: Boolean = false,
    
    // Dados do signup (SignUpViewModel)
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val neighborhood: String = "",
    val complement: String = "",
    val localNumber: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val currentStep: Int = 1,
    val totalSteps: Int = 3,
    val isLoadingLocation: Boolean = false,
    val locationError: String? = null,
    
    // Estados gerais
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignUpSuccessful: Boolean = false,
    
    // Validações de campos
    val fieldErrors: Map<String, String> = emptyMap(),
    val missingRequiredFields: List<String> = emptyList(),
    
    // Estados de criação de aluno
    val isCreatingStudent: Boolean = false,
    val studentCreationError: String? = null,
    val isStudentCreated: Boolean = false,
    val signUpStep: SignUpStep = SignUpStep.IDLE,
    
    // Campos adicionais para criação de aluno
    val gender: Gender? = null,
    val birthDate: String? = null,
    val cpf: String? = null,
    val fitnessLevel: FitnessLevel? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val emergencyContactRelationship: String? = null
)

@HiltViewModel
class OnboardingSharedViewModel @Inject constructor(
    private val franchiseService: FranchiseService,
    private val leadService: LeadService,
    private val authRepository: AuthRepository,
    private val userSessionStorage: UserSessionStorage,
    private val studentService: StudentService,
    private val application: Application
) : ViewModel() {
    
    // Escopo de aplicação para requisições críticas que não devem ser canceladas
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _uiState = MutableStateFlow(OnboardingSharedUiState())
    val uiState: StateFlow<OnboardingSharedUiState> = _uiState.asStateFlow()
    
    // ========== Métodos de Unidade (OnboardingViewModel) ==========
    
    fun loadNearbyUnits(
        lat: Double? = null,
        lng: Double? = null,
        marketSegment: String = "gym",
        radius: Int = 50,
        limit: Int = 20
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val latitude = lat ?: _uiState.value.currentLatitude
            val longitude = lng ?: _uiState.value.currentLongitude
            
            when (val result = franchiseService.findNearby(
                lat = latitude,
                lng = longitude,
                marketSegment = marketSegment,
                radius = radius,
                limit = limit
            )) {
                is Result.Success -> {
                    val unitItems = result.data.map { 
                        UnitItem.fromNearbyFranchise(it) 
                    }
                    _uiState.value = _uiState.value.copy(
                        units = unitItems,
                        filteredUnits = unitItems,
                        isLoading = false,
                        error = null,
                        hasTriedLocationSearch = true
                    )
                    
                    // Se não encontrou unidades após busca por localização, habilitar "Seguir sem Unidade"
                    if (unitItems.isEmpty() && latitude != null && longitude != null) {
                        _uiState.value = _uiState.value.copy(
                            showContinueWithoutUnit = true,
                            hasTriedLocationSearch = true
                        )
                    }
                }
                is Result.Error -> {
                    val requiresLocation = result.exception is LocationRequiredException
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Erro ao carregar unidades",
                        requiresLocation = requiresLocation,
                        hasTriedLocationSearch = true
                    )
                    
                    if (!requiresLocation && latitude != null && longitude != null) {
                        _uiState.value = _uiState.value.copy(showManualAddressInput = true)
                    }
                }
                is Result.Loading -> {
                    // Já está em loading
                }
            }
        }
    }
    
    fun updateLocation(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(
            currentLatitude = lat,
            currentLongitude = lng,
            locationPermissionGranted = true,
            error = null,
            requiresLocation = false,
            hasTriedLocationSearch = false
        )
        loadNearbyUnits(lat, lng)
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        // Se há unidades carregadas, apenas filtrar
        if (_uiState.value.units.isNotEmpty()) {
            filterUnits(query)
        }
    }
    
    private fun filterUnits(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.units
        } else {
            _uiState.value.units.filter { unit ->
                unit.name.contains(query, ignoreCase = true) ||
                unit.address.contains(query, ignoreCase = true) ||
                unit.city.contains(query, ignoreCase = true) ||
                unit.state.contains(query, ignoreCase = true) ||
                unit.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }
        _uiState.value = _uiState.value.copy(filteredUnits = filtered)
    }
    
    /**
     * Detecta se o texto parece ser um endereço completo
     */
    private fun looksLikeAddress(query: String): Boolean {
        if (query.isBlank()) return false
        
        val trimmed = query.trim()
        
        // Critérios para considerar como endereço:
        // 1. Contém vírgula
        if (trimmed.contains(',')) return true
        
        // 2. Tem mais de 20 caracteres
        if (trimmed.length > 20) return true
        
        // 3. Contém palavras-chave de endereço
        val addressKeywords = listOf(
            "rua", "avenida", "av.", "av", "praça", "praça", "travessa", 
            "alameda", "estrada", "rodovia", "br-", "km", "número", "nº", "n°"
        )
        val lowerQuery = trimmed.lowercase()
        if (addressKeywords.any { lowerQuery.contains(it) }) return true
        
        // 4. Contém números seguidos de texto (ex: "Rua 123", "123 Main St")
        val numberPattern = Regex("\\d+\\s+[a-zA-Z]|[a-zA-Z]+\\s+\\d+")
        if (numberPattern.containsMatchIn(trimmed)) return true
        
        return false
    }
    
    /**
     * Busca por endereço ou filtra unidades existentes
     * Decide automaticamente se deve fazer geocodificação ou apenas filtrar
     */
    suspend fun searchAddressOrFilter(
        query: String,
        locationHelper: LocationHelper
    ) {
        if (query.isBlank()) {
            filterUnits(query)
            return
        }
        
        // Se há unidades carregadas e não parece ser endereço, apenas filtrar
        if (_uiState.value.units.isNotEmpty() && !looksLikeAddress(query)) {
            filterUnits(query)
            return
        }
        
        // Se parece ser endereço ou não há unidades, fazer geocodificação
        if (looksLikeAddress(query) || _uiState.value.units.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                hasTriedManualSearch = true,
                manualAddress = query
            )
            geocodeAndSearchUnits(query, locationHelper)
        } else {
            // Caso contrário, apenas filtrar
            filterUnits(query)
        }
    }
    
    fun selectUnit(unit: UnitItem) {
        _uiState.value = _uiState.value.copy(
            selectedUnit = unit,
            selectedUnitId = unit.unitId,
            selectedUnitName = unit.name,
            cameFromWithoutUnit = false, // Se selecionou uma unidade, não veio sem unidade
            // Pré-preencher campos de lead com dados da unidade selecionada
            gymName = unit.name,
            gymEmail = unit.owner.email,
            gymPhone = unit.owner.phone ?: "",
            gymAddress = unit.address,
            gymCity = unit.city,
            leadState = unit.state
        )
        viewModelScope.launch {
            userSessionStorage.saveSelectedUnit(unit.unitId, unit.name)
        }
    }
    
    fun selectGoal(goal: String) {
        _uiState.value = _uiState.value.copy(selectedGoal = goal)
    }
    
    fun updateManualAddress(address: String) {
        _uiState.value = _uiState.value.copy(manualAddress = address)
    }
    
    fun showManualAddressInput(show: Boolean) {
        _uiState.value = _uiState.value.copy(showManualAddressInput = show)
    }
    
    fun showContinueWithoutUnit(show: Boolean) {
        _uiState.value = _uiState.value.copy(showContinueWithoutUnit = show)
    }
    
    suspend fun geocodeAndSearchUnits(
        address: String,
        locationHelper: LocationHelper
    ) {
        _uiState.value = _uiState.value.copy(
            isGeocoding = true,
            error = null,
            manualAddress = address,
            hasTriedManualSearch = true
        )
        
        val geocodeResult = locationHelper.geocodeAddress(address)
        
        if (geocodeResult != null) {
            updateLocation(geocodeResult.latitude, geocodeResult.longitude)
            _uiState.value = _uiState.value.copy(isGeocoding = false)
            
            // Aguardar um pouco para garantir que as unidades foram carregadas
            kotlinx.coroutines.delay(500)
            
            if (_uiState.value.filteredUnits.isEmpty() && !_uiState.value.isLoading) {
                _uiState.value = _uiState.value.copy(showContinueWithoutUnit = true)
            }
        } else {
            _uiState.value = _uiState.value.copy(
                isGeocoding = false,
                error = "Não foi possível localizar o endereço. Você pode continuar sem selecionar uma unidade.",
                showContinueWithoutUnit = true
            )
        }
    }
    
    // ========== Métodos de Lead (OnboardingLeadViewModel) ==========
    
    fun selectLeadType(type: LeadType) {
        _uiState.value = _uiState.value.copy(leadType = type)
    }
    
    fun updateGymName(name: String) {
        _uiState.value = _uiState.value.copy(gymName = name)
    }
    
    fun updateLeadEmail(email: String) {
        _uiState.value = _uiState.value.copy(leadEmail = email)
    }
    
    fun updateLeadAddress(address: String) {
        _uiState.value = _uiState.value.copy(leadAddress = address)
    }
    
    fun updateLeadPhone(phone: String) {
        _uiState.value = _uiState.value.copy(leadPhone = phone)
    }
    
    fun updateLeadCity(city: String) {
        _uiState.value = _uiState.value.copy(leadCity = city)
    }
    
    fun updateLeadState(state: String) {
        _uiState.value = _uiState.value.copy(leadState = state)
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
    
    fun setLeadManualAddress(address: String, coordinates: Pair<Double, Double>? = null) {
        _uiState.value = _uiState.value.copy(
            leadManualAddress = address,
            leadManualCoordinates = coordinates,
            cameFromWithoutUnit = true,
            leadAddress = address
        )
    }
    
    suspend fun submitLead(): Result<LeadResponse> {
        val state = _uiState.value
        
        // Validar campos obrigatórios para academia
        if (state.leadType == LeadType.GYM) {
            if (state.gymName.isBlank()) {
                _uiState.value = state.copy(error = "Nome da academia é obrigatório")
                return Result.Error(Exception("Nome da academia é obrigatório"))
            }
            if (state.leadEmail.isBlank()) {
                _uiState.value = state.copy(error = "Email é obrigatório")
                return Result.Error(Exception("Email é obrigatório"))
            }
            if (state.leadPhone.isBlank()) {
                _uiState.value = state.copy(error = "Telefone é obrigatório")
                return Result.Error(Exception("Telefone é obrigatório"))
            }
            if (state.leadAddress.isBlank()) {
                _uiState.value = state.copy(error = "Endereço é obrigatório")
                return Result.Error(Exception("Endereço é obrigatório"))
            }
            if (state.leadCity.isBlank()) {
                _uiState.value = state.copy(error = "Cidade é obrigatória")
                return Result.Error(Exception("Cidade é obrigatória"))
            }
            if (state.leadState.isBlank()) {
                _uiState.value = state.copy(error = "Estado é obrigatório")
                return Result.Error(Exception("Estado é obrigatório"))
            }
            if (state.averageStudents == null) {
                _uiState.value = state.copy(error = "Média de alunos é obrigatória")
                return Result.Error(Exception("Média de alunos é obrigatória"))
            }
        }
        
        // Se for aluno, enviar lead também
        if (state.leadType == LeadType.STUDENT) {
            // Validar campos mínimos para aluno
            if (state.selectedUnit == null) {
                // Quando veio sem unidade, validar campos da academia
                if (state.gymName.isBlank()) {
                    _uiState.value = state.copy(error = "Nome da academia é obrigatório")
                    return Result.Error(Exception("Nome da academia é obrigatório"))
                }
                if (state.leadEmail.isBlank()) {
                    _uiState.value = state.copy(error = "Email é obrigatório")
                    return Result.Error(Exception("Email é obrigatório"))
                }
                if (state.leadPhone.isBlank()) {
                    _uiState.value = state.copy(error = "Telefone é obrigatório")
                    return Result.Error(Exception("Telefone é obrigatório"))
                }
                if (state.leadAddress.isBlank()) {
                    _uiState.value = state.copy(error = "Endereço da academia é obrigatório")
                    return Result.Error(Exception("Endereço da academia é obrigatório"))
                }
                if (state.leadCity.isBlank()) {
                    _uiState.value = state.copy(error = "Cidade da academia é obrigatória")
                    return Result.Error(Exception("Cidade da academia é obrigatória"))
                }
                if (state.leadState.isBlank()) {
                    _uiState.value = state.copy(error = "Estado da academia é obrigatório")
                    return Result.Error(Exception("Estado da academia é obrigatório"))
                }
            } else {
                // Quando tem unidade selecionada, validar email (obrigatório pela API)
                val finalEmail = state.selectedUnit?.owner?.email ?: state.leadEmail
                if (finalEmail.isBlank()) {
                    _uiState.value = state.copy(error = "Email é obrigatório")
                    return Result.Error(Exception("Email é obrigatório"))
                }
            }
            
            // Preparar metadata para lead de aluno
            val metadata = buildMap<String, String> {
                state.selectedUnitName?.let { put("selectedUnitName", it) }
                state.selectedGoal?.let { put("goal", it) }
                
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
                    if (state.leadManualAddress.isNotBlank()) {
                        put("manualAddress", state.leadManualAddress)
                    }
                    state.leadManualCoordinates?.let { (lat, lng) ->
                        put("manualCoordinates", "$lat,$lng")
                    }
                    put("gymName", state.gymName)
                    put("gymAddress", state.leadAddress)
                }
            }
            
            // Criar LeadRequest para aluno
            // Para leads de estudante, name e email devem ser do estudante
            // Usar dados da unidade selecionada se disponível, senão usar dados do estado
            val finalGymName = state.selectedUnit?.name ?: state.gymName
            val finalGymEmail = state.selectedUnit?.owner?.email ?: state.leadEmail
            val finalGymPhone = state.selectedUnit?.owner?.phone ?: state.leadPhone
            val finalGymAddress = state.selectedUnit?.address ?: state.leadAddress
            val finalGymCity = state.selectedUnit?.city ?: state.leadCity
            val finalGymState = state.selectedUnit?.state ?: state.leadState
            // Obter unitId do estado - priorizar selectedUnit?.unitId, depois selectedUnitId
            val finalUnitId = state.selectedUnit?.unitId ?: state.selectedUnitId
            
            // Adicionar dados da academia ao metadata
            val metadataWithGym = metadata.toMutableMap().apply {
                if (finalGymName.isNotBlank()) put("gymName", finalGymName)
                if (finalGymEmail.isNotBlank()) put("gymEmail", finalGymEmail)
                if (finalGymPhone.isNotBlank()) put("gymPhone", finalGymPhone)
                if (finalGymAddress.isNotBlank()) put("gymAddress", finalGymAddress)
                if (finalGymCity.isNotBlank()) put("gymCity", finalGymCity)
                if (finalGymState.isNotBlank()) put("gymState", finalGymState)
            }
            
            // Para leads de aluno, usar dados da academia no name, email e phone
            val finalName = state.leadName.ifBlank { state.name }
            val finalEmail = state.leadEmail.ifBlank { state.email }
            val finalPhone = state.leadPhone
            val finalCity = state.city.ifBlank { state.leadCity }
            val finalStateValue = state.state.ifBlank { state.leadState }
            
            val leadRequest = LeadRequest(
                name = finalName,
                email = finalEmail,
                phone = finalPhone,
                city = finalCity,
                state = finalStateValue,
                unitId = finalUnitId,
                marketSegment = "gym",
                userType = "student",
                objectives = state.selectedGoal?.let {
                    LeadObjectives(
                        primary = it,
                        secondary = emptyList(),
                        interestedInFranchise = false
                    )
                },
                metadata = metadataWithGym
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
        
        // Preparar metadata
        val metadata = buildMap<String, String> {
            state.selectedUnitName?.let { put("selectedUnitName", it) }
            put("address", state.leadAddress)
            state.averageStudents?.let { put("averageStudents", it) }
            state.selectedGoal?.let { put("goal", it) }
            
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
                if (state.leadManualAddress.isNotBlank()) {
                    put("manualAddress", state.leadManualAddress)
                }
                state.leadManualCoordinates?.let { (lat, lng) ->
                    put("manualCoordinates", "$lat,$lng")
                }
            }
        }
        
        // Obter unitId do estado - priorizar selectedUnit?.unitId, depois selectedUnitId
        val finalUnitId = state.selectedUnit?.unitId ?: state.selectedUnitId
        
        // Criar LeadRequest
        val leadRequest = LeadRequest(
            name = state.leadName,
            email = state.leadEmail,
            phone = state.leadPhone,
            city = state.leadCity,
            state = state.leadState,
            unitId = finalUnitId,
            marketSegment = "gym",
            userType = "academia",
            objectives = state.selectedGoal?.let {
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
                // Marcar onboarding como completo quando lead de academia for enviado
                markOnboardingCompleted()
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
    
    // ========== Métodos de SignUp ==========
    
    fun updateName(name: String) {
        val fieldErrors = _uiState.value.fieldErrors.toMutableMap()
        // Validar se tem pelo menos 2 palavras
        val words = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (words.size < 2 && name.isNotBlank()) {
            fieldErrors["name"] = "O sobrenome é obrigatório. Por favor, informe seu nome completo."
        } else {
            fieldErrors.remove("name")
        }
        _uiState.value = _uiState.value.copy(
            name = name, 
            error = null,
            fieldErrors = fieldErrors
        )
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
    
    // Métodos de atualização para campos de aluno
    fun updateGender(gender: Gender) {
        _uiState.value = _uiState.value.copy(gender = gender, error = null)
    }
    
    fun updateBirthDate(birthDate: String) {
        _uiState.value = _uiState.value.copy(birthDate = birthDate, error = null)
    }
    
    fun updateCpf(cpf: String) {
        _uiState.value = _uiState.value.copy(cpf = cpf, error = null)
    }
    
    fun updateFitnessLevel(fitnessLevel: FitnessLevel) {
        _uiState.value = _uiState.value.copy(fitnessLevel = fitnessLevel, error = null)
    }
    
    fun updateEmergencyContact(name: String, phone: String, relationship: String) {
        _uiState.value = _uiState.value.copy(
            emergencyContactName = name,
            emergencyContactPhone = phone,
            emergencyContactRelationship = relationship,
            error = null
        )
    }
    
    fun nextStep() {
        val current = _uiState.value.currentStep
        if (current < _uiState.value.totalSteps && canProceedToNextStep()) {
            _uiState.value = _uiState.value.copy(
                currentStep = current + 1,
                error = null,
                missingRequiredFields = emptyList(),
                fieldErrors = emptyMap()
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
        val missingFields = mutableListOf<String>()
        val fieldErrors = mutableMapOf<String, String>()
        
        return when (step) {
            1 -> {
                // Validar nome completo (mínimo 2 palavras)
                val nameWords = state.name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
                if (state.name.isBlank()) {
                    missingFields.add("Nome completo")
                } else if (nameWords.size < 2) {
                    fieldErrors["name"] = "O sobrenome é obrigatório. Por favor, informe seu nome completo."
                }
                
                if (state.email.isBlank()) {
                    missingFields.add("E-mail")
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
                    fieldErrors["email"] = "E-mail inválido"
                }
                
                if (state.password.isBlank()) {
                    missingFields.add("Senha")
                } else if (state.password.length < 6) {
                    fieldErrors["password"] = "Senha deve ter pelo menos 6 caracteres"
                }
                
                if (state.confirmPassword.isBlank()) {
                    missingFields.add("Confirmar senha")
                } else if (state.password != state.confirmPassword) {
                    fieldErrors["confirmPassword"] = "As senhas não coincidem"
                }
                
                _uiState.value = _uiState.value.copy(
                    missingRequiredFields = missingFields,
                    fieldErrors = fieldErrors
                )
                
                missingFields.isEmpty() && fieldErrors.isEmpty() && 
                state.name.isNotBlank() && nameWords.size >= 2 &&
                state.email.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches() &&
                state.password.isNotBlank() &&
                state.password.length >= 6 &&
                state.password == state.confirmPassword
            }
            2 -> {
                if (state.address.isBlank()) missingFields.add("Endereço")
                if (state.city.isBlank()) missingFields.add("Cidade")
                if (state.state.isBlank()) missingFields.add("Estado")
                if (state.zipCode.isBlank()) missingFields.add("CEP")
                if (state.neighborhood.isBlank()) missingFields.add("Bairro")
                if (state.localNumber.isBlank()) missingFields.add("Número local")
                if (state.latitude == null || state.longitude == null) {
                    missingFields.add("Localização (use o botão 'Usar minha localização')")
                }
                
                _uiState.value = _uiState.value.copy(
                    missingRequiredFields = missingFields,
                    fieldErrors = fieldErrors
                )
                
                missingFields.isEmpty() &&
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
    
    fun loadLocationAndFillAddress(locationHelper: LocationHelper) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingLocation = true,
                locationError = null
            )
            
            if (!locationHelper.hasLocationPermission()) {
                _uiState.value = _uiState.value.copy(
                    isLoadingLocation = false,
                    locationError = "Permissão de localização necessária. Por favor, permita o acesso à localização nas configurações do aplicativo."
                )
                return@launch
            }
            
            val location = locationHelper.getCurrentLocation()
            if (location == null) {
                _uiState.value = _uiState.value.copy(
                    isLoadingLocation = false,
                    locationError = "Não foi possível obter sua localização. Por favor, preencha o endereço manualmente."
                )
                return@launch
            }
            
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
    
    fun signUp() {
        val name = _uiState.value.name.trim()
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword
        // Obter unitId do estado - priorizar selectedUnit?.unitId, depois selectedUnitId, senão DEFAULT_UNIT_ID
        val unitId = _uiState.value.selectedUnit?.unitId 
            ?: _uiState.value.selectedUnitId 
            ?: DEFAULT_UNIT_ID
        val unitName = _uiState.value.selectedUnit?.name 
            ?: _uiState.value.selectedUnitName
        
        // Validações
        when {
            name.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Nome é obrigatório")
                return
            }
            name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size < 2 -> {
                _uiState.value = _uiState.value.copy(error = "O sobrenome é obrigatório. Por favor, informe seu nome completo.")
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
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                signUpStep = SignUpStep.CREATING_USER
            )
            
            val finalUnitName = when {
                !unitName.isNullOrBlank() -> unitName
                _uiState.value.cameFromWithoutUnit && _uiState.value.gymName.isNotBlank() -> _uiState.value.gymName
                else -> "Sem Unidade"
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
                    // Marcar onboarding como completo quando signup for bem-sucedido
                    markOnboardingCompleted()
                    
                    // Extrair user.id e criar aluno
                    val userId = result.data.id
                    
                    // Determinar unitId a ser usado
                    val finalUnitId = unitId ?: DEFAULT_UNIT_ID
                    
                    // Atualizar estado para USER_CREATED
                    _uiState.value = _uiState.value.copy(
                        signUpStep = SignUpStep.USER_CREATED
                    )
                    
                    // Usar applicationScope para criar aluno (não será cancelado)
                    applicationScope.launch {
                        createStudentAfterSignUp(userId, finalUnitId)
                        
                        // Se for aluno, enviar lead com dados do aluno após cadastro
                        if (_uiState.value.leadType == LeadType.STUDENT) {
                            _uiState.value = _uiState.value.copy(signUpStep = SignUpStep.SENDING_LEAD)
                            sendLeadAfterSignUp(name, email)  // Usar dados do aluno do SignUpStep
                        } else if (_uiState.value.cameFromWithoutUnit) {
                            // Fallback caso não tenha leadType definido mas veio sem unidade (legado)
                            _uiState.value = _uiState.value.copy(signUpStep = SignUpStep.SENDING_LEAD)
                            sendLeadAfterSignUp(name, email)
                        }

                        
                        // Marcar como completo
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSignUpSuccessful = true,
                            signUpStep = SignUpStep.COMPLETED
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Erro ao criar conta",
                        signUpStep = SignUpStep.ERROR
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erro desconhecido",
                        signUpStep = SignUpStep.ERROR
                    )
                }
            }
        }
    }
    
    private suspend fun createStudentAfterSignUp(userId: String, unitId: String) {
        val state = _uiState.value
        
        try {
            // Atualizar estado para indicar que está criando aluno
            _uiState.value = _uiState.value.copy(
                isCreatingStudent = true,
                studentCreationError = null,
                signUpStep = SignUpStep.CREATING_STUDENT
            )
            
            // Verificar se aluno já existe antes de criar
            val existingStudentResult = studentService.getStudentByUserId(userId)
            if (existingStudentResult is Result.Success) {
                // Aluno já existe, marcar como sucesso
                android.util.Log.i("OnboardingSharedViewModel", "Aluno já existe: ${existingStudentResult.data.id}")
                _uiState.value = _uiState.value.copy(
                    isCreatingStudent = false,
                    isStudentCreated = true,
                    studentCreationError = null
                )
                return
            }
            
            // Mapear dados do estado para CreateStudentRequest
            val address = if (state.address.isNotBlank() && state.city.isNotBlank() && state.state.isNotBlank()) {
                Address(
                    street = state.address,
                    number = state.localNumber,
                    complement = state.complement,
                    neighborhood = state.neighborhood,
                    city = state.city,
                    state = state.state,
                    zipCode = state.zipCode
                )
            } else {
                null
            }
            
            // Obter telefone (prioridade: leadPhone se veio sem unidade, senão vazio)
            val phone = if (state.cameFromWithoutUnit && state.leadPhone.isNotBlank()) {
                state.leadPhone
            } else {
                null
            }
            
            // Formatar CPF (remover máscara)
            val cpf = state.cpf?.takeIf { it.isNotBlank() }?.filter { it.isDigit() }?.takeIf { it.isNotEmpty() }
            
            // Formatar data de nascimento para ISO 8601 (YYYY-MM-DD)
            val birthDate = state.birthDate?.takeIf { it.isNotBlank() }?.let { dateStr ->
                formatBirthDateToISO(dateStr)
            }
            
            // Criar EmergencyContact se todos os campos estiverem preenchidos
            val emergencyContact = if (
                state.emergencyContactName?.isNotBlank() == true &&
                state.emergencyContactPhone?.isNotBlank() == true &&
                state.emergencyContactRelationship?.isNotBlank() == true
            ) {
                EmergencyContact(
                    name = state.emergencyContactName!!,
                    phone = state.emergencyContactPhone!!,
                    relationship = state.emergencyContactRelationship!!
                )
            } else {
                null
            }
            
            // Criar HealthInfo se fitnessLevel estiver preenchido
            val healthInfo = state.fitnessLevel?.let { level ->
                HealthInfo(
                    medicalConditions = null,
                    medications = null,
                    injuries = null,
                    fitnessLevel = level
                )
            }
            
            val createStudentRequest = CreateStudentRequest(
                name = state.name,
                email = state.email,
                phone = phone,
                cpf = cpf,
                birthDate = birthDate,
                gender = state.gender,  // Pode ser null, não será incluído no JSON
                address = address,
                emergencyContact = emergencyContact,
                healthInfo = healthInfo,
                userId = userId,
                unitId = unitId
            )
            
            // Logar informação se usar DEFAULT_UNIT_ID
            if (unitId == DEFAULT_UNIT_ID) {
                android.util.Log.i("OnboardingSharedViewModel", "Criando aluno com unidade padrão. Será atualizado após processamento do lead.")
            }
            
            when (val result = studentService.createStudent(userId, unitId, createStudentRequest)) {
                is Result.Success -> {
                    android.util.Log.i("OnboardingSharedViewModel", "Aluno criado com sucesso: ${result.data.id}")
                    _uiState.value = _uiState.value.copy(
                        isCreatingStudent = false,
                        isStudentCreated = true,
                        studentCreationError = null
                    )
                }
                is Result.Error -> {
                    // Verificar se é erro 409 (Conflict) - aluno já existe
                    val errorMessage = result.exception.message ?: "Erro ao criar aluno"
                    val isConflict = errorMessage.contains("409") || errorMessage.contains("Conflict") || 
                                    errorMessage.contains("already exists") || errorMessage.contains("já existe")
                    
                    if (isConflict) {
                        // Tratar como sucesso (aluno já existe)
                        android.util.Log.i("OnboardingSharedViewModel", "Aluno já existe (409 Conflict)")
                        _uiState.value = _uiState.value.copy(
                            isCreatingStudent = false,
                            isStudentCreated = true,
                            studentCreationError = null
                        )
                    } else {
                        android.util.Log.e("OnboardingSharedViewModel", "Erro ao criar aluno: $errorMessage", result.exception)
                        _uiState.value = _uiState.value.copy(
                            isCreatingStudent = false,
                            isStudentCreated = false,
                            studentCreationError = errorMessage
                        )
                    }
                }
                else -> {
                    android.util.Log.w("OnboardingSharedViewModel", "Resultado desconhecido ao criar aluno")
                    _uiState.value = _uiState.value.copy(
                        isCreatingStudent = false,
                        studentCreationError = "Erro desconhecido ao criar aluno"
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("OnboardingSharedViewModel", "Exceção ao criar aluno: ${e.message}", e)
            _uiState.value = _uiState.value.copy(
                isCreatingStudent = false,
                studentCreationError = e.message ?: "Erro ao criar aluno"
            )
        }
    }
    
    private suspend fun sendLeadAfterSignUp(studentName: String, studentEmail: String) {
        val state = _uiState.value
        
        // Validar se há dados mínimos para enviar lead (unidade selecionada ou dados da academia)
        val hasUnit = state.selectedUnitId != null || state.selectedUnit != null
        val hasGymData = state.gymName.isNotBlank() || state.selectedUnit != null
        
        if (!hasUnit && !hasGymData) {
            android.util.Log.w("OnboardingSharedViewModel", "Não há dados suficientes para enviar lead de aluno")
            return
        }
        
        try {
            // Preparar dados da academia para metadata
            val finalGymName = state.selectedUnit?.name ?: state.gymName
            val finalGymEmail = state.selectedUnit?.owner?.email ?: state.leadEmail
            val finalGymPhone = state.selectedUnit?.owner?.phone ?: state.leadPhone
            val finalGymAddress = state.selectedUnit?.address ?: state.leadAddress
            val finalGymCity = state.selectedUnit?.city ?: state.leadCity
            val finalGymState = state.selectedUnit?.state ?: state.leadState
            
            val metadata = buildMap<String, String> {
                state.selectedUnitName?.let { put("selectedUnitName", it) }
                state.selectedGoal?.let { put("goal", it) }
                put("source", "app-signup")
                put("studentName", studentName)
                put("studentEmail", studentEmail)
                
                // Adicionar dados da academia ao metadata
                if (finalGymName.isNotBlank()) put("gymName", finalGymName)
                if (finalGymEmail.isNotBlank()) put("gymEmail", finalGymEmail)
                if (finalGymPhone.isNotBlank()) put("gymPhone", finalGymPhone)
                if (finalGymAddress.isNotBlank()) put("gymAddress", finalGymAddress)
                if (finalGymCity.isNotBlank()) put("gymCity", finalGymCity)
                if (finalGymState.isNotBlank()) put("gymState", finalGymState)
                
                // Campos adicionais quando veio sem unidade
                if (state.cameFromWithoutUnit) {
                    state.leadManualAddress.takeIf { it.isNotBlank() }?.let { put("manualAddress", it) }
                    state.leadManualCoordinates?.let { (lat, lng) -> put("manualCoordinates", "$lat,$lng") }
                    state.responsibleName.takeIf { it.isNotBlank() }?.let { put("responsibleName", it) }
                    state.responsiblePhone.takeIf { it.isNotBlank() }?.let { put("responsiblePhone", it) }
                    state.responsibleEmail.takeIf { it.isNotBlank() }?.let { put("responsibleEmail", it) }
                }
            }
            
            // Para leads de aluno, SEMPRE usar dados do aluno nos campos principais
            val finalPhone = state.leadPhone.takeIf { it.isNotBlank() } ?: ""  // Telefone do aluno (quando veio sem unidade)
            val finalCity = state.city.takeIf { it.isNotBlank() } ?: state.leadCity
            val finalStateValue = state.state.takeIf { it.isNotBlank() } ?: state.leadState
            val finalUnitId = state.selectedUnit?.unitId ?: state.selectedUnitId
            
            val leadRequest = LeadRequest(
                name = studentName,  // Nome do aluno (parâmetro da função)
                email = studentEmail,  // Email do aluno (parâmetro da função)
                phone = finalPhone,  // Telefone do aluno
                city = finalCity,
                state = finalStateValue,
                unitId = finalUnitId,
                marketSegment = "gym",
                userType = "student",
                objectives = state.selectedGoal?.let {
                    LeadObjectives(
                        primary = it,
                        secondary = emptyList(),
                        interestedInFranchise = false
                    )
                },
                metadata = metadata  // Dados da academia vão aqui
            )
            
            leadService.postLead(leadRequest)
            // Não bloquear o fluxo se lead falhar - usuário já está registrado
        } catch (e: Exception) {
            android.util.Log.e("OnboardingSharedViewModel", "Erro ao enviar lead: ${e.message}", e)
            // Não bloquear o fluxo - usuário já está registrado
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, studentCreationError = null)
    }
    
    /**
     * Retry da criação do aluno - verifica se usuário já existe antes de tentar criar
     */
    fun retryStudentCreation() {
        applicationScope.launch {
            // Verificar se usuário já existe
            val userResult = authRepository.getCurrentUser()
            if (userResult is Result.Success) {
                val userId = userResult.data.id
                val state = _uiState.value
                val finalUnitId = state.selectedUnitId ?: DEFAULT_UNIT_ID
                android.util.Log.d("OnboardingSharedViewModel", "Tentando criar aluno para usuário: $userId, unidade: $finalUnitId")

                // Tentar criar apenas o aluno (usuário já existe)
                createStudentAfterSignUp(userId, finalUnitId)
                
                // Se criação foi bem-sucedida, marcar como completo
                if (_uiState.value.isStudentCreated) {
                    if (state.cameFromWithoutUnit) {
                        _uiState.value = _uiState.value.copy(signUpStep = SignUpStep.SENDING_LEAD)
                        sendLeadAfterSignUp(state.name, state.email)
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignUpSuccessful = true,
                        signUpStep = SignUpStep.COMPLETED
                    )
                }
            } else {
                // Usuário não existe, mostrar erro
                _uiState.value = _uiState.value.copy(
                    error = "Usuário não encontrado. Por favor, tente fazer o cadastro novamente.",
                    signUpStep = SignUpStep.ERROR
                )
            }
        }
    }
    
    /**
     * Marca o onboarding como completo e salva no storage
     */
    fun markOnboardingCompleted() {
        viewModelScope.launch {
            userSessionStorage.setOnboardingCompleted(true)
        }
    }
    
    /**
     * Limpa o estado do onboarding após conclusão do fluxo
     */
    fun clearOnboardingState() {
        _uiState.value = OnboardingSharedUiState()
    }
    
    /**
     * Formata data de nascimento de DD/MM/YYYY para ISO 8601 (YYYY-MM-DD)
     */
    private fun formatBirthDateToISO(dateStr: String): String? {
        return try {
            val digits = dateStr.filter { it.isDigit() }
            if (digits.length == 8) {
                val day = digits.substring(0, 2)
                val month = digits.substring(2, 4)
                val year = digits.substring(4, 8)
                "$year-$month-$day"
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
