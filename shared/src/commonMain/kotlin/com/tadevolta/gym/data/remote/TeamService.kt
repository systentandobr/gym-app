package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.manager.TokenManager
import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.data.repositories.AuthRepository
import com.tadevolta.gym.utils.auth.UnauthenticatedException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.tadevolta.gym.utils.config.EnvironmentConfig
import kotlinx.serialization.json.Json

interface TeamService {
    suspend fun getTeams(): Result<List<Team>>
    suspend fun getTeam(id: String): Result<Team>
    suspend fun getTeamMetrics(teamId: String): Result<TeamMetrics>
}

class TeamServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: suspend () -> String?,
    private val authRepository: AuthRepository? = null,
    private val tokenManager: TokenManager? = null
) : TeamService {
    
    override suspend fun getTeams(): Result<List<Team>> {
        return try {
            val response = if (authRepository != null || tokenManager != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenManager = tokenManager,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/teams")
                        }
                        method = HttpMethod.Get
                        
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/teams") {
                    
                }
            }
            
            // Tratar erro 404
            if (response.status.value == 404) {
                try {
                    val errorResponse: ErrorResponse = response.body()
                    val errorMessage = errorResponse.formattedMessage 
                        ?: errorResponse.message 
                        ?: errorResponse.error 
                        ?: "Times não encontrados"
                    return Result.Error(Exception(errorMessage))
                } catch (e: Exception) {
                    return Result.Error(Exception("Times não encontrados (404)"))
                }
            }
            
            // Tratar outros erros HTTP
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                return Result.Error(Exception("Erro ao buscar times: ${errorBody ?: "Erro do servidor"}"))
            }
            
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<List<Team>> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar times"))
            }
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: io.ktor.serialization.JsonConvertException) {
            Result.Error(Exception("Erro ao processar resposta do servidor: ${e.message}"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getTeam(id: String): Result<Team> {
        return try {
            val response = if (authRepository != null || tokenManager != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenManager = tokenManager,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url("${EnvironmentConfig.API_BASE_URL}/teams/$id")
                        method = HttpMethod.Get
                        
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/teams/$id") {
                    
                }
            }
            
            // Tratar erro 404
            if (response.status.value == 404) {
                try {
                    val errorResponse: ErrorResponse = response.body()
                    val errorMessage = errorResponse.formattedMessage 
                        ?: errorResponse.message 
                        ?: errorResponse.error 
                        ?: "Time não encontrado"
                    return Result.Error(Exception(errorMessage))
                } catch (e: Exception) {
                    return Result.Error(Exception("Time não encontrado (404)"))
                }
            }
            
            // Tratar outros erros HTTP
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                return Result.Error(Exception("Erro ao buscar time: ${errorBody ?: "Erro do servidor"}"))
            }
            
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<Team> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar time"))
            }
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: io.ktor.serialization.JsonConvertException) {
            Result.Error(Exception("Erro ao processar resposta do servidor: ${e.message}"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getTeamMetrics(teamId: String): Result<TeamMetrics> {
        return try {
            val response = if (authRepository != null || tokenManager != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenManager = tokenManager,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/teams/$teamId/metrics")
                        }
                        method = HttpMethod.Get
                        
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/teams/$teamId/metrics") {
                    
                }
            }
            
            // Tratar erro 404
            if (response.status.value == 404) {
                try {
                    val errorResponse: ErrorResponse = response.body()
                    val errorMessage = errorResponse.formattedMessage 
                        ?: errorResponse.message 
                        ?: errorResponse.error 
                        ?: "Métricas do time não encontradas"
                    return Result.Error(Exception(errorMessage))
                } catch (e: Exception) {
                    return Result.Error(Exception("Métricas do time não encontradas (404)"))
                }
            }
            
            // Tratar outros erros HTTP
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                return Result.Error(Exception("Erro ao buscar métricas do time: ${errorBody ?: "Erro do servidor"}"))
            }
            
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<TeamMetrics> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar métricas do time"))
            }
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: io.ktor.serialization.JsonConvertException) {
            Result.Error(Exception("Erro ao processar resposta do servidor: ${e.message}"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
