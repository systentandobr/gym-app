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
    
    /**
     * Força logout e limpa todos os dados de autenticação.
     * Usado quando a reautenticação falha completamente.
     */
    suspend fun forceLogout() {
        _authState.value = AuthState.Loading
        authService.logout()
        tokenStorage.clearTokens()
        userSessionStorage?.clearCachedCredentials()
        _authState.value = AuthState.Unauthenticated
    }
    
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
        unitId: String? = null,
        address: String? = null,
        city: String? = null,
        state: String? = null,
        zipCode: String? = null,
        neighborhood: String? = null,
        complement: String? = null,
        localNumber: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<User> {
        _authState.value = AuthState.Loading
        
        return when (val result = authService.signUp(
            name, email, password, unitName, unitId,
            address, city, state, zipCode, neighborhood, complement, localNumber, latitude, longitude
        )) {
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
        userSessionStorage?.clearCachedCredentials()
        _authState.value = AuthState.Unauthenticated
    }

    suspend fun updateProfile(data: UpdateUserData): Result<User> {
        return when (val result = userService.updateProfile(data)) {
            is Result.Success -> {
                tokenStorage.saveUser(result.data)
                _authState.value = AuthState.Authenticated(result.data)
                Result.Success(result.data)
            }
            is Result.Error -> result
            else -> Result.Error(Exception("Erro desconhecido"))
        }
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
                // Se refresh token falhar, tentar reautenticação com cache
                when (val reauthResult = reauthenticateWithCache()) {
                    is Result.Success -> true
                    else -> false
                }
            }
        }
    }
    
    /**
     * Tenta reautenticar usando credenciais em cache.
     * Retorna Result.Success se a reautenticação for bem-sucedida,
     * Result.Error caso contrário.
     */
    suspend fun reauthenticateWithCache(): Result<User> {
        val userSessionStorage = userSessionStorage ?: return Result.Error(
            Exception("UserSessionStorage não disponível")
        )
        
        // Buscar credenciais em cache
        val cachedCredentials = userSessionStorage.getCachedCredentials()
            ?: return Result.Error(
                Exception("Credenciais em cache não encontradas")
            )
        
        // Verificar se cache é válido
        if (!cachedCredentials.isValid()) {
            userSessionStorage.clearCachedCredentials()
            return Result.Error(
                Exception("Cache de credenciais expirado")
            )
        }
        
        // Tentar login com credenciais em cache
        return when (val result = authService.login(
            username = cachedCredentials.username,
            password = cachedCredentials.password,
            domain = EnvironmentConfig.DOMAIN
        )) {
            is Result.Success -> {
                // Salvar novos tokens
                tokenStorage.saveTokens(result.data.tokens)
                tokenStorage.saveUser(result.data.user)
                
                // Atualizar credenciais em cache com novo timestamp
                userSessionStorage.saveCredentials(
                    username = cachedCredentials.username,
                    email = cachedCredentials.email,
                    password = cachedCredentials.password
                )
                
                _authState.value = AuthState.Authenticated(result.data.user)
                Result.Success(result.data.user)
            }
            is Result.Error -> {
                // Se login falhar, limpar cache de credenciais
                userSessionStorage.clearCachedCredentials()
                logout()
                result
            }
            else -> {
                userSessionStorage.clearCachedCredentials()
                logout()
                Result.Error(Exception("Erro desconhecido ao reautenticar"))
            }
        }
    }
    
    suspend fun getAccessToken(): String? {
        return tokenStorage.getAccessToken()
    }
}
