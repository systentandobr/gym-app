package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

interface SubscriptionService {
    suspend fun getSubscription(studentId: String): Result<StudentSubscription>
    suspend fun getSubscriptionPlans(): Result<List<SubscriptionPlan>>
}

class SubscriptionServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: () -> String?
) : SubscriptionService {
    
    override suspend fun getSubscription(studentId: String): Result<StudentSubscription> {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            // Buscar subscription do student
            val response = client.get("/students/$studentId") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            val studentResponse: ApiResponse<Student> = json.decodeFromString(response.bodyAsText())
            
            if (studentResponse.success && studentResponse.data?.subscription != null) {
                Result.Success(studentResponse.data.subscription)
            } else {
                Result.Error(Exception("Assinatura não encontrada"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getSubscriptionPlans(): Result<List<SubscriptionPlan>> {
        return try {
            val response = client.get("/subscriptions/plans") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<List<SubscriptionPlan>> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar planos de assinatura"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
