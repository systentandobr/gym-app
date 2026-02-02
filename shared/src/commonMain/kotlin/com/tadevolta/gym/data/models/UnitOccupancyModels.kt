package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class UnitOccupancyStatus {
    NOT_BUSY,           // Não muito movimentado
    MODERATELY_BUSY,    // Moderadamente movimentado
    BUSY,               // Movimentado
    VERY_BUSY           // Muito movimentado
}

@Serializable
data class PeakHoursData(
    val hour: Int,              // Hora do dia (0-23)
    val checkInCount: Int,      // Número de check-ins nesta hora
    val averageCheckIns: Double  // Média histórica de check-ins nesta hora
)

@Serializable
data class UnitOccupancyResponse(
    val unitId: String,
    val dayOfWeek: Int,          // 0=Domingo, 1=Segunda, etc.
    val peakHours: List<PeakHoursData>,
    val currentStatus: UnitOccupancyStatus,
    val currentHour: Int,        // Hora atual (0-23)
    val averageStayMinutes: Int  // Tempo médio de permanência em minutos
)
