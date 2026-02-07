package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.manager.TokenManager
import com.tadevolta.gym.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.tadevolta.gym.utils.config.EnvironmentConfig
import kotlinx.serialization.json.Json

interface UserService {
    suspend fun getCurrentUser(): Result<User>
    suspend fun updateProfile(data: UpdateUserData): Result<User>
}

class UserServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: suspend () -> String?,
    private val authRepository: com.tadevolta.gym.data.repositories.AuthRepository? = null,
    private val tokenManager: TokenManager? = null
) : UserService {
    
    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = if (authRepository != null || tokenManager != null) {
                // Usar helper com retry automático
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenManager = tokenManager,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/users/profile")
                        }
                        method = HttpMethod.Get
                        
                    },
                    responseHandler = { it }
                )
            } else {
                // Fallback sem retry
                client.get("${EnvironmentConfig.API_BASE_URL}/users/profile") {
                    
                }
            }
            
            val apiResponse: ApiResponse<User> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar usuário"))
            }
        } catch (e: com.tadevolta.gym.utils.auth.UnauthenticatedException) {
            // Propagar exceção de autenticação
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateProfile(data: UpdateUserData): Result<User> {
        return try {
            val response = if (authRepository != null || tokenManager != null) {
                // Usar helper com retry automático
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenManager = tokenManager,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/users/profile")
                        }
                        method = HttpMethod.Patch
                        
                        contentType(ContentType.Application.Json)
                        setBody(data)
                    },
                    responseHandler = { it }
                )
            } else {
                // Fallback sem retry
                client.patch("${EnvironmentConfig.API_BASE_URL}/users/profile") {
                    
                    contentType(ContentType.Application.Json)
                    setBody(data)
                }
            }
            
            val apiResponse: ApiResponse<User> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao atualizar perfil"))
            }
        } catch (e: com.tadevolta.gym.utils.auth.UnauthenticatedException) {
            // Propagar exceção de autenticação
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
