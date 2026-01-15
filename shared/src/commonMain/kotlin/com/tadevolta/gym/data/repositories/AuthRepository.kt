package com.tadevolta.gym.data.repositories

import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.data.remote.AuthService
import com.tadevolta.gym.data.remote.UserService
import com.tadevolta.gym.utils.auth.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TokenStorage {
    suspend fun saveTokens(tokens: AuthTokens)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
}

class AuthRepository(
    private val authService: AuthService,
    private val userService: UserService,
    private val tokenStorage: TokenStorage
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    suspend fun login(email: String, password: String): Result<User> {
        _authState.value = AuthState.Loading
        
        return when (val result = authService.login(email, password)) {
            is Result.Success -> {
                tokenStorage.saveTokens(result.data.tokens)
                _authState.value = AuthState.Authenticated(result.data.user)
                Result.Success(result.data.user)
            }
            is Result.Error -> {
                _authState.value = AuthState.Error(result.exception.message ?: "Erro ao fazer login")
                result
            }
            else -> {
                _authState.value = AuthState.Error("Erro desconhecido")
                Result.Error(Exception("Erro desconhecido"))
            }
        }
    }
    
    suspend fun logout() {
        _authState.value = AuthState.Loading
        authService.logout()
        tokenStorage.clearTokens()
        _authState.value = AuthState.Unauthenticated
    }
    
    suspend fun getCurrentUser(): Result<User> {
        return userService.getCurrentUser()
    }
    
    suspend fun refreshTokenIfNeeded(): Boolean {
        val refreshToken = tokenStorage.getRefreshToken() ?: return false
        
        return when (val result = authService.refreshToken(refreshToken)) {
            is Result.Success -> {
                tokenStorage.saveTokens(result.data)
                true
            }
            else -> {
                logout()
                false
            }
        }
    }
    
    suspend fun getAccessToken(): String? {
        return tokenStorage.getAccessToken()
    }
}
