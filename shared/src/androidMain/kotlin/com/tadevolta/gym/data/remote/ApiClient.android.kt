package com.tadevolta.gym.data.remote

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.tadevolta.gym.utils.config.EnvironmentConfig

actual fun createHttpClient(): HttpClient = HttpClient(Android) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true  // Incluir campos com valores padrão na serialização
        })
    }
    
    install(Logging) {
        level = LogLevel.ALL
        logger = object : Logger {
            override fun log(message: String) {
                android.util.Log.d("Ktor", message)
            }
        }
    }
    
    engine {
        connectTimeout = 30_000
        socketTimeout = 30_000
        // Configurar DNS para usar Google DNS como fallback
        // Isso ajuda em emuladores que podem ter problemas de DNS
    }
}
