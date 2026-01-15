package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Student(
    val id: String,
    val unitId: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val cpf: String? = null,
    val birthDate: String? = null,
    val gender: Gender? = null,
    val address: Address? = null,
    val emergencyContact: EmergencyContact? = null,
    val healthInfo: HealthInfo? = null,
    val subscription: StudentSubscription? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
enum class Gender {
    @Serializable(with = GenderSerializer::class)
    MALE,
    @Serializable(with = GenderSerializer::class)
    FEMALE,
    @Serializable(with = GenderSerializer::class)
    OTHER
}

@Serializable
data class Address(
    val street: String? = null,
    val number: String? = null,
    val complement: String? = null,
    val neighborhood: String? = null,
    val city: String,
    val state: String,
    val zipCode: String? = null
)

@Serializable
data class EmergencyContact(
    val name: String,
    val phone: String,
    val relationship: String
)

@Serializable
data class HealthInfo(
    val medicalConditions: List<String>? = null,
    val medications: List<String>? = null,
    val injuries: List<String>? = null,
    val fitnessLevel: FitnessLevel
)

@Serializable
enum class FitnessLevel {
    @Serializable(with = FitnessLevelSerializer::class)
    BEGINNER,
    @Serializable(with = FitnessLevelSerializer::class)
    INTERMEDIATE,
    @Serializable(with = FitnessLevelSerializer::class)
    ADVANCED
}

// Serializers
object GenderSerializer : KSerializer<Gender> {
    override val descriptor: SerialDescriptor = serialDescriptor<String>()
    
    override fun serialize(encoder: Encoder, value: Gender) {
        encoder.encodeString(value.name.lowercase())
    }
    
    override fun deserialize(decoder: Decoder): Gender {
        return when (decoder.decodeString().lowercase()) {
            "male" -> Gender.MALE
            "female" -> Gender.FEMALE
            "other" -> Gender.OTHER
            else -> Gender.OTHER
        }
    }
}

object FitnessLevelSerializer : KSerializer<FitnessLevel> {
    override val descriptor: SerialDescriptor = serialDescriptor<String>()
    
    override fun serialize(encoder: Encoder, value: FitnessLevel) {
        encoder.encodeString(value.name.lowercase())
    }
    
    override fun deserialize(decoder: Decoder): FitnessLevel {
        return when (decoder.decodeString().lowercase()) {
            "beginner" -> FitnessLevel.BEGINNER
            "intermediate" -> FitnessLevel.INTERMEDIATE
            "advanced" -> FitnessLevel.ADVANCED
            else -> FitnessLevel.BEGINNER
        }
    }
}
