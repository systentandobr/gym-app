package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import com.tadevolta.gym.utils.config.EnvironmentConfig

interface AuthService {
    suspend fun login(username: String, password: String, domain: String): Result<LoginResponse>
    suspend fun signUp(
        name: String, 
        email: String, 
        password: String,
        unitName: String? = null,
        unitId: String? = null
    ): Result<LoginResponse>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(refreshToken: String): Result<AuthTokens>
}

class AuthServiceImpl(
    private val client: HttpClient
) : AuthService {
    
    override suspend fun login(username: String, password: String, domain: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(
                username = username,
                password = password,
                domain = domain
            )
            
            // Tentar primeiro com SYS-SEGURANÇA
            val response = try {
                client.post("${EnvironmentConfig.SYS_SEGURANCA_BASE_URL}/api/v1/auth/login") {
                    headers {
                        append("X-API-Key", EnvironmentConfig.SYS_SEGURANCA_API_KEY)
                        append("Content-Type", "application/json")
                    }
                    contentType(io.ktor.http.ContentType.Application.Json)
                    setBody(request)
                }
            } catch (e: Exception) {
                // Fallback para API tradicional
                client.post("${EnvironmentConfig.SYS_SEGURANCA_BASE_URL}/api/v1/auth/login") {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    setBody(request)
                }
            }
            
            // A API retorna diretamente LoginApiResponse, não ApiResponse
            val apiResponse: LoginApiResponse = response.body()
            
            // Extrair expiração do token JWT (iat + 1200 minutos = 1200 * 60 segundos)
            // Por padrão, vamos usar 1200 minutos (1200 * 60 segundos) se não conseguir decodificar
            val expiresAt = System.currentTimeMillis() / 1000 + (1200 * 60) // 1200 minutos a partir de agora
            
            // Mapear para o formato esperado
            val profile = apiResponse.user.profile
            val primaryRole = apiResponse.user.roles.firstOrNull()?.name ?: "user"
            
            val user = User(
                id = apiResponse.user.id,
                name = "${profile?.firstName ?: ""} ${profile?.lastName ?: ""}".trim().takeIf { it.isNotBlank() } 
                    ?: apiResponse.user.username,
                email = apiResponse.user.email,
                role = primaryRole,
                unitId = profile?.unitId,
                avatar = profile?.avatar,
                phone = null, // Não vem na resposta de login
                status = UserStatus.ACTIVE, // Assumir ACTIVE se não vier
                emailVerified = true, // Assumir verificado se fez login
                createdAt = null,
                updatedAt = null
            )
            
            val tokens = AuthTokens(
                token = apiResponse.accessToken,
                refreshToken = apiResponse.refreshToken,
                expiresAt = expiresAt
            )
            
            val loginResponse = LoginResponse(
                user = user,
                tokens = tokens
            )
            
            Result.Success(loginResponse)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun signUp(
        name: String, 
        email: String, 
        password: String,
        unitName: String?,
        unitId: String?
    ): Result<LoginResponse> {
        return try {
            // Dividir nome em firstName e lastName
            val nameParts = name.trim().split(" ", limit = 2)
            val firstName = nameParts.firstOrNull() ?: ""
            val lastName = nameParts.getOrNull(1) ?: ""
            val username = email.split("@").firstOrNull() ?: email
            
            val request = SignUpRequest(
                username = username,
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                country = "BR",
                state = "",
                zipCode = "",
                localNumber = unitId ?: "",
                unitName = unitName ?: "",
                address = "",
                complement = "",
                neighborhood = "",
                city = "",
                latitude = null,
                longitude = null,
                domain = EnvironmentConfig.DOMAIN
            )
            
            val response = try {
                client.post("${EnvironmentConfig.SYS_SEGURANCA_BASE_URL}/api/v1/auth/register") {
                    headers {
                        append("X-API-Key", EnvironmentConfig.SYS_SEGURANCA_API_KEY)
                        append("Content-Type", "application/json")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            } catch (e: Exception) {
                // Fallback para API tradicional
                client.post("${EnvironmentConfig.SYS_SEGURANCA_BASE_URL}/api/v1/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            }
            
            val apiResponse: ApiResponse<LoginResponse> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao criar conta"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            client.post("${EnvironmentConfig.SYS_SEGURANCA_BASE_URL}/api/v1/auth/logout")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun refreshToken(refreshToken: String): Result<AuthTokens> {
        return try {
            val response = client.post("${EnvironmentConfig.SYS_SEGURANCA_BASE_URL}/api/v1/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("refreshToken" to refreshToken))
            }
            val apiResponse: ApiResponse<AuthTokens> = response.body()
            
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
