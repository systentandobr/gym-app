package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
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
}

class GamificationServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: () -> String?
) : GamificationService {
    
    override suspend fun getGamificationData(userId: String): Result<GamificationData> {
        return try {
            val response = client.get("${EnvironmentConfig.API_BASE_URL}/gamification/users/$userId") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
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
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getRanking(unitId: String, limit: Int): Result<List<RankingPosition>> {
        return try {
            val response = client.get("${EnvironmentConfig.API_BASE_URL}/gamification/ranking") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                parameter("unitId", unitId)
                parameter("limit", limit)
            }
            val apiResponse: ApiResponse<List<RankingPosition>> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar ranking"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getWeeklyActivity(studentId: String): Result<WeeklyActivity> {
        return try {
            val response = client.get("${EnvironmentConfig.API_BASE_URL}/gamification/students/$studentId/weekly-activity") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            val apiResponse: ApiResponse<WeeklyActivity> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar atividade semanal"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun shareProgress(userId: String): Result<ShareableProgress> {
        return try {
            val response = client.post("${EnvironmentConfig.API_BASE_URL}/gamification/users/$userId/share") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            val apiResponse: ApiResponse<ShareableProgress> = response.body()
            
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
