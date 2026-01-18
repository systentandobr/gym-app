package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

interface CheckInService {
    suspend fun checkIn(studentId: String, location: Location?): Result<CheckIn>
    suspend fun getCheckInStats(studentId: String): Result<CheckInStats>
    suspend fun getCheckInHistory(studentId: String, limit: Int = 30): Result<List<CheckIn>>
}

class CheckInServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: () -> String?
) : CheckInService {
    
    override suspend fun checkIn(studentId: String, location: Location?): Result<CheckIn> {
        return try {
            val response = client.post("/students/$studentId/check-in") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                setBody(mapOf(
                    "location" to location
                ))
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<CheckIn> = json.decodeFromString(response.bodyAsText())
            
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
            val response = client.get("/students/$studentId/check-in/stats") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<CheckInStats> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar estatísticas de check-in"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getCheckInHistory(studentId: String, limit: Int): Result<List<CheckIn>> {
        return try {
            val response = client.get("/students/$studentId/check-in/history") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                parameter("limit", limit)
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<List<CheckIn>> = json.decodeFromString(response.bodyAsText())
            
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
