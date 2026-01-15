package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.data.repositories.AuthRepository
import com.tadevolta.gym.data.remote.CheckInService
import com.tadevolta.gym.data.remote.GamificationService
import com.tadevolta.gym.data.repositories.TrainingPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val user: User? = null,
    val checkInStats: CheckInStats? = null,
    val currentTrainingPlan: TrainingPlan? = null,
    val gamificationData: GamificationData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val checkInService: CheckInService,
    private val gamificationService: GamificationService,
    private val trainingPlanRepository: TrainingPlanRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Carregar usuário atual
            when (val userResult = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(user = userResult.data)
                    loadUserSpecificData(userResult.data)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = userResult.exception.message,
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }
    
    private suspend fun loadUserSpecificData(user: User) {
        user.unitId?.let {
            // Carregar check-in stats
            checkInService.getCheckInStats(user.id).let { result ->
                if (result is Result.Success) {
                    _uiState.value = _uiState.value.copy(checkInStats = result.data)
                }
            }
            
            // Carregar gamificação
            gamificationService.getGamificationData(user.id).let { result ->
                if (result is Result.Success) {
                    _uiState.value = _uiState.value.copy(gamificationData = result.data)
                }
            }
            
            // Carregar plano de treino atual
            trainingPlanRepository.getTrainingPlans(user.id).collect { plans ->
                _uiState.value = _uiState.value.copy(
                    currentTrainingPlan = plans.firstOrNull(),
                    isLoading = false
                )
            }
        }
    }
    
    fun checkIn() {
        viewModelScope.launch {
            val userId = _uiState.value.user?.id ?: return@launch
            when (val result = checkInService.checkIn(userId, null)) {
                is Result.Success -> {
                    loadDashboardData() // Recarregar dados
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.exception.message)
                }
                else -> {}
            }
        }
    }
}
