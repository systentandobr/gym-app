package com.tadevolta.gym.data.remote

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
    private val tokenProvider: () -> String?,
    private val authRepository: com.tadevolta.gym.data.repositories.AuthRepository? = null
) : UserService {
    
    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = if (authRepository != null) {
                // Usar helper com retry automático
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url("${EnvironmentConfig.API_BASE_URL}/users/profile")
                        method = HttpMethod.Get
                        headers {
                            tokenProvider()?.let { append("Authorization", "Bearer $it") }
                        }
                    },
                    responseHandler = { it }
                )
            } else {
                // Fallback sem retry
                client.get("${EnvironmentConfig.API_BASE_URL}/users/profile") {
                    headers {
                        tokenProvider()?.let { append("Authorization", "Bearer $it") }
                    }
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
            val response = if (authRepository != null) {
                // Usar helper com retry automático
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url("${EnvironmentConfig.API_BASE_URL}/users/profile")
                        method = HttpMethod.Patch
                        headers {
                            tokenProvider()?.let { append("Authorization", "Bearer $it") }
                        }
                        contentType(ContentType.Application.Json)
                        setBody(data)
                    },
                    responseHandler = { it }
                )
            } else {
                // Fallback sem retry
                client.patch("${EnvironmentConfig.API_BASE_URL}/users/profile") {
                    headers {
                        tokenProvider()?.let { append("Authorization", "Bearer $it") }
                    }
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
