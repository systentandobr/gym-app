package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CachedCredentials(
    val username: String,
    val email: String,
    val password: String,
    val cachedAt: Long // timestamp em millis
) {
    companion object {
        const val CACHE_EXPIRATION_DAYS = 90L
        const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
        
        fun isCacheValid(cachedAt: Long): Boolean {
            val expirationTime = cachedAt + (CACHE_EXPIRATION_DAYS * MILLIS_PER_DAY)
            return System.currentTimeMillis() < expirationTime
        }
    }
    
    fun isValid(): Boolean {
        return isCacheValid(cachedAt)
    }
}
