package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PrivacySettings(
    val publicProfile: Boolean = true,
    val showAchievementsInRanking: Boolean = true,
    val shareWorkouts: Boolean = false,
    val marketing: Boolean = true
)
