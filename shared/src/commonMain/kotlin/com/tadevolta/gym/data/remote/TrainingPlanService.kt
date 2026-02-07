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
import io.ktor.utils.io.*
import com.tadevolta.gym.utils.config.EnvironmentConfig
import kotlinx.serialization.json.Json

interface TrainingPlanService {
    suspend fun getTrainingPlans(studentId: String? = null, status: String? = null): Result<List<TrainingPlan>>
    suspend fun getTrainingPlanById(id: String): Result<TrainingPlan>
}

class TrainingPlanServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: suspend () -> String?,
    private val authRepository: AuthRepository? = null,
    private val tokenManager: TokenManager? = null
) : TrainingPlanService {
    
    override suspend fun getTrainingPlans(studentId: String?, status: String?): Result<List<TrainingPlan>> {
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
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/training-plans")
                        }
                        studentId?.let { parameter("studentId", it) }
                        status?.let { parameter("status", it) }
                        method = HttpMethod.Get
                        
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/training-plans") {
                    
                    studentId?.let { parameter("studentId", it) }
                    status?.let { parameter("status", it) }
                }
            }
            
            // Tratar erros HTTP
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                return Result.Error(Exception("Erro ao buscar planos de treino: ${errorBody ?: "Erro do servidor"}"))
            }
            
            // A API retorna diretamente TrainingPlansResponse com data, total, page, limit
            val plansResponse: TrainingPlansResponse = response.body()
            Result.Success(plansResponse.data)
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            // Em caso de erro de deserialização, retornar lista vazia ao invés de quebrar
            Result.Success(emptyList())
        }
    }
    
    override suspend fun getTrainingPlanById(id: String): Result<TrainingPlan> {
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
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/training-plans/$id")
                        }
                        method = HttpMethod.Get
                        
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/training-plans/$id") {
                    
                }
            }
            
            // Tratar erro 404
            if (response.status.value == 404) {
                try {
                    val errorResponse: ErrorResponse = response.body()
                    val errorMessage = errorResponse.formattedMessage 
                        ?: errorResponse.message 
                        ?: errorResponse.error 
                        ?: "Plano de treino não encontrado"
                    return Result.Error(Exception(errorMessage))
                } catch (e: Exception) {
                    return Result.Error(Exception("Plano de treino não encontrado (404)"))
                }
            }
            
            // Tratar outros erros HTTP
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                return Result.Error(Exception("Erro ao buscar plano de treino: ${errorBody ?: "Erro do servidor"}"))
            }
            
            // A API retorna TrainingPlan diretamente, não envolto em ApiResponse
            // Tentar deserializar diretamente primeiro
            try {
                val trainingPlan: TrainingPlan = response.body()
                return Result.Success(trainingPlan)
            } catch (e: io.ktor.serialization.JsonConvertException) {
                // Se falhar, pode ser que esteja tentando deserializar como ApiResponse
                // Tentar ler como texto e deserializar manualmente
                try {
                    val json = Json { ignoreUnknownKeys = true }
                    val responseText = response.bodyAsText()
                    
                    // Tentar deserializar como TrainingPlan diretamente
                    try {
                        val trainingPlan: TrainingPlan = json.decodeFromString(TrainingPlan.serializer(), responseText)
                        return Result.Success(trainingPlan)
                    } catch (e2: kotlinx.serialization.SerializationException) {
                        // Se falhar, tentar como ApiResponse<TrainingPlan> (fallback)
                        try {
                            val apiResponse: ApiResponse<TrainingPlan> = json.decodeFromString(
                                ApiResponse.serializer(TrainingPlan.serializer()), 
                                responseText
                            )
                            if (apiResponse.success && apiResponse.data != null) {
                                return Result.Success(apiResponse.data)
                            } else {
                                return Result.Error(Exception(apiResponse.error ?: "Erro ao buscar plano de treino"))
                            }
                        } catch (e3: kotlinx.serialization.SerializationException) {
                            return Result.Error(Exception("Erro ao processar resposta do servidor. Formato não reconhecido: ${e2.message}"))
                        }
                    }
                } catch (e2: Exception) {
                    return Result.Error(Exception("Erro ao processar resposta do servidor: ${e.message}"))
                }
            } catch (e: Exception) {
                return Result.Error(Exception("Erro ao buscar plano de treino: ${e.message}"))
            }
        } catch (e: UnauthenticatedException) {
            return Result.Error(e)
        } catch (e: Exception) {
            return Result.Error(Exception("Erro ao buscar plano de treino: ${e.message}"))
        }
    }
    
}
