package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

interface GamificationService {
    suspend fun getGamificationData(userId: String): Result<GamificationData>
    suspend fun getRanking(unitId: String, limit: Int = 50): Result<List<RankingPosition>>
    suspend fun shareProgress(userId: String): Result<ShareableProgress>
}

class GamificationServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: () -> String?
) : GamificationService {
    
    override suspend fun getGamificationData(userId: String): Result<GamificationData> {
        return try {
            val response = client.get("/gamification/users/$userId") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<GamificationData> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar dados de gamificação"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getRanking(unitId: String, limit: Int): Result<List<RankingPosition>> {
        return try {
            val response = client.get("/gamification/ranking") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                parameter("unitId", unitId)
                parameter("limit", limit)
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<List<RankingPosition>> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar ranking"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun shareProgress(userId: String): Result<ShareableProgress> {
        return try {
            val response = client.post("/gamification/users/$userId/share") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<ShareableProgress> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao gerar compartilhamento"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
