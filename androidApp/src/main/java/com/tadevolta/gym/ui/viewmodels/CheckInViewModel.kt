package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.CheckIn
import com.tadevolta.gym.data.models.CheckInStats
import com.tadevolta.gym.data.models.Location as LocationModel
import com.tadevolta.gym.data.remote.CheckInService
import com.tadevolta.gym.data.remote.FranchiseService
import com.tadevolta.gym.data.repositories.AuthRepository
import com.tadevolta.gym.data.repositories.TrainingPlanRepository
import com.tadevolta.gym.data.repositories.UserSessionStorage
import com.tadevolta.gym.utils.LocationHelper
import com.tadevolta.gym.utils.LocationValidator
import com.tadevolta.gym.utils.getStudentId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckInUiState(
    val checkInStats: CheckInStats? = null,
    val checkInHistory: List<CheckIn> = emptyList(),
    val isCheckingIn: Boolean = false,
    val isValidatingLocation: Boolean = false,
    val locationError: String? = null,
    val isOutOfRange: Boolean = false,
    val selectedUnitName: String? = null,
    val selectedUnitLocation: LocationModel? = null
)

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val checkInService: CheckInService,
    private val authRepository: AuthRepository,
    private val userSessionStorage: UserSessionStorage,
    private val locationHelper: LocationHelper,
    private val franchiseService: FranchiseService,
    private val trainingPlanRepository: TrainingPlanRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()
    
    // StateFlows derivados para compatibilidade
    val checkInStats: StateFlow<CheckInStats?> = _uiState.map { it.checkInStats }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    val checkInHistory: StateFlow<List<CheckIn>> = _uiState.map { it.checkInHistory }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val isCheckingIn: StateFlow<Boolean> = _uiState.map { it.isCheckingIn }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
    
    init {
        loadCheckInData()
        loadSelectedUnitLocation()
    }
    
    private fun loadCheckInData() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCachedUser() ?: when (val result = authRepository.getCurrentUser()) {
                    is com.tadevolta.gym.data.models.Result.Success -> result.data
                    else -> return@launch
                }
                
                // Obter studentId
                val studentId = getStudentId(user.id, trainingPlanRepository)
                
                // Carregar stats - sempre retorna Success (mesmo que vazio em caso de erro)
                when (val result = checkInService.getCheckInStats(studentId)) {
                    is com.tadevolta.gym.data.models.Result.Success -> {
                        _uiState.value = _uiState.value.copy(checkInStats = result.data)
                    }
                    is com.tadevolta.gym.data.models.Result.Error -> {
                        // Em caso de erro, usar stats vazios
                        _uiState.value = _uiState.value.copy(checkInStats = CheckInStats(
                            totalCheckIns = 0,
                            currentStreak = 0,
                            longestStreak = 0,
                            checkInsThisYear = 0,
                            checkInsLast365Days = 0
                        ))
                    }
                    else -> {}
                }
                
                // Carregar histórico - sempre retorna Success (mesmo que vazio em caso de erro)
                when (val result = checkInService.getCheckInHistory(studentId, 30)) {
                    is com.tadevolta.gym.data.models.Result.Success -> {
                        _uiState.value = _uiState.value.copy(checkInHistory = result.data.checkIns)
                    }
                    is com.tadevolta.gym.data.models.Result.Error -> {
                        // Em caso de erro, usar histórico vazio
                        _uiState.value = _uiState.value.copy(checkInHistory = emptyList())
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                // Em caso de erro inesperado, garantir que a UI não quebre
                _uiState.value = _uiState.value.copy(
                    checkInStats = CheckInStats(
                        totalCheckIns = 0,
                        currentStreak = 0,
                        longestStreak = 0,
                        checkInsThisYear = 0,
                        checkInsLast365Days = 0
                    ),
                    checkInHistory = emptyList()
                )
            }
        }
    }
    
    private fun loadSelectedUnitLocation() {
        viewModelScope.launch {
            val (unitId, unitName) = userSessionStorage.getSelectedUnit()
            _uiState.value = _uiState.value.copy(selectedUnitName = unitName)
            
            // Se não tem unitId, tentar do User
            val finalUnitId = unitId ?: authRepository.getCachedUser()?.unitId
            
            // Não fazer chamada de API sem coordenadas
            // A validação de localização será feita no backend durante o check-in
            // Se precisar da localização da unidade no futuro, pode ser armazenada
            // quando a unidade é selecionada no onboarding ou buscar com outro endpoint
            if (finalUnitId != null) {
                // Por enquanto, apenas armazenar o nome da unidade
                // A validação de localização será feita no backend quando o check-in for realizado
            }
        }
    }
    
    fun performCheckIn() {
        viewModelScope.launch {
            val user = authRepository.getCachedUser() ?: when (val result = authRepository.getCurrentUser()) {
                is com.tadevolta.gym.data.models.Result.Success -> result.data
                else -> {
                    _uiState.value = _uiState.value.copy(
                        locationError = "Usuário não autenticado"
                    )
                    return@launch
                }
            }
            
            _uiState.value = _uiState.value.copy(
                isCheckingIn = false,
                isValidatingLocation = true,
                locationError = null,
                isOutOfRange = false
            )
            
            // Verificar permissão de localização
            if (!locationHelper.hasLocationPermission()) {
                _uiState.value = _uiState.value.copy(
                    isValidatingLocation = false,
                    locationError = "Permissão de localização necessária"
                )
                return@launch
            }
            
            // Obter localização atual do usuário
            val userLocation = locationHelper.getCurrentLocation()
            if (userLocation == null) {
                _uiState.value = _uiState.value.copy(
                    isValidatingLocation = false,
                    locationError = "Não foi possível obter sua localização"
                )
                return@launch
            }
            
            val userLocationModel = LocationModel(
                lat = userLocation.latitude,
                lng = userLocation.longitude
            )
            
            // Validar localização se tivermos coordenadas da unidade
            val unitLocation = _uiState.value.selectedUnitLocation
            if (unitLocation != null) {
                val isWithinRange = LocationValidator.isWithinRange(
                    userLocationModel,
                    unitLocation
                )
                
                if (!isWithinRange) {
                    _uiState.value = _uiState.value.copy(
                        isValidatingLocation = false,
                        isOutOfRange = true,
                        locationError = "Você está fora do alcance da unidade"
                    )
                    return@launch
                }
            }
            
            // Fazer check-in
            _uiState.value = _uiState.value.copy(
                isValidatingLocation = false,
                isCheckingIn = true
            )
            
            // Obter studentId
            val studentId = getStudentId(user.id, trainingPlanRepository)
            
            when (val result = checkInService.checkIn(studentId, userLocationModel)) {
                is com.tadevolta.gym.data.models.Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCheckingIn = false,
                        isOutOfRange = false,
                        locationError = null
                    )
                    loadCheckInData() // Recarregar dados
                }
                is com.tadevolta.gym.data.models.Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCheckingIn = false,
                        locationError = result.exception.message ?: "Erro ao fazer check-in"
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isCheckingIn = false,
                        locationError = "Erro desconhecido"
                    )
                }
            }
        }
    }
    
    fun clearLocationError() {
        _uiState.value = _uiState.value.copy(
            locationError = null,
            isOutOfRange = false
        )
    }
    
    fun retryLocationValidation() {
        performCheckIn()
    }
}
