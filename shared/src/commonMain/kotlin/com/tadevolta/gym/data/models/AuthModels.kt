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
    val username: String,
    val password: String,
    val domain: String = "tadevolta-gym-app"
)

@Serializable
data class SignUpRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val country: String = "BR",
    val state: String = "",
    val zipCode: String = "",
    val localNumber: String = "",
    val unitName: String = "",
    val unitId: String = "",
    val address: String = "",
    val complement: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val domain: String = "tadevolta-gym-app"
)

@Serializable
data class LoginResponse(
    val user: User,
    val tokens: AuthTokens
)

// Modelo de resposta real da API de login
@Serializable
data class LoginApiResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: LoginApiUser
)

@Serializable
data class LoginApiUser(
    val id: String,
    val username: String,
    val email: String,
    val domain: String? = null,
    val profile: LoginApiProfile? = null,
    val roles: List<LoginApiRole> = emptyList()
)

@Serializable
data class LoginApiProfile(
    val firstName: String? = null,
    val lastName: String? = null,
    val unitId: String? = null,
    val avatar: String? = null,
    val location: LoginApiLocation? = null,
    val domain: String? = null
)

@Serializable
data class LoginApiLocation(
    val unitName: String? = null,
    val address: String? = null,
    val localNumber: String? = null,
    val complement: String? = null,
    val neighborhood: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class LoginApiRole(
    val id: String,
    val name: String,
    val description: String? = null,
    val permissions: List<String> = emptyList(),
    val isSystem: Boolean = false,
    val isActive: Boolean = true
)

@Serializable
data class UpdateUserData(
    val email: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null
)

@Serializable
data class ForgotPasswordRequest(
    val email: String,
    val domain: String = "tadevolta-gym-app"
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
