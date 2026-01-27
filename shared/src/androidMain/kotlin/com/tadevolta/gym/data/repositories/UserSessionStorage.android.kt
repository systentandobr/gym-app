package com.tadevolta.gym.data.repositories

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tadevolta.gym.data.models.CachedCredentials
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

actual class SecureUserSessionStorage(
    private val context: Context
) : UserSessionStorage {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "user_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val json = Json { ignoreUnknownKeys = true }
    
    actual override suspend fun saveSelectedUnit(unitId: String, unitName: String) {
        sharedPreferences.edit()
            .putString("selected_unit_id", unitId)
            .putString("selected_unit_name", unitName)
            .apply()
    }
    
    actual override suspend fun getSelectedUnit(): Pair<String?, String?> {
        val unitId = sharedPreferences.getString("selected_unit_id", null)
        val unitName = sharedPreferences.getString("selected_unit_name", null)
        return Pair(unitId, unitName)
    }
    
    actual override suspend fun clearSelectedUnit() {
        sharedPreferences.edit()
            .remove("selected_unit_id")
            .remove("selected_unit_name")
            .apply()
    }
    
    actual override suspend fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit()
            .putBoolean("onboarding_completed", completed)
            .apply()
    }
    
    actual override suspend fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean("onboarding_completed", false)
    }
    
    actual override suspend fun saveCredentials(username: String, email: String, password: String) {
        val cachedCredentials = CachedCredentials(
            username = username,
            email = email,
            password = password,
            cachedAt = System.currentTimeMillis()
        )
        sharedPreferences.edit()
            .putString("cached_credentials", json.encodeToString(CachedCredentials.serializer(), cachedCredentials))
            .apply()
    }
    
    actual override suspend fun getCachedCredentials(): CachedCredentials? {
        val credentialsJson = sharedPreferences.getString("cached_credentials", null) ?: return null
        return try {
            val credentials = json.decodeFromString<CachedCredentials>(credentialsJson)
            // Verificar se o cache ainda é válido
            if (credentials.isValid()) {
                credentials
            } else {
                // Cache expirado, limpar
                clearCachedCredentials()
                null
            }
        } catch (e: Exception) {
            // Erro ao decodificar, limpar cache corrompido
            clearCachedCredentials()
            null
        }
    }
    
    actual override suspend fun clearCachedCredentials() {
        sharedPreferences.edit()
            .remove("cached_credentials")
            .apply()
    }
    
    actual override suspend fun isCredentialsCacheValid(): Boolean {
        val credentials = getCachedCredentials()
        return credentials != null && credentials.isValid()
    }
}
