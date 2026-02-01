package com.tadevolta.gym.utils.config

import android.util.Log
import com.tadevolta.gym.shared.BuildConfig
import java.io.File
import java.util.Properties

actual object EnvironmentConfig {
    actual val API_BASE_URL: String = BuildConfig.API_BASE_URL
    actual val SYS_SEGURANCA_API_KEY: String = BuildConfig.SYS_SEGURANCA_API_KEY
    actual val SYS_SEGURANCA_BASE_URL: String = BuildConfig.SYS_SEGURANCA_BASE_URL
    actual val DOMAIN: String = "tadevolta-gym-app"
    
    private fun getEnv(key: String, defaultValue: String): String {
        // 1. Tentar ler de BuildConfig primeiro (configurado no build.gradle.kts a partir de local.properties)
        try {
            val buildConfigValue = try {
                BuildConfig::class.java.getField(key).get(null) as? String
            } catch (e: NoSuchFieldException) {
                // Campo não existe no BuildConfig
                null
            } catch (e: Exception) {
                Log.w("EnvironmentConfig", "Erro ao ler $key do BuildConfig: ${e.message}")
                null
            }
            
            if (!buildConfigValue.isNullOrBlank()) {
                Log.d("EnvironmentConfig", "Lendo $key do BuildConfig: ${if (key.contains("KEY")) "***" else buildConfigValue}")
                return buildConfigValue
            }
        } catch (e: Exception) {
            Log.w("EnvironmentConfig", "Erro ao acessar BuildConfig: ${e.message}")
        }
        
        // 2. Fallback: Tentar ler de local.properties em runtime (útil durante desenvolvimento)
        // Nota: Isso só funciona se o arquivo estiver acessível no dispositivo/emulador
        // Em produção, sempre use BuildConfig
        try {
            val possiblePaths = listOf(
                File("/data/local/tmp/local.properties"), // Caminho comum em emuladores
                File("/sdcard/local.properties"), // SD card
                File(System.getProperty("user.home") + "/local.properties") // Home do usuário
            )
            
            for (propertiesFile in possiblePaths) {
                if (propertiesFile.exists() && propertiesFile.isFile && propertiesFile.canRead()) {
                    val properties = Properties()
                    propertiesFile.inputStream().use { properties.load(it) }
                    val value = properties.getProperty(key)
                    if (!value.isNullOrBlank()) {
                        Log.d("EnvironmentConfig", "Lendo $key de ${propertiesFile.path}")
                        return value.trim()
                    }
                }
            }
        } catch (e: Exception) {
            // Silenciar erro - fallback não é crítico
            Log.d("EnvironmentConfig", "Não foi possível ler .properties em runtime: ${e.message}")
        }
        
        // 3. Se não encontrou, retornar defaultValue e avisar
        if (defaultValue.isBlank()) {
            Log.w("EnvironmentConfig", "Aviso: chave não foi encontrado. Configure KEY de segurança em .properties (raiz do projeto) e faça rebuild do projeto.")
        }
        return defaultValue
    }
}
