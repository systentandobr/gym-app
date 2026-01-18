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
