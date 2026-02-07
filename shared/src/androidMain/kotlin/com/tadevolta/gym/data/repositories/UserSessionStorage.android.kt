package com.tadevolta.gym.data.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tadevolta.gym.data.models.CachedCredentials
import com.tadevolta.gym.data.models.State
import com.tadevolta.gym.data.models.City
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import javax.crypto.AEADBadTagException
import java.io.IOException
import android.util.Log

// Helper function para serializar listas
private inline fun <reified T> encodeList(json: Json, list: List<T>): String {
    return json.encodeToString(serializer<List<T>>(), list)
}

actual class SecureUserSessionStorage(
    private val context: Context
) : UserSessionStorage {
    
    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = createEncryptedSharedPreferences()
    
    /**
     * Cria o EncryptedSharedPreferences com tratamento de erro para corrupção de dados.
     * Se ocorrer erro de criptografia, deleta o arquivo e cria um novo.
     */
    private fun createEncryptedSharedPreferences(): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                context,
                "user_session",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: AEADBadTagException) {
            Log.e("SecureUserSessionStorage", "AEADBadTagException ao criar EncryptedSharedPreferences. Deletando arquivo corrompido.", e)
            deleteCorruptedPreferences()
            createFreshSharedPreferences()
        } catch (e: IOException) {
            Log.e("SecureUserSessionStorage", "IOException ao criar EncryptedSharedPreferences. Deletando arquivo corrompido.", e)
            deleteCorruptedPreferences()
            createFreshSharedPreferences()
        } catch (e: Exception) {
            Log.e("SecureUserSessionStorage", "Erro ao criar EncryptedSharedPreferences: ${e.javaClass.simpleName}. Tentando recuperação.", e)
            deleteCorruptedPreferences()
            try {
                createFreshSharedPreferences()
            } catch (fallbackException: Exception) {
                Log.e("SecureUserSessionStorage", "Falha na recuperação. Usando SharedPreferences não criptografado como fallback.", fallbackException)
                context.getSharedPreferences("user_session_fallback", Context.MODE_PRIVATE)
            }
        }
    }
    
    /**
     * Deleta o arquivo de preferências corrompido.
     */
    private fun deleteCorruptedPreferences() {
        try {
            context.deleteSharedPreferences("user_session")
            Log.i("SecureUserSessionStorage", "Arquivo de preferências corrompido deletado com sucesso")
        } catch (e: Exception) {
            Log.e("SecureUserSessionStorage", "Erro ao deletar preferências corrompidas: ${e.message}", e)
            // Tentar limpar o arquivo manualmente
            try {
                context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
            } catch (clearException: Exception) {
                Log.e("SecureUserSessionStorage", "Erro ao limpar preferências: ${clearException.message}", clearException)
            }
        }
    }
    
    /**
     * Cria um novo EncryptedSharedPreferences limpo.
     */
    private fun createFreshSharedPreferences(): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            "user_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
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
    
    actual override suspend fun saveStatesAndCities(states: List<State>, cities: List<City>) {
        try {
            val statesJson = encodeList(json, states)
            val citiesJson = encodeList(json, cities)
            sharedPreferences.edit()
                .putString("cached_states", statesJson)
                .putString("cached_cities", citiesJson)
                .apply()
        } catch (e: Exception) {
            Log.e("SecureUserSessionStorage", "Erro ao salvar estados e cidades: ${e.message}", e)
        }
    }
    
    actual override suspend fun getStates(): List<State> {
        val statesJson = safeRead { sharedPreferences.getString("cached_states", null) } ?: return emptyList()
        return try {
            json.decodeFromString<List<State>>(statesJson)
        } catch (e: Exception) {
            Log.e("SecureUserSessionStorage", "Erro ao decodificar estados: ${e.message}", e)
            emptyList()
        }
    }
    
    actual override suspend fun getCitiesByState(stateId: String): List<City> {
        val citiesJson = safeRead { sharedPreferences.getString("cached_cities", null) } ?: return emptyList()
        return try {
            val allCities = json.decodeFromString<List<City>>(citiesJson)
            allCities.filter { it.stateId == stateId }
        } catch (e: Exception) {
            Log.e("SecureUserSessionStorage", "Erro ao decodificar cidades: ${e.message}", e)
            emptyList()
        }
    }
}
