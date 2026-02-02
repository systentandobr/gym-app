package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.tadevolta.gym.utils.config.EnvironmentConfig
import kotlinx.serialization.json.*

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
            val jsonBody = buildJsonObject {
                location?.let {
                    putJsonObject("location") {
                        put("lat", it.lat)
                        put("lng", it.lng)
                    }
                }
            }
            
            val response = client.post("${EnvironmentConfig.API_BASE_URL}/gamification/students/$studentId/check-in") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
            }
            
            // Tratar erro 404 separadamente
            if (response.status.value == 404) {
                try {
                    val errorResponse: ErrorResponse = response.body()
                    val errorMessage = errorResponse.formattedMessage 
                        ?: errorResponse.message 
                        ?: errorResponse.error 
                        ?: "Endpoint de check-in não está disponível no momento."
                    return Result.Error(Exception(errorMessage))
                } catch (e: Exception) {
                    // Se não conseguir deserializar como ErrorResponse, usar mensagem genérica mais clara
                    return Result.Error(Exception(
                        "O serviço de check-in não está disponível no momento. " +
                        "Por favor, tente novamente mais tarde ou entre em contato com o suporte."
                    ))
                }
            }
            
            // Tratar outros erros HTTP antes de tentar deserializar
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                
                // Tentar deserializar como objeto de erro para identificar tipo
                val errorType = try {
                    val errorJson = errorBody?.let { 
                        kotlinx.serialization.json.Json.parseToJsonElement(it).jsonObject
                    }
                    val errorCode = errorJson?.get("error")?.jsonPrimitive?.content
                    
                    when (errorCode) {
                        "LOCATION_OUT_OF_RANGE" -> com.tadevolta.gym.data.models.CheckInErrorType.LOCATION_OUT_OF_RANGE
                        "TRAINING_IN_PROGRESS" -> com.tadevolta.gym.data.models.CheckInErrorType.TRAINING_IN_PROGRESS
                        "CHECK_IN_ALREADY_DONE" -> com.tadevolta.gym.data.models.CheckInErrorType.ALREADY_DONE
                        else -> com.tadevolta.gym.data.models.CheckInErrorType.GENERIC
                    }
                } catch (e: Exception) {
                    com.tadevolta.gym.data.models.CheckInErrorType.GENERIC
                }
                
                val errorMessage = try {
                    val errorJson = errorBody?.let { 
                        kotlinx.serialization.json.Json.parseToJsonElement(it).jsonObject
                    }
                    errorJson?.get("message")?.jsonPrimitive?.content 
                        ?: "Erro ao fazer check-in: ${errorBody ?: "Erro do servidor (${response.status.value})"}"
                } catch (e: Exception) {
                    "Erro ao fazer check-in: ${errorBody ?: "Erro do servidor (${response.status.value})"}"
                }
                
                return Result.Error(com.tadevolta.gym.data.models.CheckInException(
                    errorMessage,
                    errorType
                ))
            }
            
            // Tentar deserializar como ApiResponse apenas se não houver erros HTTP
            val apiResponse: ApiResponse<CheckIn> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao fazer check-in"))
            }
        } catch (e: io.ktor.serialization.JsonConvertException) {
            // Erro de deserialização - pode ser formato inesperado da resposta
            Result.Error(Exception("Erro ao processar resposta do servidor: ${e.message}"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getCheckInStats(studentId: String): Result<CheckInStats> {
        return try {
            // Buscar histórico completo para calcular stats
            when (val historyResult = getCheckInHistory(studentId, limit = 100)) {
                is Result.Success -> {
                    val history = historyResult.data
                    // Calcular checkInsLast365Days usando o total do histórico
                    val checkInsLast365Days = history.total
                    Result.Success(CheckInStats.fromHistory(history, checkInsLast365Days))
                }
                is Result.Error -> {
                    // Se houver erro, retornar stats vazios ao invés de quebrar
                    Result.Success(CheckInStats(
                        totalCheckIns = 0,
                        currentStreak = 0,
                        longestStreak = 0,
                        checkInsThisYear = 0,
                        checkInsLast365Days = 0
                    ))
                }
                else -> {
                    Result.Success(CheckInStats(
                        totalCheckIns = 0,
                        currentStreak = 0,
                        longestStreak = 0,
                        checkInsThisYear = 0,
                        checkInsLast365Days = 0
                    ))
                }
            }
        } catch (e: Exception) {
            // Em caso de erro, retornar stats vazios ao invés de quebrar
            Result.Success(CheckInStats(
                totalCheckIns = 0,
                currentStreak = 0,
                longestStreak = 0,
                checkInsThisYear = 0,
                checkInsLast365Days = 0
            ))
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
            
            // Tratar erro 404 - aluno não encontrado ou sem check-ins
            if (response.status.value == 404) {
                // Retornar histórico vazio ao invés de erro
                return Result.Success(CheckInHistory(
                    checkIns = emptyList(),
                    total = 0,
                    currentStreak = 0,
                    longestStreak = 0
                ))
            }
            
            val apiResponse: ApiResponse<CheckInHistory> = response.body()
            
            if (apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar histórico de check-in"))
            }
        } catch (e: Exception) {
            // Se for erro de deserialização ou 404, retornar histórico vazio
            Result.Success(CheckInHistory(
                checkIns = emptyList(),
                total = 0,
                currentStreak = 0,
                longestStreak = 0
            ))
        }
    }
}
