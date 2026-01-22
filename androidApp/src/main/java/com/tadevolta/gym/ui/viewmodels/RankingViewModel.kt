package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.CheckInStats
import com.tadevolta.gym.data.models.GamificationData
import com.tadevolta.gym.data.models.RankingPosition
import com.tadevolta.gym.data.remote.CheckInService
import com.tadevolta.gym.data.remote.GamificationService
import com.tadevolta.gym.data.repositories.AuthRepository
import com.tadevolta.gym.data.repositories.TrainingPlanRepository
import com.tadevolta.gym.utils.getStudentId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val gamificationService: GamificationService,
    private val checkInService: CheckInService,
    private val authRepository: AuthRepository,
    private val trainingPlanRepository: TrainingPlanRepository
) : ViewModel() {
    
    private val _ranking = MutableStateFlow<List<RankingPosition>>(emptyList())
    val ranking: StateFlow<List<RankingPosition>> = _ranking.asStateFlow()
    
    private val _userGamificationData = MutableStateFlow<GamificationData?>(null)
    val userGamificationData: StateFlow<GamificationData?> = _userGamificationData.asStateFlow()
    
    private val _checkInStats = MutableStateFlow<CheckInStats?>(null)
    val checkInStats: StateFlow<CheckInStats?> = _checkInStats.asStateFlow()
    
    init {
        loadRankingData()
    }
    
    private fun loadRankingData() {
        viewModelScope.launch {
            // Tentar carregar do cache primeiro
            val user = authRepository.getCachedUser() ?: when (val result = authRepository.getCurrentUser()) {
                is com.tadevolta.gym.data.models.Result.Success -> result.data
                else -> return@launch
            }
            
            // Carregar dados do usuário
            when (val result = gamificationService.getGamificationData(user.id)) {
                is com.tadevolta.gym.data.models.Result.Success -> {
                    _userGamificationData.value = result.data
                }
                else -> {}
            }
            
            // Obter studentId
            val studentId = getStudentId(user.id, trainingPlanRepository)
            
            // Carregar check-in stats
            when (val result = checkInService.getCheckInStats(studentId)) {
                is com.tadevolta.gym.data.models.Result.Success -> {
                    _checkInStats.value = result.data
                }
                else -> {}
            }
            
            // Carregar ranking - usar unitId do User ou UserSessionStorage
            val unitId = user.unitId
            if (unitId != null) {
                when (val result = gamificationService.getRanking(unitId, 50)) {
                    is com.tadevolta.gym.data.models.Result.Success -> {
                        _ranking.value = result.data
                    }
                    else -> {}
                }
            }
        }
    }
    
    fun shareProgress() {
        viewModelScope.launch {
            val userId = _userGamificationData.value?.userId ?: return@launch
            when (val result = gamificationService.shareProgress(userId)) {
                is com.tadevolta.gym.data.models.Result.Success -> {
                    // TODO: Implementar compartilhamento (Intent para compartilhar)
                }
                else -> {}
            }
        }
    }
}
