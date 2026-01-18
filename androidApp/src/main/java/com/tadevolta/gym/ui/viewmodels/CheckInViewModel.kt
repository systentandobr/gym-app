package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.CheckIn
import com.tadevolta.gym.data.models.CheckInStats
import com.tadevolta.gym.data.remote.CheckInService
import com.tadevolta.gym.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val checkInService: CheckInService,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _checkInStats = MutableStateFlow<CheckInStats?>(null)
    val checkInStats: StateFlow<CheckInStats?> = _checkInStats.asStateFlow()
    
    private val _checkInHistory = MutableStateFlow<List<CheckIn>>(emptyList())
    val checkInHistory: StateFlow<List<CheckIn>> = _checkInHistory.asStateFlow()
    
    private val _isCheckingIn = MutableStateFlow(false)
    val isCheckingIn: StateFlow<Boolean> = _isCheckingIn.asStateFlow()
    
    init {
        loadCheckInData()
    }
    
    private fun loadCheckInData() {
        viewModelScope.launch {
            val user = when (val result = authRepository.getCurrentUser()) {
                is com.tadevolta.gym.data.models.Result.Success -> result.data
                else -> return@launch
            }
            
            // Carregar stats
            when (val result = checkInService.getCheckInStats(user.id)) {
                is com.tadevolta.gym.data.models.Result.Success -> {
                    _checkInStats.value = result.data
                }
                else -> {}
            }
            
            // Carregar histórico
            when (val result = checkInService.getCheckInHistory(user.id, 30)) {
                is com.tadevolta.gym.data.models.Result.Success -> {
                    _checkInHistory.value = result.data
                }
                else -> {}
            }
        }
    }
    
    fun performCheckIn() {
        viewModelScope.launch {
            val user = when (val result = authRepository.getCurrentUser()) {
                is com.tadevolta.gym.data.models.Result.Success -> result.data
                else -> return@launch
            }
            
            _isCheckingIn.value = true
            when (checkInService.checkIn(user.id, null)) {
                is com.tadevolta.gym.data.models.Result.Success -> {
                    loadCheckInData() // Recarregar dados
                }
                else -> {
                    // Erro silencioso - pode ser tratado no futuro com feedback ao usuário
                }
            }
            _isCheckingIn.value = false
        }
    }
}
