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
    // Removido executedSets - agora está em TrainingExecution
    val imageUrl: String? = null, // GIF ou imagem (mantido para compatibilidade)
    val images: List<String>? = null, // Array de imagens do catálogo
    val videoUrl: String? = null,
    // Campos do catálogo de exercícios
    val description: String? = null,
    val muscleGroups: List<String>? = null,
    val equipment: List<String>? = null,
    val difficulty: ExerciseDifficulty? = null
) {
    /**
     * Propriedade de conveniência para obter a URL da primeira imagem.
     * Prioriza o array `images` se disponível, caso contrário usa `imageUrl`.
     */
    val primaryImageUrl: String? get() = images?.firstOrNull() ?: imageUrl
}

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

@Serializable(with = TrainingPlanStatusSerializer::class)
enum class TrainingPlanStatus {
    ACTIVE,
    PAUSED,
    COMPLETED
}

@Serializable(with = ExerciseDifficultySerializer::class)
enum class ExerciseDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

/**
 * Modelo para exercício do catálogo retornado pela API /exercises/{id}
 * Tem campos diferentes do modelo Exercise usado em planos de treino
 */
@Serializable
data class CatalogExercise(
    val id: String,
    val unitId: String,
    val name: String,
    val description: String? = null,
    val muscleGroups: List<String>? = null,
    val equipment: List<String>? = null,
    val defaultSets: Int? = null,
    val defaultReps: String? = null,
    val defaultRestTime: Int? = null,
    val difficulty: ExerciseDifficulty? = null,
    val targetGender: String? = null, // "male", "female", "other"
    val images: List<String>? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    /**
     * Converte CatalogExercise para Exercise usado em planos de treino
     */
    fun toExercise(): Exercise {
        return Exercise(
            exerciseId = id,
            name = name,
            sets = defaultSets ?: 3,
            reps = defaultReps ?: "10-12",
            weight = null,
            restTime = defaultRestTime,
            notes = description,
            imageUrl = images?.firstOrNull(),
            images = images,
            videoUrl = null,
            description = description,
            muscleGroups = muscleGroups,
            equipment = equipment,
            difficulty = difficulty
        )
    }
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
