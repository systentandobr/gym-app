package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CheckIn(
    val id: String,
    val studentId: String, // ID do aluno (relacionado ao schema de students)
    val date: String, // ISO 8601
    val points: Int,
    val unitId: String,
    val metadata: CheckInMetadata? = null
)

@Serializable
data class CheckInMetadata(
    val location: Location? = null,
    val device: String? = null
)

@Serializable
data class Location(
    val lat: Double,
    val lng: Double
)

@Serializable
data class CheckInHistory(
    val checkIns: List<CheckIn>,
    val total: Int,
    val currentStreak: Int,
    val longestStreak: Int
)

@Serializable
data class CheckInStats(
    val totalCheckIns: Int,
    val currentStreak: Int, // dias consecutivos
    val longestStreak: Int,
    val checkInsThisYear: Int, // para 10/365
    val checkInsLast365Days: Int
) {
    companion object {
        fun fromHistory(history: CheckInHistory, checkInsLast365Days: Int = 0): CheckInStats {
            return CheckInStats(
                totalCheckIns = history.total,
                currentStreak = history.currentStreak,
                longestStreak = history.longestStreak,
                checkInsThisYear = checkInsLast365Days,
                checkInsLast365Days = checkInsLast365Days
            )
        }
    }
}
