package com.tadevolta.gym.data.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tadevolta.gym.data.models.CachedCredentials
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.crypto.AEADBadTagException
import java.io.IOException
import android.util.Log

actual class SecureUserSessionStorage(
    private val context: Context
) : UserSessionStorage {
    
    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "user_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Limpa todos os dados de forma síncrona (para uso em tratamento de erros).
     */
    private fun clearAll() {
        try {
            sharedPreferences.edit().clear().apply()
        } catch (e: Exception) {
            Log.e("SecureUserSessionStorage", "Erro ao limpar dados: ${e.message}", e)
        }
    }
    
    /**
     * Tenta recuperar dados de forma segura, limpando dados corrompidos se necessário.
     */
    private fun <T> safeRead(operation: () -> T?): T? {
        return try {
            operation()
        } catch (e: AEADBadTagException) {
            // Dados corrompidos - limpar e retornar null
            Log.e("SecureUserSessionStorage", "Erro de criptografia (AEADBadTagException): ${e.message}", e)
            try {
                clearAll()
            } catch (clearException: Exception) {
                Log.e("SecureUserSessionStorage", "Erro ao limpar dados corrompidos", clearException)
            }
            null
        } catch (e: IOException) {
            // Erro de I/O - pode ser corrupção de arquivo
            Log.e("SecureUserSessionStorage", "Erro de I/O ao ler dados: ${e.message}", e)
            null
        } catch (e: Exception) {
            // Outros erros
            Log.e("SecureUserSessionStorage", "Erro ao ler dados: ${e.message}", e)
            null
        }
    }
    
    actual override suspend fun saveSelectedUnit(unitId: String, unitName: String) {
        sharedPreferences.edit()
            .putString("selected_unit_id", unitId)
            .putString("selected_unit_name", unitName)
            .apply()
    }
    
    actual override suspend fun getSelectedUnit(): Pair<String?, String?> {
        val unitId = safeRead { sharedPreferences.getString("selected_unit_id", null) }
        val unitName = safeRead { sharedPreferences.getString("selected_unit_name", null) }
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
        return safeRead { sharedPreferences.getBoolean("onboarding_completed", false) } ?: false
    }
    
    actual override suspend fun saveCredentials(username: String, email: String, password: String) {
        val cachedCredentials = CachedCredentials(
            username = username,
            email = email,
            password = password,
            cachedAt = System.currentTimeMillis()
        )
        try {
            sharedPreferences.edit()
                .putString("cached_credentials", json.encodeToString(CachedCredentials.serializer(), cachedCredentials))
                .apply()
        } catch (e: Exception) {
            Log.e("SecureUserSessionStorage", "Erro ao salvar credenciais: ${e.message}", e)
            // Tentar limpar dados corrompidos e salvar novamente
            if (e is AEADBadTagException || e is IOException) {
                try {
                    clearAll()
                    sharedPreferences.edit()
                        .putString("cached_credentials", json.encodeToString(CachedCredentials.serializer(), cachedCredentials))
                        .apply()
                } catch (retryException: Exception) {
                    Log.e("SecureUserSessionStorage", "Erro ao tentar recuperar após corrupção", retryException)
                }
            }
        }
    }
    
    actual override suspend fun getCachedCredentials(): CachedCredentials? {
        val credentialsJson = safeRead { sharedPreferences.getString("cached_credentials", null) } ?: return null
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
            Log.e("SecureUserSessionStorage", "Erro ao decodificar credenciais: ${e.message}", e)
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
