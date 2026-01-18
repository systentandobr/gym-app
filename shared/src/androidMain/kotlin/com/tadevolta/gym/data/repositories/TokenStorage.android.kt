package com.tadevolta.gym.data.repositories

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tadevolta.gym.data.models.AuthTokens
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

actual class SecureTokenStorage(private val context: Context) : TokenStorage {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val json = Json { ignoreUnknownKeys = true }
    
    actual override suspend fun saveTokens(tokens: AuthTokens) {
        sharedPreferences.edit()
            .putString("tokens", json.encodeToString(AuthTokens.serializer(), tokens))
            .apply()
    }
    
    actual override suspend fun getAccessToken(): String? {
        val tokensJson = sharedPreferences.getString("tokens", null) ?: return null
        return try {
            val tokens = json.decodeFromString<AuthTokens>(tokensJson)
            // Verificar se o token expirou
            if (System.currentTimeMillis() < tokens.expiresAt) {
                tokens.token
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    actual override suspend fun getRefreshToken(): String? {
        val tokensJson = sharedPreferences.getString("tokens", null) ?: return null
        return try {
            val tokens = json.decodeFromString<AuthTokens>(tokensJson)
            tokens.refreshToken
        } catch (e: Exception) {
            null
        }
    }
    
    actual override suspend fun clearTokens() {
        sharedPreferences.edit().clear().apply()
    }
}
