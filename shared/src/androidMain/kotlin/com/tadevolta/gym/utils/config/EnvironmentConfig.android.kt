package com.tadevolta.gym.utils.config

import com.tadevolta.gym.shared.BuildConfig

actual object EnvironmentConfig {
    actual val API_BASE_URL: String = BuildConfig.API_BASE_URL
    actual val SYS_SEGURANCA_API_KEY: String = getEnv("SYS_SEGURANCA_API_KEY", "")
    actual val SYS_SEGURANCA_BASE_URL: String = BuildConfig.SYS_SEGURANCA_BASE_URL
    actual val DOMAIN: String = "tadevolta-gym-app"
    
    private fun getEnv(key: String, defaultValue: String): String {
        // Tentar ler de local.properties ou usar defaultValue
        // Em produção, isso viria de variáveis de ambiente seguras
        return defaultValue
    }
}
