package com.tadevolta.gym.data.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tadevolta.gym.data.models.AuthTokens
import com.tadevolta.gym.data.models.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.crypto.AEADBadTagException
import java.io.IOException
import android.util.Log

actual class SecureTokenStorage(private val context: Context) : TokenStorage {
    
    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Limpa tokens de forma síncrona (para uso em tratamento de erros).
     */
    private fun clearTokensSync() {
        try {
            sharedPreferences.edit().clear().apply()
        } catch (e: Exception) {
            Log.e("SecureTokenStorage", "Erro ao limpar tokens: ${e.message}", e)
            // Se houver erro ao limpar, tentar deletar o arquivo de preferências
            try {
                context.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
            } catch (fallbackException: Exception) {
                Log.e("SecureTokenStorage", "Erro ao limpar fallback: ${fallbackException.message}", fallbackException)
            }
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
            Log.e("SecureTokenStorage", "Erro de criptografia (AEADBadTagException): ${e.message}", e)
            try {
                clearTokensSync()
            } catch (clearException: Exception) {
                Log.e("SecureTokenStorage", "Erro ao limpar tokens corrompidos", clearException)
            }
            null
        } catch (e: IOException) {
            // Erro de I/O - pode ser corrupção de arquivo
            Log.e("SecureTokenStorage", "Erro de I/O ao ler dados: ${e.message}", e)
            null
        } catch (e: Exception) {
            // Outros erros
            Log.e("SecureTokenStorage", "Erro ao ler dados: ${e.message}", e)
            null
        }
    }
    
    actual override suspend fun saveTokens(tokens: AuthTokens) {
        sharedPreferences.edit()
            .putString("tokens", json.encodeToString(AuthTokens.serializer(), tokens))
            .apply()
    }
    
    actual override suspend fun getAccessToken(): String? {
        val tokensJson = safeRead { sharedPreferences.getString("tokens", null) } ?: return null
        return try {
            val tokens = json.decodeFromString<AuthTokens>(tokensJson)
            // Verificar se o token expirou (expiresAt está em segundos, converter para millis)
            val expiresAtMillis = tokens.expiresAt * 1000
            if (System.currentTimeMillis() < expiresAtMillis) {
                tokens.token
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SecureTokenStorage", "Erro ao decodificar tokens: ${e.message}", e)
            null
        }
    }
    
    actual override suspend fun getRefreshToken(): String? {
        val tokensJson = safeRead { sharedPreferences.getString("tokens", null) } ?: return null
        return try {
            val tokens = json.decodeFromString<AuthTokens>(tokensJson)
            tokens.refreshToken
        } catch (e: Exception) {
            Log.e("SecureTokenStorage", "Erro ao decodificar refresh token: ${e.message}", e)
            null
        }
    }
    
    actual override suspend fun clearTokens() {
        try {
            sharedPreferences.edit().clear().apply()
        } catch (e: Exception) {
            Log.e("SecureTokenStorage", "Erro ao limpar tokens: ${e.message}", e)
            // Se houver erro ao limpar, tentar deletar o arquivo de preferências
            try {
                context.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
            } catch (fallbackException: Exception) {
                Log.e("SecureTokenStorage", "Erro ao limpar fallback: ${fallbackException.message}", fallbackException)
            }
        }
    }
    
    actual override suspend fun saveUser(user: User) {
        try {
            sharedPreferences.edit()
                .putString("user", json.encodeToString(User.serializer(), user))
                .apply()
        } catch (e: Exception) {
            Log.e("SecureTokenStorage", "Erro ao salvar usuário: ${e.message}", e)
            // Tentar limpar dados corrompidos e salvar novamente
            if (e is AEADBadTagException || e is IOException) {
                try {
                    clearTokens()
                    sharedPreferences.edit()
                        .putString("user", json.encodeToString(User.serializer(), user))
                        .apply()
                } catch (retryException: Exception) {
                    Log.e("SecureTokenStorage", "Erro ao tentar recuperar após corrupção", retryException)
                }
            }
        }
    }
    
    actual override suspend fun getUser(): User? {
        val userJson = safeRead { sharedPreferences.getString("user", null) } ?: return null
        return try {
            json.decodeFromString<User>(userJson)
        } catch (e: Exception) {
            Log.e("SecureTokenStorage", "Erro ao decodificar usuário: ${e.message}", e)
            null
        }
    }
}
