package com.tadevolta.gym.data.models

/**
 * Exceção customizada para erros de check-in com tipo identificável
 */
class CheckInException(
    message: String,
    val errorType: CheckInErrorType
) : Exception(message)

/**
 * Tipos de erro de check-in
 */
enum class CheckInErrorType {
    LOCATION_OUT_OF_RANGE,
    TRAINING_IN_PROGRESS,
    ALREADY_DONE,
    GENERIC
}
