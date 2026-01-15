package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

interface TrainingPlanService {
    suspend fun getTrainingPlans(studentId: String): Result<List<TrainingPlan>>
    suspend fun getTrainingPlanById(id: String): Result<TrainingPlan>
    suspend fun updateExerciseExecution(
        planId: String,
        exerciseId: String,
        executedSets: List<ExecutedSet>
    ): Result<TrainingPlan>
}

class TrainingPlanServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: () -> String?
) : TrainingPlanService {
    
    override suspend fun getTrainingPlans(studentId: String): Result<List<TrainingPlan>> {
        return try {
            val response = client.get("/training-plans") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                parameter("studentId", studentId)
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<List<TrainingPlan>> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar planos de treino"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getTrainingPlanById(id: String): Result<TrainingPlan> {
        return try {
            val response = client.get("/training-plans/$id") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<TrainingPlan> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar plano de treino"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateExerciseExecution(
        planId: String,
        exerciseId: String,
        executedSets: List<ExecutedSet>
    ): Result<TrainingPlan> {
        return try {
            val response = client.patch("/training-plans/$planId/exercises/$exerciseId") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                setBody(mapOf("executedSets" to executedSets))
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<TrainingPlan> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao atualizar execução do exercício"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
