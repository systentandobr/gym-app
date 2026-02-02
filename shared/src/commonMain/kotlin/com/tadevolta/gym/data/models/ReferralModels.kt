package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ReferralRequest(
    val userId: String,
    val leadId: String? = null,
    val gymName: String? = null,
    val gymAddress: String? = null,
    val gymCity: String? = null,
    val gymState: String? = null,
    val responsibleName: String? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class ReferralResponse(
    val id: String,
    val userId: String,
    val leadId: String? = null,
    val status: String,
    val createdAt: String,
    val updatedAt: String? = null
)
