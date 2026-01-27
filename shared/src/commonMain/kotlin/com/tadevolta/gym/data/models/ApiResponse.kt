package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val message: String? = null
)

// Modelo para erros HTTP que não seguem o formato ApiResponse padrão
// Suporta message como String ou List<String>
@Serializable(with = ErrorResponseSerializer::class)
data class ErrorResponse(
    val message: String? = null,
    val messageList: List<String>? = null,
    val error: String? = null,
    val statusCode: Int? = null
) {
    // Propriedade computada para obter mensagem formatada
    val formattedMessage: String?
        get() = when {
            !messageList.isNullOrEmpty() -> messageList.joinToString("\n")
            !message.isNullOrBlank() -> message
            else -> null
        }
}

// Custom serializer para ErrorResponse que suporta message como String ou List<String>
object ErrorResponseSerializer : KSerializer<ErrorResponse> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ErrorResponse") {
        element<String?>("message", isOptional = true)
        element<List<String>?>("messageList", isOptional = true)
        element<String?>("error", isOptional = true)
        element<Int?>("statusCode", isOptional = true)
    }
    
    override fun serialize(encoder: Encoder, value: ErrorResponse) {
        encoder as JsonEncoder
        val jsonObject = buildJsonObject {
            value.message?.let { put("message", it) }
            value.messageList?.let { put("message", JsonArray(it.map { msg -> JsonPrimitive(msg) })) }
            value.error?.let { put("error", it) }
            value.statusCode?.let { put("statusCode", it) }
        }
        encoder.encodeJsonElement(jsonObject)
    }
    
    override fun deserialize(decoder: Decoder): ErrorResponse {
        decoder as JsonDecoder
        val json = decoder.decodeJsonElement() as? JsonObject ?: return ErrorResponse()
        
        val message: String? = json["message"]?.let { element ->
            when (element) {
                is JsonPrimitive -> if (element.isString) element.content else null
                is JsonArray -> null // Será tratado como messageList
                else -> null
            }
        }
        
        val messageList: List<String>? = json["message"]?.let { element ->
            when (element) {
                is JsonArray -> element.mapNotNull { 
                    (it as? JsonPrimitive)?.contentOrNull 
                }
                else -> null
            }
        }
        
        val error = (json["error"] as? JsonPrimitive)?.contentOrNull
        val statusCode = (json["statusCode"] as? JsonPrimitive)?.intOrNull
        
        return ErrorResponse(
            message = message,
            messageList = messageList,
            error = error,
            statusCode = statusCode
        )
    }
}

@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val total: Int,
    val page: Int,
    val limit: Int
)

@Serializable
data class TrainingPlansResponse(
    val data: List<com.tadevolta.gym.data.models.TrainingPlan>,
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
