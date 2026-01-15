package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val unitId: String? = null,
    val avatar: String? = null,
    val phone: String? = null,
    val status: UserStatus,
    val emailVerified: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
enum class UserStatus {
    @Serializable(with = UserStatusSerializer::class)
    ACTIVE,
    @Serializable(with = UserStatusSerializer::class)
    INACTIVE,
    @Serializable(with = UserStatusSerializer::class)
    PENDING,
    @Serializable(with = UserStatusSerializer::class)
    SUSPENDED
}

@Serializable
data class AuthTokens(
    val token: String,
    val refreshToken: String,
    val expiresAt: Long
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val domain: String = "tadevolta-gym-app"
)

@Serializable
data class LoginResponse(
    val user: User,
    val tokens: AuthTokens
)

@Serializable
data class UpdateUserData(
    val email: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null
)

// Serializer para UserStatus
object UserStatusSerializer : KSerializer<UserStatus> {
    override val descriptor: SerialDescriptor = serialDescriptor<String>()
    
    override fun serialize(encoder: Encoder, value: UserStatus) {
        encoder.encodeString(value.name.lowercase())
    }
    
    override fun deserialize(decoder: Decoder): UserStatus {
        return when (decoder.decodeString().lowercase()) {
            "active" -> UserStatus.ACTIVE
            "inactive" -> UserStatus.INACTIVE
            "pending" -> UserStatus.PENDING
            "suspended" -> UserStatus.SUSPENDED
            else -> UserStatus.PENDING
        }
    }
}
