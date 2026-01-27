package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class TrainingPlan(
    val id: String,
    val unitId: String,
    val studentId: String,
    val name: String,
    val description: String? = null,
    val objectives: List<String> = emptyList(),
    val weeklySchedule: List<WeeklySchedule> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val startDate: String,
    val endDate: String? = null,
    @Serializable(with = TrainingPlanStatusSerializer::class)
    val status: TrainingPlanStatus,
    val progress: TrainingPlanProgress? = null,
    val isTemplate: Boolean = false,
    val targetGender: Gender? = null,
    val templateId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class WeeklySchedule(
    val dayOfWeek: Int, // 0-6 (domingo-sábado)
    val timeSlots: List<TimeSlot> = emptyList(),
    val exercises: List<Exercise> = emptyList()
)

@Serializable
data class TimeSlot(
    val startTime: String, // HH:mm
    val endTime: String, // HH:mm
    val activity: String
)

@Serializable
data class Exercise(
    val exerciseId: String? = null,
    val name: String,
    val sets: Int,
    val reps: String, // "10-12" ou "até a falha"
    val weight: Double? = null,
    val restTime: Int? = null, // segundos
    val notes: String? = null,
    // Campos para execução
    val executedSets: List<ExecutedSet>? = null,
    val imageUrl: String? = null, // GIF ou imagem
    val videoUrl: String? = null,
    // Campos do catálogo de exercícios
    val description: String? = null,
    val muscleGroups: List<String>? = null,
    val equipment: List<String>? = null,
    val difficulty: ExerciseDifficulty? = null
)

@Serializable
data class ExecutedSet(
    val setNumber: Int,
    val plannedReps: String,
    val executedReps: Int? = null,
    val plannedWeight: Double? = null,
    val executedWeight: Double? = null,
    val completed: Boolean = false,
    val timestamp: String? = null
)

@Serializable
enum class TrainingPlanStatus {
    @Serializable(with = TrainingPlanStatusSerializer::class)
    ACTIVE,
    @Serializable(with = TrainingPlanStatusSerializer::class)
    PAUSED,
    @Serializable(with = TrainingPlanStatusSerializer::class)
    COMPLETED
}

@Serializable
enum class ExerciseDifficulty {
    @Serializable(with = ExerciseDifficultySerializer::class)
    BEGINNER,
    @Serializable(with = ExerciseDifficultySerializer::class)
    INTERMEDIATE,
    @Serializable(with = ExerciseDifficultySerializer::class)
    ADVANCED
}

@Serializable
data class TrainingPlanProgress(
    val completedObjectives: List<String> = emptyList(),
    val lastUpdate: String? = null,
    val notes: String? = null
)

// Serializers
object TrainingPlanStatusSerializer : KSerializer<TrainingPlanStatus> {
    override val descriptor: SerialDescriptor = serialDescriptor<String>()
    
    override fun serialize(encoder: Encoder, value: TrainingPlanStatus) {
        encoder.encodeString(value.name.lowercase())
    }
    
    override fun deserialize(decoder: Decoder): TrainingPlanStatus {
        return when (decoder.decodeString().lowercase()) {
            "active" -> TrainingPlanStatus.ACTIVE
            "paused" -> TrainingPlanStatus.PAUSED
            "completed" -> TrainingPlanStatus.COMPLETED
            else -> TrainingPlanStatus.ACTIVE
        }
    }
}

object ExerciseDifficultySerializer : KSerializer<ExerciseDifficulty> {
    override val descriptor: SerialDescriptor = serialDescriptor<String>()
    
    override fun serialize(encoder: Encoder, value: ExerciseDifficulty) {
        encoder.encodeString(value.name.lowercase())
    }
    
    override fun deserialize(decoder: Decoder): ExerciseDifficulty {
        return when (decoder.decodeString().lowercase()) {
            "beginner" -> ExerciseDifficulty.BEGINNER
            "intermediate" -> ExerciseDifficulty.INTERMEDIATE
            "advanced" -> ExerciseDifficulty.ADVANCED
            else -> ExerciseDifficulty.BEGINNER
        }
    }
}
