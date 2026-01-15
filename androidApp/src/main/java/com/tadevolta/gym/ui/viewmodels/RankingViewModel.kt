package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.GamificationData
import com.tadevolta.gym.data.models.RankingPosition
import com.tadevolta.gym.data.remote.GamificationService
import com.tadevolta.gym.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RankingViewModel(
    private val gamificationService: GamificationService,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _ranking = MutableStateFlow<List<RankingPosition>>(emptyList())
    val ranking: StateFlow<List<RankingPosition>> = _ranking.asStateFlow()
    
    private val _userGamificationData = MutableStateFlow<GamificationData?>(null)
    val userGamificationData: StateFlow<GamificationData?> = _userGamificationData.asStateFlow()
    
    init {
        loadRankingData()
    }
    
    private fun loadRankingData() {
        viewModelScope.launch {
            val user = when (val result = authRepository.getCurrentUser()) {
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
            
            // Carregar ranking
            user.unitId?.let { unitId ->
                when (val result = gamificationService.getRanking(unitId, 50)) {
                    is com.tadevolta.gym.data.models.Result.Success -> {
                        _ranking.value = result.data
                    }
                    else -> {}
                }
            }
        }
    }
}
