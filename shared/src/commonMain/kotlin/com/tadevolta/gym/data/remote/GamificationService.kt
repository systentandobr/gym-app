package com.tadevolta.gym.data.remote

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

interface GamificationService {
    suspend fun getGamificationData(userId: String): Result<GamificationData>
    suspend fun getRanking(unitId: String, limit: Int = 50): Result<List<RankingPosition>>
    suspend fun getWeeklyActivity(studentId: String): Result<WeeklyActivity>
    suspend fun shareProgress(userId: String): Result<ShareableProgress>
    suspend fun getTeamMetrics(teamId: String): Result<TeamMetrics>
    suspend fun getTeamsRanking(unitId: String): Result<List<TeamRankingPosition>>
}

class GamificationServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: () -> String?,
    private val authRepository: AuthRepository? = null
) : GamificationService {
    
    override suspend fun getGamificationData(userId: String): Result<GamificationData> {
        return try {
            val response = if (authRepository != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/gamification/students/$userId")
                        }
                        method = HttpMethod.Get
                        headers {
                            tokenProvider()?.let { append("Authorization", "Bearer $it") }
                        }
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/gamification/students/$userId") {
                    headers {
                        tokenProvider()?.let { append("Authorization", "Bearer $it") }
                    }
                }
            }
            
            // Tratar erro 404 separadamente
            if (response.status.value == 404) {
                try {
                    val errorResponse: ErrorResponse = response.body()
                    val errorMessage = errorResponse.formattedMessage 
                        ?: errorResponse.message 
                        ?: errorResponse.error 
                        ?: "Usuário não encontrado"
                    return Result.Error(Exception(errorMessage))
                } catch (e: Exception) {
                    return Result.Error(Exception("Usuário não encontrado (404)"))
                }
            }
            
            // Tratar erro 500 ou outros erros HTTP
            if (response.status.value >= 400) {
                // Se houver erro do servidor, retornar erro mas não quebrar
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                return Result.Error(Exception("Erro ao buscar dados de gamificação: ${errorBody ?: "Erro do servidor"}"))
            }
            
            val apiResponse: ApiResponse<GamificationData> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar dados de gamificação"))
            }
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: io.ktor.serialization.JsonConvertException) {
            Result.Error(Exception("Erro ao processar resposta do servidor: ${e.message}"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getRanking(unitId: String, limit: Int): Result<List<RankingPosition>> {
        return try {
            val response = if (authRepository != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/gamification/ranking")
                        }
                        parameter("unitId", unitId)
                        parameter("limit", limit)
                        method = HttpMethod.Get
                        headers {
                            tokenProvider()?.let { append("Authorization", "Bearer $it") }
                        }
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/gamification/ranking") {
                    headers {
                        tokenProvider()?.let { append("Authorization", "Bearer $it") }
                    }
                    parameter("unitId", unitId)
                    parameter("limit", limit)
                }
            }
            
            // Tratar erro 404
            if (response.status.value == 404) {
                try {
                    val errorResponse: ErrorResponse = response.body()
                    val errorMessage = errorResponse.formattedMessage 
                        ?: errorResponse.message 
                        ?: errorResponse.error 
                        ?: "Ranking não encontrado"
                    return Result.Error(Exception(errorMessage))
                } catch (e: Exception) {
                    return Result.Error(Exception("Ranking não encontrado (404)"))
                }
            }
            
            val apiResponse: ApiResponse<List<RankingPosition>> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar ranking"))
            }
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: io.ktor.serialization.JsonConvertException) {
            Result.Error(Exception("Erro ao processar resposta do servidor: ${e.message}"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getWeeklyActivity(studentId: String): Result<WeeklyActivity> {
        return try {
            val response = if (authRepository != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url("${EnvironmentConfig.API_BASE_URL}/gamification/students/$studentId/weekly-activity")
                        method = HttpMethod.Get
                        headers {
                            tokenProvider()?.let { append("Authorization", "Bearer $it") }
                        }
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/gamification/students/$studentId/weekly-activity") {
                    headers {
                        tokenProvider()?.let { append("Authorization", "Bearer $it") }
                    }
                }
            }
            
            // Tratar erro 404
            if (response.status.value == 404) {
                try {
                    val errorResponse: ErrorResponse = response.body()
                    val errorMessage = errorResponse.formattedMessage 
                        ?: errorResponse.message 
                        ?: errorResponse.error 
                        ?: "Atividade semanal não encontrada"
                    return Result.Error(Exception(errorMessage))
                } catch (e: Exception) {
                    return Result.Error(Exception("Atividade semanal não encontrada (404)"))
                }
            }
            
            val apiResponse: ApiResponse<WeeklyActivity> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar atividade semanal"))
            }
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: io.ktor.serialization.JsonConvertException) {
            Result.Error(Exception("Erro ao processar resposta do servidor: ${e.message}"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun shareProgress(userId: String): Result<ShareableProgress> {
        return try {
            val response = if (authRepository != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/gamification/students/$userId/share")
                        }
                        method = HttpMethod.Post
                        headers {
                            tokenProvider()?.let { append("Authorization", "Bearer $it") }
                        }
                    },
                    responseHandler = { it }
                )
            } else {
                client.post("${EnvironmentConfig.API_BASE_URL}/gamification/students/$userId/share") {
                    headers {
                        tokenProvider()?.let { append("Authorization", "Bearer $it") }
                    }
                }
            }
            
            // Tratar erro 404
            if (response.status.value == 404) {
                try {
                    val errorResponse: ErrorResponse = response.body()
                    val errorMessage = errorResponse.formattedMessage 
                        ?: errorResponse.message 
                        ?: errorResponse.error 
                        ?: "Usuário não encontrado"
                    return Result.Error(Exception(errorMessage))
                } catch (e: Exception) {
                    return Result.Error(Exception("Usuário não encontrado (404)"))
                }
            }
            
            val apiResponse: ApiResponse<ShareableProgress> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao gerar compartilhamento"))
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
            val response = if (authRepository != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url("${EnvironmentConfig.API_BASE_URL}/gamification/teams/$teamId/metrics")
                        method = HttpMethod.Get
                        headers {
                            tokenProvider()?.let { append("Authorization", "Bearer $it") }
                        }
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/gamification/teams/$teamId/metrics") {
                    headers {
                        tokenProvider()?.let { append("Authorization", "Bearer $it") }
                    }
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
            
            val apiResponse: ApiResponse<TeamMetrics> = response.body()
            
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
    
    override suspend fun getTeamsRanking(unitId: String): Result<List<TeamRankingPosition>> {
        return try {
            val response = if (authRepository != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/gamification/teams/ranking")
                        }
                        parameter("unitId", unitId)
                        method = HttpMethod.Get
                        headers {
                            tokenProvider()?.let { append("Authorization", "Bearer $it") }
                        }
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/gamification/teams/ranking") {
                    headers {
                        tokenProvider()?.let { append("Authorization", "Bearer $it") }
                    }
                    parameter("unitId", unitId)
                }
            }
            
            // Tratar erro 404
            if (response.status.value == 404) {
                try {
                    val errorResponse: ErrorResponse = response.body()
                    val errorMessage = errorResponse.formattedMessage 
                        ?: errorResponse.message 
                        ?: errorResponse.error 
                        ?: "Ranking de times não encontrado"
                    return Result.Error(Exception(errorMessage))
                } catch (e: Exception) {
                    return Result.Error(Exception("Ranking de times não encontrado (404)"))
                }
            }
            
            // Tratar outros erros HTTP
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                return Result.Error(Exception("Erro ao buscar ranking de times: ${errorBody ?: "Erro do servidor"}"))
            }
            
            val apiResponse: ApiResponse<List<TeamRankingPosition>> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar ranking de times"))
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
