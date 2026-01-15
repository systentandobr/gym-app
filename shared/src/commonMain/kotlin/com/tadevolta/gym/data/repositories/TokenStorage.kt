package com.tadevolta.gym.data.repositories

import com.tadevolta.gym.data.models.AuthTokens
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

expect class SecureTokenStorage : TokenStorage {
    override suspend fun saveTokens(tokens: AuthTokens)
    override suspend fun getAccessToken(): String?
    override suspend fun getRefreshToken(): String?
    override suspend fun clearTokens()
}
