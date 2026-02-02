package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
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
    private val tokenProvider: () -> String?
) : TrainingService {
    
    override suspend fun createTrainingExecution(planId: String): Result<TrainingExecution> {
        return try {
            val response = client.post("${EnvironmentConfig.API_BASE_URL}/trainings/executions") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                contentType(ContentType.Application.Json)
                setBody(mapOf("trainingPlanId" to planId))
            }
            
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                return Result.Error(Exception("Erro ao criar execução de treino: ${errorBody ?: "Erro do servidor"}"))
            }
            
            // Tentar deserializar como ApiResponse primeiro
            try {
                val apiResponse: ApiResponse<TrainingExecution> = response.body()
                if (apiResponse.success && apiResponse.data != null) {
                    return Result.Success(apiResponse.data)
                } else {
                    return Result.Error(Exception(apiResponse.error ?: "Erro ao criar execução de treino"))
                }
            } catch (e: Exception) {
                // Se falhar, tentar deserializar diretamente como TrainingExecution
                try {
                    val trainingExecution: TrainingExecution = response.body()
                    return Result.Success(trainingExecution)
                } catch (e2: Exception) {
                    return Result.Error(Exception("Erro ao deserializar resposta: ${e2.message}"))
                }
            }
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
            val response = client.patch("${EnvironmentConfig.API_BASE_URL}/trainings/executions/$trainingId/exercises/$exerciseId") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                contentType(ContentType.Application.Json)
                setBody(mapOf("executedSets" to executedSets))
            }
            
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                return Result.Error(Exception("Erro ao atualizar execução do exercício: ${errorBody ?: "Erro do servidor"}"))
            }
            
            val apiResponse: ApiResponse<TrainingExecution> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao atualizar execução do exercício"))
            }
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
            
            val response = client.patch("${EnvironmentConfig.API_BASE_URL}/trainings/executions/$trainingId/complete") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                return Result.Error(Exception("Erro ao finalizar execução de treino: ${errorBody ?: "Erro do servidor"}"))
            }
            
            // Tentar deserializar como ApiResponse primeiro
            try {
                val apiResponse: ApiResponse<TrainingExecution> = response.body()
                if (apiResponse.success && apiResponse.data != null) {
                    return Result.Success(apiResponse.data)
                } else {
                    return Result.Error(Exception(apiResponse.error ?: "Erro ao finalizar execução de treino"))
                }
            } catch (e: Exception) {
                // Se falhar, tentar deserializar diretamente como TrainingExecution
                try {
                    val trainingExecution: TrainingExecution = response.body()
                    return Result.Success(trainingExecution)
                } catch (e2: Exception) {
                    return Result.Error(Exception("Erro ao deserializar resposta: ${e2.message}"))
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getActiveTrainingExecution(): Result<TrainingExecution?> {
        return try {
            val response = client.get("${EnvironmentConfig.API_BASE_URL}/trainings/executions/active") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            
            if (response.status.value == 404) {
                // Não há execução ativa
                return Result.Success(null)
            }
            
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                return Result.Error(Exception("Erro ao buscar execução ativa: ${errorBody ?: "Erro do servidor"}"))
            }
            
            val apiResponse: ApiResponse<TrainingExecution?> = response.body()
            
            if (apiResponse.success) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar execução ativa"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getTrainingExecutionsByPlan(planId: String): Result<List<TrainingExecution>> {
        return try {
            val response = client.get("${EnvironmentConfig.API_BASE_URL}/trainings/executions/plan/$planId") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                return Result.Error(Exception("Erro ao buscar histórico de execuções: ${errorBody ?: "Erro do servidor"}"))
            }
            
            // Tentar deserializar como ApiResponse primeiro
            try {
                val apiResponse: ApiResponse<List<TrainingExecution>> = response.body()
                if (apiResponse.success && apiResponse.data != null) {
                    return Result.Success(apiResponse.data)
                } else {
                    return Result.Error(Exception(apiResponse.error ?: "Erro ao buscar histórico de execuções"))
                }
            } catch (e: Exception) {
                // Se falhar, tentar deserializar diretamente como List
                try {
                    val executions: List<TrainingExecution> = response.body()
                    return Result.Success(executions)
                } catch (e2: Exception) {
                    return Result.Error(Exception("Erro ao deserializar resposta: ${e2.message}"))
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getTrainingExecutionById(trainingId: String): Result<TrainingExecution> {
        return try {
            val response = client.get("${EnvironmentConfig.API_BASE_URL}/trainings/executions/$trainingId") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                return Result.Error(Exception("Erro ao buscar execução de treino: ${errorBody ?: "Erro do servidor"}"))
            }
            
            // Tentar deserializar como ApiResponse primeiro
            try {
                val apiResponse: ApiResponse<TrainingExecution> = response.body()
                if (apiResponse.success && apiResponse.data != null) {
                    return Result.Success(apiResponse.data)
                } else {
                    return Result.Error(Exception(apiResponse.error ?: "Erro ao buscar execução de treino"))
                }
            } catch (e: Exception) {
                // Se falhar, tentar deserializar diretamente como TrainingExecution
                try {
                    val trainingExecution: TrainingExecution = response.body()
                    return Result.Success(trainingExecution)
                } catch (e2: Exception) {
                    return Result.Error(Exception("Erro ao deserializar resposta: ${e2.message}"))
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
