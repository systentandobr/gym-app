package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.data.repositories.AuthRepository
import com.tadevolta.gym.data.remote.CheckInService
import com.tadevolta.gym.data.remote.GamificationService
import com.tadevolta.gym.data.repositories.TrainingPlanRepository
import com.tadevolta.gym.utils.getStudentId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val user: User? = null,
    val checkInStats: CheckInStats? = null,
    val currentTrainingPlan: TrainingPlan? = null,
    val gamificationData: GamificationData? = null,
    val weeklyActivity: WeeklyActivity? = null,
    val rankingPreview: List<RankingPosition> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Tentar carregar usuário do cache primeiro
            val cachedUser = authRepository.getCachedUser()
            if (cachedUser != null && authRepository.isTokenValid()) {
                _uiState.value = _uiState.value.copy(user = cachedUser)
                loadUserSpecificData(cachedUser)
            } else {
                // Se não tem cache válido, buscar do servidor
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
                    else -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            }
        }
    }
    
    private suspend fun loadUserSpecificData(user: User) {
        try {
            // Buscar planos de treino primeiro para obter studentId
            val trainingPlansFlow = trainingPlanRepository.getTrainingPlans(null)
            val plans = trainingPlansFlow.first()
            val studentId = plans.firstOrNull()?.studentId ?: user.id // Fallback para userId
            
            // Atualizar plano de treino atual
            _uiState.value = _uiState.value.copy(currentTrainingPlan = plans.firstOrNull())
            
            // Carregar dados em paralelo com studentId obtido
            val checkInStatsResult = checkInService.getCheckInStats(studentId)
            val gamificationResult = gamificationService.getGamificationData(user.id)
            val weeklyActivityResult = gamificationService.getWeeklyActivity(studentId)
            
            // Atualizar check-in stats
            if (checkInStatsResult is Result.Success) {
                _uiState.value = _uiState.value.copy(checkInStats = checkInStatsResult.data)
            }
            
            // Atualizar gamificação
            if (gamificationResult is Result.Success) {
                _uiState.value = _uiState.value.copy(gamificationData = gamificationResult.data)
            }
            
            // Atualizar atividade semanal
            if (weeklyActivityResult is Result.Success) {
                _uiState.value = _uiState.value.copy(weeklyActivity = weeklyActivityResult.data)
            }
            
            // Carregar ranking preview (top 3)
            user.unitId?.let { unitId ->
                when (val rankingResult = gamificationService.getRanking(unitId, limit = 3)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(rankingPreview = rankingResult.data)
                    }
                    else -> {}
                }
            }
            
            // Continuar observando mudanças nos planos em background
            viewModelScope.launch {
                trainingPlansFlow.collect { updatedPlans ->
                    _uiState.value = _uiState.value.copy(
                        currentTrainingPlan = updatedPlans.firstOrNull()
                    )
                }
            }
            
            // Marcar como carregado após todas as operações
            _uiState.value = _uiState.value.copy(isLoading = false)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Erro ao carregar dados: ${e.message}",
                isLoading = false
            )
        }
    }
    
    fun checkIn() {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            // Obter studentId
            val studentId = getStudentId(user.id, trainingPlanRepository)
            
            when (val result = checkInService.checkIn(studentId, null)) {
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
