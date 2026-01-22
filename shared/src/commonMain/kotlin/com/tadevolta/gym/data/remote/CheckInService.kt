package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.tadevolta.gym.utils.config.EnvironmentConfig
import kotlinx.serialization.json.Json

interface CheckInService {
    suspend fun checkIn(studentId: String, location: Location?): Result<CheckIn>
    suspend fun getCheckInStats(studentId: String): Result<CheckInStats>
    suspend fun getCheckInHistory(studentId: String, limit: Int = 50, startDate: String? = null, endDate: String? = null): Result<CheckInHistory>
}

class CheckInServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: () -> String?
) : CheckInService {
    
    override suspend fun checkIn(studentId: String, location: Location?): Result<CheckIn> {
        return try {
            val requestBody = buildMap<String, Any?> {
                location?.let {
                    put("location", mapOf(
                        "lat" to it.lat,
                        "lng" to it.lng
                    ))
                }
            }
            
            val response = client.post("${EnvironmentConfig.API_BASE_URL}/gamification/students/$studentId/check-ins") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            val apiResponse: ApiResponse<CheckIn> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao fazer check-in"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getCheckInStats(studentId: String): Result<CheckInStats> {
        return try {
            // Buscar histórico e extrair stats
            when (val historyResult = getCheckInHistory(studentId, limit = 1)) {
                is Result.Success -> {
                    val history = historyResult.data
                    // Calcular checkInsLast365Days - pode ser melhorado com endpoint específico
                    val checkInsLast365Days = history.total
                    Result.Success(CheckInStats.fromHistory(history, checkInsLast365Days))
                }
                is Result.Error -> {
                    Result.Error(historyResult.exception)
                }
                else -> {
                    Result.Error(Exception("Erro desconhecido ao buscar estatísticas"))
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getCheckInHistory(
        studentId: String,
        limit: Int,
        startDate: String?,
        endDate: String?
    ): Result<CheckInHistory> {
        return try {
            val response = client.get("${EnvironmentConfig.API_BASE_URL}/gamification/students/$studentId/check-ins") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                parameter("limit", limit)
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
            }
            val apiResponse: ApiResponse<CheckInHistory> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar histórico de check-in"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
