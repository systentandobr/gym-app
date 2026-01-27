package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.tadevolta.gym.utils.config.EnvironmentConfig
import kotlinx.serialization.json.Json

interface TrainingPlanService {
    suspend fun getTrainingPlans(studentId: String? = null, status: String? = null): Result<List<TrainingPlan>>
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
    
    override suspend fun getTrainingPlans(studentId: String?, status: String?): Result<List<TrainingPlan>> {
        return try {
            val response = client.get("${EnvironmentConfig.API_BASE_URL}/training-plans") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                studentId?.let { parameter("studentId", it) }
                status?.let { parameter("status", it) }
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
        } catch (e: Exception) {
            // Em caso de erro de deserialização, retornar lista vazia ao invés de quebrar
            Result.Success(emptyList())
        }
    }
    
    override suspend fun getTrainingPlanById(id: String): Result<TrainingPlan> {
        return try {
            val response = client.get("/training-plans/$id") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            val apiResponse: ApiResponse<TrainingPlan> = response.body()
            
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
            val response = client.patch("${EnvironmentConfig.API_BASE_URL}/training-plans/$planId/exercises/$exerciseId") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                contentType(ContentType.Application.Json)
                setBody(mapOf("executedSets" to executedSets))
            }
            val apiResponse: ApiResponse<TrainingPlan> = response.body()
            
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
