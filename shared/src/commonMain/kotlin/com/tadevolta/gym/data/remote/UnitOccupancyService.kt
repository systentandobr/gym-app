package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.manager.TokenManager
import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.data.repositories.AuthRepository
import com.tadevolta.gym.utils.auth.UnauthenticatedException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.tadevolta.gym.utils.config.EnvironmentConfig
import kotlinx.serialization.json.*
import kotlinx.datetime.*
import io.ktor.client.statement.*
import io.ktor.client.call.*

interface UnitOccupancyService {
    suspend fun getUnitOccupancy(unitId: String, dayOfWeek: Int? = null): Result<UnitOccupancyResponse>
}

class UnitOccupancyServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: suspend () -> String?,
    private val authRepository: AuthRepository? = null,
    private val tokenManager: TokenManager? = null
) : UnitOccupancyService {
    
    override suspend fun getUnitOccupancy(unitId: String, dayOfWeek: Int?): Result<UnitOccupancyResponse> {
        return try {
            val response = if (authRepository != null || tokenManager != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenManager = tokenManager,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/gamification/units/$unitId/occupancy")
                        }
                        method = HttpMethod.Get
                        
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/gamification/units/$unitId/occupancy") {
                    
                }
            }
            
            if (response.status.value >= 400) {
                // Se houver erro, retornar dados vazios
                val currentDayOfWeek = dayOfWeek ?: getCurrentDayOfWeek()
                return Result.Success(createEmptyOccupancyResponse(unitId, currentDayOfWeek))
            }
            
            // Deserializar resposta do backend usando Json
            val responseText = response.bodyAsText()
            val jsonElement = Json.parseToJsonElement(responseText)
            val jsonObject = jsonElement.jsonObject
            
            val hourlyData = jsonObject["hourlyData"]?.jsonArray ?: kotlinx.serialization.json.JsonArray(emptyList())
            val currentHour = jsonObject["currentHour"]?.jsonPrimitive?.int ?: 0
            val currentStatusStr = jsonObject["currentStatus"]?.jsonPrimitive?.content ?: "NOT_BUSY"
            
            // Converter status string para enum
            val currentStatus = when (currentStatusStr) {
                "VERY_BUSY" -> UnitOccupancyStatus.VERY_BUSY
                "BUSY" -> UnitOccupancyStatus.BUSY
                "MODERATELY_BUSY" -> UnitOccupancyStatus.MODERATELY_BUSY
                else -> UnitOccupancyStatus.NOT_BUSY
            }
            
            // Converter hourlyData para PeakHoursData
            val peakHours = hourlyData.mapNotNull { hourDataJson ->
                val hourData = hourDataJson.jsonObject
                PeakHoursData(
                    hour = hourData["hour"]?.jsonPrimitive?.int ?: 0,
                    checkInCount = hourData["checkInCount"]?.jsonPrimitive?.int ?: 0,
                    averageCheckIns = hourData["checkInCount"]?.jsonPrimitive?.double ?: 0.0 // Por enquanto usar mesmo valor
                )
            }
            
            // Calcular dia da semana atual
            val currentDayOfWeek = dayOfWeek ?: getCurrentDayOfWeek()
            
            // Tempo médio de permanência (estimativa: 30 a 40 minutos por padrão)
            val averageStayMinutes = 40
            
            Result.Success(
                UnitOccupancyResponse(
                    unitId = unitId,
                    dayOfWeek = currentDayOfWeek,
                    peakHours = peakHours,
                    currentStatus = currentStatus,
                    currentHour = currentHour,
                    averageStayMinutes = averageStayMinutes
                )
            )
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            // Em caso de erro, retornar dados vazios ao invés de quebrar
            val currentDayOfWeek = dayOfWeek ?: getCurrentDayOfWeek()
            Result.Success(createEmptyOccupancyResponse(unitId, currentDayOfWeek))
        }
    }
    
    private fun getCurrentDayOfWeek(): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return now.dayOfWeek.ordinal // 0=Domingo, 1=Segunda, etc.
    }
    
    private fun calculateAverageByHour(checkIns: List<CheckIn>, dayOfWeek: Int): Map<Int, Double> {
        val hourCounts = mutableMapOf<Int, MutableList<Int>>()
        
        checkIns.forEach { checkIn ->
            val dateTime = Instant.parse(checkIn.date)
            val localDateTime = dateTime.toLocalDateTime(TimeZone.currentSystemDefault())
            val checkInDayOfWeek = localDateTime.dayOfWeek.ordinal
            
            if (checkInDayOfWeek == dayOfWeek) {
                val hour = localDateTime.hour
                if (!hourCounts.containsKey(hour)) {
                    hourCounts[hour] = mutableListOf()
                }
                // Simplificação: assumir um check-in por dia nesta hora
                hourCounts[hour]?.add(1)
            }
        }
        
        return hourCounts.mapValues { (_, counts) ->
            counts.sum().toDouble() / counts.size.coerceAtLeast(1)
        }
    }
    
    private fun calculateCurrentStatus(currentCount: Int, average: Double): UnitOccupancyStatus {
        if (average == 0.0) {
            return if (currentCount > 0) UnitOccupancyStatus.BUSY else UnitOccupancyStatus.NOT_BUSY
        }
        
        val ratio = currentCount / average
        return when {
            ratio >= 1.5 -> UnitOccupancyStatus.VERY_BUSY
            ratio >= 1.0 -> UnitOccupancyStatus.BUSY
            ratio >= 0.5 -> UnitOccupancyStatus.MODERATELY_BUSY
            else -> UnitOccupancyStatus.NOT_BUSY
        }
    }
    
    private fun createEmptyOccupancyResponse(unitId: String, dayOfWeek: Int): UnitOccupancyResponse {
        val currentHour = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .hour
        
        return UnitOccupancyResponse(
            unitId = unitId,
            dayOfWeek = dayOfWeek,
            peakHours = (0..23).map { hour ->
                PeakHoursData(
                    hour = hour,
                    checkInCount = 0,
                    averageCheckIns = 0.0
                )
            },
            currentStatus = UnitOccupancyStatus.NOT_BUSY,
            currentHour = currentHour,
            averageStayMinutes = 40
        )
    }
}
