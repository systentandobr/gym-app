package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class GamificationData(
    val userId: String,
    val totalPoints: Int,
    val level: Int,
    val xp: Int,
    val xpToNextLevel: Int,
    val achievements: List<Achievement> = emptyList(),
    val completedTasks: List<String> = emptyList(),
    val ranking: RankingPosition? = null
)

@Serializable
data class RankingPosition(
    val position: Int,
    val totalPoints: Int,
    val level: Int,
    val unitId: String,
    val unitName: String,
    val userId: String? = null,
    val userName: String? = null
)

@Serializable
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val rarity: AchievementRarity,
    val unlockedAt: String? = null
)

@Serializable
enum class AchievementRarity {
    @Serializable(with = AchievementRaritySerializer::class)
    COMMON,
    @Serializable(with = AchievementRaritySerializer::class)
    RARE,
    @Serializable(with = AchievementRaritySerializer::class)
    EPIC,
    @Serializable(with = AchievementRaritySerializer::class)
    LEGENDARY
}

@Serializable
data class ShareableProgress(
    val imageUrl: String,
    val text: String,
    val stats: ProgressStats
)

@Serializable
data class ProgressStats(
    val totalCheckIns: Int,
    val currentStreak: Int,
    val level: Int,
    val totalPoints: Int,
    val completedWorkouts: Int,
    val completedExercises: Int
)

@Serializable
data class WeeklyActivity(
    val period: ActivityPeriod,
    val dailyActivity: List<DailyActivity>,
    val summary: ActivitySummary
)

@Serializable
data class ActivityPeriod(
    val startDate: String,
    val endDate: String
)

@Serializable
data class DailyActivity(
    val date: String, // YYYY-MM-DD
    val dayOfWeek: String, // DOM, SEG, TER, QUA, QUI, SEX, SAB
    val checkIns: Int,
    val workoutsCompleted: Int,
    val exercisesCompleted: Int,
    val totalPoints: Int,
    val activities: List<Activity> = emptyList()
)

@Serializable
data class Activity(
    val type: String, // CHECK_IN, WORKOUT_COMPLETION, EXERCISE_COMPLETION
    val time: String, // HH:mm
    val points: Int,
    val description: String
)

@Serializable
data class ActivitySummary(
    val totalCheckIns: Int,
    val totalWorkouts: Int,
    val totalExercises: Int,
    val totalPoints: Int,
    val averagePointsPerDay: Int
)

// Serializers
object AchievementRaritySerializer : KSerializer<AchievementRarity> {
    override val descriptor: SerialDescriptor = serialDescriptor<String>()
    
    override fun serialize(encoder: Encoder, value: AchievementRarity) {
        encoder.encodeString(value.name.lowercase())
    }
    
    override fun deserialize(decoder: Decoder): AchievementRarity {
        return when (decoder.decodeString().lowercase()) {
            "common" -> AchievementRarity.COMMON
            "rare" -> AchievementRarity.RARE
            "epic" -> AchievementRarity.EPIC
            "legendary" -> AchievementRarity.LEGENDARY
            else -> AchievementRarity.COMMON
        }
    }
}
