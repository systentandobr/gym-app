package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Sessão de treino executada (execução de um TrainingPlan)
 */
@Serializable
data class TrainingExecution(
    val id: String,
    val trainingPlanId: String,
    val userId: String,
    val unitId: String,
    val startedAt: String,
    val completedAt: String? = null,
    @Serializable(with = TrainingExecutionStatusSerializer::class)
    val status: TrainingExecutionStatus,
    val exercises: List<ExerciseExecution> = emptyList(),
    val totalDurationSeconds: Int? = null,
    val metadata: Map<String, String>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Execução de um exercício dentro de uma TrainingExecution
 */
@Serializable
data class ExerciseExecution(
    val exerciseId: String? = null,
    val name: String,
    val executedSets: List<ExecutedSet> = emptyList()
)

/**
 * Status de uma execução de treino
 */
@Serializable(with = TrainingExecutionStatusSerializer::class)
enum class TrainingExecutionStatus {
    IN_PROGRESS,
    COMPLETED,
    ABANDONED
}

/**
 * Serializer para TrainingExecutionStatus
 */
object TrainingExecutionStatusSerializer : KSerializer<TrainingExecutionStatus> {
    override val descriptor: SerialDescriptor = serialDescriptor<String>()

    override fun serialize(encoder: Encoder, value: TrainingExecutionStatus) {
        val stringValue = when (value) {
            TrainingExecutionStatus.IN_PROGRESS -> "in_progress"
            TrainingExecutionStatus.COMPLETED -> "completed"
            TrainingExecutionStatus.ABANDONED -> "abandoned"
        }
        encoder.encodeString(stringValue)
    }

    override fun deserialize(decoder: Decoder): TrainingExecutionStatus {
        val stringValue = decoder.decodeString()
        return when (stringValue.lowercase()) {
            "in_progress" -> TrainingExecutionStatus.IN_PROGRESS
            "completed" -> TrainingExecutionStatus.COMPLETED
            "abandoned" -> TrainingExecutionStatus.ABANDONED
            else -> throw IllegalArgumentException("Unknown TrainingExecutionStatus: $stringValue")
        }
    }
}
