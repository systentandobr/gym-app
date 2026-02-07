package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.manager.TokenManager
import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.data.repositories.AuthRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.tadevolta.gym.utils.config.EnvironmentConfig
import kotlinx.serialization.json.Json

interface BioimpedanceService {
    suspend fun getHistory(studentId: String): Result<BioimpedanceHistory>
    suspend fun getProgress(studentId: String, period: String): Result<BioimpedanceProgress>
    suspend fun createMeasurement(studentId: String, measurement: BioimpedanceMeasurement): Result<BioimpedanceMeasurement>
}

class BioimpedanceServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: suspend () -> String?,
    private val authRepository: AuthRepository? = null,
    private val tokenManager: TokenManager? = null
) : BioimpedanceService {
    
    override suspend fun getHistory(studentId: String): Result<BioimpedanceHistory> {
        return try {
            val response = executeWithRetry(
                client = client,
                authRepository = authRepository,
                tokenManager = tokenManager,
                tokenProvider = tokenProvider,
                maxRetries = 3,
                requestBuilder = {
                    url("${EnvironmentConfig.API_BASE_URL}/students/$studentId/bioimpedance/history")
                },
                responseHandler = { it }
            )
            
            val apiResponse: ApiResponse<BioimpedanceHistory> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar histórico de bioimpedância"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getProgress(studentId: String, period: String): Result<BioimpedanceProgress> {
        return try {
            val response = executeWithRetry(
                client = client,
                authRepository = authRepository,
                tokenManager = tokenManager,
                tokenProvider = tokenProvider,
                maxRetries = 3,
                requestBuilder = {
                    url("${EnvironmentConfig.API_BASE_URL}/students/$studentId/bioimpedance/progress")
                    parameter("period", period)
                },
                responseHandler = { it }
            )
            
            val apiResponse: ApiResponse<BioimpedanceProgress> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar progresso de bioimpedância"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun createMeasurement(
        studentId: String,
        measurement: BioimpedanceMeasurement
    ): Result<BioimpedanceMeasurement> {
        return try {
            val response = executeWithRetry(
                client = client,
                authRepository = authRepository,
                tokenManager = tokenManager,
                tokenProvider = tokenProvider,
                maxRetries = 3,
                requestBuilder = {
                    method = HttpMethod.Post
                    url("${EnvironmentConfig.API_BASE_URL}/students/$studentId/bioimpedance")
                    contentType(ContentType.Application.Json)
                    setBody(measurement)
                },
                responseHandler = { it }
            )
            
            val apiResponse: ApiResponse<BioimpedanceMeasurement> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao criar avaliação de bioimpedância"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
