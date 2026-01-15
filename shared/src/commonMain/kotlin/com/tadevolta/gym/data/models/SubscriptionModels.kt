package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class StudentSubscription(
    val planId: String,
    val status: SubscriptionStatus,
    val startDate: String,
    val endDate: String? = null,
    val paymentStatus: PaymentStatus,
    val lastPaymentDate: String? = null,
    val nextPaymentDate: String? = null,
    val plan: SubscriptionPlan? = null
)

@Serializable
enum class SubscriptionStatus {
    @Serializable(with = SubscriptionStatusSerializer::class)
    ACTIVE,
    @Serializable(with = SubscriptionStatusSerializer::class)
    SUSPENDED,
    @Serializable(with = SubscriptionStatusSerializer::class)
    CANCELLED,
    @Serializable(with = SubscriptionStatusSerializer::class)
    EXPIRED
}

@Serializable
enum class PaymentStatus {
    @Serializable(with = PaymentStatusSerializer::class)
    PAID,
    @Serializable(with = PaymentStatusSerializer::class)
    PENDING,
    @Serializable(with = PaymentStatusSerializer::class)
    OVERDUE
}

@Serializable
data class SubscriptionPlan(
    val id: String,
    val unitId: String,
    val name: String,
    val description: String? = null,
    val price: Long, // em centavos
    val duration: Int, // em dias
    val features: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// Serializers
object SubscriptionStatusSerializer : KSerializer<SubscriptionStatus> {
    override val descriptor: SerialDescriptor = serialDescriptor<String>()
    
    override fun serialize(encoder: Encoder, value: SubscriptionStatus) {
        encoder.encodeString(value.name.lowercase())
    }
    
    override fun deserialize(decoder: Decoder): SubscriptionStatus {
        return when (decoder.decodeString().lowercase()) {
            "active" -> SubscriptionStatus.ACTIVE
            "suspended" -> SubscriptionStatus.SUSPENDED
            "cancelled" -> SubscriptionStatus.CANCELLED
            "expired" -> SubscriptionStatus.EXPIRED
            else -> SubscriptionStatus.ACTIVE
        }
    }
}

object PaymentStatusSerializer : KSerializer<PaymentStatus> {
    override val descriptor: SerialDescriptor = serialDescriptor<String>()
    
    override fun serialize(encoder: Encoder, value: PaymentStatus) {
        encoder.encodeString(value.name.lowercase())
    }
    
    override fun deserialize(decoder: Decoder): PaymentStatus {
        return when (decoder.decodeString().lowercase()) {
            "paid" -> PaymentStatus.PAID
            "pending" -> PaymentStatus.PENDING
            "overdue" -> PaymentStatus.OVERDUE
            else -> PaymentStatus.PENDING
        }
    }
}
