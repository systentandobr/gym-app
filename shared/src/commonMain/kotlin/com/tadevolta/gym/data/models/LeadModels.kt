package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LeadRequest(
    val name: String,
    val email: String,
    val phone: String,
    val city: String? = null,
    val state: String? = null,
    val unitId: String? = null,
    val marketSegment: String = "gym",
    val userType: String, // "student" ou "franchise"
    val objectives: LeadObjectives? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class LeadObjectives(
    val primary: String? = null,
    val secondary: List<String> = emptyList(),
    val interestedInFranchise: Boolean = false
)

@Serializable
data class LeadResponse(
    val id: String,
    val unitId: String? = null,
    val name: String,
    val email: String,
    val phone: String,
    val city: String? = null,
    val state: String? = null,
    val userType: String,
    val marketSegment: String,
    val status: String,
    val source: String? = null,
    val score: Int? = null,
    val objectives: LeadObjectives? = null,
    val metadata: Map<String, String>? = null,
    val tags: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
    // Pipeline removido pois contém Any que não é serializável
    // Se necessário no futuro, criar um modelo específico para Pipeline
    val createdAt: String,
    val updatedAt: String? = null
)
