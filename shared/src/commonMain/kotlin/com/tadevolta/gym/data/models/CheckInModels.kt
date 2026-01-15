package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CheckIn(
    val id: String,
    val studentId: String,
    val unitId: String,
    val timestamp: String,
    val location: Location? = null
)

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class CheckInStats(
    val totalCheckIns: Int,
    val currentStreak: Int, // dias consecutivos
    val longestStreak: Int,
    val checkInsThisYear: Int, // para 10/365
    val checkInsLast365Days: Int
)
