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

interface TrainingService {
    suspend fun createTrainingExecution(planId: String): Result<TrainingExecution>
    suspend fun updateExerciseExecution(
        trainingId: String,
        exerciseId: String,
        executedSets: List<ExecutedSet>
    ): Result<TrainingExecution>
    suspend fun completeTrainingExecution(
        trainingId: String,
        totalDurationSeconds: Int? = null
    ): Result<TrainingExecution>
    suspend fun getActiveTrainingExecution(): Result<TrainingExecution?>
    suspend fun getTrainingExecutionsByPlan(planId: String): Result<List<TrainingExecution>>
    suspend fun getTrainingExecutionById(trainingId: String): Result<TrainingExecution>
}

class TrainingServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: suspend () -> String?,
    private val authRepository: AuthRepository? = null,
    private val tokenManager: TokenManager? = null
) : TrainingService {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private suspend inline fun <reified T> parseResponse(
        response: HttpResponse,
        errorPrefix: String
    ): Result<T> {
        if (response.status.value >= 400) {
            val errorBody = try {
                response.bodyAsText()
            } catch (e: Exception) {
                null
            }
            return Result.Error(Exception("$errorPrefix: ${errorBody ?: "Erro do servidor"}"))
        }

        val bodyString = response.bodyAsText()
        return try {
            // Tentar deserializar como ApiResponse primeiro
            val apiResponse = json.decodeFromString<ApiResponse<T>>(bodyString)
            if (apiResponse.success) {
                @Suppress("UNCHECKED_CAST")
                Result.Success(apiResponse.data as T)
            } else {
                Result.Error(Exception(apiResponse.error ?: apiResponse.message ?: errorPrefix))
            }
        } catch (e: Exception) {
            // Se falhar, tentar deserializar diretamente como T
            try {
                val data = json.decodeFromString<T>(bodyString)
                Result.Success(data)
            } catch (e2: Exception) {
                Result.Error(Exception("$errorPrefix: Erro ao processar resposta do servidor"))
            }
        }
    }
    
    override suspend fun createTrainingExecution(planId: String): Result<TrainingExecution> {
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
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/trainings/executions")
                        }
                        method = HttpMethod.Post
                        
                        contentType(ContentType.Application.Json)
                        setBody(mapOf("trainingPlanId" to planId))
                    },
                    responseHandler = { it }
                )
            } else {
                client.post("${EnvironmentConfig.API_BASE_URL}/trainings/executions") {
                    
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("trainingPlanId" to planId))
                }
            }
            
            parseResponse(response, "Erro ao criar execução de treino")
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateExerciseExecution(
        trainingId: String,
        exerciseId: String,
        executedSets: List<ExecutedSet>
    ): Result<TrainingExecution> {
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
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/trainings/executions/$trainingId/exercises/$exerciseId")
                        }
                        method = HttpMethod.Patch
                        
                        contentType(ContentType.Application.Json)
                        setBody(mapOf("executedSets" to executedSets))
                    },
                    responseHandler = { it }
                )
            } else {
                client.patch("${EnvironmentConfig.API_BASE_URL}/trainings/executions/$trainingId/exercises/$exerciseId") {
                    
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("executedSets" to executedSets))
                }
            }
            
            parseResponse(response, "Erro ao atualizar execução do exercício")
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun completeTrainingExecution(
        trainingId: String,
        totalDurationSeconds: Int?
    ): Result<TrainingExecution> {
        return try {
            val body = if (totalDurationSeconds != null) {
                mapOf("totalDurationSeconds" to totalDurationSeconds)
            } else {
                emptyMap<String, Any>()
            }
            
            val response = if (authRepository != null || tokenManager != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenManager = tokenManager,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url("${EnvironmentConfig.API_BASE_URL}/trainings/executions/$trainingId/complete")
                        method = HttpMethod.Patch
                        
                        contentType(ContentType.Application.Json)
                        setBody(body)
                    },
                    responseHandler = { it }
                )
            } else {
                client.patch("${EnvironmentConfig.API_BASE_URL}/trainings/executions/$trainingId/complete") {
                    
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            }
            
            parseResponse(response, "Erro ao finalizar execução de treino")
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getActiveTrainingExecution(): Result<TrainingExecution?> {
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
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/trainings/executions/active")
                        }
                        method = HttpMethod.Get
                        
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/trainings/executions/active") {
                    
                }
            }
            
            if (response.status.value == 404) {
                 return Result.Success(null)
            }

            parseResponse(response, "Erro ao buscar execução ativa")
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getTrainingExecutionsByPlan(planId: String): Result<List<TrainingExecution>> {
        return try {
            val response = if (authRepository != null || tokenManager != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenManager = tokenManager,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url("${EnvironmentConfig.API_BASE_URL}/trainings/executions/plan/$planId")
                        method = HttpMethod.Get
                        
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/trainings/executions/plan/$planId") {
                    
                }
            }
            
            parseResponse(response, "Erro ao buscar histórico de execuções")
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getTrainingExecutionById(trainingId: String): Result<TrainingExecution> {
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
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/trainings/executions/$trainingId")
                        }
                        method = HttpMethod.Get
                        
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/trainings/executions/$trainingId") {
                    
                }
            }
            
            parseResponse(response, "Erro ao buscar execução de treino")
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
