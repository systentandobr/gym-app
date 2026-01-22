package com.tadevolta.gym.data.repositories

import com.tadevolta.gym.data.models.AuthTokens
import com.tadevolta.gym.data.models.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

interface TokenStorage {
    suspend fun saveTokens(tokens: AuthTokens)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
    suspend fun saveUser(user: User)
    suspend fun getUser(): User?
}

expect class SecureTokenStorage : TokenStorage {
    override suspend fun saveTokens(tokens: AuthTokens)
    override suspend fun getAccessToken(): String?
    override suspend fun getRefreshToken(): String?
    override suspend fun clearTokens()
    override suspend fun saveUser(user: User)
    override suspend fun getUser(): User?
}
