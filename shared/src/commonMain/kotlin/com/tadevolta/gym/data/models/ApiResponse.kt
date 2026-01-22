package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val message: String? = null
)

@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val total: Int,
    val page: Int,
    val limit: Int
)

@Serializable
data class TrainingPlansResponse(
    val plans: List<com.tadevolta.gym.data.models.TrainingPlan>,
    val total: Int,
    val page: Int,
    val limit: Int
)

// Result wrapper para uso interno
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
