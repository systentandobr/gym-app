package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Team(
    val id: String,
    val unitId: String,
    val name: String,
    val description: String? = null,
    val studentIds: List<String> = emptyList(),
    val students: List<Student>? = null,
    val metrics: TeamMetrics? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class TeamMetrics(
    val totalStudents: Int,
    val totalCheckIns: Int,
    val completedTrainings: Int,
    val plannedTrainings: Int,
    val completionRate: Double,
    val averagePoints: Double,
    val currentStreak: Int
)

@Serializable
data class TeamRankingPosition(
    val position: Int,
    val teamId: String,
    val teamName: String,
    val totalCheckIns: Int,
    val completionRate: Double,
    val averagePoints: Double,
    val totalStudents: Int
)
