package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable

@Serializable
data class BioimpedanceMeasurement(
    val id: String,
    val studentId: String,
    val date: String, // ISO 8601 format
    val weight: Double, // kg
    val bodyFat: Double, // %
    val muscle: Double, // kg
    val isBestRecord: Boolean = false
)

@Serializable
data class BioimpedanceHistory(
    val measurements: List<BioimpedanceMeasurement>
)

@Serializable
data class BioimpedanceProgress(
    val period: String, // "6 meses"
    val title: String, // "Progresso Galáctico"
    val weightData: List<DataPoint>,
    val bodyFatData: List<DataPoint>
)

@Serializable
data class DataPoint(
    val month: String, // "MAI", "JUN", etc.
    val value: Double
)
