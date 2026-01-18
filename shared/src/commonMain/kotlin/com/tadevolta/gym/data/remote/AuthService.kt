package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import com.tadevolta.gym.utils.config.EnvironmentConfig

interface AuthService {
    suspend fun login(email: String, password: String): Result<LoginResponse>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(refreshToken: String): Result<AuthTokens>
}

class AuthServiceImpl(
    private val client: HttpClient
) : AuthService {
    
    override suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(
                email = email,
                password = password,
                domain = EnvironmentConfig.DOMAIN
            )
            val json = Json { ignoreUnknownKeys = true }
            
            // Tentar primeiro com SYS-SEGURANÇA
            val response = try {
                client.post("${EnvironmentConfig.SYS_SEGURANCA_BASE_URL}/auth/login") {
                    headers {
                        append("X-API-Key", EnvironmentConfig.SYS_SEGURANCA_API_KEY)
                    }
                    setBody(request)
                }
            } catch (e: Exception) {
                // Fallback para API tradicional
                client.post("${EnvironmentConfig.API_BASE_URL}/auth/login") {
                    setBody(request)
                }
            }
            
            val apiResponse: ApiResponse<LoginResponse> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao fazer login"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            client.post("${EnvironmentConfig.API_BASE_URL}/auth/logout")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun refreshToken(refreshToken: String): Result<AuthTokens> {
        return try {
            val response = client.post("${EnvironmentConfig.API_BASE_URL}/auth/refresh") {
                setBody(mapOf("refreshToken" to refreshToken))
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<AuthTokens> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao renovar token"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
