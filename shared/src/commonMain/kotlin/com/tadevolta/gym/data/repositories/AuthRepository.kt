package com.tadevolta.gym.data.repositories

import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.data.remote.AuthService
import com.tadevolta.gym.data.remote.UserService
import com.tadevolta.gym.utils.auth.AuthState
import com.tadevolta.gym.utils.config.EnvironmentConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val authService: AuthService,
    private val userService: UserService,
    private val tokenStorage: TokenStorage,
    private val userSessionStorage: UserSessionStorage? = null // Opcional para compatibilidade
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    suspend fun login(email: String, password: String, domain: String): Result<User> {
        _authState.value = AuthState.Loading
        
        return when (val result = authService.login(email, password, domain)) {
            is Result.Success -> {
                tokenStorage.saveTokens(result.data.tokens)
                tokenStorage.saveUser(result.data.user)
                // Salvar unitId no UserSessionStorage se disponível
                result.data.user.unitId?.let { unitId ->
                    userSessionStorage?.let { storage ->
                        kotlinx.coroutines.runBlocking {
                            storage.saveSelectedUnit(unitId, result.data.user.name)
                        }
                    }
                }
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
    
    suspend fun signUp(
        name: String, 
        email: String, 
        password: String,
        unitName: String? = null,
        unitId: String? = null
    ): Result<User> {
        _authState.value = AuthState.Loading
        
        return when (val result = authService.signUp(name, email, password, unitName, unitId)) {
            is Result.Success -> {
                tokenStorage.saveTokens(result.data.tokens)
                tokenStorage.saveUser(result.data.user)
                // Salvar unitId no UserSessionStorage
                val finalUnitId = unitId ?: result.data.user.unitId
                finalUnitId?.let { id ->
                    userSessionStorage?.let { storage ->
                        kotlinx.coroutines.runBlocking {
                            storage.saveSelectedUnit(id, unitName ?: result.data.user.name)
                        }
                    }
                }
                _authState.value = AuthState.Authenticated(result.data.user)
                Result.Success(result.data.user)
            }
            is Result.Error -> {
                _authState.value = AuthState.Error(result.exception.message ?: "Erro ao criar conta")
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
    
    suspend fun getCachedUser(): User? {
        return tokenStorage.getUser()
    }
    
    suspend fun isTokenValid(): Boolean {
        val token = tokenStorage.getAccessToken()
        return token != null
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
