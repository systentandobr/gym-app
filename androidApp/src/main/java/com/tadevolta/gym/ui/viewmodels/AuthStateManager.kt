package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthStateManager @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _isAuthenticated = MutableStateFlow<Boolean?>(null) // null = checking
    val isAuthenticated: StateFlow<Boolean?> = _isAuthenticated.asStateFlow()
    
    init {
        checkAuthState()
        // Observar mudanças no estado de autenticação do AuthRepository
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                when (authState) {
                    is com.tadevolta.gym.utils.auth.AuthState.Unauthenticated -> {
                        _isAuthenticated.value = false
                    }
                    is com.tadevolta.gym.utils.auth.AuthState.Authenticated -> {
                        _isAuthenticated.value = true
                    }
                    else -> {
                        // Loading ou Error - não mudar estado de autenticação
                    }
                }
            }
        }
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            // Verificar se há token válido e usuário em cache
            val hasValidToken = authRepository.isTokenValid()
            val cachedUser = authRepository.getCachedUser()
            
            if (hasValidToken && cachedUser != null) {
                // Tentar refresh se necessário
                val refreshed = authRepository.refreshTokenIfNeeded()
                _isAuthenticated.value = refreshed || hasValidToken
            } else {
                // Se não tem token válido, tentar refresh
                if (!hasValidToken) {
                    val refreshed = authRepository.refreshTokenIfNeeded()
                    _isAuthenticated.value = refreshed
                } else {
                    _isAuthenticated.value = false
                }
            }
        }
    }
    
    fun refreshAuthState() {
        checkAuthState()
    }
}
