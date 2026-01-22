package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.GamificationData
import com.tadevolta.gym.data.models.User
import com.tadevolta.gym.data.remote.GamificationService
import com.tadevolta.gym.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gamificationService: GamificationService
) : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _gamificationData = MutableStateFlow<GamificationData?>(null)
    val gamificationData: StateFlow<GamificationData?> = _gamificationData.asStateFlow()
    
    init {
        loadProfileData()
    }
    
    private fun loadProfileData() {
        viewModelScope.launch {
            // Carregar usuário atual
            when (val result = authRepository.getCurrentUser()) {
                is com.tadevolta.gym.data.models.Result.Success -> {
                    _user.value = result.data
                    
                    // Carregar dados de gamificação
                    when (val gamificationResult = gamificationService.getGamificationData(result.data.id)) {
                        is com.tadevolta.gym.data.models.Result.Success -> {
                            _gamificationData.value = gamificationResult.data
                        }
                        else -> {}
                    }
                }
                else -> {}
            }
        }
    }
}
